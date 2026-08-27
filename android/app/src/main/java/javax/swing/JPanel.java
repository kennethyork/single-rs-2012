package javax.swing;

import java.awt.Container;
import java.awt.LayoutManager;

/**
 * Swing JPanel shim. On Android this is a headless container.
 */
public class JPanel extends Container {
    public JPanel() {}
    public JPanel(LayoutManager layout) { setLayout(layout); }
}
