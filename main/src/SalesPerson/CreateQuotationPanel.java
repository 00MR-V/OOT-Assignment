package SalesPerson;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import database.DBConnection;

public class CreateQuotationPanel extends JPanel {

    private int salesPersonId;
    private boolean editMode = false;
    private int quotationId;  // For editing mode
    private int customerId;   // For storing the associated customer ID (for update)

    // Customer Details Components
    private JTextField tfCustomerName;
    private JTextField tfCustomerAddress;
    private JTextField tfCustomerContact;

    // New: Delivery Address Component
    private JTextField tfDeliveryAddress;

    // Quotation Details Components
    private JRadioButton rbStandard;
    private JRadioButton rbExpress;
    private ButtonGroup bgDelivery;
    private JCheckBox chDiscount;
    private JTextField discountField; // For discount modification (percentage)
    private JTextField expressFeeField; // New field for express fee (default Rs175)

    // Items for Sale Panel (Dynamic)
    private JPanel itemsSelectionPanel;
    private List<ItemSelection> itemSelections;

    // Summary / Total Panel
    private JTextArea summaryArea; // Displays detailed breakdown

    // Additional Information
    private JTextArea taAdditionalInfo;

    // Action Buttons
    private JButton btnSubmit;
    private JButton btnClear;

    // Inner class to hold item selection components
    private class ItemSelection {
        String itemName;
        int stock;
        double price;
        JCheckBox checkBox;
        JTextField quantityField;

        ItemSelection(String itemName, int stock, double price) {
            this.itemName = itemName;
            this.stock = stock;
            this.price = price;
            // Show item name, stock, and price (with "Rs")
            this.checkBox = new JCheckBox(
                itemName + " (Stock: " + stock + ", Price: Rs " + price + ")"
            );
            this.quantityField = new JTextField(5);
            this.quantityField.setEnabled(false);

            // Enable quantity field only if checkbox is selected; update summary on change.
            this.checkBox.addActionListener(e -> {
                quantityField.setEnabled(checkBox.isSelected());
                if (!checkBox.isSelected()) {
                    quantityField.setText("");
                }
                updateSummary();
            });
            this.quantityField.addKeyListener(new KeyAdapter() {
                @Override
                public void keyReleased(KeyEvent e) {
                    updateSummary();
                }
            });
        }
    }

    // Constructor for creating a new quotation
    public CreateQuotationPanel(int salesPersonId) {
        this.salesPersonId = salesPersonId;
        initializeUI();
        loadItemsForSale();
    }

    // Overloaded constructor for editing an existing quotation
    public CreateQuotationPanel(int salesPersonId, int quotationId) {
        this(salesPersonId);
        this.editMode = true;
        this.quotationId = quotationId;
        btnSubmit.setText("Save Quotation");
        loadExistingData();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- Customer Details Panel ---
        JPanel customerPanel = new JPanel(new GridBagLayout());
        customerPanel.setBorder(BorderFactory.createTitledBorder("Customer Details"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        gbc.gridx = 0; gbc.gridy = 0;
        customerPanel.add(new JLabel("Customer Name:"), gbc);
        gbc.gridx = 1;
        tfCustomerName = new JTextField(20);
        customerPanel.add(tfCustomerName, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        customerPanel.add(new JLabel("Address:"), gbc);
        gbc.gridx = 1;
        tfCustomerAddress = new JTextField(20);
        customerPanel.add(tfCustomerAddress, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        customerPanel.add(new JLabel("Contact:"), gbc);
        gbc.gridx = 1;
        tfCustomerContact = new JTextField(15);
        customerPanel.add(tfCustomerContact, gbc);

        // --- New: Delivery Details Panel ---
        JPanel deliveryPanel = new JPanel(new GridBagLayout());
        deliveryPanel.setBorder(BorderFactory.createTitledBorder("Delivery Details"));
        GridBagConstraints gbcDel = new GridBagConstraints();
        gbcDel.insets = new Insets(5, 5, 5, 5);
        gbcDel.fill = GridBagConstraints.HORIZONTAL;
        gbcDel.gridx = 0; gbcDel.gridy = 0;
        deliveryPanel.add(new JLabel("Delivery Address:"), gbcDel);
        gbcDel.gridx = 1;
        tfDeliveryAddress = new JTextField(20);
        deliveryPanel.add(tfDeliveryAddress, gbcDel);

        // --- Quotation Details Panel ---
        JPanel quotationPanel = new JPanel(new GridBagLayout());
        quotationPanel.setBorder(BorderFactory.createTitledBorder("Quotation Details"));
        GridBagConstraints gbc2 = new GridBagConstraints();
        gbc2.insets = new Insets(5, 5, 5, 5);
        gbc2.fill = GridBagConstraints.HORIZONTAL;
        
        gbc2.gridx = 0; gbc2.gridy = 0;
        quotationPanel.add(new JLabel("Delivery Option:"), gbc2);
        gbc2.gridx = 1;
        rbStandard = new JRadioButton("Standard", true);
        rbExpress = new JRadioButton("Express");
        bgDelivery = new ButtonGroup();
        bgDelivery.add(rbStandard);
        bgDelivery.add(rbExpress);
        JPanel delivOptPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        delivOptPanel.add(rbStandard);
        delivOptPanel.add(rbExpress);
        quotationPanel.add(delivOptPanel, gbc2);
        
        gbc2.gridx = 0; gbc2.gridy = 1;
        quotationPanel.add(new JLabel("Express Fee:"), gbc2);
        gbc2.gridx = 1;
        expressFeeField = new JTextField(5);
        expressFeeField.setEnabled(false); // Disabled by default (Standard selected)
        expressFeeField.setText("0");
        quotationPanel.add(expressFeeField, gbc2);
        
        // Listeners to toggle express fee field.
        rbStandard.addActionListener(e -> {
            expressFeeField.setEnabled(false);
            expressFeeField.setText("0");
            updateSummary();
        });
        rbExpress.addActionListener(e -> {
            expressFeeField.setEnabled(true);
            if(expressFeeField.getText().trim().isEmpty() || expressFeeField.getText().equals("0")){
                expressFeeField.setText("175");
            }
            updateSummary();
        });
        
        gbc2.gridx = 0; gbc2.gridy = 2;
        quotationPanel.add(new JLabel("Apply Discount:"), gbc2);
        gbc2.gridx = 1;
        chDiscount = new JCheckBox("Apply Discount");
        quotationPanel.add(chDiscount, gbc2);
        gbc2.gridx = 2;
        discountField = new JTextField(5);
        discountField.setEnabled(false);
        quotationPanel.add(discountField, gbc2);
        chDiscount.addActionListener(e -> {
            discountField.setEnabled(chDiscount.isSelected());
            if (!chDiscount.isSelected()) {
                discountField.setText("");
            }
            updateSummary();
        });
        discountField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                updateSummary();
            }
        });
        expressFeeField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                updateSummary();
            }
        });
        
        // --- Items for Sale Panel ---
        itemsSelectionPanel = new JPanel();
        itemsSelectionPanel.setLayout(new BoxLayout(itemsSelectionPanel, BoxLayout.Y_AXIS));
        itemsSelectionPanel.setBorder(BorderFactory.createTitledBorder("Items for Sale"));
        itemSelections = new ArrayList<>();
        
        // --- Summary Panel ---
        summaryArea = new JTextArea(12, 30);
        summaryArea.setEditable(false);
        summaryArea.setBorder(BorderFactory.createTitledBorder("Total Amount Breakdown"));
        
        // Combine items panel (left) and summary area (right) using JSplitPane.
        // The items panel is now wrapped in a JScrollPane to allow scrolling.
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(new JScrollPane(itemsSelectionPanel));
        splitPane.setRightComponent(new JScrollPane(summaryArea));
        splitPane.setDividerLocation(350);
        
        // --- Additional Information & Buttons Panel ---
        JPanel additionalPanel = new JPanel(new BorderLayout(10, 10));
        additionalPanel.setBorder(BorderFactory.createTitledBorder("Additional Information"));
        taAdditionalInfo = new JTextArea(4, 20);
        JScrollPane taScrollPane = new JScrollPane(taAdditionalInfo);
        additionalPanel.add(taScrollPane, BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnSubmit = new JButton("Submit Quotation");
        btnClear = new JButton("Clear");
        buttonPanel.add(btnSubmit);
        buttonPanel.add(btnClear);
        additionalPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // --- Combine Top Panels ---
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.add(customerPanel);
        topPanel.add(Box.createVerticalStrut(10));
        topPanel.add(deliveryPanel);  // New Delivery Details panel
        topPanel.add(Box.createVerticalStrut(10));
        topPanel.add(quotationPanel);
        topPanel.add(Box.createVerticalStrut(10));
        
        // Layout the main panel.
        add(topPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
        add(additionalPanel, BorderLayout.SOUTH);
        
        // --- Action Listeners ---
        btnSubmit.addActionListener(e -> saveQuotation());
        btnClear.addActionListener(e -> clearFields());
    }

    /**
     * Loads items from the "items" table and displays them with checkboxes and quantity fields.
     */
    private void loadItemsForSale() {
        itemsSelectionPanel.removeAll();
        itemSelections.clear();
        try {
            Connection conn = DBConnection.getConnection();
            String query = "SELECT item_name, stock_level, price FROM items ORDER BY item_name";
            PreparedStatement pst = conn.prepareStatement(query);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                String itemName = rs.getString("item_name");
                int stock = rs.getInt("stock_level");
                double price = rs.getDouble("price");
                ItemSelection itemSel = new ItemSelection(itemName, stock, price);
                itemSelections.add(itemSel);
                
                // Create a panel for each item.
                JPanel itemPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                itemPanel.add(itemSel.checkBox);
                itemPanel.add(new JLabel("Qty:"));
                itemPanel.add(itemSel.quantityField);
                itemsSelectionPanel.add(itemPanel);
            }
            rs.close();
            pst.close();
            itemsSelectionPanel.revalidate();
            itemsSelectionPanel.repaint();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error loading items for sale: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Recalculates the summary:
     * - Computes subtotal from selected items.
     * - Applies discount (if any) and adds express fee (if Express delivery).
     * - Displays line-by-line details and final grand total.
     */
    private void updateSummary() {
        StringBuilder sb = new StringBuilder();
        double subtotal = 0.0;
        int lineCount = 0;
        for (ItemSelection sel : itemSelections) {
            if (sel.checkBox.isSelected()) {
                int qty = 0;
                try {
                    qty = Integer.parseInt(sel.quantityField.getText().trim());
                } catch (NumberFormatException ex) {
                    // Skip if invalid.
                }
                if (qty > 0) {
                    double lineTotal = qty * sel.price;
                    subtotal += lineTotal;
                    lineCount++;
                    sb.append(String.format("Item #%d: %s\n   Qty: %d @ Rs %.2f => Rs %.2f\n\n",
                            lineCount, sel.itemName, qty, sel.price, lineTotal));
                }
            }
        }
        // Apply discount if any.
        double discountPct = 0.0;
        if (chDiscount.isSelected()) {
            try {
                discountPct = Double.parseDouble(discountField.getText().trim());
            } catch (NumberFormatException ex) { }
        }
        double discountAmt = subtotal * (discountPct / 100.0);
        double totalAfterDiscount = subtotal - discountAmt;
        // Add express fee if Express is selected.
        double expressFee = 0.0;
        if (rbExpress.isSelected()) {
            try {
                expressFee = Double.parseDouble(expressFeeField.getText().trim());
            } catch (NumberFormatException ex) { }
        }
        double grandTotal = totalAfterDiscount + expressFee;
        
        sb.append(String.format("Subtotal: Rs %.2f\n", subtotal));
        if (discountAmt > 0.0) {
            sb.append(String.format("Discount (%.0f%%): -Rs %.2f\n", discountPct, discountAmt));
        }
        if (rbExpress.isSelected()) {
            sb.append(String.format("Express Fee: Rs %.2f\n", expressFee));
        }
        sb.append(String.format("Grand Total: Rs %.2f\n", grandTotal));
        
        summaryArea.setText(sb.toString());
    }

    /**
     * Saves (or updates) the quotation header along with the line items.
     * The computed total, express fee, and delivery address are stored in the database.
     */
    private void saveQuotation() {
        String customerName = tfCustomerName.getText().trim();
        String customerAddress = tfCustomerAddress.getText().trim();
        String customerContact = tfCustomerContact.getText().trim();
        String deliveryAddress = tfDeliveryAddress.getText().trim();
        
        String deliveryOption = rbStandard.isSelected() ? "Standard" : "Express";
        boolean discountApplied = chDiscount.isSelected();
        double discountValue = 0.0;
        if (discountApplied) {
            try {
                discountValue = Double.parseDouble(discountField.getText().trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                        "Discount must be a valid number (for %).", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        double expressFee = 0.0;
        if (rbExpress.isSelected()) {
            try {
                expressFee = Double.parseDouble(expressFeeField.getText().trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                        "Express fee must be a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        String additionalInfo = taAdditionalInfo.getText().trim();
        
        // Gather selected items.
        List<ItemSelection> selectedItems = new ArrayList<>();
        for (ItemSelection sel : itemSelections) {
            if (sel.checkBox.isSelected()) {
                String qtyText = sel.quantityField.getText().trim();
                if (qtyText.isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                            "Please enter quantity for selected item: " + sel.itemName,
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                try {
                    int qty = Integer.parseInt(qtyText);
                    if (qty > sel.stock) {
                        JOptionPane.showMessageDialog(this,
                                "Quantity for " + sel.itemName + " exceeds stock (" + sel.stock + ").",
                                "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    if (qty <= 0) {
                        JOptionPane.showMessageDialog(this,
                                "Quantity must be > 0 for item: " + sel.itemName,
                                "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    selectedItems.add(sel);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this,
                            "Invalid quantity for item: " + sel.itemName,
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
        }
        
        if (customerName.isEmpty() || customerAddress.isEmpty() || customerContact.isEmpty() || deliveryAddress.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please fill in all mandatory fields (Name, Address, Contact, Delivery Address).",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Recalculate totals.
        double subtotal = 0.0;
        for (ItemSelection sel : selectedItems) {
            int qty = Integer.parseInt(sel.quantityField.getText().trim());
            subtotal += qty * sel.price;
        }
        double discountAmt = subtotal * (discountValue / 100.0);
        double totalAfterDiscount = subtotal - discountAmt;
        double grandTotal = totalAfterDiscount + expressFee;
        
        Connection conn = DBConnection.getConnection();
        try {
            conn.setAutoCommit(false);
            
            if (!editMode) {
                // 1) Insert the customer.
                String sqlCustomer = "INSERT INTO customers (name, address, contact_details) VALUES (?, ?, ?)";
                PreparedStatement pstCust = conn.prepareStatement(sqlCustomer, Statement.RETURN_GENERATED_KEYS);
                pstCust.setString(1, customerName);
                pstCust.setString(2, customerAddress);
                pstCust.setString(3, customerContact);
                pstCust.executeUpdate();
                ResultSet keys = pstCust.getGeneratedKeys();
                if (keys.next()) {
                    customerId = keys.getInt(1);
                }
                keys.close();
                pstCust.close();
                
                // 2) Insert into quotations including delivery_address, express fee, and total_amount.
                String sqlQuotation = "INSERT INTO quotations (customer_id, sales_person_id, date_created, status, "
                        + "delivery_option, discount, express_fee, total_amount, additional_info, delivery_address) "
                        + "VALUES (?, ?, CURRENT_DATE(), ?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement pstQuot = conn.prepareStatement(sqlQuotation, Statement.RETURN_GENERATED_KEYS);
                pstQuot.setInt(1, customerId);
                pstQuot.setInt(2, salesPersonId);
                pstQuot.setString(3, "Pending");
                pstQuot.setString(4, deliveryOption);
                pstQuot.setDouble(5, discountValue);
                pstQuot.setDouble(6, expressFee);
                pstQuot.setDouble(7, grandTotal);
                pstQuot.setString(8, additionalInfo);
                pstQuot.setString(9, deliveryAddress);
                pstQuot.executeUpdate();
                ResultSet quotKeys = pstQuot.getGeneratedKeys();
                if (quotKeys.next()) {
                    quotationId = quotKeys.getInt(1);
                }
                quotKeys.close();
                pstQuot.close();
            } else {
                // Edit mode: update the customer.
                String sqlUpdateCust = "UPDATE customers SET name = ?, address = ?, contact_details = ? WHERE customer_id = ?";
                PreparedStatement pstCust = conn.prepareStatement(sqlUpdateCust);
                pstCust.setString(1, customerName);
                pstCust.setString(2, customerAddress);
                pstCust.setString(3, customerContact);
                pstCust.setInt(4, customerId);
                pstCust.executeUpdate();
                pstCust.close();
                
                // Update the quotations row.
                String sqlUpdateQuotation = "UPDATE quotations SET delivery_option = ?, discount = ?, express_fee = ?, total_amount = ?, additional_info = ?, delivery_address = ? WHERE quotation_id = ?";
                PreparedStatement pstQuot = conn.prepareStatement(sqlUpdateQuotation);
                pstQuot.setString(1, deliveryOption);
                pstQuot.setDouble(2, discountValue);
                pstQuot.setDouble(3, expressFee);
                pstQuot.setDouble(4, grandTotal);
                pstQuot.setString(5, additionalInfo);
                pstQuot.setString(6, deliveryAddress);
                pstQuot.setInt(7, quotationId);
                pstQuot.executeUpdate();
                pstQuot.close();
                
                // Clear old items from quotation_items.
                String sqlDelItems = "DELETE FROM quotation_items WHERE quotation_id = ?";
                PreparedStatement pstDel = conn.prepareStatement(sqlDelItems);
                pstDel.setInt(1, quotationId);
                pstDel.executeUpdate();
                pstDel.close();
            }
            
            // 3) Insert each selected item into "quotation_items".
            for (ItemSelection sel : selectedItems) {
                int itemId = getItemIdByName(conn, sel.itemName);
                int qty = Integer.parseInt(sel.quantityField.getText().trim());
                String sqlItems = "INSERT INTO quotation_items (quotation_id, item_id, quantity, price) VALUES (?, ?, ?, ?)";
                PreparedStatement pstItems = conn.prepareStatement(sqlItems);
                pstItems.setInt(1, quotationId);
                pstItems.setInt(2, itemId);
                pstItems.setInt(3, qty);
                pstItems.setDouble(4, sel.price);
                pstItems.executeUpdate();
                pstItems.close();
            }
            
            conn.commit();
            conn.setAutoCommit(true);
            
            JOptionPane.showMessageDialog(this,
                    editMode ? "Quotation updated successfully!" : "Quotation submitted successfully!",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            
            clearFields();
            updateSummary();
            
        } catch (SQLException ex) {
            ex.printStackTrace();
            try { conn.rollback(); } catch (SQLException se) { se.printStackTrace(); }
            JOptionPane.showMessageDialog(this,
                    "Error while saving quotation: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Looks up the item_id from the "items" table given an item name.
     */
    private int getItemIdByName(Connection conn, String itemName) throws SQLException {
        String sql = "SELECT item_id FROM items WHERE item_name = ?";
        PreparedStatement pst = conn.prepareStatement(sql);
        pst.setString(1, itemName);
        ResultSet rs = pst.executeQuery();
        if (rs.next()) {
            int id = rs.getInt("item_id");
            rs.close();
            pst.close();
            return id;
        } else {
            rs.close();
            pst.close();
            throw new SQLException("Item not found: " + itemName);
        }
    }

    private void clearFields() {
        tfCustomerName.setText("");
        tfCustomerAddress.setText("");
        tfCustomerContact.setText("");
        tfDeliveryAddress.setText("");
        chDiscount.setSelected(false);
        discountField.setText("");
        discountField.setEnabled(false);
        rbStandard.setSelected(true);
        expressFeeField.setEnabled(false);
        expressFeeField.setText("0");
        for (ItemSelection sel : itemSelections) {
            sel.checkBox.setSelected(false);
            sel.quantityField.setText("");
            sel.quantityField.setEnabled(false);
        }
        taAdditionalInfo.setText("");
        summaryArea.setText("");
    }

    /**
     * For edit mode: load existing quotation data from the DB and pre-populate fields.
     */
    private void loadExistingData() {
        try {
            Connection conn = DBConnection.getConnection();
            String query = "SELECT c.customer_id, c.name, c.address, c.contact_details, "
                    + "q.delivery_option, q.discount, q.express_fee, q.total_amount, q.additional_info, q.delivery_address "
                    + "FROM quotations q JOIN customers c ON q.customer_id = c.customer_id "
                    + "WHERE q.quotation_id = ?";
            PreparedStatement pst = conn.prepareStatement(query);
            pst.setInt(1, quotationId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                customerId = rs.getInt("customer_id");
                tfCustomerName.setText(rs.getString("name"));
                tfCustomerAddress.setText(rs.getString("address"));
                tfCustomerContact.setText(rs.getString("contact_details"));
                tfDeliveryAddress.setText(rs.getString("delivery_address"));
                
                String delivery = rs.getString("delivery_option");
                if ("Standard".equalsIgnoreCase(delivery)) {
                    rbStandard.setSelected(true);
                    expressFeeField.setEnabled(false);
                    expressFeeField.setText("0");
                } else {
                    rbExpress.setSelected(true);
                    expressFeeField.setEnabled(true);
                    expressFeeField.setText(rs.getString("express_fee"));
                }
                
                double discountVal = rs.getDouble("discount");
                if (discountVal > 0) {
                    chDiscount.setSelected(true);
                    discountField.setEnabled(true);
                    discountField.setText(String.valueOf(discountVal));
                } else {
                    chDiscount.setSelected(false);
                    discountField.setEnabled(false);
                    discountField.setText("");
                }
                taAdditionalInfo.setText(rs.getString("additional_info"));
            }
            rs.close();
            pst.close();
            
            // Load the items from "quotation_items"
            String sqlItems = "SELECT qi.item_id, qi.quantity, qi.price, i.item_name "
                    + "FROM quotation_items qi JOIN items i ON qi.item_id = i.item_id "
                    + "WHERE qi.quotation_id = ?";
            PreparedStatement pstItems = conn.prepareStatement(sqlItems);
            pstItems.setInt(1, quotationId);
            ResultSet rsItems = pstItems.executeQuery();
            while (rsItems.next()) {
                String itemName = rsItems.getString("item_name");
                int qty = rsItems.getInt("quantity");
                for (ItemSelection sel : itemSelections) {
                    if (sel.itemName.equalsIgnoreCase(itemName)) {
                        sel.checkBox.setSelected(true);
                        sel.quantityField.setEnabled(true);
                        sel.quantityField.setText(String.valueOf(qty));
                    }
                }
            }
            rsItems.close();
            pstItems.close();
            updateSummary();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error loading quotation data: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
