package java.awt;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

/**
 * AWT Frame shim. On Android there is no native OS window; a Frame is a
 * headless holder used for the client's AWT-style layout and window-listener
 * bookkeeping. The real output surface is the Android GameSurfaceView.
 */
public class Frame extends Window {

    public static final int NORMAL = 0;
    public static final int ICONIFIED = 1;
    public static final int MAXIMIZED_HORIZ = 2;
    public static final int MAXIMIZED_VERT = 4;
    public static final int MAXIMIZED_BOTH = 6;

    private String title;
    private boolean resizable = true;
    private boolean undecorated = false;
    private int state = NORMAL;

    public Frame() {}

    public Frame(String title) {
        this.title = title;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public boolean isResizable() { return resizable; }
    public void setResizable(boolean resizable) { this.resizable = resizable; }

    public boolean isUndecorated() { return undecorated; }
    public void setUndecorated(boolean undecorated) { this.undecorated = undecorated; }

    public int getExtendedState() { return state; }
    public void setExtendedState(int state) { this.state = state; }
    public void setState(int state) { this.state = state; }

    public void setDefaultCloseOperation(int operation) {}

    public void pack() {}
    public void toFront() {}
    public void dispose() {
        setVisible(false);
    }

    public void addWindowListener(WindowListener l) {
        if (l != null && !windowListeners.contains(l)) windowListeners.add(l);
    }

    public void removeWindowListener(WindowListener l) { windowListeners.remove(l); }

    private final java.util.List<WindowListener> windowListeners = new java.util.ArrayList<>();

    protected void processWindowEvent(int id) {
        if (windowListeners.isEmpty()) return;
        WindowEvent e = new WindowEvent(this, id);
        for (WindowListener l : windowListeners) {
            switch (id) {
                case WindowEvent.WINDOW_OPENED: l.windowOpened(e); break;
                case WindowEvent.WINDOW_CLOSING: l.windowClosing(e); break;
                case WindowEvent.WINDOW_CLOSED: l.windowClosed(e); break;
                case WindowEvent.WINDOW_ICONIFIED: l.windowIconified(e); break;
                case WindowEvent.WINDOW_DEICONIFIED: l.windowDeiconified(e); break;
                case WindowEvent.WINDOW_ACTIVATED: l.windowActivated(e); break;
                case WindowEvent.WINDOW_DEACTIVATED: l.windowDeactivated(e); break;
            }
        }
    }

    @Override
    public Insets getInsets() { return new Insets(0, 0, 0, 0); }

    @Override
    public Graphics getGraphics() { return null; }
}
