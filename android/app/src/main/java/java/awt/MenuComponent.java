package java.awt;

/**
 * AWT MenuComponent shim (referenced by MenuContainer).
 */
public class MenuComponent {
    public Font getFont() { return new Font("Dialog", Font.PLAIN, 12); }
}
