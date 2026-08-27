package java.awt;

import java.util.ArrayList;
import java.util.List;

/**
 * AWT Container shim.
 */
public class Container extends Component {

    protected final List<Component> components = new ArrayList<>();

    public Component add(Component comp) {
        comp.setParent(this);
        components.add(comp);
        return comp;
    }

    public Component add(Component comp, int index) {
        comp.setParent(this);
        components.add(index, comp);
        return comp;
    }

    public void add(Component comp, Object constraints) {
        add(comp);
    }

    public void add(String name, Component comp) {
        add(comp);
    }

    public void remove(Component comp) {
        components.remove(comp);
        comp.setParent(null);
    }

    public void removeAll() {
        for (Component c : components) c.setParent(null);
        components.clear();
    }

    public Component[] getComponents() {
        return components.toArray(new Component[0]);
    }

    public int getComponentCount() { return components.size(); }

    public void setLayout(LayoutManager mgr) {}
    public LayoutManager getLayout() { return null; }

    public Insets getInsets() { return new Insets(0, 0, 0, 0); }

    public Container getContentPane() { return this; }

    public void setPreferredSize(Dimension d) { this.preferredSize = d; }
    protected Dimension preferredSize;
    public Dimension getPreferredSize() { return preferredSize != null ? preferredSize : getSize(); }

    public void setFocusCycleRoot(boolean enabled) {}
    public boolean isFocusCycleRoot() { return true; }
    public void setFocusTraversalKeysEnabled(boolean enabled) {}
    public boolean isFocusCycleRoot(Container c) { return this == c; }

    public Graphics getGraphics() { return null; }

    public void doLayout() {}
    public void validate() {}
    public void paint(Graphics g) {
        super.paint(g);
        for (Component c : components) if (c.isVisible()) c.paint(g);
    }
}
