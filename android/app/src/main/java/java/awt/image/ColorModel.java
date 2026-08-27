package java.awt.image;

/**
 * AWT ColorModel shim base class.
 */
public abstract class ColorModel {

    protected int pixel_bits;
    protected int numComponents;

    protected ColorModel(int pixel_bits) {
        this.pixel_bits = pixel_bits;
    }

    public int getPixelSize() { return pixel_bits; }

    public abstract int getRed(int pixel);
    public abstract int getGreen(int pixel);
    public abstract int getBlue(int pixel);
    public abstract int getAlpha(int pixel);

    public int getRGB(int pixel) {
        return (getAlpha(pixel) << 24) | (getRed(pixel) << 16) | (getGreen(pixel) << 8) | getBlue(pixel);
    }

    public abstract SampleModel createCompatibleSampleModel(int w, int h);
}
