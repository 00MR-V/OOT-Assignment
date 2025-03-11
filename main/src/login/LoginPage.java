package login;
import javax.swing.*;
import SalesPerson.SalesDashboard;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

// Adjust import if your DBConnection is in a different package
import database.DBConnection;

public class LoginPage extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel statusLabel;
    private JCheckBox showPasswordCheckBox;
    private int loginAttempts = 0;

    public LoginPage() {
        // Apply system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Set up main frame
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
        splitPane.setResizeWeight(0.5);    // Keeps proportion when resizing

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

        // Welcome Label
        JLabel welcomeLabel = new JLabel("Welcome to Textile Factory");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        welcomeLabel.setHorizontalAlignment(JLabel.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(welcomeLabel, gbc);

        // Username Label & Field
        JLabel userLabel = new JLabel("Username:");
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        panel.add(userLabel, gbc);

        usernameField = new JTextField();
        usernameField.setPreferredSize(new Dimension(200, 30));
        gbc.gridx = 1;
        panel.add(usernameField, gbc);
        usernameField.addActionListener(e -> passwordField.requestFocus());

        // Password Label & Field
        JLabel passLabel = new JLabel("Password:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(passLabel, gbc);

        passwordField = new JPasswordField();
        passwordField.setPreferredSize(new Dimension(200, 30));
        gbc.gridx = 1;
        panel.add(passwordField, gbc);
        passwordField.addActionListener(e -> authenticateUser());

        // Show Password Checkbox
        showPasswordCheckBox = new JCheckBox("Show Password");
        showPasswordCheckBox.addActionListener(e ->
            passwordField.setEchoChar(showPasswordCheckBox.isSelected() ? '\u0000' : '●')
        );
        gbc.gridy = 3;
        gbc.gridx = 1;
        panel.add(showPasswordCheckBox, gbc);

        // Login Button
        JButton loginButton = new JButton("Login");
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        panel.add(loginButton, gbc);

        // Status Label
        statusLabel = new JLabel("", JLabel.CENTER);
        statusLabel.setForeground(Color.RED);
        gbc.gridy = 5;
        panel.add(statusLabel, gbc);

        // Login Action
        loginButton.addActionListener(e -> authenticateUser());

        // Exit on ESC Key
        getRootPane().registerKeyboardAction(e -> System.exit(0),
                KeyStroke.getKeyStroke("ESCAPE"),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        return panel;
    }

    private JPanel createImagePanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Load Image (Ensure it's in the project root or adjust path)
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

    // Resize Image to Fit Panel
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

        // Query the database for the user record
        try {
            Connection conn = DBConnection.getConnection();
            // IMPORTANT: Include user_id in the SELECT statement
            String query = "SELECT u.user_id, u.username, u.password, r.role_name "
                         + "FROM Users u JOIN Roles r ON u.role_id = r.role_id "
                         + "WHERE u.username = ?";
            PreparedStatement pst = conn.prepareStatement(query);
            pst.setString(1, username);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                int dbUserId = rs.getInt("user_id");
                String dbPassword = rs.getString("password");
                String roleName = rs.getString("role_name");

                if (password.equals(dbPassword)) {
                    JOptionPane.showMessageDialog(this, "✅ Login Successful! Welcome, " + username + ".");
                    this.dispose();
                    openUserDashboard(dbUserId, username, roleName);
                } else {
                    loginAttempts++;
                    statusLabel.setText("<html><font color='red'>⚠ Incorrect Credentials. Attempt: "
                            + loginAttempts + "/3</font></html>");
                }
            } else {
                loginAttempts++;
                statusLabel.setText("<html><font color='red'>⚠ User not found. Attempt: "
                        + loginAttempts + "/3</font></html>");
            }
            rs.close();
            pst.close();
        } catch (SQLException e) {
            e.printStackTrace();
            statusLabel.setText("<html><font color='red'>⚠ Error connecting to database.</font></html>");
        }
    }

    // Overloaded method to open user dashboard with userId, username, and role
    private void openUserDashboard(int userId, String username, String roleName) {
        // Example role-based navigation:
    	if ("Sales Person".equalsIgnoreCase(roleName)) {
    	    SalesDashboard sd = new SalesDashboard(userId, username, roleName);
    	    sd.setSize(800, 600);
    	    sd.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	    sd.setLocationRelativeTo(null);
    	    sd.setVisible(true);
    	}
 
        else if ("Inventory Officer".equalsIgnoreCase(roleName)) {
            // Navigate to Inventory Officer Dashboard (placeholder)
            JFrame inventoryFrame = new JFrame("Inventory Officer Dashboard");
            inventoryFrame.setSize(400, 200);
            inventoryFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            inventoryFrame.setLocationRelativeTo(null);

            JLabel roleLabel = new JLabel("Welcome, " + username + " (Inventory Officer)!", JLabel.CENTER);
            roleLabel.setFont(new Font("Arial", Font.BOLD, 16));
            inventoryFrame.add(roleLabel);

            inventoryFrame.setVisible(true);
        } 
        else if ("IS Manager".equalsIgnoreCase(roleName)) {
            // Navigate to IS Manager Dashboard (placeholder)
            JFrame managerFrame = new JFrame("IS Manager Dashboard");
            managerFrame.setSize(400, 200);
            managerFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            managerFrame.setLocationRelativeTo(null);

            JLabel roleLabel = new JLabel("Welcome, " + username + " (IS Manager)!", JLabel.CENTER);
            roleLabel.setFont(new Font("Arial", Font.BOLD, 16));
            managerFrame.add(roleLabel);

            managerFrame.setVisible(true);
        } 
        else {
            // Fallback for unknown role
            JFrame defaultFrame = new JFrame("Unknown Role");
            defaultFrame.setSize(400, 200);
            defaultFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            defaultFrame.setLocationRelativeTo(null);

            JLabel roleLabel = new JLabel("Welcome, " + username + " (Unknown Role)!", JLabel.CENTER);
            roleLabel.setFont(new Font("Arial", Font.BOLD, 16));
            defaultFrame.add(roleLabel);

            defaultFrame.setVisible(true);
        }
    }

    public static void main(String[] args) {
        new LoginPage();
    }
}
