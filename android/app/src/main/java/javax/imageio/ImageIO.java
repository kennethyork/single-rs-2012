package javax.imageio;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Android stub for javax.imageio.ImageIO. Only the write-to-PNG used by the
 * client's "dumpitems" debug command is provided; on Android it is a no-op.
 */
public final class ImageIO {

    private ImageIO() {}

    public static boolean write(BufferedImage im, String formatName, File output) throws IOException {
        return false;
    }
}
