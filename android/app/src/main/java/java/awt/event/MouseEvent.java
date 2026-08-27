package java.awt.event;

import java.awt.Component;

/**
 * AWT MouseEvent shim.
 */
public class MouseEvent extends InputEvent {

    public static final int MOUSE_FIRST = 500;
    public static final int MOUSE_LAST = 507;
    public static final int MOUSE_CLICKED = 500;
    public static final int MOUSE_PRESSED = 501;
    public static final int MOUSE_RELEASED = 502;
    public static final int MOUSE_MOVED = 503;
    public static final int MOUSE_ENTERED = 504;
    public static final int MOUSE_EXITED = 505;
    public static final int MOUSE_DRAGGED = 506;
    public static final int MOUSE_WHEEL = 507;
    public static final int NOBUTTON = 0;
    public static final int BUTTON1 = 1;
    public static final int BUTTON2 = 2;
    public static final int BUTTON3 = 3;

    protected int x;
    protected int y;
    protected int clickCount;
    protected int button;
    protected boolean popupTrigger;

    public MouseEvent(Component source, int id, long when, int modifiers, int x, int y, int clickCount, boolean popupTrigger, int button) {
        this.source = source;
        this.id = id;        this.when = when;
        this.modifiers = modifiers;
        this.x = x;
        this.y = y;
        this.clickCount = clickCount;
        this.popupTrigger = popupTrigger;
        this.button = button;
    }

    public MouseEvent(Component source, int id, long when, int modifiers, int x, int y, int clickCount, boolean popupTrigger) {
        this(source, id, when, modifiers, x, y, clickCount, popupTrigger, NOBUTTON);
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getXOnScreen() { return x; }
    public int getYOnScreen() { return y; }
    public java.awt.Point getPoint() { return new java.awt.Point(x, y); }
    public int getClickCount() { return clickCount; }
    public int getButton() { return button; }
    public boolean isPopupTrigger() { return popupTrigger; }
    public boolean isMetaDown() { return (modifiers & META_DOWN_MASK) != 0; }
    public boolean isControlDown() { return (modifiers & CTRL_DOWN_MASK) != 0; }
    public boolean isShiftDown() { return (modifiers & SHIFT_DOWN_MASK) != 0; }
    public boolean isAltDown() { return (modifiers & ALT_DOWN_MASK) != 0; }

    public void translatePoint(int dx, int dy) { x += dx; y += dy; }

    public static String getMouseModifiersText(int modifiers) { return ""; }
    public static String getMouseModifiersTextEx(int modifiers) { return ""; }
}
