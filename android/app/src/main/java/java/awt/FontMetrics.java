package java.awt;

import android.graphics.Paint;
import android.graphics.Typeface;

/**
 * AWT FontMetrics shim backed by an Android Paint. Provides the glyph-width
 * and ascent/descent metrics the 2012 client queries when building fonts.
 */
public class FontMetrics {

    private final Font font;
    private final Paint paint;

    public FontMetrics(Font font) {
        this.font = font;
        this.paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        int style = font.isBold() && font.isItalic() ? Typeface.BOLD_ITALIC
                : font.isBold() ? Typeface.BOLD
                : font.isItalic() ? Typeface.ITALIC : Typeface.NORMAL;
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, style));
        paint.setTextSize(font.getSize());
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
    }

    public static FontMetrics getFontMetrics(Font font) {
        return new FontMetrics(font);
    }

    public Font getFont() { return font; }

    public int charWidth(char ch) {
        return Math.max(1, Math.round(paint.measureText(String.valueOf(ch))));
    }

    public int charWidth(int ch) {
        return charWidth((char) ch);
    }

    public int stringWidth(String str) {
        if (str == null) return 0;
        return Math.round(paint.measureText(str));
    }

    public int charsWidth(char[] data, int off, int len) {
        if (data == null) return 0;
        return Math.round(paint.measureText(data, off, len));
    }

    public int bytesWidth(byte[] data, int off, int len) {
        if (data == null) return 0;
        char[] chars = new char[len];
        for (int i = 0; i < len; i++) chars[i] = (char) (data[off + i] & 0xff);
        return charsWidth(chars, 0, len);
    }

    public int getHeight() {
        return Math.max(1, Math.round(paint.getFontMetricsInt().descent - paint.getFontMetricsInt().ascent));
    }

    public int getAscent() {
        return Math.abs(paint.getFontMetricsInt().ascent);
    }

    public int getDescent() {
        return paint.getFontMetricsInt().descent;
    }

    public int getMaxAscent() {
        return getAscent();
    }

    public int getMaxDescent() {
        return getDescent();
    }

    public int getLeading() {
        return paint.getFontMetricsInt().leading;
    }

    public int getMaxAdvance() {
        return Math.round(paint.measureText("W"));
    }

    public Paint getPaint() { return paint; }
}
