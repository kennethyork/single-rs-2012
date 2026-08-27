package java.awt.image;

/**
 * AWT WritableRaster shim.
 */
public class WritableRaster extends Raster {

    WritableRaster(SampleModel sampleModel, DataBuffer dataBuffer, java.awt.Point location) {
        super(sampleModel, dataBuffer);
        if (location != null) {
            this.minX = location.x;
            this.minY = location.y;
        }
    }

    public void setSample(int x, int y, int b, int val) {
        sampleModel.setSample(x, y, b, val, dataBuffer);
    }

    public void setPixel(int x, int y, int[] iArray) {
        sampleModel.setPixel(x, y, iArray, dataBuffer);
    }
}
