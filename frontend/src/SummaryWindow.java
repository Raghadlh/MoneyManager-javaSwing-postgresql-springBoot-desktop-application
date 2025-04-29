import javax.swing.*;
import java.awt.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.json.JSONArray;
import org.json.JSONObject;

public class SummaryWindow {
    private JFrame frame;
    private JPanel mainPanel;
    private JPanel pieChartPanel;
    private JPanel barChartPanel;
    private JLabel smartSummaryLabel;

    private static final Color BACKGROUND_START = new Color(18, 12, 48);
    private static final Color BACKGROUND_END = new Color(33, 147, 176);
    private static final Color CARD_BACKGROUND = new Color(255, 255, 255, 30);
    private static final Color TEXT_COLOR = new Color(240, 240, 240);
    private static final int CARD_RADIUS = 20;

    public SummaryWindow(String username) {
        frame = new JFrame("Summary");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setMinimumSize(new Dimension(1000, 600));

        // Main panel with gradient
        mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gp = new GradientPaint(0, 0, BACKGROUND_START, getWidth(), getHeight(), BACKGROUND_END);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setLayout(new BorderLayout(20, 20));
        frame.add(mainPanel);

        // Header Panel
        JPanel headerPanel = createHeaderPanel(username);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Content Panel
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setOpaque(false);
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // Smart Summary Footer
        smartSummaryLabel = new JLabel("Analyzing your spending...", SwingConstants.CENTER);
        smartSummaryLabel.setFont(new Font("Segoe UI", Font.ITALIC, 16));
        smartSummaryLabel.setForeground(TEXT_COLOR);
        smartSummaryLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 20, 10));
        mainPanel.add(smartSummaryLabel, BorderLayout.SOUTH);


        // Create cards for financial information
        JPanel cardsPanel = createCardsPanel();

        // Chart container
        JPanel chartsPanel = createChartsPanel();

        // Add components to content panel
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.2;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(20, 20, 20, 10);
        contentPanel.add(cardsPanel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.8;
        gbc.insets = new Insets(20, 10, 20, 20);
        contentPanel.add(chartsPanel, gbc);

        // Fetch data
        fetchSummary();

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    private JPanel createHeaderPanel(String username) {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        JLabel titleLabel = new JLabel("Summary");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(TEXT_COLOR);

        JButton backButton = createStyledButton("← Back");
        backButton.addActionListener(e -> {
            new DashboardWindow(username);
            frame.dispose();
        });

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(backButton, BorderLayout.EAST);
        return headerPanel;
    }
    private JPanel createCardsPanel() {
        JPanel cardsPanel = new JPanel(new GridLayout(3, 1, 0, 20));
        cardsPanel.setOpaque(false);

        cardsPanel.add(createInfoCard("Total Income", "loading...", new Color(102, 255, 178, 40)));
        cardsPanel.add(createInfoCard("Total Expenses", "loading...", new Color(255, 102, 102, 40)));
        cardsPanel.add(createInfoCard("Balance", "loading...", new Color(102, 178, 255, 40)));

        return cardsPanel;
    }
    private JPanel createInfoCard(String title, String value, Color backgroundColor) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(backgroundColor);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), CARD_RADIUS, CARD_RADIUS);
            }
        };
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        card.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(TEXT_COLOR);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valueLabel.setForeground(TEXT_COLOR);
        valueLabel.setName(title.toLowerCase() + "Value");


        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }
    private JPanel createChartsPanel() {
        JPanel container = new JPanel(new GridLayout(2, 1, 30, 30)) {

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(CARD_BACKGROUND);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), CARD_RADIUS, CARD_RADIUS);
            }
        };
        container.setOpaque(false);
        container.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        pieChartPanel = new JPanel(new BorderLayout());
        pieChartPanel.setOpaque(false);

        barChartPanel = new JPanel(new BorderLayout());
        barChartPanel.setOpaque(false);

        container.add(pieChartPanel);
        container.add(barChartPanel);


        return container;
    }
    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(TEXT_COLOR);
        button.setBackground(CARD_BACKGROUND);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void fetchSummary() {
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

                        Map<String, Double> categoryTotals = new HashMap<>();

                        for (int i = 0; i < array.length(); i++) {
                            JSONObject t = array.getJSONObject(i);
                            double amount = t.getDouble("amount");
                            String type = t.getString("type");
                            String category = t.optString("category", "Other");

                            if (type.equalsIgnoreCase("INCOME")) {
                                income += amount;
                            } else {
                                expense += amount;
                                categoryTotals.put(category, categoryTotals.getOrDefault(category, 0.0) + amount);
                            }
                        }

                        double balance = income - expense;
                        double finalIncome = income;
                        double finalExpense = expense;
                        String summaryText;

                        if (income > expense) {
                            summaryText = String.format("Great! You saved %.2f SAR this period.", balance);
                        } else if (expense > income) {
                            summaryText = String.format("You overspent by %.2f SAR. Consider budgeting.", Math.abs(balance));
                        } else {
                            summaryText = "You broke even. Try to save a bit next month!";
                        }

                        SwingUtilities.invokeLater(() -> {
                            updateCardValue("Total Income", String.format("%.2f SAR", finalIncome));
                            updateCardValue("Total Expenses", String.format("%.2f SAR", finalExpense));
                            updateCardValue("Balance", String.format("%.2f SAR", balance));
                            showPieChart(finalIncome, finalExpense);
                            showCategoryBreakdownChart(categoryTotals);
                            smartSummaryLabel.setText(summaryText);

                        });


                    }
                });
    }

    private void updateCardValue(String cardTitle, String value) {
        Component[] components = mainPanel.getComponents();
        for (Component component : components) {
            if (component instanceof JPanel) {
                searchAndUpdateLabel((JPanel) component, cardTitle, value);
            }
        }
    }

    private void searchAndUpdateLabel(JPanel panel, String cardTitle, String value) {
        Component[] components = panel.getComponents();
        for (Component component : components) {
            if (component instanceof JLabel) {
                JLabel label = (JLabel) component;
                if (label.getText().equals(cardTitle)) {
                    Component[] siblings = panel.getComponents();
                    for (Component sibling : siblings) {
                        if (sibling instanceof JLabel && !sibling.equals(label)) {
                            ((JLabel) sibling).setText(value);
                            break;
                        }
                    }
                }
            } else if (component instanceof JPanel) {
                searchAndUpdateLabel((JPanel) component, cardTitle, value);
            }
        }
    }

    private void showPieChart(double income, double expense) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("Income", income);
        dataset.setValue("Expenses", expense);

        JFreeChart chart = ChartFactory.createPieChart(
                "Income vs Expenses Distribution", dataset, true, true, false
        );

        PiePlot plot = (PiePlot) chart.getPlot();
        customizePlot(plot);
        customizeChart(chart);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setOpaque(false);
        chartPanel.setBackground(new Color(0, 0, 0, 0));

        chartPanel.setPreferredSize(new Dimension(400, 300));
        pieChartPanel.removeAll();
        pieChartPanel.add(chartPanel, BorderLayout.CENTER);
        pieChartPanel.revalidate();
        pieChartPanel.repaint();



    }

    private void customizePlot(PiePlot plot) {
        plot.setBackgroundPaint(new Color(0, 0, 0, 0));
        plot.setOutlineVisible(false);
        plot.setShadowPaint(null);
        plot.setLabelBackgroundPaint(new Color(255, 255, 255, 180));
        plot.setLabelFont(new Font("Segoe UI", Font.PLAIN, 12));
        plot.setLabelPaint(Color.DARK_GRAY);
        plot.setSectionPaint("Income", new Color(97, 179, 200));
        plot.setSectionPaint("Expenses", new Color(32, 65, 74));
        plot.setLabelOutlinePaint(null);
        plot.setLabelShadowPaint(null);
    }

    private void customizeChart(JFreeChart chart) {
        chart.getTitle().setFont(new Font("Segoe UI", Font.BOLD, 20));
        chart.getTitle().setPaint(TEXT_COLOR);
        chart.setBackgroundPaint(new Color(0, 0, 0, 0));
        chart.getLegend().setBackgroundPaint(new Color(0, 0, 0, 0));
        chart.getLegend().setItemFont(new Font("Segoe UI", Font.PLAIN, 14));
        chart.getLegend().setItemPaint(TEXT_COLOR);
    }

    private void showCategoryBreakdownChart(Map<String, Double> categoryTotals) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
            dataset.addValue(entry.getValue(), "Expenses", entry.getKey());
        }

        JFreeChart barChart = ChartFactory.createBarChart(
                "Expenses by Category", "Category", "Amount (SAR)", dataset);

        CategoryPlot plot = barChart.getCategoryPlot();
        plot.setBackgroundPaint(new Color(0, 0, 0, 0));
        plot.setOutlineVisible(false);

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new Color(25, 68, 112));
        renderer.setMaximumBarWidth(0.1);
        renderer.setBarPainter(new org.jfree.chart.renderer.category.StandardBarPainter());
        renderer.setShadowVisible(false);

        barChart.setBackgroundPaint(new Color(0, 0, 0, 0));
        barChart.getTitle().setPaint(TEXT_COLOR);
        barChart.getLegend().setBackgroundPaint(new Color(0, 0, 0, 0));
        barChart.getTitle().setFont(new Font("Segoe UI", Font.BOLD, 20));
        barChart.getLegend().setItemPaint(TEXT_COLOR);

        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setTickLabelPaint(TEXT_COLOR);
        domainAxis.setLabelPaint(TEXT_COLOR);


        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setTickLabelPaint(TEXT_COLOR);
        rangeAxis.setLabelPaint(TEXT_COLOR);
        domainAxis.setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 14));
        rangeAxis.setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 14));
        barChart.getTitle().setFont(new Font("Segoe UI", Font.BOLD, 22));


        ChartPanel chartPanel = new ChartPanel(barChart);
        chartPanel.setOpaque(false);
        chartPanel.setBackground(new Color(0, 0, 0, 0));

        chartPanel.setPreferredSize(new Dimension(400, 300));
        barChartPanel.removeAll();
        barChartPanel.add(chartPanel, BorderLayout.CENTER);
        barChartPanel.revalidate();
        barChartPanel.repaint();


    }

}