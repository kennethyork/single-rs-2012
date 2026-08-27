package java.awt.event;

import java.awt.Component;

/**
 * AWT InputEvent shim base.
 */
public abstract class InputEvent {

    public static final int SHIFT_MASK = 1;
    public static final int CTRL_MASK = 2;
    public static final int META_MASK = 4;
    public static final int ALT_MASK = 8;
    public static final int SHIFT_DOWN_MASK = 64;
    public static final int CTRL_DOWN_MASK = 128;
    public static final int META_DOWN_MASK = 256;
    public static final int ALT_DOWN_MASK = 512;
    public static final int BUTTON1_DOWN_MASK = 1024;
    public static final int BUTTON2_DOWN_MASK = 2048;
    public static final int BUTTON3_DOWN_MASK = 4096;

    public static final int MOUSE_EVENT = 501;
    public static final int MOUSE_MOTION_EVENT = 503;
    public static final int KEY_EVENT = 401;

    protected long when;
    protected int modifiers;
    protected boolean consumed;
    protected Component source;
    protected int id;

    public Component getComponent() { return source; }
    public long getWhen() { return when; }
    public int getModifiersEx() { return modifiers; }

    public void consume() { consumed = true; }
    public boolean isConsumed() { return consumed; }
}
