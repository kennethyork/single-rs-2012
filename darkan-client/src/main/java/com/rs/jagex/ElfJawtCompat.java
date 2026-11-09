package com.rs.jagex;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

/**
 * Lets the 2012 Linux OpenGL library (libjaggl.so) load on modern Java.
 *
 * <p>libjaggl.so was linked against a JDK whose libjawt.so tagged its symbols
 * with the private version {@code SUNWprivate_1.1}. Current JDKs no longer
 * define that version, so the dynamic linker refuses to load the library and
 * the HD toolkit cannot start. This rewrites the library's version tables so
 * that requirement is weak and the JAWT symbols bind unversioned, which is how
 * current libjawt.so exports them. Nothing else in the library changes.
 *
 * <p>Input that is not a 64-bit little-endian ELF, or has no such requirement,
 * is returned unchanged, so this is safe to apply to every native library.
 */
final class ElfJawtCompat {

	private static final int SHT_GNU_VERNEED = 0x6ffffffe;
	private static final int SHT_GNU_VERSYM = 0x6fffffff;
	private static final short VER_FLG_WEAK = 0x2;
	private static final short VER_NDX_GLOBAL = 1;
	private static final String JAWT_VERSION = "SUNWprivate_1.1";

	private ElfJawtCompat() {
	}

	static byte[] patch(byte[] lib) {
		try {
			byte[] patched = patchOrNull(lib);
			return patched != null ? patched : lib;
		} catch (RuntimeException e) {
			return lib;
		}
	}

	private static byte[] patchOrNull(byte[] original) {
		if (original.length < 64 || original[0] != 0x7f || original[1] != 'E' || original[2] != 'L' || original[3] != 'F')
			return null;
		if (original[4] != 2 || original[5] != 1) // ELFCLASS64, little-endian
			return null;
		byte[] lib = original.clone();
		ByteBuffer b = ByteBuffer.wrap(lib).order(ByteOrder.LITTLE_ENDIAN);
		long shoff = b.getLong(0x28);
		int shentsize = b.getShort(0x3A) & 0xffff;
		int shnum = b.getShort(0x3C) & 0xffff;

		int verneed = -1, versym = -1;
		for (int i = 0; i < shnum; i++) {
			int type = b.getInt((int) (shoff + (long) i * shentsize + 4));
			if (type == SHT_GNU_VERNEED)
				verneed = i;
			else if (type == SHT_GNU_VERSYM)
				versym = i;
		}
		if (verneed < 0 || versym < 0)
			return null;

		int vnHeader = (int) (shoff + (long) verneed * shentsize);
		int vnOffset = (int) b.getLong(vnHeader + 0x18);
		int strtab = b.getInt(vnHeader + 0x28);
		int strOffset = (int) b.getLong((int) (shoff + (long) strtab * shentsize + 0x18));

		boolean[] patchedIndex = new boolean[0x8000];
		boolean changed = false;
		int vn = vnOffset;
		while (true) {
			int cnt = b.getShort(vn + 2) & 0xffff;
			int aux = vn + b.getInt(vn + 8);
			for (int a = 0; a < cnt; a++) {
				if (JAWT_VERSION.equals(cString(lib, strOffset + b.getInt(aux + 8)))) {
					b.putShort(aux + 4, (short) (b.getShort(aux + 4) | VER_FLG_WEAK));
					patchedIndex[b.getShort(aux + 6) & 0x7fff] = true;
					changed = true;
				}
				int next = b.getInt(aux + 12);
				if (next == 0)
					break;
				aux += next;
			}
			int next = b.getInt(vn + 12);
			if (next == 0)
				break;
			vn += next;
		}
		if (!changed)
			return null;

		int vsHeader = (int) (shoff + (long) versym * shentsize);
		int vsOffset = (int) b.getLong(vsHeader + 0x18);
		int vsSize = (int) b.getLong(vsHeader + 0x20);
		for (int p = vsOffset; p + 1 < vsOffset + vsSize; p += 2)
			if (patchedIndex[b.getShort(p) & 0x7fff])
				b.putShort(p, VER_NDX_GLOBAL);
		return lib;
	}

	private static String cString(byte[] data, int start) {
		int end = start;
		while (end < data.length && data[end] != 0)
			end++;
		return new String(data, start, end - start, StandardCharsets.US_ASCII);
	}
}
