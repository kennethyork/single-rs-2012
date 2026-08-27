package java.awt.image;

import java.awt.Image;

/**
 * AWT ImageObserver shim. The client passes null observers and never relies on
 * the callbacks, so this is a minimal marker interface.
 */
public interface ImageObserver {
    int WIDTH = 1;
    int HEIGHT = 2;
    int PROPERTIES = 4;
    int SOMEBITS = 8;
    int FRAMEBITS = 16;
    int ALLBITS = 32;
    int ERROR = 64;
    int ABORT = 128;

    boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height);
}
