package SalesPerson;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import database.DBConnection;

public class SalesDashboard extends JFrame {

    private int userId;
    private String username;
    private String roleName;
    private JTabbedPane tabbedPane;
    private QuotationPanel quotationPanel; // Quotations tab panel
    private JPanel dashboardPanel;         // Dashboard Overview panel

    // Instance labels for dashboard metrics
    private JLabel lblTotal;
    private JLabel lblPending;
    private JLabel lblApproved;
    private JLabel lblCancelled;
    private JLabel lblReopened;

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
            JOptionPane.showMessageDialog(this,
                "Textile Factory Sales Dashboard v1.0\nDeveloped at University of Mauritius Under Guidance of Dr Sonah",
                "About", JOptionPane.INFORMATION_MESSAGE)
        );

        // Header Panel
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel lblHeader = new JLabel("Welcome, " + username + " (" + roleName + ")");
        // Remove custom font size; use defaults
        headerPanel.add(lblHeader);
        add(headerPanel, BorderLayout.NORTH);

        // Create Tabbed Pane
        tabbedPane = new JTabbedPane();

        // Dashboard Overview Tab
        dashboardPanel = createDashboardPanel();
        tabbedPane.addTab("Dashboard", dashboardPanel);

        // Create Quotation Tab
        CreateQuotationPanel createQuotationPanel = new CreateQuotationPanel(userId);
        tabbedPane.addTab("Create Quotation", createQuotationPanel);

        // Quotations Tab (for listing and editing quotations)
        quotationPanel = new QuotationPanel(userId);
        tabbedPane.addTab("Quotations", quotationPanel);

        add(tabbedPane, BorderLayout.CENTER);

        // Footer Panel with Refresh Button
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnRefresh = new JButton("Refresh");
        footerPanel.add(btnRefresh);
        add(footerPanel, BorderLayout.SOUTH);

        // Auto-refresh logic for tabs
        tabbedPane.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                Component selected = tabbedPane.getSelectedComponent();
                if (selected == dashboardPanel) {
                    refreshDashboardMetrics();
                } else if (selected instanceof QuotationPanel) {
                    ((QuotationPanel) selected).refreshQuotations();
                }
            }
        });

        // Refresh button only refreshes the Quotations tab
        btnRefresh.addActionListener(e -> quotationPanel.refreshQuotations());
    }

    /**
     * Creates the Dashboard panel using a BoxLayout with simple rows for each metric.
     */
    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Dashboard Metrics"));

        // Content panel (vertical box layout)
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Instantiate metric labels
        lblTotal = new JLabel("0");
        lblPending = new JLabel("0");
        lblApproved = new JLabel("0");
        lblCancelled = new JLabel("0");
        lblReopened = new JLabel("0");

        // Add rows for each metric
        contentPanel.add(createMetricRow("Total Quotations:", lblTotal));
        contentPanel.add(createMetricRow("Pending Quotations:", lblPending));
        contentPanel.add(createMetricRow("Approved Quotations:", lblApproved));
        contentPanel.add(createMetricRow("Cancelled Quotations:", lblCancelled));
        contentPanel.add(createMetricRow("Reopened Quotations:", lblReopened));

        panel.add(contentPanel, BorderLayout.CENTER);

        // Load initial metrics
        refreshDashboardMetrics();
        return panel;
    }

    /**
     * Creates a single row containing a label and its value label, aligned neatly.
     */
    private JPanel createMetricRow(String labelText, JLabel valueLabel) {
        JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JLabel lbl = new JLabel(labelText);
        rowPanel.add(lbl);
        rowPanel.add(valueLabel);
        return rowPanel;
    }

    /**
     * Queries the DB and updates the metric labels in the dashboard panel.
     */
    private void refreshDashboardMetrics() {
        try {
            Connection conn = DBConnection.getConnection();

            // Total Quotations
            String totalQuery = "SELECT COUNT(*) AS total FROM quotations WHERE sales_person_id = ?";
            PreparedStatement pstTotal = conn.prepareStatement(totalQuery);
            pstTotal.setInt(1, userId);
            ResultSet rsTotal = pstTotal.executeQuery();
            int totalQuotations = 0;
            if (rsTotal.next()) {
                totalQuotations = rsTotal.getInt("total");
            }
            rsTotal.close();
            pstTotal.close();

            // Quotations by status
            String statusQuery = "SELECT status, COUNT(*) AS count FROM quotations WHERE sales_person_id = ? GROUP BY status";
            PreparedStatement pstStatus = conn.prepareStatement(statusQuery);
            pstStatus.setInt(1, userId);
            ResultSet rsStatus = pstStatus.executeQuery();
            java.util.Map<String, Integer> statusCounts = new java.util.HashMap<>();
            while (rsStatus.next()) {
                String status = rsStatus.getString("status");
                int count = rsStatus.getInt("count");
                statusCounts.put(status, count);
            }
            rsStatus.close();
            pstStatus.close();

            // Update labels
            lblTotal.setText(String.valueOf(totalQuotations));
            lblPending.setText(String.valueOf(statusCounts.getOrDefault("Pending", 0)));
            lblApproved.setText(String.valueOf(statusCounts.getOrDefault("Approved", 0)));
            lblCancelled.setText(String.valueOf(statusCounts.getOrDefault("Cancelled", 0)));
            lblReopened.setText(String.valueOf(statusCounts.getOrDefault("Reopened", 0)));

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error loading dashboard metrics: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SalesDashboard sd = new SalesDashboard(1, "sahil", "Sales Person");
        sd.setVisible(true);
    }
}
