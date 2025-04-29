import javax.swing.*;
import java.awt.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * Dashboard window displaying transaction summaries, saving goals, and navigation actions.
 */
public class DashboardWindow {
    private JFrame frame;
    private Map<String, JLabel> summaryLabels = new HashMap<>();

    // UI Constants
    private final Color PRIMARY_DARK = new Color(18, 12, 48);
    private final Color PRIMARY_LIGHT = new Color(33, 147, 176);
    private final Color CARD_INCOME = new Color(102, 255, 178, 40);
    private final Color CARD_EXPENSE = new Color(255, 102, 102, 40);
    private final Color CARD_BALANCE = new Color(102, 178, 255, 40);
    private final Color TRANSPARENT_WHITE = new Color(255, 255, 255, 30);
    private final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 32);
    private final Font SUBTITLE_FONT = new Font("Segoe UI", Font.BOLD, 24);
    private final Font REGULAR_FONT = new Font("Segoe UI", Font.PLAIN, 16);
    private static final Color PRIMARY_COLOR = new Color(18, 12, 48);
    private static final Color BUTTON_HOVER = new Color(20, 39, 80);
    private static final Color TEXT_COLOR = Color.WHITE;

    /**
     * Constructor to initialize and display the Dashboard window.
     */
    public DashboardWindow(String username) {
        frame = new JFrame("Dashboard");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setLocationRelativeTo(null);

        // Main gradient background
        JPanel gradientPanel = createGradientPanel();
        gradientPanel.setLayout(new BorderLayout());

        JPanel contentWrapper = new JPanel(new GridBagLayout());
        contentWrapper.setOpaque(false);
        contentWrapper.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // Create the main grid
        JPanel mainGrid = new JPanel(new GridBagLayout());
        mainGrid.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.BOTH;

        // Left section (Summary Cards and Goals)
        JPanel leftSection = new JPanel(new GridBagLayout());
        leftSection.setOpaque(false);

        // Summary Cards Panel
        JPanel summaryCardsPanel = createSummaryCards();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 0.4;
        leftSection.add(summaryCardsPanel, gbc);

        // Goals Panel
        JPanel goalsPanel = new JPanel(new BorderLayout());
        goalsPanel.setOpaque(false);
        goalsPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JLabel goalsTitle = new JLabel("Saving Goals", SwingConstants.LEFT);
        goalsTitle.setFont(SUBTITLE_FONT);
        goalsTitle.setForeground(Color.WHITE);

        titlePanel.add(goalsTitle, BorderLayout.CENTER);
        goalsPanel.add(titlePanel, BorderLayout.NORTH);


        JPanel goalsContent = new JPanel();
        goalsContent.setLayout(new BoxLayout(goalsContent, BoxLayout.Y_AXIS));
        goalsContent.setOpaque(false);
        fetchGoalsAndDisplay(goalsContent);

        JScrollPane scrollPane = new JScrollPane(goalsContent);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        goalsPanel.add(scrollPane, BorderLayout.CENTER);

        gbc.gridy = 1;
        gbc.weighty = 0.6;
        leftSection.add(goalsPanel, gbc);

        // Add left section to main grid
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.7;
        gbc.weighty = 1.0;
        mainGrid.add(leftSection, gbc);

        // Right section (Actions)
        JPanel rightSection = createActionPanel(username);
        gbc.gridx = 1;
        gbc.weightx = 0.3;
        mainGrid.add(rightSection, gbc);

        contentWrapper.add(mainGrid);
        gradientPanel.add(contentWrapper, BorderLayout.CENTER);
        frame.add(gradientPanel);
        frame.setVisible(true);

        fetchSummaryData();
    }

    /**
     * Creates the main background gradient panel.
     */
    private JPanel createGradientPanel() {
        return new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, PRIMARY_DARK, getWidth(), getHeight(), PRIMARY_LIGHT);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
    }

    /**
     * Creates the summary cards panel (Income, Expenses, Balance).
     */
    private JPanel createSummaryCards() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 20, 0));
        panel.setOpaque(false);
        panel.add(createCard("Total Income", "loading...", CARD_INCOME));
        panel.add(createCard("Total Expenses", "loading...", CARD_EXPENSE));
        panel.add(createCard("Balance", "loading...", CARD_BALANCE));
        return panel;
    }

    /**
     * Creates the right-side action panel (Add/View/Set Goal/Summary/Logout).
     */
    private JPanel createActionPanel(String username) {
        JPanel panel = new JPanel(new GridBagLayout()) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(18, 12, 48, 152));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 10, 0);

        // Logo
        ImageIcon logoIcon = new ImageIcon(getClass().getResource("/assets/logo.png"));
        Image scaledImage = logoIcon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
        JLabel logoLabel = new JLabel(new ImageIcon(scaledImage));
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(logoLabel, gbc);

        // Welcome text
        JLabel welcomeLabel = new JLabel("Welcome, " + username + "!", SwingConstants.CENTER);
        welcomeLabel.setFont(TITLE_FONT);
        welcomeLabel.setForeground(Color.WHITE);
        panel.add(welcomeLabel, gbc);

        // Buttons
        String[] buttonLabels = {
                "✚ Add Transaction",
                "☰ View Transactions",
                "★ Set Saving Goal",
                "Summary",
                "⍈ Logout"
        };

        for (String label : buttonLabels) {
            JButton button = new JButton(label);
            styleButton(button);
            panel.add(button, gbc);

            // Add action listeners
            button.addActionListener(e -> {
                switch (label) {
                    case "✚ Add Transaction":
                        new AddTransactionForm(username);
                        frame.dispose();
                        break;
                    case "☰ View Transactions":
                        new ViewTransactionsWindow(username);
                        frame.dispose();
                        break;
                    case "★ Set Saving Goal":
                        new AddGoalForm(username);
                        frame.dispose();
                        break;
                    case "Summary":
                        new SummaryWindow(username);
                        frame.dispose();
                        break;
                    case "⍈ Logout":
                        handleLogout();
                        break;
                }
            });
        }

        return panel;
    }

    /**
     * Styles a given JButton (hover effects, fonts, colors).
     */
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

    /**
     * Handles logout confirmation and returning to login screen.
     */
    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(frame,
                "Are you sure you want to logout?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            new LoginForm();
            frame.dispose();
        }
    }
    /**
     * Creates a single summary card (Income, Expenses, Balance).
     */
    private JPanel createCard(String title, String value, Color bg) {
        JPanel card = new JPanel(new BorderLayout(10, 10)) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(bg);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(REGULAR_FONT);
        titleLabel.setForeground(Color.WHITE);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(SUBTITLE_FONT);
        valueLabel.setForeground(Color.WHITE);

        summaryLabels.put(title.toLowerCase(), valueLabel);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private JPanel createGoalCard(JSONObject goal) {
        String name = goal.getString("name");
        double target = goal.getDouble("targetAmount");
        double saved = goal.optDouble("savedAmount", 0);
        int progress = (int) Math.round((saved / target) * 100);
        String deadline = goal.getString("deadline");

        JPanel card = new JPanel(new GridLayout(5, 1, 5, 5)) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(TRANSPARENT_WHITE);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel nameLabel = new JLabel("🎯 " + name);
        JLabel progressLabel = new JLabel(String.format("%.1f%% Complete", (saved / target) * 100));
        JLabel targetLabel = new JLabel(String.format("Target: %.2f SAR", target));
        JLabel savedLabel = new JLabel(String.format("Saved: %.2f SAR", saved));
        JLabel deadlineLabel = new JLabel("Due: " + deadline);

        if (progress == 100) {
            nameLabel.setText("🎯 " + name + " 🎉");
        }

        JProgressBar progressBar = new JProgressBar(0, 100) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int width = getWidth();
                int height = 20;
                int arc = 20; // Rounded corners

                // Draw background
                g2.setColor(new Color(255, 255, 255, 40)); // Background color
                g2.fillRoundRect(0, 0, width, height, arc, arc);

                // Draw progress fill
                int filledWidth = (int) (width * getPercentComplete());
                GradientPaint gradient = new GradientPaint(0, 0, new Color(0, 165, 145), filledWidth, 0, new Color(0, 94, 80));
                g2.setPaint(gradient);
                g2.fillRoundRect(0, 0, filledWidth, height, arc, arc);

                // Draw percentage text
                String text = getValue() + "%";
                g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                FontMetrics fm = g2.getFontMetrics();
                int textWidth = fm.stringWidth(text);
                int textHeight = fm.getAscent();

                g2.setColor(Color.WHITE);
                g2.drawString(text, (width - textWidth) / 2, (height + textHeight) / 2 - 2);

            }
        };
        progressBar.setBorder(BorderFactory.createEmptyBorder());
        progressBar.setOpaque(false);
        progressBar.setPreferredSize(new Dimension(progressBar.getPreferredSize().width, 10));
        progressBar.setValue(progress);

        for (JLabel label : new JLabel[]{nameLabel, progressLabel, targetLabel, savedLabel, deadlineLabel}) {
            label.setFont(REGULAR_FONT);
            label.setForeground(Color.WHITE);
        }

        card.add(nameLabel);
        card.add(progressBar);
        card.add(progressLabel);
        card.add(targetLabel);
        card.add(deadlineLabel);

        return card;
    }

    private void fetchSummaryData() {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/transactions"))
                .header("Cookie", AuthClient.sessionCookie)
                .GET()
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        JSONArray array = new JSONArray(response.body());
                        double income = 0, expense = 0;

                        for (int i = 0; i < array.length(); i++) {
                            JSONObject t = array.getJSONObject(i);
                            double amount = t.getDouble("amount");
                            if (t.getString("type").equalsIgnoreCase("INCOME")) income += amount;
                            else expense += amount;
                        }

                        double balance = income - expense;
                        double finalIncome = income, finalExpense = expense;

                        SwingUtilities.invokeLater(() -> {
                            updateCard("total income", String.format("%.2f SAR", finalIncome));
                            updateCard("total expenses", String.format("%.2f SAR", finalExpense));
                            updateCard("balance", String.format("%.2f SAR", balance));
                        });
                    }
                });
    }

    private void fetchGoalsAndDisplay(JPanel container) {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/saving-goals"))
                .header("Cookie", AuthClient.sessionCookie)
                .GET()
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        JSONArray goals = new JSONArray(response.body());

                        SwingUtilities.invokeLater(() -> {
                            container.removeAll();
                            for (int i = 0; i < goals.length(); i++) {
                                JSONObject goal = goals.getJSONObject(i);
                                container.add(createGoalCard(goal));
                                if (i < goals.length() - 1) {
                                    container.add(Box.createVerticalStrut(10));
                                }
                            }
                            container.revalidate();
                            container.repaint();
                        });
                    }
                });
    }

    private void updateCard(String name, String value) {
        JLabel label = summaryLabels.get(name.toLowerCase());
        if (label != null) {
            label.setText(value);
        }
    }

}