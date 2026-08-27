package java.awt;

import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;

/**
 * Software AWT Graphics shim. Renders text, rects, and images directly into an
 * int ARGB pixel buffer. Android does not ship java.awt.Graphics, so this
 * provides the subset of drawing operations the 2012 client's loading screens
 * and font generation use.
 */
public class Graphics {

    private final int[] pixels;
    private final int width;
    private final int height;

    private Color color = Color.black;
    private Font font = new Font("Dialog", Font.PLAIN, 12);
    private int clipX, clipY, clipW, clipH;
    private boolean hasClip = false;

    public Graphics(BufferedImage image) {
        this.pixels = image.getPixels();
        this.width = image.getWidth(null);
        this.height = image.getHeight(null);
    }

    public Graphics(int[] pixels, int width, int height) {
        this.pixels = pixels;
        this.width = width;
        this.height = height;
    }

    public void setColor(Color c) { this.color = c; }
    public Color getColor() { return color; }

    public void setFont(Font f) { this.font = f; }
    public Font getFont() { return font; }

    public FontMetrics getFontMetrics() {
        return new FontMetrics(font);
    }

    public FontMetrics getFontMetrics(Font f) {
        return new FontMetrics(f != null ? f : font);
    }

    private boolean clipped(int x, int y) {
        if (hasClip && (x < clipX || x >= clipX + clipW || y < clipY || y >= clipY + clipH)) return true;
        return x < 0 || x >= width || y < 0 || y >= height;
    }

    public void setClip(int x, int y, int w, int h) {
        this.clipX = x; this.clipY = y; this.clipW = w; this.clipH = h;
        this.hasClip = true;
    }

    public void setClip(Rectangle r) {
        if (r == null) { hasClip = false; return; }
        setClip(r.x, r.y, r.width, r.height);
    }

    public void clipRect(int x, int y, int w, int h) {
        if (!hasClip) { setClip(x, y, w, h); return; }
        int nx = Math.max(clipX, x), ny = Math.max(clipY, y);
        int nx2 = Math.min(clipX + clipW, x + w), ny2 = Math.min(clipY + clipH, y + h);
        setClip(nx, ny, Math.max(0, nx2 - nx), Math.max(0, ny2 - ny));
    }

    public Rectangle getClipBounds() {
        return hasClip ? new Rectangle(clipX, clipY, clipW, clipH) : new Rectangle(0, 0, width, height);
    }

    public Rectangle getClipBounds(Rectangle r) {
        if (r == null) r = new Rectangle();
        Rectangle b = getClipBounds();
        r.setBounds(b);
        return r;
    }

    public void fillRect(int x, int y, int w, int h) {
        int x2 = x + w, y2 = y + h;
        for (int py = y; py < y2; py++) {
            for (int px = x; px < x2; px++) {
                if (clipped(px, py)) continue;
                pixels[py * width + px] = color.getRGB();
            }
        }
    }

    public void drawRect(int x, int y, int w, int h) {
        fillRect(x, y, w, 1);
        fillRect(x, y + h - 1, w, 1);
        fillRect(x, y, 1, h);
        fillRect(x + w - 1, y, 1, h);
    }

    public void clearRect(int x, int y, int w, int h) {
        Color old = color;
        color = new Color(0, 0, 0, 0);
        fillRect(x, y, w, h);
        color = old;
    }

    public void drawLine(int x1, int y1, int x2, int y2) {
        int dx = Math.abs(x2 - x1), dy = Math.abs(y2 - y1);
        int sx = x1 < x2 ? 1 : -1, sy = y1 < y2 ? 1 : -1;
        int err = dx - dy;
        int x = x1, y = y1;
        while (true) {
            if (!clipped(x, y)) pixels[y * width + x] = color.getRGB();
            if (x == x2 && y == y2) break;
            int e2 = 2 * err;
            if (e2 > -dy) { err -= dy; x += sx; }
            if (e2 < dx) { err += dx; y += sy; }
        }
    }

    public void drawString(String str, int x, int y) {
        if (str == null) return;
        FontMetrics fm = new FontMetrics(font);
        android.graphics.Paint paint = fm.getPaint();
        paint.setColor(color.getRGB());
        paint.setTextSize(font.getSize());
        int ascent = fm.getAscent();
        // Render text into a temporary bitmap, then blit the opaque glyphs.
        int textW = fm.stringWidth(str);
        int textH = fm.getHeight();
        if (textW <= 0 || textH <= 0) return;
        android.graphics.Bitmap bmp = android.graphics.Bitmap.createBitmap(Math.max(textW, 1), textH, android.graphics.Bitmap.Config.ARGB_8888);
        android.graphics.Canvas c = new android.graphics.Canvas(bmp);
        c.drawColor(android.graphics.Color.TRANSPARENT);
        c.drawText(str, 0, ascent, paint);
        int[] bmpPixels = new int[textW * textH];
        bmp.getPixels(bmpPixels, 0, textW, 0, 0, textW, textH);
        int srcColor = color.getRGB();
        for (int py = 0; py < textH; py++) {
            for (int px = 0; px < textW; px++) {
                int src = bmpPixels[py * textW + px];
                int alpha = (src >>> 24) & 0xff;
                if (alpha == 0) continue;
                int dx = x + px, dy = y - ascent + py;
                if (clipped(dx, dy)) continue;
                pixels[dy * width + dx] = srcColor;
            }
        }
        bmp.recycle();
    }

    public void drawImage(Image img, int x, int y, ImageObserver observer) {
        if (!(img instanceof BufferedImage)) return;
        BufferedImage bi = (BufferedImage) img;
        int w = bi.getWidth(null), h = bi.getHeight(null);
        int[] src = bi.getPixels();
        for (int py = 0; py < h; py++) {
            for (int px = 0; px < w; px++) {
                int dx = x + px, dy = y + py;
                if (clipped(dx, dy)) continue;
                pixels[dy * width + dx] = src[py * w + px];
            }
        }
    }

    public void drawImage(Image img, int x, int y, int w, int h, ImageObserver observer) {
        if (!(img instanceof BufferedImage)) return;
        BufferedImage bi = (BufferedImage) img;
        int sw = bi.getWidth(null), sh = bi.getHeight(null);
        int[] src = bi.getPixels();
        for (int dy = 0; dy < h; dy++) {
            for (int dx = 0; dx < w; dx++) {
                int sx = sw <= 1 ? 0 : Math.min(sw - 1, (dx * sw) / w);
                int sy = sh <= 1 ? 0 : Math.min(sh - 1, (dy * sh) / h);
                int ox = x + dx, oy = y + dy;
                if (clipped(ox, oy)) continue;
                pixels[oy * width + ox] = src[sy * sw + sx];
            }
        }
    }

    public void dispose() {}

    public void copyArea(int x, int y, int w, int h, int dx, int dy) {}
    public void translate(int x, int y) {}
}
