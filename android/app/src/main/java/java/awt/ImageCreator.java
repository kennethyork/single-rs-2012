package java.awt;

import java.awt.image.BufferedImage;

/**
 * Creates BufferedImage instances from bytes/files. On Android, image decode
 * is handled by BitmapFactory via the platform bridge when needed; the default
 * path returns an empty software image.
 */
final class ImageCreator {

    private ImageCreator() {}

    static Image fromBytes(byte[] data) {
        if (data == null) return new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        return new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
    }

    static Image fromFile(String filename) {
        return new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
    }
}
