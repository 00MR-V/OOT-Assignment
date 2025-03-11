package SalesPerson;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.Field;
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
    
    private void loadQuotationsFromDB() {
        // Clear existing content
        quotationsContainer.removeAll();
        
        try {
            Connection conn = DBConnection.getConnection();
            // Retrieve quotations created by this salesperson, along with customer name
            String query = "SELECT q.quotation_id, q.date_created, q.status, c.name AS customer_name " +
                           "FROM Quotations q JOIN Customers c ON q.customer_id = c.customer_id " +
                           "WHERE q.sales_person_id = ?";
            PreparedStatement pst = conn.prepareStatement(query);
            pst.setInt(1, salesPersonId);
            ResultSet rs = pst.executeQuery();
            
            while (rs.next()) {
                int quotationId = rs.getInt("quotation_id");
                Date dateCreated = rs.getDate("date_created");
                String status = rs.getString("status");
                String customerName = rs.getString("customer_name");
                
                // Create a sub-panel for this quotation
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
                // Use a dropdown for status options
                String[] statusOptions = {"Pending", "Approved", "Cancelled"};
                JComboBox<String> cbStatus = new JComboBox<>(statusOptions);
                cbStatus.setSelectedItem(status);
                actionPanel.add(btnEdit);
                actionPanel.add(cbStatus);
                qp.add(actionPanel, BorderLayout.EAST);
                
                // Edit button: Open a new frame to edit the quotation
                btnEdit.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        QuotationData data = loadQuotationData(quotationId);
                        if (data != null) {
                            JFrame editFrame = new JFrame("Edit Quotation - #" + quotationId);
                            editFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                            // Create an instance of CreateQuotationPanel (as provided)
                            CreateQuotationPanel editPanel = new CreateQuotationPanel(salesPersonId);
                            // Populate its fields using reflection
                            populateEditablePanel(editPanel, data);
                            editFrame.add(editPanel);
                            editFrame.setSize(600,600);
                            editFrame.setLocationRelativeTo(null);
                            editFrame.setVisible(true);
                        } else {
                            JOptionPane.showMessageDialog(QuotationPanel.this, "Error loading quotation details", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                });
                
                // Status dropdown: When selection changes, update the quotation status in DB
                cbStatus.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        String newStatus = (String) cbStatus.getSelectedItem();
                        updateQuotationStatus(quotationId, newStatus);
                        loadQuotationsFromDB(); // Refresh list
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
            String query = "UPDATE Quotations SET status = ? WHERE quotation_id = ?";
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
    
    // Load detailed quotation data from the database for editing.
    // Since your DB has only customer details for a quotation,
    // we load only customer name, address, and contact.
    private QuotationData loadQuotationData(int quotationId) {
        QuotationData data = new QuotationData();
        try {
            Connection conn = DBConnection.getConnection();
            String query = "SELECT c.name AS customerName, c.address AS customerAddress, c.contact_details AS customerContact " +
                           "FROM Quotations q JOIN Customers c ON q.customer_id = c.customer_id " +
                           "WHERE q.quotation_id = ?";
            PreparedStatement pst = conn.prepareStatement(query);
            pst.setInt(1, quotationId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                data.customerName = rs.getString("customerName");
                data.customerAddress = rs.getString("customerAddress");
                data.customerContact = rs.getString("customerContact");
                // Since the extra fields are not stored, they remain null/default.
            } else {
                return null;
            }
            rs.close();
            pst.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        }
        return data;
    }
    
    // Helper class to hold quotation data (only the fields available in your DB)
    private class QuotationData {
        String customerName;
        String customerAddress;
        String customerContact;
    }
    
    // Populate the CreateQuotationPanel's fields using reflection
    // Only pre-populates the available customer details.
    private void populateEditablePanel(CreateQuotationPanel panel, QuotationData data) {
        try {
            Field f;
            // Set Customer Name
            f = panel.getClass().getDeclaredField("tfCustomerName");
            f.setAccessible(true);
            ((JTextField) f.get(panel)).setText(data.customerName);
            
            // Set Address
            f = panel.getClass().getDeclaredField("tfCustomerAddress");
            f.setAccessible(true);
            ((JTextField) f.get(panel)).setText(data.customerAddress);
            
            // Set Contact
            f = panel.getClass().getDeclaredField("tfCustomerContact");
            f.setAccessible(true);
            ((JTextField) f.get(panel)).setText(data.customerContact);
            
            // For the remaining fields (customer type, product category, quantity, delivery, discount, additional info),
            // no data is available from the DB. They will remain at default values.
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
