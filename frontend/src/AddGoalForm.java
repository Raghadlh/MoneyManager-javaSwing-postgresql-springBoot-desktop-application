import javax.swing.*;
import java.awt.*;
import java.net.URI;
import java.net.http.*;
import java.time.LocalDate;
import org.json.*;
import java.awt.FlowLayout;
import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;

/**
 * A form to add and manage saving goals.
 */

public class AddGoalForm {
    private JFrame frame;
    private JTextField goalNameField, targetAmountField;
    private JPanel goalsListPanel;
    private String username;
    private DatePicker deadlinePicker;
    private ImageIcon CalendarIcon;

    // Constructor
    public AddGoalForm(String username) {
        this.username = username;
        frame = new JFrame("Add Saving Goal");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setLocationRelativeTo(null);

        JPanel gradientPanel = new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(0, 0, new Color(18, 12, 48), getWidth(), getHeight(), new Color(33, 147, 176));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        gradientPanel.setLayout(new GridLayout(1, 2, 20, 20));
        gradientPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        gradientPanel.add(createFormPanel());
        gradientPanel.add(createGoalsListPanel());

        frame.add(gradientPanel);
        frame.setVisible(true);

        fetchGoals();
    }


    /**
     * Creates the form panel to input goal details.
     */
    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 20, 10, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        goalNameField = new JTextField(20);
        targetAmountField = new JTextField(20);


        UIUtils.styleField(goalNameField);
        UIUtils.styleField(targetAmountField);


        JButton submitButton = new JButton("✔ Submit Goal");

        JButton backButton = new JButton("⬅ Back");

        UIUtils.styleButton(submitButton);
        UIUtils.styleButton(backButton);

        int row = 0;

        formPanel.add(UIUtils.createLabel("Goal Name:"), gbc);
        gbc.gridy = ++row;
        formPanel.add(goalNameField, gbc);

        gbc.gridy = ++row;
        formPanel.add(UIUtils.createLabel("Target Amount:"), gbc);
        gbc.gridy = ++row;
        formPanel.add(targetAmountField, gbc);

        gbc.gridy = ++row;
        formPanel.add(UIUtils.createLabel("Deadline:"), gbc);
        gbc.gridy = ++row;
        DatePickerSettings settings = new DatePickerSettings();
        settings.setAllowKeyboardEditing(false);

        CalendarIcon= new ImageIcon(getClass().getResource("/assets/calendar.png"));
        Image scaledImage = CalendarIcon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
        CalendarIcon = new ImageIcon(scaledImage);


        deadlinePicker = new DatePicker(settings);
        deadlinePicker.setOpaque(false);
        deadlinePicker.getComponentToggleCalendarButton().setText("");
        deadlinePicker.getComponentToggleCalendarButton().setIcon(CalendarIcon);
        deadlinePicker.getComponentDateTextField().setFont(new Font("Segoe UI", Font.PLAIN, 16));

        formPanel.add(deadlinePicker, gbc);


        gbc.gridy = ++row;
        formPanel.add(submitButton, gbc);
        gbc.gridy = ++row;
        formPanel.add(backButton, gbc);

        submitButton.addActionListener(e -> submitGoal());
        backButton.addActionListener(e -> {
            new DashboardWindow(username);
            frame.dispose();
        });

        return formPanel;
    }

    /**
     * Creates the panel listing all the saving goals.
     */
    private JScrollPane createGoalsListPanel() {
        goalsListPanel = new JPanel();
        goalsListPanel.setLayout(new WrapLayout(FlowLayout.LEFT, 20, 20));
        goalsListPanel.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(goalsListPanel);


        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        return scrollPane;
    }

    /**
     * Fetch goals from the backend server and display them.
     */
    private void fetchGoals() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/saving-goals"))
                .header("Cookie", AuthClient.sessionCookie)
                .GET()
                .build();

        HttpClient.newHttpClient().sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> SwingUtilities.invokeLater(() -> {
                    goalsListPanel.removeAll();

                    JSONArray goals = new JSONArray(response.body());
                    for (int i = 0; i < goals.length(); i++) {
                        JSONObject g = goals.getJSONObject(i);
                        goalsListPanel.add(createGoalCard(g));
                        goalsListPanel.add(Box.createVerticalStrut(10));
                    }
                    goalsListPanel.revalidate();
                    goalsListPanel.repaint();
                }));

    }


    /**
     * Creates a visual card for a goal.
     */
    private JPanel createGoalCard(JSONObject goal) {
        String name = goal.getString("name");
        double target = goal.getDouble("targetAmount");
        String deadline = goal.getString("deadline");
        long id = goal.getLong("id");

        JPanel card = new JPanel(new BorderLayout(10, 10)) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(255, 255, 255, 50));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JLabel titleLabel = new JLabel(name);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);

        JLabel targetLabel = new JLabel("🎯 Target: " + target + " SAR");
        JLabel deadlineLabel = new JLabel("⏰ Deadline: " + deadline);
        for (JLabel label : new JLabel[]{targetLabel, deadlineLabel}) {
            label.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            label.setForeground(Color.WHITE);
        }

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        textPanel.add(titleLabel);
        textPanel.add(targetLabel);
        textPanel.add(deadlineLabel);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setOpaque(false);
        JButton edit = new JButton("✎ Edit");
        JButton delete = new JButton("✖ Delete");
        UIUtils.styleButton(edit);
        UIUtils.styleButton(delete);
        actions.add(edit);
        actions.add(delete);

        card.add(textPanel, BorderLayout.CENTER);
        card.add(actions, BorderLayout.SOUTH);

        edit.addActionListener(e -> {
            goalNameField.setText(name);
            targetAmountField.setText(String.valueOf(target));
            deadlinePicker.setText(deadline);
            goalNameField.putClientProperty("editId", id);
        });

        delete.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(frame, "Are you sure?", "Delete Goal", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                HttpRequest deleteRequest = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/saving-goals/" + id))
                        .header("Cookie", AuthClient.sessionCookie)
                        .DELETE()
                        .build();
                HttpClient.newHttpClient().sendAsync(deleteRequest, HttpResponse.BodyHandlers.ofString())
                        .thenAccept(resp -> SwingUtilities.invokeLater(this::fetchGoals));
            }
        });

        return card;
    }


    /**
     * Submits a new or updated saving goal to the server.
     */
    private void submitGoal() {
        try {
            String name = goalNameField.getText().trim();
            double amount = Double.parseDouble(targetAmountField.getText().trim());
            LocalDate deadline = deadlinePicker.getDate();
            if (deadline == null) {
                JOptionPane.showMessageDialog(frame, "Please select a deadline date.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (amount <= 0) {
                JOptionPane.showMessageDialog(frame, "Amount must be greater than 0.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String json = String.format("{\"name\":\"%s\", \"targetAmount\":%.2f, \"deadline\":\"%s\"}", name, amount, deadline);

            Object editId = goalNameField.getClientProperty("editId");
            HttpRequest request;

            if (editId != null) {
                // Update existing goal
                long id = (long) editId;
                request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/saving-goals/" + id))
                        .header("Content-Type", "application/json")
                        .header("Cookie", AuthClient.sessionCookie)
                        .PUT(HttpRequest.BodyPublishers.ofString(json))
                        .build();
            } else {
                // Add new goal
                request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/saving-goals"))
                        .header("Content-Type", "application/json")
                        .header("Cookie", AuthClient.sessionCookie)
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build();
            }

            HttpClient.newHttpClient().sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        if (response.statusCode() == 200 || response.statusCode() == 201 || response.statusCode() == 204) {
                            SwingUtilities.invokeLater(() -> {
                                ToastMessage toast = new ToastMessage("Goal Added!", 2000);
                                int x = frame.getX() + (frame.getWidth() - toast.getWidth()) / 2;
                                int y = frame.getY() + frame.getHeight() - toast.getHeight() - 50;
                                toast.showToast(x, y);

                                goalNameField.setText("");
                                targetAmountField.setText("");
                               deadlinePicker.setText("");
                                goalNameField.putClientProperty("editId", null);
                                fetchGoals();
                            });
                        } else {
                            SwingUtilities.invokeLater(() ->
                                    JOptionPane.showMessageDialog(frame, "Error: " + response.body()));
                        }
                    });

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "Invalid input. Please check your data.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

}
