package java.awt;

import java.awt.image.ImageObserver;
import java.util.ArrayList;
import java.util.List;

/**
 * AWT MediaTracker shim. Software images load synchronously, so images are
 * always immediately available; all wait operations complete instantly.
 */
public class MediaTracker {

    public static final int LOADING = 1;
    public static final int ABORTED = 2;
    public static final int ERRORED = 4;
    public static final int COMPLETE = 8;

    private final Component target;
    private final List<Image> tracked = new ArrayList<>();

    public MediaTracker(Component comp) {
        this.target = comp;
    }

    public void addImage(Image image, int id) {
        if (image != null) tracked.add(image);
    }

    public void addImage(Image image, int id, int w, int h) {
        addImage(image, id);
    }

    public void waitForID(int id) throws InterruptedException {}
    public void waitForAll() throws InterruptedException {}
    public boolean waitForAll(long ms) throws InterruptedException { return true; }
    public boolean waitForID(int id, long ms) throws InterruptedException { return true; }

    public int statusID(int id, boolean load) { return COMPLETE; }
    public int statusAll(boolean load) { return COMPLETE; }
    public boolean isErrorAny() { return false; }
    public boolean isErrorID(int id) { return false; }
    public boolean checkAll() { return true; }
    public boolean checkAll(boolean load) { return true; }
    public boolean checkID(int id) { return true; }
    public boolean checkID(int id, boolean load) { return true; }
    public void removeImage(Image image) { tracked.remove(image); }
    public void removeImage(Image image, int id) { removeImage(image); }
    public void removeImage(Image image, int id, int w, int h) { removeImage(image); }
    public Object[] getErrorsAny() { return null; }
    public Object[] getErrorsID(int id) { return null; }
    public int statusID(int id, boolean load, boolean verify) { return COMPLETE; }
}
