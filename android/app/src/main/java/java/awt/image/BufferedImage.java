package java.awt.image;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.util.Hashtable;

/**
 * AWT BufferedImage shim. Backed by an int ARGB pixel array and the software
 * Graphics implementation. The single-pixel-packed raster writes directly into
 * the backing int array so both rendering and read-back see the same pixels.
 */
public class BufferedImage extends Image {

    public static final int TYPE_CUSTOM = 0;
    public static final int TYPE_INT_RGB = 1;
    public static final int TYPE_INT_ARGB = 2;
    public static final int TYPE_INT_ARGB_PRE = 3;
    public static final int TYPE_3BYTE_BGR = 5;
    public static final int TYPE_BYTE_GRAY = 10;

    private final int width;
    private final int height;
    private final int type;
    private final int[] pixels;
    private final ColorModel colorModel;
    private final WritableRaster raster;
    private final Hashtable<String, Object> properties;
    private Graphics graphics;

    public BufferedImage(int width, int height, int imageType) {
        this.width = width;
        this.height = height;
        this.type = imageType;
        this.properties = new Hashtable<>();
        this.pixels = new int[width * height];

        int aMask, rMask, gMask, bMask;
        if (imageType == TYPE_INT_ARGB || imageType == TYPE_INT_ARGB_PRE) {
            aMask = 0xff000000; rMask = 0x00ff0000; gMask = 0x0000ff00; bMask = 0x000000ff;
        } else if (imageType == TYPE_3BYTE_BGR) {
            aMask = 0xff000000; rMask = 0x00ff0000; gMask = 0x0000ff00; bMask = 0x000000ff;
            for (int i = 0; i < pixels.length; i++) pixels[i] = 0xff000000;
        } else {
            // TYPE_INT_RGB and default: opaque
            aMask = 0xff000000; rMask = 0x00ff0000; gMask = 0x0000ff00; bMask = 0x000000ff;
            for (int i = 0; i < pixels.length; i++) pixels[i] = 0xff000000;
        }

        this.colorModel = new DirectColorModel(32, rMask, gMask, bMask, aMask);
        DataBufferInt db = new DataBufferInt(pixels, pixels.length);
        SampleModel sm = colorModel.createCompatibleSampleModel(width, height);
        this.raster = new WritableRaster(sm, db, null);
    }

    public BufferedImage(ColorModel cm, WritableRaster raster, boolean isRasterPremultiplied, Hashtable<?, ?> properties) {
        this.width = raster.getWidth();
        this.height = raster.getHeight();
        this.colorModel = cm;
        this.raster = raster;
        this.type = TYPE_CUSTOM;
        this.properties = new Hashtable<>();
        if (properties != null) {
            for (java.util.Map.Entry<?, ?> e : properties.entrySet()) {
                this.properties.put(String.valueOf(e.getKey()), e.getValue());
            }
        }
        DataBufferInt db = (DataBufferInt) raster.getDataBuffer();
        this.pixels = db.getData();
    }

    public int getType() { return type; }
    public ColorModel getColorModel() { return colorModel; }
    public WritableRaster getRaster() { return raster; }

    public int[] getRGB(int startX, int startY, int w, int h, int[] rgbArray, int offset, int scansize) {
        if (rgbArray == null) rgbArray = new int[offset + scansize * h];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int px = startX + x, py = startY + y;
                rgbArray[offset + y * scansize + x] = pixels[py * width + px] | 0xff000000;
            }
        }
        return rgbArray;
    }

    public int getRGB(int x, int y) {
        return pixels[y * width + x] | 0xff000000;
    }

    public void setRGB(int startX, int startY, int w, int h, int[] rgbArray, int offset, int scansize) {
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int src = rgbArray[offset + y * scansize + x];
                pixels[(startY + y) * width + startX + x] = src;
            }
        }
    }

    public void setRGB(int x, int y, int rgb) {
        pixels[y * width + x] = rgb;
    }

    public int[] getPixels() { return pixels; }

    public Object getProperty(String name) { return properties.get(name); }
    public String[] getPropertyNames() { return properties.keySet().toArray(new String[0]); }

    @Override
    public int getWidth(ImageObserver observer) { return width; }

    @Override
    public int getHeight(ImageObserver observer) { return height; }

    @Override
    public synchronized Graphics getGraphics() {
        if (graphics == null) {
            graphics = new java.awt.Graphics(this);
        }
        return graphics;
    }

    @Override
    public void flush() {
        graphics = null;
    }
}
