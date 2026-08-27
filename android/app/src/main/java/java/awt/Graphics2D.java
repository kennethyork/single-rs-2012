package java.awt;

/**
 * AWT Graphics2D shim. Only the world server's dungeon-layout debug renderer
 * (com.rs.game.content.skills.dungeoneering.Dungeon#draw) uses Graphics2D, and
 * it never runs on Android -- but it is compiled as part of the shared server
 * sources, so the type has to exist.
 *
 * Drawing calls delegate to the Graphics shim; the 2D-only transform and
 * rendering-hint calls are recorded or ignored.
 */
public class Graphics2D extends Graphics {

    private double scaleX = 1.0;
    private double scaleY = 1.0;

    public Graphics2D(java.awt.image.BufferedImage image) { super(image); }
    public Graphics2D(int[] pixels, int width, int height) { super(pixels, width, height); }

    public void scale(double sx, double sy) { scaleX *= sx; scaleY *= sy; }
    public double getScaleX() { return scaleX; }
    public double getScaleY() { return scaleY; }

    public void rotate(double theta) {}
    public void rotate(double theta, double x, double y) {}
    public void shear(double shx, double shy) {}
    public void translate(double tx, double ty) { translate((int) tx, (int) ty); }

    public void setStroke(Object stroke) {}
    public void setPaint(Object paint) { if (paint instanceof Color c) setColor(c); }
    public void setComposite(Object comp) {}
    public void setRenderingHint(Object key, Object value) {}
    public Object getRenderingHint(Object key) { return null; }
    public void setRenderingHints(java.util.Map<?, ?> hints) {}

    public void draw(Shape s) {}
    public void fill(Shape s) {}
}
