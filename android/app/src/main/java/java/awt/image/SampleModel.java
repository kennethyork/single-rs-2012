package java.awt.image;

/**
 * AWT SampleModel shim. Only the subset needed to build a packed ARGB raster
 * is provided.
 */
public abstract class SampleModel {

    protected int width;
    protected int height;
    protected int numBands;
    protected int dataType;

    protected SampleModel(int dataType, int w, int h, int numBands) {
        this.dataType = dataType;
        this.width = w;
        this.height = h;
        this.numBands = numBands;
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getNumBands() { return numBands; }
    public int getDataType() { return dataType; }

    public abstract int getSample(int x, int y, int b, DataBuffer data);
    public abstract void setSample(int x, int y, int b, int val, DataBuffer data);
    public abstract int[] getPixel(int x, int y, int[] iArray, DataBuffer data);
    public abstract int[] getPixels(int x, int y, int w, int h, int[] iArray, DataBuffer data);
    public abstract void setPixel(int x, int y, int[] iArray, DataBuffer data);
}
