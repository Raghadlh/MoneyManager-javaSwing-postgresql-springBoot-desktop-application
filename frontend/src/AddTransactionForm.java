import javax.swing.*;
import java.awt.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;


/**
 * Form window for adding a new transaction.
 */
public class AddTransactionForm {
    private JFrame frame;
    private DatePicker datePicker;
    private ImageIcon CalendarIcon;


    public AddTransactionForm(String username) {
        frame = new JFrame("Add Transaction");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setLocationRelativeTo(null);

        JPanel gradientPanel = getJPanel();

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 20, 10, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Form fields
        JComboBox<String> typeBox = new JComboBox<>(new String[]{"INCOME", "EXPENSE"});
        JTextField categoryField = new JTextField(20);
        JTextField amountField = new JTextField(20);

        DatePickerSettings settings = new DatePickerSettings();
        settings.setAllowKeyboardEditing(false);
        CalendarIcon= new ImageIcon(getClass().getResource("/assets/calendar.png"));
        Image scaledImage = CalendarIcon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
        CalendarIcon = new ImageIcon(scaledImage);

        datePicker = new DatePicker(settings);
        datePicker.setOpaque(false);

        datePicker.getComponentToggleCalendarButton().setText("");
        datePicker.getComponentToggleCalendarButton().setIcon(CalendarIcon);
        datePicker.getComponentDateTextField().setFont(new Font("Segoe UI", Font.PLAIN, 16));

        JTextField noteField = new JTextField(20);



        JCheckBox isSavingCheck = new JCheckBox("Saving");
        isSavingCheck.setForeground(Color.WHITE);


        JButton submitButton = new JButton("✔ Submit");
        JButton backButton = new JButton("⬅ Back to Dashboard");

        UIUtils.styleField(categoryField);
        UIUtils.styleField(amountField);
        UIUtils.styleField(noteField);
        UIUtils.styleButton(submitButton);
        UIUtils.styleButton(backButton);

        // Add to panel
        int row = 0;

        JLabel title = new JLabel("Add New Transaction");
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(Color.WHITE);
        title.setHorizontalAlignment(SwingConstants.CENTER);

        formPanel.add(UIUtils.createLabel("Transaction Type:"), gbc);
        gbc.gridy = ++row;
        formPanel.add(typeBox, gbc);

        gbc.gridy = ++row;
        formPanel.add(UIUtils.createLabel("Category:"), gbc);
        gbc.gridy = ++row;
        formPanel.add(categoryField, gbc);

        gbc.gridy = ++row;
        formPanel.add(UIUtils.createLabel("Amount:"), gbc);
        gbc.gridy = ++row;
        formPanel.add(amountField, gbc);

        gbc.gridy = ++row;
        formPanel.add(UIUtils.createLabel("Date (YYYY-MM-DD):"), gbc);
        gbc.gridy = ++row;
        formPanel.add(datePicker, gbc);

        gbc.gridy = ++row;
        formPanel.add(UIUtils.createLabel("Note:"), gbc);
        gbc.gridy = ++row;
        formPanel.add(noteField, gbc);

        gbc.gridy = ++row;
        formPanel.add(isSavingCheck, gbc);

        gbc.gridy = ++row;
        formPanel.add(submitButton, gbc);
        gbc.gridy = ++row;
        formPanel.add(backButton, gbc);

        gradientPanel.add(formPanel);
        frame.add(gradientPanel);
        frame.setVisible(true);

        // Submit action
        submitButton.addActionListener(e -> handleSubmit(
                typeBox.getSelectedItem().toString(),
                categoryField.getText().trim(),
                amountField.getText().trim(),
                datePicker.getDate(),
                noteField.getText().trim(),
                isSavingCheck.isSelected(),
                frame
        ));

        // Back
        backButton.addActionListener(e -> {
            new ViewTransactionsWindow(username);
            frame.dispose();
        });
    }


    /**
     * Creates a JPanel with a gradient background.
     */
    private static JPanel getJPanel() {
        JPanel gradientPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                Color color1 = new Color(18, 12, 48);
                Color color2 = new Color(33, 147, 176);
                GradientPaint gp = new GradientPaint(0, 0, color1, getWidth(), getHeight(), color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        gradientPanel.setLayout(new GridBagLayout());
        return gradientPanel;
    }


    /**
     * Handles the submission of a new transaction.
     */
    private void handleSubmit(String type, String category, String amountStr, LocalDate date, String note, boolean isSaving, JFrame frame)
    {
        try {
            if (type.isEmpty() || category.isEmpty() || amountStr.isEmpty() || date == null)
            {
                JOptionPane.showMessageDialog(frame, "All fields are required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            double amount = Double.parseDouble(amountStr);

            String noteValue = note.trim();

            String json = String.format(
                    "{\"type\":\"%s\", \"category\":\"%s\", \"amount\":%.2f, \"date\":\"%s\", \"note\":\"%s\", \"saving\":%b}",
                    type, category, amount, date, noteValue, isSaving
            );



            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/transactions"))
                    .header("Content-Type", "application/json")
                    .header("Cookie", AuthClient.sessionCookie)
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        if (response.statusCode() == 200 || response.statusCode() == 201) {
                            SwingUtilities.invokeLater(() -> {
                                ToastMessage toast = new ToastMessage("Transaction Added!", 2000);
                                int x = frame.getX() + (frame.getWidth() - toast.getWidth()) / 2;
                                int y = frame.getY() + frame.getHeight() - toast.getHeight() - 50;
                                toast.showToast(x, y);

                            });

                        } else {
                            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(frame, "Failed: " + response.body(), "Error", JOptionPane.ERROR_MESSAGE));
                        }
                    });

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "Invalid input or date format. Use YYYY-MM-DD.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
