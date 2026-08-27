package java.awt;

/**
 * AWT Robot shim. Android has no desktop cursor automation; mouse moves are
 * forwarded to the platform input bridge so the client's cursor can be moved.
 */
public class Robot {

    public Robot() {}

    public void mouseMove(int x, int y) {
        com.rs.android.AndroidPlatform.onMouseMoved(x, y);
    }

    public void mousePress(int buttons) {}
    public void mouseRelease(int buttons) {}
    public void keyPress(int keycode) {}
    public void keyRelease(int keycode) {}
    public void delay(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) { }
    }
}
