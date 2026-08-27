package java.awt.image;

/**
 * AWT Raster shim. Provides createWritableRaster used by the client's software
 * renderer to wrap a packed int pixel buffer in an Image.
 */
public class Raster {

    protected SampleModel sampleModel;
    protected DataBuffer dataBuffer;
    protected int width;
    protected int height;
    protected int minX;
    protected int minY;

    protected Raster(SampleModel sampleModel, DataBuffer dataBuffer) {
        this.sampleModel = sampleModel;
        this.dataBuffer = dataBuffer;
        this.width = sampleModel.getWidth();
        this.height = sampleModel.getHeight();
        this.minX = 0;
        this.minY = 0;
    }

    public static WritableRaster createWritableRaster(SampleModel sm, DataBuffer db, java.awt.Point location) {
        return new WritableRaster(sm, db, location);
    }

    public SampleModel getSampleModel() { return sampleModel; }
    public DataBuffer getDataBuffer() { return dataBuffer; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getMinX() { return minX; }
    public int getMinY() { return minY; }

    public int getSample(int x, int y, int b) {
        return sampleModel.getSample(x, y, b, dataBuffer);
    }

    public int[] getPixels(int x, int y, int w, int h, int[] iArray) {
        return sampleModel.getPixels(x, y, w, h, iArray, dataBuffer);
    }
}
