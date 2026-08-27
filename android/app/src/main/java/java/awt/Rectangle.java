package java.awt;

import java.io.Serializable;

/**
 * AWT Rectangle shim.
 */
public class Rectangle implements Shape, Serializable {
    public int x;
    public int y;
    public int width;
    public int height;

    public Rectangle() {}
    public Rectangle(Rectangle r) { this(r.x, r.y, r.width, r.height); }
    public Rectangle(int x, int y, int width, int height) { this.x = x; this.y = y; this.width = width; this.height = height; }
    public Rectangle(int width, int height) { this(0, 0, width, height); }
    public Rectangle(Dimension d) { this(0, 0, d.width, d.height); }
    public Rectangle(Point p, Dimension d) { this(p.x, p.y, d.width, d.height); }
    public Rectangle(Point p) { this(p.x, p.y, 0, 0); }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }
    public boolean isEmpty() { return width <= 0 || height <= 0; }
    public Rectangle getBounds() { return new Rectangle(x, y, width, height); }
    public Rectangle getBounds2D() { return new Rectangle(x, y, width, height); }

    public boolean contains(int px, int py) {
        return px >= x && px < x + width && py >= y && py < y + height;
    }
    public boolean contains(Point p) { return contains(p.x, p.y); }
    public boolean contains(double px, double py) { return contains((int) px, (int) py); }

    public void setLocation(int x, int y) { this.x = x; this.y = y; }
    public void setLocation(Point p) { x = p.x; y = p.y; }
    public void setSize(int width, int height) { this.width = width; this.height = height; }
    public void setSize(Dimension d) { width = d.width; height = d.height; }
    public void setBounds(int x, int y, int width, int height) { this.x = x; this.y = y; this.width = width; this.height = height; }
    public void setBounds(Rectangle r) { setBounds(r.x, r.y, r.width, r.height); }
    public void setRect(double x, double y, double w, double h) { setBounds((int) x, (int) y, (int) w, (int) h); }
    public void setRect(Rectangle r) { setBounds(r); }

    public void translate(int dx, int dy) { x += dx; y += dy; }
    public void translate(double dx, double dy) { x += (int) dx; y += (int) dy; }
    public void grow(int h, int v) { x -= h; y -= v; width += 2 * h; height += 2 * v; }

    public void add(int newx, int newy) {
        if (isEmpty()) { x = newx; y = newy; width = 1; height = 1; }
        else {
            int x1 = Math.min(x, newx), y1 = Math.min(y, newy);
            int x2 = Math.max(x + width, newx), y2 = Math.max(y + height, newy);
            setBounds(x1, y1, x2 - x1, y2 - y1);
        }
    }
    public void add(Point p) { add(p.x, p.y); }
    public void add(Rectangle r) {
        if (r.isEmpty()) return;
        int x1 = Math.min(x, r.x), y1 = Math.min(y, r.y);
        int x2 = Math.max(x + width, r.x + r.width), y2 = Math.max(y + height, r.y + r.height);
        setBounds(x1, y1, x2 - x1, y2 - y1);
    }

    public Rectangle intersection(Rectangle r) {
        int x1 = Math.max(x, r.x), y1 = Math.max(y, r.y);
        int x2 = Math.min(x + width, r.x + r.width), y2 = Math.min(y + height, r.y + r.height);
        return new Rectangle(x1, y1, Math.max(0, x2 - x1), Math.max(0, y2 - y1));
    }
    public boolean intersects(Rectangle r) {
        return r.x < x + width && x < r.x + r.width && r.y < y + height && y < r.y + r.height;
    }

    public Rectangle union(Rectangle r) {
        Rectangle copy = new Rectangle(this);
        copy.add(r);
        return copy;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Rectangle)) return false;
        Rectangle r = (Rectangle) o;
        return x == r.x && y == r.y && width == r.width && height == r.height;
    }

    @Override
    public int hashCode() { return x * 7 + y * 31 + width * 17 + height; }

    @Override
    public String toString() {
        return getClass().getName() + "[x=" + x + ",y=" + y + ",width=" + width + ",height=" + height + "]";
    }

    @Override
    public Object clone() { return new Rectangle(this); }
}
