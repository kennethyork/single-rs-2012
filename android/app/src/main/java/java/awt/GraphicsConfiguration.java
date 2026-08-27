package java.awt;

import java.awt.image.ColorModel;
import java.awt.image.ImageObserver;

/**
 * AWT GraphicsConfiguration shim.
 */
public class GraphicsConfiguration {

    public GraphicsDevice getDevice() { return null; }

    public java.awt.image.BufferedImage createCompatibleImage(int w, int h) {
        return new java.awt.image.BufferedImage(w, h, java.awt.image.BufferedImage.TYPE_INT_ARGB);
    }

    public java.awt.image.BufferedImage createCompatibleImage(int w, int h, int transparency) {
        return new java.awt.image.BufferedImage(w, h, java.awt.image.BufferedImage.TYPE_INT_ARGB);
    }

    public ColorModel getColorModel() { return new java.awt.image.DirectColorModel(32, 0xff0000, 0xff00, 0xff, 0xff000000); }
    public Rectangle getBounds() { return new Rectangle(0, 0, 1280, 720); }
}
