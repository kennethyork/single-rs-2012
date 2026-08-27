package java.awt.datatransfer;

/**
 * AWT DataFlavor shim.
 */
public class DataFlavor {

    public static final DataFlavor stringFlavor = new DataFlavor(String.class, "Java Unicode String");
    public static final DataFlavor plainTextFlavor = new DataFlavor(String.class, "Plain Text");

    private final Class<?> representationClass;
    private final String humanPresentableName;

    public DataFlavor(Class<?> representationClass, String humanPresentableName) {
        this.representationClass = representationClass;
        this.humanPresentableName = humanPresentableName;
    }

    public Class<?> getRepresentationClass() { return representationClass; }
    public String getHumanPresentableName() { return humanPresentableName; }
    public String getMimeType() { return "application/x-java-serialized-object"; }

    @Override
    public boolean equals(Object o) {
        return o instanceof DataFlavor && ((DataFlavor) o).representationClass == representationClass;
    }

    @Override
    public int hashCode() {
        return representationClass != null ? representationClass.hashCode() : 0;
    }
}
