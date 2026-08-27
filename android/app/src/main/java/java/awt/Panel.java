package java.awt;

/**
 * AWT Panel shim. The desktop client's Loader extends Panel; on Android this is
 * a headless container (no OS window).
 */
public class Panel extends Container {
    public Panel() {}
    public Panel(LayoutManager layout) { setLayout(layout); }
}
