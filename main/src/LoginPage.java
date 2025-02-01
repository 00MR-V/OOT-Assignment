import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

public class LoginPage extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel statusLabel;
    private JCheckBox showPasswordCheckBox;
    private int loginAttempts = 0;

    // Mock user data
    private Map<String, String> userDatabase;

    public LoginPage() {
        // Initialize users
        userDatabase = new HashMap<>();
        userDatabase.put("admin", "admin123");
        userDatabase.put("sales", "sales123");
        userDatabase.put("inventory", "inventory123");

        // Apply system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 🔹 Set up main frame
        setTitle("Login - Textile Factory System");
        setSize(800, 400); // Set initial size
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);

        // Create two panels (Left: Login, Right: Image)
        JPanel loginPanel = createLoginPanel();
        JPanel imagePanel = createImagePanel();

        // Split Pane (Left: Login Form | Right: Image)
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, loginPanel, imagePanel);
        splitPane.setDividerLocation(400); // Initial split position (Half-Half)
        splitPane.setResizeWeight(0.5); // Keeps proportion when resizing

        add(splitPane);
        setVisible(true);
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBackground(Color.WHITE);

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
        panel.add(welcomeLabel, gbc);

        // 🔹 Username Label & Field
        JLabel userLabel = new JLabel("Username:");
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        panel.add(userLabel, gbc);

        usernameField = new JTextField();
        usernameField.setPreferredSize(new Dimension(200, 30));
        gbc.gridx = 1;
        panel.add(usernameField, gbc);
        usernameField.addActionListener(e -> passwordField.requestFocus());

        // 🔹 Password Label & Field
        JLabel passLabel = new JLabel("Password:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(passLabel, gbc);

        passwordField = new JPasswordField();
        passwordField.setPreferredSize(new Dimension(200, 30));
        gbc.gridx = 1;
        panel.add(passwordField, gbc);
        passwordField.addActionListener(e -> authenticateUser());

        // 🔹 Show Password Checkbox
        showPasswordCheckBox = new JCheckBox("Show Password");
        showPasswordCheckBox.addActionListener(e -> 
            passwordField.setEchoChar(showPasswordCheckBox.isSelected() ? '\u0000' : '●')
        );
        gbc.gridy = 3;
        gbc.gridx = 1;
        panel.add(showPasswordCheckBox, gbc);

        // 🔹 Login Button
        JButton loginButton = new JButton("Login");
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        panel.add(loginButton, gbc);

        // 🔹 Status Label
        statusLabel = new JLabel("", JLabel.CENTER);
        statusLabel.setForeground(Color.RED);
        gbc.gridy = 5;
        panel.add(statusLabel, gbc);

        // 🔹 Login Action
        loginButton.addActionListener(e -> authenticateUser());

        // 🔹 Exit on ESC Key
        getRootPane().registerKeyboardAction(e -> System.exit(0),
                KeyStroke.getKeyStroke("ESCAPE"),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        return panel;
    }

    private JPanel createImagePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // 🔹 Load Image (Ensure it's in the project root)
        ImageIcon icon = new ImageIcon("textile_logo.jpg"); 
        JLabel imageLabel = new JLabel(icon);
        imageLabel.setHorizontalAlignment(JLabel.CENTER);

        // Resize the image dynamically
        panel.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                ImageIcon scaledIcon = scaleImage(icon, panel.getWidth(), panel.getHeight());
                imageLabel.setIcon(scaledIcon);
            }
        });

        panel.add(imageLabel, BorderLayout.CENTER);
        return panel;
    }

    // 🔹 Resize Image to Fit Panel
    private ImageIcon scaleImage(ImageIcon icon, int width, int height) {
        Image originalImage = icon.getImage();
        Image resizedImage = originalImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(resizedImage);
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
            this.dispose();
            openUserDashboard(username);
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
