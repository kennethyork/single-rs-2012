package java.awt;

/**
 * AWT BorderLayout shim. On Android there is no window layout manager; this is
 * a no-op that satisfies the client's layout calls.
 */
public class BorderLayout implements LayoutManager {
    public static final String NORTH = "North";
    public static final String SOUTH = "South";
    public static final String EAST = "East";
    public static final String WEST = "West";
    public static final String CENTER = "Center";

    public BorderLayout() {}
    public BorderLayout(int hgap, int vgap) {}

    @Override
    public void layoutContainer(Container parent) {}
    @Override
    public void addLayoutComponent(String name, Component comp) {}
    @Override
    public void removeLayoutComponent(Component comp) {}
    @Override
    public Dimension preferredLayoutSize(Container parent) { return parent.getSize(); }
    @Override
    public Dimension minimumLayoutSize(Container parent) { return parent.getSize(); }
}
