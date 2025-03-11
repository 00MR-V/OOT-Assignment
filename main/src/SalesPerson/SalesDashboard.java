package SalesPerson;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;

import database.DBConnection; // Adjust package if needed

public class SalesDashboard extends JFrame {

    private int userId;
    private String username;
    private String roleName;
    private JTabbedPane tabbedPane;
    private DefaultListModel<String> orderListModel; // For dynamic orders

    public SalesDashboard(int userId, String username, String roleName) {
        super("Sales Dashboard - " + username);
        this.userId = userId;
        this.username = username;
        this.roleName = roleName;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Setup Menu Bar
        JMenuBar menuBar = new JMenuBar();
        JMenu menuFile = new JMenu("File");
        JMenuItem miLogout = new JMenuItem("Logout");
        JMenuItem miExit = new JMenuItem("Exit");
        menuFile.add(miLogout);
        menuFile.add(miExit);
        menuBar.add(menuFile);
        JMenu menuHelp = new JMenu("Help");
        JMenuItem miAbout = new JMenuItem("About");
        menuHelp.add(miAbout);
        menuBar.add(menuHelp);
        setJMenuBar(menuBar);

        // Menu Actions
        miLogout.addActionListener(e -> {
            new login.LoginPage().setVisible(true);
            dispose();
        });
        miExit.addActionListener(e -> System.exit(0));
        miAbout.addActionListener(e -> 
            JOptionPane.showMessageDialog(this, "Textile Factory Sales Dashboard v1.0\nDeveloped at University of Mauritius Under Guidance of Dr Sonah", 
                                          "About", JOptionPane.INFORMATION_MESSAGE)
        );

        // Header Panel
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
     
        add(headerPanel, BorderLayout.NORTH);

        // Tabbed Pane with multiple functions
        tabbedPane = new JTabbedPane();

        // Dashboard Overview Tab
        JPanel overviewTab = new JPanel(new BorderLayout());
        overviewTab.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel overviewLabel = new JLabel("Sales Overview", JLabel.CENTER);
        overviewLabel.setFont(new Font("Arial", Font.ITALIC, 20));
        overviewTab.add(overviewLabel, BorderLayout.CENTER);
        tabbedPane.addTab("Dashboard", overviewTab);

        // Create Quotation Tab (rich design)
        CreateQuotationPanel createQuotationPanel = new CreateQuotationPanel(userId);
        tabbedPane.addTab("Create Quotation", createQuotationPanel);

        // Orders Tab (empty by default; orders loaded dynamically)
     // Replace the orders tab with quotation tab:
        QuotationPanel quotationPanel = new QuotationPanel(userId);
        tabbedPane.addTab("Quotations", quotationPanel);

        add(tabbedPane, BorderLayout.CENTER);

        // Footer Panel with Refresh Button
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnRefresh = new JButton("Refresh");
        footerPanel.add(btnRefresh);
        add(footerPanel, BorderLayout.SOUTH);

        btnRefresh.addActionListener(e -> loadOrdersFromDB());
    }

    // This method should query the database and populate orderListModel accordingly.
    private void loadOrdersFromDB() {
        // Example: Clear the model and load new orders from the DB.
        orderListModel.clear();
        // For demonstration, no orders are loaded. Replace with your DB query.
        // Example:
        // Connection conn = DBConnection.getConnection();
        // String query = "SELECT order_id, status FROM Orders WHERE ...";
        // Execute query, iterate through ResultSet, then:
        // orderListModel.addElement("Order #" + orderId + " - " + status);
        JOptionPane.showMessageDialog(this, "Orders refreshed from database.", "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        // For testing purpose with dummy data; remove before final deployment.
        SalesDashboard sd = new SalesDashboard(1, "sahil", "Sales Person");
        sd.setVisible(true);
    }
}
