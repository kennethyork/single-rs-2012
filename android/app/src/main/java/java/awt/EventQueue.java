package java.awt;

import java.awt.AWTEvent;

/**
 * AWT EventQueue shim. Android has no AWT event dispatch; the game posts dummy
 * ActionEvents to advance its UI loop. This queue simply tracks the last posted
 * event.
 */
public class EventQueue {

    private AWTEvent lastEvent;

    public void postEvent(AWTEvent theEvent) {
        synchronized (this) {
            lastEvent = theEvent;
        }
    }

    public AWTEvent peekEvent() {
        synchronized (this) {
            return lastEvent;
        }
    }

    public AWTEvent peekEvent(int id) {
        synchronized (this) {
            return lastEvent;
        }
    }

    public AWTEvent getNextEvent() throws InterruptedException {
        synchronized (this) {
            AWTEvent e = lastEvent;
            lastEvent = null;
            return e;
        }
    }

    public boolean isDispatchThread() { return false; }
}
