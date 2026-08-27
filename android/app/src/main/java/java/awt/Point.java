package java.awt;

import java.io.Serializable;

/**
 * AWT Point shim.
 */
public class Point implements Serializable {
    public int x;
    public int y;

    public Point() {}
    public Point(Point p) { x = p.x; y = p.y; }
    public Point(int x, int y) { this.x = x; this.y = y; }

    public double getX() { return x; }
    public double getY() { return y; }
    public void setLocation(int x, int y) { this.x = x; this.y = y; }
    public void setLocation(double x, double y) { this.x = (int) Math.floor(x); this.y = (int) Math.floor(y); }
    public void setLocation(Point p) { x = p.x; y = p.y; }
    public void move(int x, int y) { this.x = x; this.y = y; }
    public void translate(int dx, int dy) { x += dx; y += dy; }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Point)) return false;
        Point p = (Point) o;
        return x == p.x && y == p.y;
    }

    @Override
    public int hashCode() { return x * 31 + y; }

    @Override
    public String toString() { return "Point[x=" + x + ",y=" + y + "]"; }

    @Override
    public Object clone() { return new Point(this); }
}
