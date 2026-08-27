package java.awt.datatransfer;

import java.io.IOException;

public class UnsupportedFlavorException extends Exception {
    private static final long serialVersionUID = 1L;

    public UnsupportedFlavorException(DataFlavor flavor) {
        super("flavor = " + String.valueOf(flavor));
    }
}
