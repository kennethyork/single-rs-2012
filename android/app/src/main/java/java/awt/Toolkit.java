package java.awt;

import java.awt.datatransfer.Clipboard;

/**
 * AWT Toolkit shim. Provides the minimal toolkit entry points the client uses.
 */
public abstract class Toolkit {

    private static final Toolkit INSTANCE = new Toolkit() {};

    protected Toolkit() {}

    public static synchronized Toolkit getDefaultToolkit() {
        return INSTANCE;
    }

    public EventQueue getSystemEventQueue() {
        return new EventQueue();
    }

    public Clipboard getSystemClipboard() {
        return new Clipboard();
    }

    public Image createImage(byte[] imagedata) {
        return ImageCreator.fromBytes(imagedata);
    }

    public Image createImage(String filename) {
        return ImageCreator.fromFile(filename);
    }

    public Cursor createCustomCursor(Image cursor, Point hotSpot, String name) {
        return new Cursor(Cursor.CUSTOM_CURSOR);
    }

    public Cursor createCustomCursor(java.awt.image.BufferedImage cursor, Point hotSpot, String name) {
        return new Cursor(Cursor.CUSTOM_CURSOR);
    }

    public void beep() {}
}
