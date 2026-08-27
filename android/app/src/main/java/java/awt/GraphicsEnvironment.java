package java.awt;

/**
 * AWT GraphicsEnvironment shim. Android exposes a single raster screen device.
 */
public class GraphicsEnvironment {

    private static final GraphicsDevice[] DEVICES = new GraphicsDevice[]{new GraphicsDevice()};

    public static synchronized GraphicsEnvironment getLocalGraphicsEnvironment() {
        return new GraphicsEnvironment();
    }

    public boolean isHeadless() { return false; }

    public GraphicsDevice getDefaultScreenDevice() {
        return DEVICES[0];
    }

    public GraphicsDevice[] getScreenDevices() {
        return DEVICES;
    }

    public int getScreenDevicesCount() { return DEVICES.length; }

    public java.awt.image.BufferedImage createCompatibleImage(java.awt.image.BufferedImage image) {
        return new java.awt.image.BufferedImage(image.getWidth(null), image.getHeight(null), java.awt.image.BufferedImage.TYPE_INT_ARGB);
    }
}
