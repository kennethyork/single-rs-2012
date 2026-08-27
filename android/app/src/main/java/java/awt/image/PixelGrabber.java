package java.awt.image;

import java.awt.Image;
import java.awt.image.ImageObserver;

/**
 * AWT PixelGrabber shim. Reads the backing int pixel array of a BufferedImage.
 */
public class PixelGrabber {

    private final Image image;
    private final int x, y, w, h;
    private final int[] pix;
    private final int off, scansize;
    private boolean grabbed = false;

    public PixelGrabber(Image img, int x, int y, int w, int h, int[] pix, int off, int scansize) {
        this.image = img;
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.pix = pix;
        this.off = off;
        this.scansize = scansize;
    }

    public boolean grabPixels() throws InterruptedException {
        return grabPixels(0L);
    }

    public synchronized boolean grabPixels(long ms) throws InterruptedException {
        if (grabbed) return true;
        if (!(image instanceof BufferedImage)) return false;
        BufferedImage bi = (BufferedImage) image;
        int imgW = bi.getWidth(null), imgH = bi.getHeight(null);
        int[] src = bi.getPixels();
        int gw = Math.min(w, imgW - x), gh = Math.min(h, imgH - y);
        for (int py = 0; py < gh; py++) {
            for (int px = 0; px < gw; px++) {
                int srcIdx = (y + py) * imgW + (x + px);
                pix[off + py * scansize + px] = src[srcIdx];
            }
        }
        grabbed = true;
        return true;
    }

    public int getWidth() { return w; }
    public int getHeight() { return h; }
    public Object getStatus() { return Integer.valueOf(ImageObserver.ALLBITS); }
    public int status() { return ImageObserver.ALLBITS; }
    public synchronized void startGrabbing() { grabbed = true; }
    public synchronized boolean isGrabbing() { return !grabbed; }
    public synchronized void abortGrabbing() { grabbed = true; }
}
