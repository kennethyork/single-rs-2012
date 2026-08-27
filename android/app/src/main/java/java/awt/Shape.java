package java.awt;

/**
 * AWT Shape shim. Only the basic geometric interface needed by the client is
 * provided.
 */
public interface Shape {
    java.awt.Rectangle getBounds();
    Rectangle getBounds2D();
    boolean contains(double x, double y);
    boolean contains(int x, int y);
    boolean isEmpty();
}
