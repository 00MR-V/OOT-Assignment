package SalesPerson;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import database.DBConnection;

public class QuotationPanel extends JPanel {
    private int salesPersonId;
    private JPanel quotationsContainer;
    private JScrollPane scrollPane;
    
    public QuotationPanel(int salesPersonId) {
        this.salesPersonId = salesPersonId;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
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
    
    // Public method to refresh quotations (e.g., from a Refresh button)
    public void refreshQuotations() {
        loadQuotationsFromDB();
    }
    
    private void loadQuotationsFromDB() {
        // Clear existing content
        quotationsContainer.removeAll();
        
        try {
            Connection conn = DBConnection.getConnection();
            // This query includes columns like discount, express_fee, etc.
            // even though we won't display them in the preview.
            String query = 
                "SELECT q.quotation_id, q.date_created, q.status, c.name AS customer_name, "
              + "q.delivery_option, q.discount, q.express_fee, q.total_amount, q.additional_info "
              + "FROM quotations q "
              + "JOIN customers c ON q.customer_id = c.customer_id "
              + "WHERE q.sales_person_id = ? "
              + "ORDER BY q.date_created DESC, q.quotation_id DESC";
            
            PreparedStatement pst = conn.prepareStatement(query);
            pst.setInt(1, salesPersonId);
            ResultSet rs = pst.executeQuery();
            
            while (rs.next()) {
                int quotationId = rs.getInt("quotation_id");
                Date dateCreated = rs.getDate("date_created");
                String status = rs.getString("status");
                String customerName = rs.getString("customer_name");
                
                // Even though we fetch these columns, we won't display them in the preview:
                String deliveryOption = rs.getString("delivery_option");
                double discount = rs.getDouble("discount");
                double expressFee = rs.getDouble("express_fee");
                double totalAmount = rs.getDouble("total_amount");
                String additionalInfo = rs.getString("additional_info");
                
                // Minimal display: ID, Customer Name, Date, Status, and Total Amount.
                String details = "<html>"
                        + "<b>Quotation ID:</b> " + quotationId + "<br>"
                        + "<b>Customer:</b> " + customerName + "<br>"
                        + "<b>Date:</b> " + dateCreated + "<br>"
                        + "<b>Status:</b> " + status + "<br>"
                        + "<b>Total Amount:</b> Rs " + String.format("%.2f", totalAmount)
                        + "</html>";
                
                // Create a sub-panel for each quotation
                JPanel qp = new JPanel(new BorderLayout(10, 10));
                // Reduced maximum height here (changed from 150 to 100)
                qp.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
                qp.setBorder(BorderFactory.createLineBorder(Color.GRAY));
                
                // Left side: Display minimal details
                JLabel detailsLabel = new JLabel(details);
                qp.add(detailsLabel, BorderLayout.CENTER);
                
                // Right side: Action components (Edit button and status dropdown)
                JPanel actionPanel = new JPanel(new GridLayout(2, 1, 5, 5));
                JButton btnEdit = new JButton("Edit");
                String[] statusOptions = {"Pending", "Approved", "Cancelled", "Reopened"};
                JComboBox<String> cbStatus = new JComboBox<>(statusOptions);
                cbStatus.setSelectedItem(status);
                actionPanel.add(btnEdit);
                actionPanel.add(cbStatus);
                qp.add(actionPanel, BorderLayout.EAST);
                
                // Edit button => open CreateQuotationPanel in edit mode
                btnEdit.addActionListener(e -> {
                    JFrame editFrame = new JFrame("Edit Quotation - #" + quotationId);
                    editFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    CreateQuotationPanel editPanel = new CreateQuotationPanel(salesPersonId, quotationId);
                    editFrame.add(editPanel);
                    editFrame.setSize(800, 600);
                    editFrame.setLocationRelativeTo(null);
                    editFrame.setVisible(true);
                });
                
                // Status dropdown => update DB and refresh
                cbStatus.addActionListener(e -> {
                    String newStatus = (String) cbStatus.getSelectedItem();
                    updateQuotationStatus(quotationId, newStatus);
                    loadQuotationsFromDB();
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
    
    // If you still want to fetch item lines for internal logic, keep this.
    // Otherwise, you can remove it.
    private static class ItemLine {
        String itemName;
        int quantity;
        double price;
        ItemLine(String itemName, int quantity, double price) {
            this.itemName = itemName;
            this.quantity = quantity;
            this.price = price;
        }
    }
    
    private List<ItemLine> loadItemsForQuotation(Connection conn, int quotationId) throws SQLException {
        List<ItemLine> result = new ArrayList<>();
        String sql = 
            "SELECT i.item_name, qi.quantity, qi.price "
          + "FROM quotation_items qi "
          + "JOIN items i ON qi.item_id = i.item_id "
          + "WHERE qi.quotation_id = ?";
        PreparedStatement pst = conn.prepareStatement(sql);
        pst.setInt(1, quotationId);
        ResultSet rs = pst.executeQuery();
        while (rs.next()) {
            String itemName = rs.getString("item_name");
            int quantity = rs.getInt("quantity");
            double price = rs.getDouble("price");
            result.add(new ItemLine(itemName, quantity, price));
        }
        rs.close();
        pst.close();
        return result;
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
