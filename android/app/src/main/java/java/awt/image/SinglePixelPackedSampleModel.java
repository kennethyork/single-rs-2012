package java.awt.image;

/**
 * AWT SinglePixelPackedSampleModel shim for a 32-bit ARGB image.
 */
public class SinglePixelPackedSampleModel extends SampleModel {

    private final int[] bitMasks;
    private final int[] bitOffsets;
    private final int[] bitSizes;
    private final int scanlineStride;

    public SinglePixelPackedSampleModel(int dataType, int w, int h, int[] bitMasks) {
        super(dataType, w, h, bitMasks.length);
        this.bitMasks = bitMasks.clone();
        this.bitOffsets = new int[bitMasks.length];
        this.bitSizes = new int[bitMasks.length];
        this.scanlineStride = w;
        for (int i = 0; i < bitMasks.length; i++) {
            int mask = bitMasks[i];
            int off = 0;
            while ((mask & 1) == 0) { mask >>>= 1; off++; }
            bitOffsets[i] = off;
            int size = 0;
            while (mask != 0) { mask >>>= 1; size++; }
            bitSizes[i] = size;
        }
    }

    public int getScanlineStride() { return scanlineStride; }
    public int[] getBitMasks() { return bitMasks.clone(); }

    @Override
    public int getSample(int x, int y, int b, DataBuffer data) {
        int pixel = data.getElem(y * scanlineStride + x);
        return (pixel & bitMasks[b]) >>> bitOffsets[b];
    }

    @Override
    public void setSample(int x, int y, int b, int val, DataBuffer data) {
        int idx = y * scanlineStride + x;
        int pixel = data.getElem(idx);
        pixel = (pixel & ~bitMasks[b]) | ((val << bitOffsets[b]) & bitMasks[b]);
        data.setElem(idx, pixel);
    }

    @Override
    public int[] getPixel(int x, int y, int[] iArray, DataBuffer data) {
        if (iArray == null) iArray = new int[numBands];
        int pixel = data.getElem(y * scanlineStride + x);
        for (int b = 0; b < numBands; b++) {
            iArray[b] = (pixel & bitMasks[b]) >>> bitOffsets[b];
        }
        return iArray;
    }

    @Override
    public int[] getPixels(int x, int y, int w, int h, int[] iArray, DataBuffer data) {
        if (iArray == null) iArray = new int[w * h * numBands];
        int out = 0;
        for (int py = y; py < y + h; py++) {
            for (int px = x; px < x + w; px++) {
                int pixel = data.getElem(py * scanlineStride + px);
                for (int b = 0; b < numBands; b++) {
                    iArray[out++] = (pixel & bitMasks[b]) >>> bitOffsets[b];
                }
            }
        }
        return iArray;
    }

    @Override
    public void setPixel(int x, int y, int[] iArray, DataBuffer data) {
        int idx = y * scanlineStride + x;
        int pixel = 0;
        for (int b = 0; b < numBands; b++) {
            pixel |= (iArray[b] << bitOffsets[b]) & bitMasks[b];
        }
        data.setElem(idx, pixel);
    }
}
