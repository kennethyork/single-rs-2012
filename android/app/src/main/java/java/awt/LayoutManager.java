package java.awt;

/**
 * AWT LayoutManager shim.
 */
public interface LayoutManager {
    void layoutContainer(Container parent);
    void addLayoutComponent(String name, Component comp);
    void removeLayoutComponent(Component comp);
    Dimension preferredLayoutSize(Container parent);
    Dimension minimumLayoutSize(Container parent);
}
