#!/usr/bin/env python3
"""Build a new release archive from a previous one with an updated client.

Release archives contain the game cache and a bundled Java runtime, which are
not stored in this repository. This takes a previous release archive, renames
its top folder to the new version, and replaces only:

  client.jar               the newly built client
  README.md                the repository README
  README-<PLATFORM>.txt    gets docs/releases/platform-note.txt appended once
  cache/preferences.dat    (if present) graphics raised to the HD defaults

Every other file keeps its exact bytes, permissions, and symlinks.

Usage: repack-release.py OLD_ARCHIVE NEW_ARCHIVE CLIENT_JAR
"""
import io
import os
import re
import sys
import tarfile
import time
import zipfile

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

# Byte offsets in a version-27 preferences.dat (see GamePreferences.encode()).
PREFS_HD = {1: 2,    # anti-aliasing: 4x multisample
            2: 1,    # bloom (also enables the post-processing pass)
            5: 2,    # render distance: high
            16: 0,   # maximum screen size: no limit
            20: 5,   # graphics toolkit: OpenGL HD
            22: 2}   # water: HD


def raise_prefs(data):
    if len(data) < 36 or data[0] != 27:
        return data
    out = bytearray(data)
    for offset, value in PREFS_HD.items():
        out[offset] = value
    return bytes(out)


def make_replacer(client_jar):
    readme = open(os.path.join(ROOT, "README.md"), "rb").read()
    note = open(os.path.join(ROOT, "docs", "releases", "platform-note.txt"), "rb").read()
    client = open(client_jar, "rb").read()

    def replace(rel, data):
        if rel == "client.jar":
            replace.found_client = True
            return client
        if rel == "README.md":
            return readme
        if rel == "cache/preferences.dat":
            return raise_prefs(data)
        if re.fullmatch(r"README-[A-Z]+\.txt", rel):
            if note.strip() in data:
                return data
            return data.rstrip(b"\n") + b"\n\n" + note
        return None

    replace.found_client = False
    return replace


def split(name):
    top, _, rel = name.partition("/")
    return top, rel


def repack_tar(old, new, replace):
    replaced = []
    with tarfile.open(old) as src, tarfile.open(new, "w:gz", compresslevel=6) as dst:
        old_top = new_top = None
        for member in src:
            top, rel = split(member.name)
            if old_top is None:
                old_top = top
                new_top = os.path.basename(new).split(".tar")[0]
            data = src.extractfile(member).read() if member.isfile() else None
            member.name = new_top + member.name[len(old_top):]
            if member.islnk():
                member.linkname = new_top + member.linkname[len(old_top):]
            if data is not None:
                updated = replace(rel, data)
                if updated is not None and updated != data:
                    data = updated
                    member.size = len(data)
                    member.mtime = int(time.time())
                    replaced.append(rel)
                dst.addfile(member, io.BytesIO(data))
            else:
                dst.addfile(member)
    return replaced


def repack_zip(old, new, replace):
    replaced = []
    new_top = os.path.basename(new)[: -len(".zip")]
    with zipfile.ZipFile(old) as src, zipfile.ZipFile(new, "w", zipfile.ZIP_DEFLATED, compresslevel=6) as dst:
        for info in src.infolist():
            top, rel = split(info.filename)
            data = src.read(info)
            date_time = info.date_time
            if not info.is_dir():
                updated = replace(rel, data)
                if updated is not None and updated != data:
                    data = updated
                    date_time = time.localtime()[:6]
                    replaced.append(rel)
            out = zipfile.ZipInfo(new_top + info.filename[len(top):], date_time)
            out.external_attr = info.external_attr
            out.create_system = info.create_system
            out.compress_type = info.compress_type
            dst.writestr(out, data)
    return replaced


def main():
    if len(sys.argv) != 4:
        sys.exit(__doc__)
    old, new, client_jar = sys.argv[1:]
    replace = make_replacer(client_jar)
    if old.endswith(".tar.gz"):
        replaced = repack_tar(old, new, replace)
    elif old.endswith(".zip"):
        replaced = repack_zip(old, new, replace)
    else:
        sys.exit(f"Unsupported archive: {old}")
    if not replace.found_client:
        sys.exit(f"{old} has no client.jar to replace")
    print(f"{os.path.basename(new)}: replaced {', '.join(sorted(replaced))}")


if __name__ == "__main__":
    main()
