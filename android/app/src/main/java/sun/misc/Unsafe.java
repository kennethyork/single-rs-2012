package sun.misc;

/**
 * Compile-time shim for sun.misc.Unsafe.
 *
 * The hardware/OpenGL renderers reference sun.misc.Unsafe, which is not
 * available as a compilable API on Android. These code paths are never executed
 * on Android (the safe-mode JavaRenderer is used instead), so this class only
 * needs to satisfy the compiler. The methods mirror the tiny subset the client
 * calls: putInt/putFloat/putShort into direct memory.
 */
public final class Unsafe {

    private static final Unsafe theUnsafe = new Unsafe();

    private Unsafe() {}

    public static Unsafe getUnsafe() {
        return theUnsafe;
    }

    public void putInt(long address, int value) {}

    public void putFloat(long address, float value) {}

    public void putShort(long address, short value) {}

    public void putLong(long address, long value) {}

    public long allocateMemory(long bytes) {
        return 0L;
    }

    public void freeMemory(long address) {}

    public void copyMemory(long srcAddress, long destAddress, long bytes) {}

    public long reallocateMemory(long address, long bytes) {
        return 0L;
    }

    public void setMemory(long address, long bytes, byte value) {}

    public int getInt(long address) { return 0; }
    public float getFloat(long address) { return 0f; }
    public short getShort(long address) { return 0; }
    public long getLong(long address) { return 0L; }

    public Object getObject(Object o, long offset) { return null; }
    public void putObject(Object o, long offset, Object x) {}

    public int arrayBaseOffset(Class<?> arrayClass) { return 0; }
    public int arrayIndexScale(Class<?> arrayClass) { return 1; }

    public long objectFieldOffset(java.lang.reflect.Field f) { return 0L; }

    public int pageSize() { return 4096; }
}
