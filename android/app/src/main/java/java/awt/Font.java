package java.awt;

/**
 * AWT Font shim backed by an Android Typeface/Paint-compatible family+style
 * description. FontMetrics converts these into Android Paint metrics.
 */
public class Font {

    public static final int PLAIN = 0;
    public static final int BOLD = 1;
    public static final int ITALIC = 2;

    public static final String SANS_SERIF = "SansSerif";
    public static final String SERIF = "Serif";
    public static final String MONOSPACED = "Monospaced";
    public static final String DIALOG = "Dialog";
    public static final String DIALOG_INPUT = "DialogInput";

    private final String name;
    private final int style;
    private final int size;

    public Font(String name, int style, int size) {
        this.name = name != null ? name : DIALOG;
        this.style = style;
        this.size = size;
    }

    public String getName() { return name; }
    public String getFontName() { return name; }
    public String getFamily() { return name; }
    public int getStyle() { return style; }
    public int getSize() { return size; }
    public boolean isPlain() { return style == PLAIN; }
    public boolean isBold() { return (style & BOLD) != 0; }
    public boolean isItalic() { return (style & ITALIC) != 0; }

    public static Font decode(String str) {
        String s = str != null ? str : "Dialog-12";
        String family = DIALOG;
        int style = PLAIN;
        int size = 12;
        String[] parts = s.split("-");
        if (parts.length >= 1 && parts[0].length() > 0) family = parts[0];
        if (parts.length >= 2) {
            if (parts[1].contains("bold") || parts[1].equalsIgnoreCase("bold")) style |= BOLD;
            if (parts[1].contains("italic") || parts[1].equalsIgnoreCase("italic")) style |= ITALIC;
        }
        if (parts.length >= 3) {
            try { size = Integer.parseInt(parts[2]); } catch (NumberFormatException ignored) {}
        }
        return new Font(family, style, size);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Font)) return false;
        Font f = (Font) o;
        return style == f.style && size == f.size && name.equals(f.name);
    }

    @Override
    public int hashCode() {
        return (name.hashCode() * 31 + style) * 31 + size;
    }

    @Override
    public String toString() {
        return name + "-" + (isBold() ? "bold" : "") + (isItalic() ? "italic" : "") + "-" + size;
    }
}
