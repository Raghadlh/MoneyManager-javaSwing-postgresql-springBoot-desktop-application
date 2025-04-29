import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class UIUtils {
    private static final Color PRIMARY_COLOR = new Color(18, 12, 48);
    private static final Color BUTTON_HOVER = new Color(20, 39, 80);
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Color FIELD_BORDER = new Color(220, 220, 220);

    public static void styleButton(JButton button) {
        button.setFocusPainted(false);
        button.setBackground(PRIMARY_COLOR);
        button.setForeground(TEXT_COLOR);
        button.setFont(new Font("Segoe UI", Font.BOLD, 16));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(12, 25, 12, 25));

        button.setContentAreaFilled(true);
        button.setOpaque(true);

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(BUTTON_HOVER);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(PRIMARY_COLOR);
            }
        });
    }

    public static void styleField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(FIELD_BORDER, 1),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        field.setBackground(Color.WHITE);
        field.setCaretColor(Color.DARK_GRAY);
    }

    public static JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        label.setForeground(Color.WHITE);
        return label;
    }
}
