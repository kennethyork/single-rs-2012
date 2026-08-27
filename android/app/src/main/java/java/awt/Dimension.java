package java.awt;

/**
 * AWT Dimension shim.
 */
public class Dimension implements java.io.Serializable {
    public int width;
    public int height;

    public Dimension() {}
    public Dimension(Dimension d) { width = d.width; height = d.height; }
    public Dimension(int width, int height) { this.width = width; this.height = height; }

    public double getWidth() { return width; }
    public double getHeight() { return height; }
    public void setSize(int width, int height) { this.width = width; this.height = height; }
    public void setSize(double width, double height) { this.width = (int) Math.ceil(width); this.height = (int) Math.ceil(height); }
    public void setSize(Dimension d) { width = d.width; height = d.height; }
    public void setDimension(int width, int height) { this.width = width; this.height = height; }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Dimension)) return false;
        Dimension d = (Dimension) o;
        return width == d.width && height == d.height;
    }

    @Override
    public int hashCode() { return width + (height * 31); }

    @Override
    public String toString() {
        return getClass().getName() + "[width=" + width + ",height=" + height + "]";
    }

    @Override
    public Object clone() {
        return new Dimension(this);
    }
}
