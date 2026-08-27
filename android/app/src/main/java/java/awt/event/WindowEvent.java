package java.awt.event;

import java.awt.AWTEvent;
import java.awt.Component;

public class WindowEvent extends AWTEvent {
    public static final int WINDOW_FIRST = 200;
    public static final int WINDOW_OPENED = 200;
    public static final int WINDOW_CLOSING = 201;
    public static final int WINDOW_CLOSED = 202;
    public static final int WINDOW_ICONIFIED = 203;
    public static final int WINDOW_DEICONIFIED = 204;
    public static final int WINDOW_ACTIVATED = 205;
    public static final int WINDOW_DEACTIVATED = 206;
    public static final int WINDOW_LAST = 206;

    public WindowEvent(Component source, int id) {
        super(source, id);
    }
}
