import javax.swing.*;
import java.awt.*;

public class LoginForm {
    private JTextField username;
    private JPasswordField password;
    private JButton loginButton;
    private JButton signupButton;
    private JFrame frame;

    public LoginForm() {
        frame = new JFrame("Login");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setUndecorated(false);
        frame.setLocationRelativeTo(null);



        // Gradient background panel
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

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridBagLayout());
        formPanel.setOpaque(false);

        username = new JTextField(20);
        password = new JPasswordField(20);
        loginButton = new JButton("Login");
        signupButton = new JButton("Sign Up");

        UIUtils.styleField(username);
        UIUtils.styleField(password);
        UIUtils.styleButton(loginButton);
        UIUtils.styleButton(signupButton);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets  = new Insets(20, 15, 20, 15);;
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
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        JLabel title = new JLabel("Welcome Back!");
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(Color.WHITE);
        formPanel.add(title, gbc);


        gbc.gridy++;
        gbc.gridwidth = 1;
        formPanel.add(UIUtils.createLabel("Username "), gbc);
        gbc.gridx = 1;
        formPanel.add(username, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        formPanel.add(UIUtils.createLabel("Password "), gbc);
        gbc.gridx = 1;
        formPanel.add(password, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        formPanel.add(loginButton, gbc);

        gbc.gridy++;
        formPanel.add(signupButton, gbc);

        gradientPanel.add(formPanel);
        frame.add(gradientPanel);
        frame.setVisible(true);

        loginButton.addActionListener(e -> {
            String user = username.getText();
            String pass = new String(password.getPassword());
            AuthClient.login(user, pass, this::onLoginSuccess, this::onLoginFailure);
        });

        signupButton.addActionListener(e -> {
            new SignUpForm();
            frame.dispose();
        });
    }

    private void onLoginSuccess() {
        new  DashboardWindow(username.getText());
        frame.dispose();
    }

    private void onLoginFailure() {
        JOptionPane.showMessageDialog(frame, "Login failed. Please try again.", "Login Error", JOptionPane.ERROR_MESSAGE);
    }


}
