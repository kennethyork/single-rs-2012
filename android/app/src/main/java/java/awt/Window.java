package java.awt;

/**
 * AWT Window shim base for Frame. Android has no separate OS window.
 */
public class Window extends Container {

    private GraphicsConfiguration graphicsConfiguration;
    private boolean undecorated;

    public Window() {
        this(null);
    }

    public Window(GraphicsConfiguration gc) {
        this.graphicsConfiguration = gc;
    }

    public void setUndecorated(boolean undecorated) { this.undecorated = undecorated; }
    public boolean isUndecorated() { return undecorated; }
    public void enableInputMethods(boolean enable) {}
    public void dispose() {}
}
