package java.awt;

/**
 * AWT DisplayMode shim. On Android there is a single fixed display; this is a
 * value type returned by the GraphicsDevice stub.
 */
public final class DisplayMode {

    public static final int BIT_DEPTH_MULTI = -1;
    public static final int REFRESH_RATE_UNKNOWN = 0;

    private final int width;
    private final int height;
    private final int bitDepth;
    private final int refreshRate;

    public DisplayMode(int width, int height, int bitDepth, int refreshRate) {
        this.width = width;
        this.height = height;
        this.bitDepth = bitDepth;
        this.refreshRate = refreshRate;
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getBitDepth() { return bitDepth; }
    public int getRefreshRate() { return refreshRate; }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DisplayMode)) return false;
        DisplayMode d = (DisplayMode) o;
        return width == d.width && height == d.height && bitDepth == d.bitDepth && refreshRate == d.refreshRate;
    }

    @Override
    public int hashCode() {
        return width + height + bitDepth + refreshRate;
    }

    @Override
    public String toString() {
        return width + "x" + height + "x" + bitDepth + "@" + refreshRate;
    }
}
