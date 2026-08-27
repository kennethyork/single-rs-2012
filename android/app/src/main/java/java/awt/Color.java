package java.awt;

/**
 * AWT Color shim. Android does not ship java.awt.Color.
 */
public class Color {

    public static final Color white = new Color(255, 255, 255);
    public static final Color WHITE = white;
    public static final Color black = new Color(0, 0, 0);
    public static final Color BLACK = black;
    public static final Color red = new Color(255, 0, 0);
    public static final Color RED = red;
    public static final Color green = new Color(0, 255, 0);
    public static final Color GREEN = green;
    public static final Color blue = new Color(0, 0, 255);
    public static final Color BLUE = blue;
    public static final Color gray = new Color(128, 128, 128);
    public static final Color GRAY = gray;
    public static final Color lightGray = new Color(192, 192, 192);
    public static final Color LIGHT_GRAY = lightGray;
    public static final Color darkGray = new Color(64, 64, 64);
    public static final Color DARK_GRAY = darkGray;
    public static final Color yellow = new Color(255, 255, 0);
    public static final Color YELLOW = yellow;
    public static final Color cyan = new Color(0, 255, 255);
    public static final Color CYAN = cyan;
    public static final Color magenta = new Color(255, 0, 255);
    public static final Color MAGENTA = magenta;
    public static final Color orange = new Color(255, 200, 0);
    public static final Color ORANGE = orange;
    public static final Color pink = new Color(255, 175, 175);
    public static final Color PINK = pink;

    private final int argb;

    public Color(int r, int g, int b) {
        this(r, g, b, 255);
    }

    public Color(int r, int g, int b, int a) {
        argb = ((a & 0xff) << 24) | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
    }

    public Color(int rgb) {
        this(rgb, true);
    }

    public Color(int rgb, boolean hasAlpha) {
        int a = hasAlpha ? ((rgb >>> 24) & 0xff) : 255;
        int r = (rgb >> 16) & 0xff;
        int g = (rgb >> 8) & 0xff;
        int b = rgb & 0xff;
        argb = ((a & 0xff) << 24) | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
    }

    public static Color getColor(String prop) { return black; }

    public int getRed() { return (argb >> 16) & 0xff; }
    public int getGreen() { return (argb >> 8) & 0xff; }
    public int getBlue() { return argb & 0xff; }
    public int getAlpha() { return (argb >>> 24) & 0xff; }

    public int getRGB() { return argb; }

    public Color brighter() {
        int r = getRed(), g = getGreen(), b = getBlue();
        int i = 3;
        if (r == 0 && g == 0 && b == 0) return new Color(i, i, i);
        r = Math.min(255, (int) (r / 0.7));
        g = Math.min(255, (int) (g / 0.7));
        b = Math.min(255, (int) (b / 0.7));
        return new Color(r, g, b);
    }

    public Color darker() {
        return new Color(Math.max(0, (int) (getRed() * 0.7)),
                Math.max(0, (int) (getGreen() * 0.7)),
                Math.max(0, (int) (getBlue() * 0.7)));
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Color && ((Color) o).argb == argb;
    }

    @Override
    public int hashCode() { return argb; }

    @Override
    public String toString() {
        return "Color[r=" + getRed() + ",g=" + getGreen() + ",b=" + getBlue() + "]";
    }
}
