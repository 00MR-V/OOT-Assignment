package SalesPerson;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import database.DBConnection;

public class QuotationPanel extends JPanel {
    private int salesPersonId;
    private JPanel quotationsContainer;
    private JScrollPane scrollPane;
    
    public QuotationPanel(int salesPersonId) {
        this.salesPersonId = salesPersonId;
        setLayout(new BorderLayout(10,10));
        setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        
        // Header label
        JLabel headerLabel = new JLabel("Your Quotations", JLabel.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 20));
        add(headerLabel, BorderLayout.NORTH);
        
        // Container for dynamic list of quotations
        quotationsContainer = new JPanel();
        quotationsContainer.setLayout(new BoxLayout(quotationsContainer, BoxLayout.Y_AXIS));
        
        scrollPane = new JScrollPane(quotationsContainer);
        add(scrollPane, BorderLayout.CENTER);
        
        // Load quotations from the database
        loadQuotationsFromDB();
    }
    
    // Public method to refresh quotations (for the refresh button)
    public void refreshQuotations() {
        loadQuotationsFromDB();
    }
    
    private void loadQuotationsFromDB() {
        // Clear existing content
        quotationsContainer.removeAll();
        
        try {
            Connection conn = DBConnection.getConnection();
            // Retrieve quotations in descending order (latest first)
            String query = "SELECT q.quotation_id, q.date_created, q.status, c.name AS customer_name " +
                           "FROM quotations q JOIN customers c ON q.customer_id = c.customer_id " +
                           "WHERE q.sales_person_id = ? ORDER BY q.date_created DESC, q.quotation_id DESC";
            PreparedStatement pst = conn.prepareStatement(query);
            pst.setInt(1, salesPersonId);
            ResultSet rs = pst.executeQuery();
            
            while (rs.next()) {
                int quotationId = rs.getInt("quotation_id");
                Date dateCreated = rs.getDate("date_created");
                String status = rs.getString("status");
                String customerName = rs.getString("customer_name");
                
                // Create a sub-panel for each quotation
                JPanel qp = new JPanel(new BorderLayout(10,10));
                qp.setBorder(BorderFactory.createLineBorder(Color.GRAY));
                qp.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
                
                // Left side: Display quotation details
                JPanel detailsPanel = new JPanel();
                detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
                detailsPanel.add(new JLabel("Quotation ID: " + quotationId));
                detailsPanel.add(new JLabel("Customer: " + customerName));
                detailsPanel.add(new JLabel("Date: " + dateCreated));
                detailsPanel.add(new JLabel("Status: " + status));
                qp.add(detailsPanel, BorderLayout.CENTER);
                
                // Right side: Action components (Edit button and status dropdown)
                JPanel actionPanel = new JPanel(new GridLayout(2, 1, 5, 5));
                JButton btnEdit = new JButton("Edit");
                String[] statusOptions = {"Pending", "Approved", "Cancelled", "Reopened"};
                JComboBox<String> cbStatus = new JComboBox<>(statusOptions);
                cbStatus.setSelectedItem(status);
                actionPanel.add(btnEdit);
                actionPanel.add(cbStatus);
                qp.add(actionPanel, BorderLayout.EAST);
                
                // Edit button: Open CreateQuotationPanel in edit mode
                btnEdit.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        JFrame editFrame = new JFrame("Edit Quotation - #" + quotationId);
                        editFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                        CreateQuotationPanel editPanel = new CreateQuotationPanel(salesPersonId, quotationId);
                        editFrame.add(editPanel);
                        editFrame.setSize(600,600);
                        editFrame.setLocationRelativeTo(null);
                        editFrame.setVisible(true);
                    }
                });
                
                // Status dropdown: When selection changes, update status in DB and refresh list
                cbStatus.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        String newStatus = (String) cbStatus.getSelectedItem();
                        updateQuotationStatus(quotationId, newStatus);
                        loadQuotationsFromDB();
                    }
                });
                
                quotationsContainer.add(qp);
                quotationsContainer.add(Box.createVerticalStrut(10));
            }
            
            rs.close();
            pst.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error loading quotations: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
        
        revalidate();
        repaint();
    }
    
    private void updateQuotationStatus(int quotationId, String newStatus) {
        try {
            Connection conn = DBConnection.getConnection();
            String query = "UPDATE quotations SET status = ? WHERE quotation_id = ?";
            PreparedStatement pst = conn.prepareStatement(query);
            pst.setString(1, newStatus);
            pst.setInt(2, quotationId);
            int rows = pst.executeUpdate();
            pst.close();
            if (rows > 0) {
                JOptionPane.showMessageDialog(this,
                        "Quotation #" + quotationId + " updated to " + newStatus + ".",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Update failed for Quotation #" + quotationId + ".",
                        "Info", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error updating quotation: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
