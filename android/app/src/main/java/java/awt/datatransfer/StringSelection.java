package java.awt.datatransfer;

import java.io.IOException;

/**
 * AWT StringSelection shim.
 */
public class StringSelection implements Transferable, ClipboardOwner {

    private final String data;

    public StringSelection(String data) {
        this.data = data;
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (flavor.equals(DataFlavor.stringFlavor)) {
            return data;
        }
        throw new UnsupportedFlavorException(flavor);
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{DataFlavor.stringFlavor};
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavor.equals(DataFlavor.stringFlavor);
    }

    @Override
    public void lostOwnership(Clipboard clipboard, Transferable contents) {}
}
