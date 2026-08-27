package java.awt;

import java.awt.image.ColorModel;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.util.Hashtable;

/**
 * AWT GraphicsDevice shim. Android has a single, fixed screen; fullscreen
 * window management is not available, so those operations are no-ops.
 */
public class GraphicsDevice {

    private final GraphicsConfiguration[] configs = new GraphicsConfiguration[]{new GraphicsConfiguration()};
    private DisplayMode mode = new DisplayMode(1280, 720, 32, 60);

    public String getIDstring() { return "AndroidDisplay"; }
    public int getType() { return TYPE_RASTER_SCREEN; }

    public static final int TYPE_RASTER_SCREEN = 0;
    public static final int TYPE_PRINTER = 1;
    public static final int TYPE_IMAGE_BUFFER = 2;

    public GraphicsConfiguration getDefaultConfiguration() { return configs[0]; }
    public GraphicsConfiguration[] getConfigurations() { return configs; }

    public boolean isFullScreenSupported() { return false; }
    public void setFullScreenWindow(Window w) {}
    public Window getFullScreenWindow() { return null; }

    public DisplayMode getDisplayMode() { return mode; }
    public DisplayMode[] getDisplayModes() { return new DisplayMode[]{mode}; }
    public void setDisplayMode(DisplayMode dm) { mode = dm; }

    public int getAvailableAcceleratedMemory() { return 0; }
}
