package javax.swing;

import java.awt.Component;

/**
 * Swing JOptionPane shim. On Android there is no dialog; showMessageDialog is a
 * no-op (errors are logged by the client's showError path).
 */
public class JOptionPane {
    public static final int PLAIN_MESSAGE = -1;
    public static final int ERROR_MESSAGE = 0;
    public static final int INFORMATION_MESSAGE = 1;
    public static final int WARNING_MESSAGE = 2;
    public static final int QUESTION_MESSAGE = 3;

    public static void showMessageDialog(Component parent, Object message) {
        System.err.println(String.valueOf(message));
    }

    public static void showMessageDialog(Component parent, Object message, String title, int messageType) {
        System.err.println(String.valueOf(message));
    }
}
