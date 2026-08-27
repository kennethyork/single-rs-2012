package java.awt.datatransfer;

/**
 * AWT ClipboardOwner shim.
 */
public interface ClipboardOwner {
    void lostOwnership(Clipboard clipboard, Transferable contents);
}
