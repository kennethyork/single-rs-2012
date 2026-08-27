package java.awt.image;

/**
 * AWT DataBuffer shim. Base class for the raster data buffer.
 */
public abstract class DataBuffer {

    public static final int TYPE_BYTE = 0;
    public static final int TYPE_USHORT = 1;
    public static final int TYPE_SHORT = 2;
    public static final int TYPE_INT = 3;
    public static final int TYPE_FLOAT = 4;
    public static final int TYPE_DOUBLE = 5;
    public static final int TYPE_UNDEFINED = 32;

    protected int dataType;
    protected int banks;
    protected int offset;
    protected int size;

    protected DataBuffer(int dataType, int size) {
        this.dataType = dataType;
        this.size = size;
        this.banks = 1;
        this.offset = 0;
    }

    public int getDataType() { return dataType; }
    public int getSize() { return size; }
    public int getOffset() { return offset; }
    public int getNumBanks() { return banks; }

    public abstract int getElem(int bank, int i);
    public int getElem(int i) { return getElem(0, i); }
    public abstract void setElem(int bank, int i, int val);
    public void setElem(int i, int val) { setElem(0, i, val); }
}
