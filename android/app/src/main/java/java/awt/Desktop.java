package java.awt;

import java.net.URI;

/**
 * AWT Desktop shim. Android has no desktop environment, so browsing is
 * delegated to the platform bridge if available.
 */
public class Desktop {

    public enum Action {
        OPEN, EDIT, PRINT, BROWSE, MAIL
    }

    private static final Desktop INSTANCE = new Desktop();

    private Desktop() {}

    public static boolean isDesktopSupported() {
        return true;
    }

    public static Desktop getDesktop() {
        return INSTANCE;
    }

    public boolean isSupported(Action action) {
        return action == Action.BROWSE;
    }

    public void browse(URI uri) throws java.io.IOException {
        com.rs.android.AndroidPlatform.openUri(uri.toString());
    }
}
