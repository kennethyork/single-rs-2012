package java.awt.datatransfer;

/**
 * AWT Clipboard shim. Android clipboard access is bridged through
 * AndroidPlatform; this keeps a simple in-memory Transferable for the client.
 */
public class Clipboard {

    private Transferable contents;

    public Clipboard() {}

    public Transferable getContents(Object requestor) {
        return contents;
    }

    public void setContents(Transferable contents, ClipboardOwner owner) {
        this.contents = contents;
    }

    public String getName() { return "system"; }
}
