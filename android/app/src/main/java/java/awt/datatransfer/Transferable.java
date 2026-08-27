package java.awt.datatransfer;

/**
 * AWT Transferable shim.
 */
public interface Transferable {
    Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, java.io.IOException;
    DataFlavor[] getTransferDataFlavors();
    boolean isDataFlavorSupported(DataFlavor flavor);
}
