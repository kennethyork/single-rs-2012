package java.awt.image;

/**
 * AWT DataBufferInt shim backed by an int pixel array.
 */
public class DataBufferInt extends DataBuffer {

    protected int[] data;

    public DataBufferInt(int[] dataArray, int size) {
        this(dataArray, size, 0);
    }

    public DataBufferInt(int[] dataArray, int size, int offset) {
        super(TYPE_INT, size);
        this.data = dataArray;
        this.offset = offset;
    }

    public int[] getData() { return data; }
    public int[] getData(int bank) { return data; }

    @Override
    public int getElem(int bank, int i) {
        return data[offset + i];
    }

    @Override
    public void setElem(int bank, int i, int val) {
        data[offset + i] = val;
    }
}
