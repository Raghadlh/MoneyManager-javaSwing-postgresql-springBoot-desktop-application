import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.concurrent.*;

public class ToastMessage extends JWindow {
    private static final Color BACKGROUND_COLOR = new Color(102, 255, 214, 84);
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final int PADDING = 20;
    private static final int CORNER_RADIUS = 15;
    private static final Font MESSAGE_FONT = new Font("Segoe UI", Font.BOLD, 16);

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final int duration;
    private float opacity = 1.0f;

    public ToastMessage(String message, int durationMillis) {
        this.duration = durationMillis;
        initializeComponents(message);
        setWindowShape();
    }

    private void initializeComponents(String message) {
        setBackground(new Color(0, 0, 0, 0));
        setLayout(new BorderLayout());
        JLabel label = createMessageLabel(message);
        JPanel panel = createBackgroundPanel();
        panel.add(label);
        add(panel);
        pack();
        setAlwaysOnTop(true);


    }

    private JLabel createMessageLabel(String message) {
        JLabel label = new JLabel(message, SwingConstants.CENTER);
        label.setForeground(TEXT_COLOR);
        label.setFont(MESSAGE_FONT);
        label.setOpaque(false);
        return label;
    }

    private JPanel createBackgroundPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
                g2d.setColor(new Color(0, 0, 0, 0));
                g2d.fillRect(0, 0, getWidth(), getHeight());

                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                RoundRectangle2D.Float background = new RoundRectangle2D.Float(
                        0, 0, getWidth(), getHeight(), CORNER_RADIUS, CORNER_RADIUS
                );
                g2d.setColor(BACKGROUND_COLOR);
                g2d.fill(background);
            }

        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING));
        return panel;
    }


    private void setWindowShape() {
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                setShape(new RoundRectangle2D.Float(
                        0, 0, getWidth(), getHeight(), CORNER_RADIUS, CORNER_RADIUS
                ));
            }
        });
    }

    public void showToast(int x, int y) {
        setLocation(x, y);
        setVisible(true);

        // Fade in animation
        Timer fadeInTimer = new Timer(50, e -> {
            opacity = Math.min(opacity + 0.1f, 1.0f);
            repaint();
            if (opacity >= 1.0f) {
                ((Timer)e.getSource()).stop();
            }
        });
        opacity = 0.0f;
        fadeInTimer.start();

        // Schedule fade out
        scheduler.schedule(() -> {
            Timer fadeOutTimer = new Timer(50, e -> {
                opacity = Math.max(opacity - 0.1f, 0.0f);
                repaint();
                if (opacity <= 0.0f) {
                    ((Timer)e.getSource()).stop();
                    dispose();
                    scheduler.shutdown();
                }
            });
            SwingUtilities.invokeLater(fadeOutTimer::start);
        }, duration - 500, TimeUnit.MILLISECONDS);
    }

}