package javax.swing;

import java.awt.Component;
import java.awt.Container;

/**
 * Swing JFrame shim. On Android there is no OS window; this is a headless
 * container that satisfies the client's windowing calls.
 */
public class JFrame extends Container {
    public static final int EXIT_ON_CLOSE = 3;

    private String title;
    private boolean visible;

    public JFrame() {}
    public JFrame(String title) { this.title = title; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public void setDefaultCloseOperation(int operation) {}
    public void setResizable(boolean resizable) {}
    public void pack() {}
    public void setVisible(boolean visible) { this.visible = visible; }
    public boolean isVisible() { return visible; }
    public void dispose() { visible = false; }
    public void toFront() {}

    @Override
    public Container getContentPane() { return this; }
}
