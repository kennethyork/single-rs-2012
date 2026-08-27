package java.awt;

import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;
import java.awt.image.ImageObserver;
import java.util.ArrayList;
import java.util.List;

/**
 * AWT Component shim. On Android the "component" is a headless holder; the
 * actual rendering and input are bridged through AndroidPlatform/GameSurfaceView.
 * This provides the event-listener bookkeeping and basic geometry the client
 * relies on.
 */
public class Component implements java.io.Serializable, ImageObserver {

    protected String name;
    protected int x, y, width, height;
    protected boolean visible = true;
    protected Component parent;
    protected Color background = Color.black;
    protected Font font = new Font("Dialog", Font.PLAIN, 12);
    protected final List<KeyListener> keyListeners = new ArrayList<>();
    protected final List<FocusListener> focusListeners = new ArrayList<>();
    protected final List<MouseListener> mouseListeners = new ArrayList<>();
    protected final List<MouseMotionListener> mouseMotionListeners = new ArrayList<>();
    protected final List<MouseWheelListener> mouseWheelListeners = new ArrayList<>();

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Component getParent() { return parent; }
    void setParent(Component parent) { this.parent = parent; }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }

    public Dimension getSize() { return new Dimension(width, height); }
    public void setSize(int w, int h) { this.width = w; this.height = h; }
    public void setSize(Dimension d) { setSize(d.width, d.height); }

    public java.awt.Point getLocation() { return new java.awt.Point(x, y); }
    public void setLocation(int x, int y) { this.x = x; this.y = y; }
    public void setLocation(java.awt.Point p) { setLocation(p.x, p.y); }

    public Rectangle getBounds() { return new Rectangle(x, y, width, height); }
    public void setBounds(int x, int y, int w, int h) { this.x = x; this.y = y; this.width = w; this.height = h; }
    public void setBounds(Rectangle r) { setBounds(r.x, r.y, r.width, r.height); }

    public void setVisible(boolean visible) { this.visible = visible; }
    public boolean isVisible() { return visible; }
    public boolean isShowing() { return visible; }
    public boolean isDisplayable() { return visible; }

    public void setBackground(Color c) { this.background = c; }
    public Color getBackground() { return background; }
    public void setForeground(Color c) {}
    public Color getForeground() { return Color.black; }

    public void setFocusable(boolean focusable) {}
    public boolean isFocusable() { return true; }
    public void requestFocus() {}
    public void setFocusTraversalKeysEnabled(boolean enabled) {}
    public void setFocusCycleRoot(boolean enabled) {}

    public java.awt.Point getLocationOnScreen() {
        int ox = 0, oy = 0;
        Component cur = this;
        while (cur != null) { ox += cur.x; oy += cur.y; cur = cur.parent; }
        return new java.awt.Point(ox, oy);
    }

    public Graphics getGraphics() { return null; }
    public FontMetrics getFontMetrics(java.awt.Font f) { return new FontMetrics(f != null ? f : new Font("Dialog", Font.PLAIN, 12)); }

    public Image createImage(int width, int height) {
        return new java.awt.image.BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_ARGB);
    }

    public void repaint() {}
    public void repaint(long tm) {}
    public void repaint(int x, int y, int w, int h) {}
    public void repaint(long tm, int x, int y, int w, int h) {}

    public void paint(Graphics g) {}
    public void update(Graphics g) { paint(g); }

    public void addKeyListener(KeyListener l) { if (l != null && !keyListeners.contains(l)) keyListeners.add(l); }
    public void removeKeyListener(KeyListener l) { keyListeners.remove(l); }
    public void addFocusListener(FocusListener l) { if (l != null && !focusListeners.contains(l)) focusListeners.add(l); }
    public void removeFocusListener(FocusListener l) { focusListeners.remove(l); }
    public void addMouseListener(MouseListener l) { if (l != null && !mouseListeners.contains(l)) mouseListeners.add(l); }
    public void removeMouseListener(MouseListener l) { mouseListeners.remove(l); }
    public void addMouseMotionListener(MouseMotionListener l) { if (l != null && !mouseMotionListeners.contains(l)) mouseMotionListeners.add(l); }
    public void removeMouseMotionListener(MouseMotionListener l) { mouseMotionListeners.remove(l); }
    public void addMouseWheelListener(MouseWheelListener l) { if (l != null && !mouseWheelListeners.contains(l)) mouseWheelListeners.add(l); }
    public void removeMouseWheelListener(MouseWheelListener l) { mouseWheelListeners.remove(l); }

    public Toolkit getToolkit() { return Toolkit.getDefaultToolkit(); }

    public void setCursor(Cursor cursor) {}
    public Cursor getCursor() { return Cursor.getDefaultCursor(); }
    public void setCursor(Cursor cursor, boolean hidden) {}

    public void enableInputMethods(boolean enable) {}

    public void remove(Component comp) {}
    public void remove(Canvas canvas) {}

    // java.awt.MenuContainer is implemented by the desktop Loader, which
    // inherits these from Component (as it does on the JDK) rather than
    // declaring them itself.
    public Font getFont() { return font; }
    public void setFont(Font f) { this.font = f; }
    public void remove(MenuComponent comp) {}
    public boolean postEvent(Event evt) { return false; }

    /**
     * Delivers an event to this component's listeners.
     *
     * The shim collects listeners the client registers but has no event queue to
     * feed them -- there is no AWT underneath. com.rs.android.GameSurfaceView
     * calls this to turn Android touches into the mouse events the client is
     * waiting for.
     *
     * Takes an InputEvent rather than an AWTEvent: in this shim InputEvent is
     * its own root, not an AWTEvent subclass.
     */
    public void dispatchInputEvent(InputEvent event) {
        if (event instanceof MouseEvent mouse) {
            switch (mouse.getID()) {
                case MouseEvent.MOUSE_PRESSED:
                    for (MouseListener l : mouseListeners) l.mousePressed(mouse);
                    break;
                case MouseEvent.MOUSE_RELEASED:
                    for (MouseListener l : mouseListeners) l.mouseReleased(mouse);
                    break;
                case MouseEvent.MOUSE_CLICKED:
                    for (MouseListener l : mouseListeners) l.mouseClicked(mouse);
                    break;
                case MouseEvent.MOUSE_ENTERED:
                    for (MouseListener l : mouseListeners) l.mouseEntered(mouse);
                    break;
                case MouseEvent.MOUSE_EXITED:
                    for (MouseListener l : mouseListeners) l.mouseExited(mouse);
                    break;
                case MouseEvent.MOUSE_MOVED:
                    for (MouseMotionListener l : mouseMotionListeners) l.mouseMoved(mouse);
                    break;
                case MouseEvent.MOUSE_DRAGGED:
                    for (MouseMotionListener l : mouseMotionListeners) l.mouseDragged(mouse);
                    break;
                default:
                    break;
            }
        } else if (event instanceof KeyEvent key) {
            switch (key.getID()) {
                case KeyEvent.KEY_PRESSED:
                    for (KeyListener l : keyListeners) l.keyPressed(key);
                    break;
                case KeyEvent.KEY_RELEASED:
                    for (KeyListener l : keyListeners) l.keyReleased(key);
                    break;
                case KeyEvent.KEY_TYPED:
                    for (KeyListener l : keyListeners) l.keyTyped(key);
                    break;
                default:
                    break;
            }
        }
    }

    public Object getTreeLock() { return this; }

    public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) { return false; }

    List<KeyListener> keyListeners() { return keyListeners; }
    List<FocusListener> focusListeners() { return focusListeners; }
    List<MouseListener> mouseListeners() { return mouseListeners; }
    List<MouseMotionListener> mouseMotionListeners() { return mouseMotionListeners; }
    List<MouseWheelListener> mouseWheelListeners() { return mouseWheelListeners; }
}
