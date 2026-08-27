package java.awt;

/**
 * AWT MenuContainer shim. The desktop client's Loader implements it.
 */
public interface MenuContainer {
    Font getFont();
    void remove(MenuComponent comp);
    boolean postEvent(Event evt);
}
