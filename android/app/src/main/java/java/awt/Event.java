package java.awt;

/**
 * AWT Event shim (referenced by MenuContainer.postEvent).
 */
public class Event {
    public Object target;
    public long when;
    public int id;
    public int x;
    public int y;
    public int key;
    public int modifiers;
    public Object arg;

    public Event(Object target, long when, int id, int x, int y, int key, int modifiers, Object arg) {
        this.target = target;
        this.when = when;
        this.id = id;
        this.x = x;
        this.y = y;
        this.key = key;
        this.modifiers = modifiers;
        this.arg = arg;
    }
}
