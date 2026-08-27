package java.awt.event;

import java.awt.Component;

/**
 * AWT MouseWheelEvent shim.
 */
public class MouseWheelEvent extends MouseEvent {

    public static final int WHEEL_UNIT_SCROLL = 0;
    public static final int WHEEL_BLOCK_SCROLL = 1;

    private final int wheelRotation;

    public MouseWheelEvent(Component source, int id, long when, int modifiers, int x, int y, int clickCount, boolean popupTrigger, int scrollType, int scrollAmount, int wheelRotation) {
        super(source, id, when, modifiers, x, y, clickCount, popupTrigger, NOBUTTON);
        this.wheelRotation = wheelRotation;
    }

    public int getWheelRotation() { return wheelRotation; }
    public int getScrollType() { return WHEEL_UNIT_SCROLL; }
    public int getScrollAmount() { return 1; }
    public int getUnitsToScroll() { return wheelRotation; }
}
