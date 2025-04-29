import javax.swing.*;
import java.awt.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;

public class EditTransactionForm {
    private JFrame frame;
    private ImageIcon CalendarIcon;

    public EditTransactionForm(String username, Long transactionId, String type, String category, String amountStr, String dateStr, String note, boolean isSaving)
    {
        frame = new JFrame("Edit Transaction");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setLocationRelativeTo(null);

        JPanel gradientPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(0, 0, new Color(18, 12, 48), getWidth(), getHeight(), new Color(33, 147, 176));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        gradientPanel.setLayout(new GridBagLayout());

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 20, 10, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JComboBox<String> typeBox = new JComboBox<>(new String[]{"INCOME", "EXPENSE"});
        JTextField categoryField = new JTextField(category,20);
        JTextField amountField = new JTextField(amountStr,20);


        DatePickerSettings settings = new DatePickerSettings();
        settings.setAllowKeyboardEditing(false);
        CalendarIcon= new ImageIcon(getClass().getResource("/assets/calendar.png"));
        Image scaledImage = CalendarIcon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
        CalendarIcon = new ImageIcon(scaledImage);

        DatePicker datePicker = new DatePicker(settings);
        datePicker.setOpaque(false);
        datePicker.setDate(LocalDate.parse(dateStr));
        datePicker.getComponentToggleCalendarButton().setText("");
        datePicker.getComponentToggleCalendarButton().setIcon(CalendarIcon);
        datePicker.getComponentDateTextField().setFont(new Font("Segoe UI", Font.PLAIN, 16));

        JTextField noteField = new JTextField(note ,20);

        JCheckBox isSavingCheck = new JCheckBox("Mark this as Saving");
        isSavingCheck.setSelected(isSaving);
        isSavingCheck.setForeground(Color.WHITE);



        typeBox.setSelectedItem(type);

        JButton updateButton = new JButton("✔ Update");
        JButton backButton = new JButton("⬅ Cancel");

        UIUtils.styleField(categoryField);
        UIUtils.styleField(amountField);
        UIUtils.styleField(noteField);
        UIUtils.styleButton(updateButton);
        UIUtils.styleButton(backButton);

        int row = 0;
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
        formPanel.add(updateButton, gbc);
        gbc.gridy = ++row;
        formPanel.add(backButton, gbc);


        gradientPanel.add(formPanel);
        frame.add(gradientPanel);
        frame.setVisible(true);

        updateButton.addActionListener(e -> {
            try {
                double amount = Double.parseDouble(amountField.getText());
                LocalDate date = datePicker.getDate();


                String json = String.format(
                        "{\"type\":\"%s\", \"category\":\"%s\", \"amount\":%.2f, \"date\":\"%s\", \"note\":\"%s\", \"saving\":%b}",
                        typeBox.getSelectedItem(), categoryField.getText().trim(), amount, date, noteField.getText().trim(), isSavingCheck.isSelected()
                );


                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/transactions/" + transactionId))
                        .header("Content-Type", "application/json")
                        .header("Cookie", AuthClient.sessionCookie)
                        .PUT(HttpRequest.BodyPublishers.ofString(json))
                        .build();

                client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                        .thenAccept(response -> {
                            if (response.statusCode() == 200 || response.statusCode() == 204) {
                                SwingUtilities.invokeLater(() -> {
                                    JOptionPane.showMessageDialog(frame, "Transaction updated!");
                                    new ViewTransactionsWindow(username);
                                    frame.dispose();
                                });
                            } else {
                                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(frame, "Failed to update: " + response.body()));
                            }
                        });

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Invalid input. Please check your data.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        backButton.addActionListener(e -> {
            new ViewTransactionsWindow(username);
            frame.dispose();
        });
    }
}
