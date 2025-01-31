import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;

public class LoginPage extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel statusLabel;
    private JCheckBox showPasswordCheckBox;
    private int loginAttempts = 0;

    // Mock user data (for testing)
    private Map<String, String> userDatabase;

    public LoginPage() {
        // Initialize users (Username -> Password)
        userDatabase = new HashMap<>();
        userDatabase.put("admin", "admin123");
        userDatabase.put("sales", "sales123");
        userDatabase.put("inventory", "inventory123");

        // Apply system-native UI for modern look
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 🔹 Window Settings
        setTitle("Login - Textile Factory System");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);
        setLayout(new GridBagLayout());
        
        // 🔹 Layout Configuration
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 🔹 Welcome Label
        JLabel welcomeLabel = new JLabel("Welcome to Textile Factory");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        welcomeLabel.setHorizontalAlignment(JLabel.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        add(welcomeLabel, gbc);

        // 🔹 Username Label & Field
        JLabel userLabel = new JLabel("Username:");
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        add(userLabel, gbc);

        usernameField = new JTextField();
        usernameField.setPreferredSize(new Dimension(200, 30));
        gbc.gridx = 1;
        add(usernameField, gbc);

        // Auto-focus password field when Enter is pressed in username
        usernameField.addActionListener(e -> passwordField.requestFocus());

        // 🔹 Password Label & Field
        JLabel passLabel = new JLabel("Password:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        add(passLabel, gbc);

        passwordField = new JPasswordField();
        passwordField.setPreferredSize(new Dimension(200, 30));
        gbc.gridx = 1;
        add(passwordField, gbc);

        // Auto-submit when Enter is pressed in password field
        passwordField.addActionListener(e -> authenticateUser());

        // 🔹 Show Password Checkbox
        showPasswordCheckBox = new JCheckBox("Show Password");
        showPasswordCheckBox.addActionListener(e -> 
            passwordField.setEchoChar(showPasswordCheckBox.isSelected() ? '\u0000' : '●')
        );
        gbc.gridy = 3;
        gbc.gridx = 1;
        add(showPasswordCheckBox, gbc);

        // 🔹 Login Button
        JButton loginButton = new JButton("Login");
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        add(loginButton, gbc);

        // 🔹 Status Label (for errors)
        statusLabel = new JLabel("", JLabel.CENTER);
        statusLabel.setForeground(Color.RED);
        gbc.gridy = 5;
        add(statusLabel, gbc);

        // 🔹 Login Action Listener
        loginButton.addActionListener(e -> authenticateUser());

        // 🔹 Exit on ESC Key
        getRootPane().registerKeyboardAction(e -> System.exit(0),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        setVisible(true);
    }

    private void authenticateUser() {
        if (loginAttempts >= 3) {
            statusLabel.setText("<html><font color='red'>🚫 Too many failed attempts. Try later.</font></html>");
            return;
        }

        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("<html><font color='red'>⚠ Please enter both fields!</font></html>");
            return;
        }

        if (userDatabase.containsKey(username) && userDatabase.get(username).equals(password)) {
            JOptionPane.showMessageDialog(this, "✅ Login Successful! Welcome, " + username + ".");
            this.dispose(); // Close login window
            openUserDashboard(username); // Open respective dashboard
        } else {
            loginAttempts++;
            statusLabel.setText("<html><font color='red'>⚠ Incorrect Credentials. Attempt: " 
                + loginAttempts + "/3</font></html>");
            passwordField.setText(""); 
            passwordField.requestFocus();
        }
    }

    private void openUserDashboard(String role) {
        JFrame dashboard = new JFrame(role.toUpperCase() + " Dashboard");
        dashboard.setSize(400, 200);
        dashboard.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        dashboard.setLocationRelativeTo(null);

        JLabel roleLabel = new JLabel("Welcome, " + role + "!", JLabel.CENTER);
        roleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        dashboard.add(roleLabel);

        dashboard.setVisible(true);
    }

    public static void main(String[] args) {
        new LoginPage();
    }
}
