package java.awt;

import java.util.EventListener;

/**
 * AWT AWTEvent base shim.
 */
public abstract class AWTEvent {
    public static final long RESERVED_ID_MAX = 0xFFFFFFFFL;

    protected Object source;
    protected int id;

    protected AWTEvent(Object source, int id) {
        this.source = source;
        this.id = id;
    }

    public Object getSource() { return source; }
    public int getID() { return id; }
    public String paramString() { return String.valueOf(id); }

    @Override
    public String toString() {
        return getClass().getName() + "[id=" + id + ",source=" + source + "]";
    }
}
