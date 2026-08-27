package java.awt;

/**
 * AWT Cursor shim. Android has no desktop cursor; this is a value type.
 */
public class Cursor {

    public static final int DEFAULT_CURSOR = 0;
    public static final int CROSSHAIR_CURSOR = 1;
    public static final int TEXT_CURSOR = 2;
    public static final int WAIT_CURSOR = 3;
    public static final int HAND_CURSOR = 12;
    public static final int MOVE_CURSOR = 13;
    public static final int CUSTOM_CURSOR = -1;

    private final int type;

    public Cursor(int type) {
        this.type = type;
    }

    public int getType() { return type; }

    public static Cursor getDefaultCursor() {
        return new Cursor(DEFAULT_CURSOR);
    }

    public static Cursor getPredefinedCursor(int type) {
        return new Cursor(type);
    }

    public String getName() { return "Cursor[" + type + "]"; }

    @Override
    public String toString() { return getName(); }
}
