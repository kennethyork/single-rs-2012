#!/usr/bin/env python3
"""Static NewApi check for code Android Lint does not cover.

Lint checks the app's own sources, not the classes inside a prebuilt dependency.
rs.darkan:core is compiled for Java 24 and calls APIs Android gained late (or
never), and each one only shows up as a NoSuchMethodError at runtime -- one
emulator round-trip per bug. This resolves every platform method and field
reference against the SDK's api-versions.xml (the same database Lint uses) and
reports what is unavailable at the project's minSdk.

It reads the APK's dex, not the jars, because that is what actually runs: by
then D8 has desugared records, backported List.of/Set.of, and rewritten the
java.nio covariant returns, so scanning the inputs instead reports a pile of
false positives.

Pass an APK to see what actually ships, or a jar/class directory to get results
attributed to the calling class -- which is what you want for rs.darkan:core.
Anything D8 handles for us (record desugaring, backported List.of/Set.of/Map.of,
the java.nio covariant returns) is excluded, and members the SDK database does
not model at all -- typically inherited from a non-public superclass like
AbstractStringBuilder -- are reported separately as unverified rather than mixed
in with real findings.

Usage: check-api-levels.py <minSdk> <android-sdk-dir> <apk|jar|classes-dir>...
"""
import collections
import os
import re
import struct
import sys
import xml.etree.ElementTree as ET
import zipfile

PLATFORM = ('java/', 'javax/', 'jdk/', 'sun/', 'android/', 'dalvik/', 'org/w3c/',
            'org/xml/', 'org/json/', 'org/apache/http/', 'libcore/')

# D8 rewrites these away, so a reference to one is not a runtime problem.
DESUGARED_OWNERS = ('java/lang/Record', 'java/lang/runtime/')
DESUGARED = {
    # Immutable collection factories are among D8's backported methods.
    ('java/util/List', 'of'), ('java/util/Set', 'of'), ('java/util/Map', 'of'),
    ('java/util/Map', 'ofEntries'), ('java/util/Map', 'entry'),
    ('java/util/List', 'copyOf'), ('java/util/Set', 'copyOf'), ('java/util/Map', 'copyOf'),
}
# java.nio's covariant returns (ByteBuffer.flip() returning ByteBuffer rather
# than Buffer, and friends) are rewritten by D8 to the Buffer-returning form.
NIO_COVARIANT = ('java/nio/ByteBuffer', 'java/nio/CharBuffer', 'java/nio/IntBuffer',
                 'java/nio/LongBuffer', 'java/nio/ShortBuffer', 'java/nio/FloatBuffer',
                 'java/nio/DoubleBuffer', 'java/nio/MappedByteBuffer')


def is_desugared(owner, member):
    if owner.startswith(DESUGARED_OWNERS):
        return True
    name = member.split('(')[0]
    if (owner, name) in DESUGARED:
        return True
    if owner in NIO_COVARIANT and member.endswith(f'L{owner};'):
        return True
    return False

Cls = collections.namedtuple('Cls', 'since members supers')


def load_api(sdk_dir):
    path = os.path.join(sdk_dir, 'platforms', 'android-34', 'data', 'api-versions.xml')
    classes = {}
    for c in ET.parse(path).getroot().findall('class'):
        members, supers = {}, []
        for m in c:
            if m.tag in ('method', 'field'):
                members[m.get('name')] = int(m.get('since', c.get('since', 1)))
            elif m.tag in ('extends', 'implements'):
                supers.append(m.get('name'))
        classes[c.get('name')] = Cls(int(c.get('since', 1)), members, supers)
    return classes


def member_since(api, owner, name, seen=None):
    """Lowest API level the member is available at, walking superclasses."""
    seen = seen or set()
    if owner in seen or owner not in api:
        return None
    seen.add(owner)
    cls = api[owner]
    if name in cls.members:
        return max(cls.members[name], cls.since)
    for sup in cls.supers:
        found = member_since(api, sup, name, seen)
        if found is not None:
            return max(found, cls.since)
    return None


def refs(data):
    """(owner, name+descriptor, is_method) for every member reference in a class."""
    count = struct.unpack('>H', data[8:10])[0]
    pool, out, i, p = {}, [], 1, 10
    while i < count:
        tag = data[p]
        if tag == 1:
            n = struct.unpack('>H', data[p + 1:p + 3])[0]
            pool[i] = data[p + 3:p + 3 + n].decode('utf-8', 'replace')
            p += 3 + n
        elif tag in (7, 8, 16, 19, 20):
            pool[i] = ('idx', struct.unpack('>H', data[p + 1:p + 3])[0], tag)
            p += 3
        elif tag == 15:
            p += 4
        elif tag in (9, 10, 11, 12):
            pool[i] = ('ref', struct.unpack('>HH', data[p + 1:p + 5]), tag)
            p += 5
        elif tag in (3, 4, 17, 18):
            p += 5
        elif tag in (5, 6):
            p += 9
            i += 1
        else:
            return []
        i += 1
    for entry in pool.values():
        if not (isinstance(entry, tuple) and entry[0] == 'ref' and entry[2] in (9, 10, 11)):
            continue
        cls_i, nat_i = entry[1]
        cls_e, nat_e = pool.get(cls_i), pool.get(nat_i)
        if not (isinstance(cls_e, tuple) and isinstance(nat_e, tuple)):
            continue
        owner = pool.get(cls_e[1])
        if not isinstance(owner, str) or nat_e[0] != 'ref':
            continue
        name, desc = pool.get(nat_e[1][0]), pool.get(nat_e[1][1])
        if isinstance(name, str) and isinstance(desc, str):
            is_method = entry[2] != 9
            out.append((owner, name + desc if is_method else name, is_method))
    return out


def uleb128(data, off):
    result = shift = 0
    while True:
        b = data[off]
        off += 1
        result |= (b & 0x7F) << shift
        if not b & 0x80:
            return result, off
        shift += 7


def dex_refs(data):
    """(owner, member, is_method) for every method and field id in a dex."""
    u4 = lambda o: struct.unpack_from('<I', data, o)[0]
    u2 = lambda o: struct.unpack_from('<H', data, o)[0]

    string_ids_size, string_ids_off = u4(56), u4(60)
    type_ids_size, type_ids_off = u4(64), u4(68)
    proto_ids_size, proto_ids_off = u4(72), u4(76)
    field_ids_size, field_ids_off = u4(80), u4(84)
    method_ids_size, method_ids_off = u4(88), u4(92)

    strings = []
    for i in range(string_ids_size):
        off = u4(string_ids_off + i * 4)
        _, off = uleb128(data, off)          # utf16 length, then MUTF-8 bytes
        end = data.index(b'\x00', off)
        strings.append(data[off:end].decode('utf-8', 'replace'))

    types = [strings[u4(type_ids_off + i * 4)] for i in range(type_ids_size)]

    protos = []
    for i in range(proto_ids_size):
        base = proto_ids_off + i * 12
        ret, params_off = types[u4(base + 4)], u4(base + 8)
        params = ''
        if params_off:
            n = u4(params_off)
            params = ''.join(types[u2(params_off + 4 + j * 2)] for j in range(n))
        protos.append(f'({params}){ret}')

    def owner_of(descriptor):
        # Lcom/example/Foo; -> com/example/Foo, and skip arrays/primitives.
        if not descriptor.startswith('L') or not descriptor.endswith(';'):
            return None
        return descriptor[1:-1]

    out = []
    for i in range(method_ids_size):
        base = method_ids_off + i * 8
        owner = owner_of(types[u2(base)])
        if owner:
            out.append((owner, strings[u4(base + 4)] + protos[u2(base + 2)], True))
    for i in range(field_ids_size):
        base = field_ids_off + i * 8
        owner = owner_of(types[u2(base)])
        if owner:
            # api-versions.xml records field names without a descriptor.
            out.append((owner, strings[u4(base + 4)], False))
    return out


def class_sources(path):
    if path.endswith('.jar'):
        with zipfile.ZipFile(path) as z:
            for e in z.namelist():
                if e.endswith('.class'):
                    yield f'{os.path.basename(path)}!{e}', z.read(e)
    else:
        for root, _, files in os.walk(path):
            for f in files:
                if f.endswith('.class'):
                    full = os.path.join(root, f)
                    yield os.path.relpath(full, path), open(full, 'rb').read()


def sources(path):
    if path.endswith(('.apk', '.aab', '.zip')):
        with zipfile.ZipFile(path) as z:
            for e in sorted(z.namelist()):
                if e.endswith('.dex'):
                    yield e, z.read(e)
    elif path.endswith('.dex'):
        yield os.path.basename(path), open(path, 'rb').read()
    else:
        yield from class_sources(path)


def main():
    min_sdk, sdk_dir, targets = int(sys.argv[1]), sys.argv[2], sys.argv[3:]
    api = load_api(sdk_dir)
    problems = collections.defaultdict(set)
    unverified = collections.defaultdict(set)
    scanned = 0
    for target in targets:
        if not os.path.exists(target):
            print(f'  (skipped, missing: {target})')
            continue
        is_dex = target.endswith(('.apk', '.aab', '.zip', '.dex'))
        for origin, data in sources(target):
            scanned += 1
            for owner, member, is_method in (dex_refs(data) if is_dex else refs(data)):
                if not owner.startswith(PLATFORM) or owner.startswith('['):
                    continue
                if owner not in api:
                    continue          # not a platform class at all
                if is_desugared(owner, member):
                    continue
                key = member          # fields already carry no descriptor
                since = member_since(api, owner, key)
                if since is None:
                    unverified[(owner, key)].add(origin)
                elif since > min_sdk:
                    problems[(owner, key, f'API {since}')].add(origin)
    print(f'Scanned {scanned} unit(s) against minSdk {min_sdk}.\n')
    for (owner, member, why), origins in sorted(problems.items()):
        print(f'{why:>8}  {owner}.{member}')
        for o in sorted(origins)[:5]:
            print(f'          <- {o}')
    if unverified:
        print(f'\nNot in the SDK database ({len(unverified)}) -- usually inherited from a')
        print('non-public superclass, so treat as unverified rather than broken:')
        for (owner, member), origins in sorted(unverified.items())[:15]:
            print(f'          {owner}.{member}  <- {sorted(origins)[0]}')
    print(f'\n{len(problems)} reference(s) newer than minSdk {min_sdk}.')
    return 1 if problems else 0


if __name__ == '__main__':
    sys.exit(main())
