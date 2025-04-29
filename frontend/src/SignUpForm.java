import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.net.URI;
import java.net.http.*;

public class SignUpForm {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField emailField;
    private JButton signupButton;
    private JButton backToLoginButton;
    private JFrame frame;

    public SignUpForm() {
        frame = new JFrame("Sign Up");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setUndecorated(false);
        frame.setLocationRelativeTo(null);

        // Gradient background
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

        // Transparent form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);

        usernameField = new JTextField(20);
        passwordField = new JPasswordField(20);
        emailField = new JTextField(20);
        signupButton = new JButton("Sign Up");
        backToLoginButton = new JButton("Back to Login");

        UIUtils.styleField(usernameField);
        UIUtils.styleField(passwordField);
        UIUtils.styleField(emailField);
        UIUtils.styleButton(signupButton);
        UIUtils.styleButton(backToLoginButton);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 15, 20, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;

        // Load the logo
        ImageIcon logoIcon = new ImageIcon(getClass().getResource("/assets/logo.png"));

        Image scaledImage = logoIcon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
        logoIcon = new ImageIcon(scaledImage);

        JLabel logoLabel = new JLabel(logoIcon);
        gbc.insets = new Insets(0, 0, 20, 0);
        formPanel.add(logoLabel, gbc);

        gbc.gridy++;

        JLabel title = new JLabel("Create a New Account");
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(Color.WHITE);
        formPanel.add(title, gbc);

        // Username
        gbc.gridy++;
        gbc.gridwidth = 1;
        formPanel.add(UIUtils.createLabel("Username "), gbc);
        gbc.gridx = 1;
        formPanel.add(usernameField, gbc);

        // Password
        gbc.gridx = 0;
        gbc.gridy++;
        formPanel.add(UIUtils.createLabel("Password "), gbc);
        gbc.gridx = 1;
        formPanel.add(passwordField, gbc);

        // Email
        gbc.gridx = 0;
        gbc.gridy++;
        formPanel.add(UIUtils.createLabel("Email "), gbc);
        gbc.gridx = 1;
        formPanel.add(emailField, gbc);

        // Sign up button
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        formPanel.add(signupButton, gbc);

        // Back to log in button
        gbc.gridy++;
        formPanel.add(backToLoginButton, gbc);

        gradientPanel.add(formPanel);
        frame.add(gradientPanel);
        frame.setVisible(true);

        // Actions
        signupButton.addActionListener(this::handleSignup);
        backToLoginButton.addActionListener(e -> {
            new LoginForm();
            frame.dispose();
        });
    }
    private void handleSignup(ActionEvent e) {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        String email = emailField.getText().trim();

        if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "All fields are required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            JOptionPane.showMessageDialog(frame, "Please enter a valid email address.", "Invalid Email", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Prepare JSON payload
        String json = String.format("{\"username\":\"%s\", \"password\":\"%s\", \"email\":\"%s\"}",
                username, password, email);

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/req/signup"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        if (response.statusCode() == 200 || response.statusCode() == 201) {
                            SwingUtilities.invokeLater(() -> {
                                JOptionPane.showMessageDialog(frame, "Signup successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                                new LoginForm();
                                frame.dispose();
                            });
                        } else if (response.statusCode() == 409) {
                            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(frame, "Username already exists. Try a different one.", "Signup Error", JOptionPane.WARNING_MESSAGE));
                        } else {
                            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(frame, "Signup failed: " + response.body(), "Error", JOptionPane.ERROR_MESSAGE));
                        }
                    })
                    .exceptionally(ex -> {
                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(frame, "Signup error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE));
                        return null;
                    });
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "Unexpected error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

}
