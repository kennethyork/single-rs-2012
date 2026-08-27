package java.awt.image;

/**
 * AWT DirectColorModel shim for packed RGB/RGBA pixel formats.
 */
public class DirectColorModel extends ColorModel {

    private final int rMask, gMask, bMask, aMask;
    private final int rOff, gOff, bOff, aOff;

    public DirectColorModel(int bits, int rmask, int gmask, int bmask) {
        this(bits, rmask, gmask, bmask, 0);
    }

    public DirectColorModel(int bits, int rmask, int gmask, int bmask, int amask) {
        super(bits);
        this.rMask = rmask;
        this.gMask = gmask;
        this.bMask = bmask;
        this.aMask = amask;
        this.rOff = offsetOf(rmask);
        this.gOff = offsetOf(gmask);
        this.bOff = offsetOf(bmask);
        this.aOff = amask == 0 ? 24 : offsetOf(amask);
    }

    private static int offsetOf(int mask) {
        if (mask == 0) return 0;
        int off = 0;
        while ((mask & 1) == 0) { mask >>>= 1; off++; }
        return off;
    }

    private static int norm(int v) {
        return v & 0xff;
    }

    @Override
    public int getRed(int pixel) { return norm(pixel >> rOff); }
    @Override
    public int getGreen(int pixel) { return norm(pixel >> gOff); }
    @Override
    public int getBlue(int pixel) { return norm(pixel >> bOff); }
    @Override
    public int getAlpha(int pixel) { return aMask == 0 ? 255 : norm(pixel >> aOff); }

    @Override
    public SampleModel createCompatibleSampleModel(int w, int h) {
        if (aMask != 0) {
            return new SinglePixelPackedSampleModel(DataBuffer.TYPE_INT, w, h, new int[]{aMask, rMask, gMask, bMask});
        }
        return new SinglePixelPackedSampleModel(DataBuffer.TYPE_INT, w, h, new int[]{rMask, gMask, bMask});
    }

    public int getRedMask() { return rMask; }
    public int getGreenMask() { return gMask; }
    public int getBlueMask() { return bMask; }
    public int getAlphaMask() { return aMask; }
}
