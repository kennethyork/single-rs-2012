package java.awt;

import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;

/**
 * AWT Image shim. This is a software image backed by an int ARGB pixel array.
 * It is not tied to a Bitmap so it can be read back by PixelGrabber and blitted
 * to a SurfaceView through the game canvas.
 */
public abstract class Image {

    public static final int SCALE_DEFAULT = 1;
    public static final int SCALE_FAST = 2;
    public static final int SCALE_SMOOTH = 4;
    public static final int SCALE_REPLICATE = 8;
    public static final int SCALE_AREA_AVERAGING = 16;

    public abstract int getWidth(ImageObserver observer);

    public abstract int getHeight(ImageObserver observer);

    public abstract Graphics getGraphics();

    public abstract void flush();

    public Image getScaledInstance(int width, int height, int hints) {
        return this;
    }

    public ImageProducer getSource() { return null; }

    public Object getProperty(String name, ImageObserver observer) { return null; }
}
