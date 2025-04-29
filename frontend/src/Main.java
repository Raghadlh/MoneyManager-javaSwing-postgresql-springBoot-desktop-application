import javax.swing.*;

public class Main {
    public static void main(String[] args) {

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            System.err.println("Failed to set look and feel.");
        }

        SwingUtilities.invokeLater(() -> {
            try {
                new LoginForm();
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "An unexpected error occurred while launching the application.",
                        "Startup Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
