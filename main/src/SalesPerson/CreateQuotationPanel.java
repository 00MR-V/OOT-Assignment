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
    private int quotationId;      // For editing mode
    private int customerId;       // To store associated customer id (for update)

    // Customer Details Components
    private JTextField tfCustomerName;
    private JTextField tfCustomerAddress;
    private JTextField tfCustomerContact;
    private JComboBox<String> cbCustomerType;  // e.g., Regular, VIP, Corporate

    // Quotation Details Components
    private JComboBox<String> cbProductCategory;
    private JTextField tfQuantity;
    private JRadioButton rbStandard;
    private JRadioButton rbExpress;
    private ButtonGroup bgDelivery;
    private JCheckBox chDiscount;

    // Available Items (using JList)
    private JList<String> listAvailableItems;
    private DefaultListModel<String> listModel;

    // Additional Information
    private JTextArea taAdditionalInfo;

    // Action Buttons
    private JButton btnSubmit;
    private JButton btnClear;

    // Constructor for creating a new quotation
    public CreateQuotationPanel(int salesPersonId) {
        this.salesPersonId = salesPersonId;
        initializeUI();
        loadAvailableItems();
    }

    // Overloaded constructor for editing an existing quotation
    public CreateQuotationPanel(int salesPersonId, int quotationId) {
        this(salesPersonId);
        this.editMode = true;
        this.quotationId = quotationId;
        loadExistingData();
        btnSubmit.setText("Save Quotation");
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

        gbc.gridx = 0;
        gbc.gridy = 0;
        customerPanel.add(new JLabel("Customer Name:"), gbc);
        gbc.gridx = 1;
        tfCustomerName = new JTextField(20);
        customerPanel.add(tfCustomerName, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        customerPanel.add(new JLabel("Address:"), gbc);
        gbc.gridx = 1;
        tfCustomerAddress = new JTextField(20);
        customerPanel.add(tfCustomerAddress, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        customerPanel.add(new JLabel("Contact:"), gbc);
        gbc.gridx = 1;
        tfCustomerContact = new JTextField(15);
        customerPanel.add(tfCustomerContact, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        customerPanel.add(new JLabel("Customer Type:"), gbc);
        gbc.gridx = 1;
        String[] types = {"Regular", "VIP", "Corporate"};
        cbCustomerType = new JComboBox<>(types);
        customerPanel.add(cbCustomerType, gbc);

        // --- Quotation Details Panel ---
        JPanel quotationPanel = new JPanel(new GridBagLayout());
        quotationPanel.setBorder(BorderFactory.createTitledBorder("Quotation Details"));
        GridBagConstraints gbc2 = new GridBagConstraints();
        gbc2.insets = new Insets(5, 5, 5, 5);
        gbc2.fill = GridBagConstraints.HORIZONTAL;

        gbc2.gridx = 0;
        gbc2.gridy = 0;
        quotationPanel.add(new JLabel("Product Category:"), gbc2);
        gbc2.gridx = 1;
        String[] categories = {"Cotton", "Silk", "Wool", "Synthetic"};
        cbProductCategory = new JComboBox<>(categories);
        quotationPanel.add(cbProductCategory, gbc2);

        gbc2.gridx = 0;
        gbc2.gridy = 1;
        quotationPanel.add(new JLabel("Quantity:"), gbc2);
        gbc2.gridx = 1;
        tfQuantity = new JTextField(10);
        quotationPanel.add(tfQuantity, gbc2);

        gbc2.gridx = 0;
        gbc2.gridy = 2;
        quotationPanel.add(new JLabel("Delivery Option:"), gbc2);
        gbc2.gridx = 1;
        rbStandard = new JRadioButton("Standard", true);
        rbExpress = new JRadioButton("Express");
        bgDelivery = new ButtonGroup();
        bgDelivery.add(rbStandard);
        bgDelivery.add(rbExpress);
        JPanel deliveryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        deliveryPanel.add(rbStandard);
        deliveryPanel.add(rbExpress);
        quotationPanel.add(deliveryPanel, gbc2);

        gbc2.gridx = 0;
        gbc2.gridy = 3;
        quotationPanel.add(new JLabel("Discount Applicable:"), gbc2);
        gbc2.gridx = 1;
        chDiscount = new JCheckBox("Apply Discount");
        quotationPanel.add(chDiscount, gbc2);

        // --- Available Items Panel ---
        JPanel itemsPanel = new JPanel(new BorderLayout());
        itemsPanel.setBorder(BorderFactory.createTitledBorder("Available Items"));
        listModel = new DefaultListModel<>();
        // Items will be loaded dynamically from the database (see loadAvailableItems method)
        listAvailableItems = new JList<>(listModel);
        listAvailableItems.setVisibleRowCount(4);
        listAvailableItems.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane itemsScrollPane = new JScrollPane(listAvailableItems);
        itemsPanel.add(itemsScrollPane, BorderLayout.CENTER);

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

        // --- Combine all panels into a vertical layout ---
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.add(customerPanel);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(quotationPanel);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(itemsPanel);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(additionalPanel);

        add(mainPanel, BorderLayout.CENTER);

        // --- Action Listeners ---
        btnSubmit.addActionListener(e -> saveQuotation());
        btnClear.addActionListener(e -> clearFields());
    }

    // Loads available items dynamically from the "items" table
    private void loadAvailableItems() {
        listModel.clear();
        try {
            Connection conn = DBConnection.getConnection();
            String query = "SELECT item_name, stock_level FROM items ORDER BY item_name";
            PreparedStatement pst = conn.prepareStatement(query);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                String itemName = rs.getString("item_name");
                int stock = rs.getInt("stock_level");
                // Display as "Item Name (Stock: X)"
                listModel.addElement(itemName + " (Stock: " + stock + ")");
            }
            rs.close();
            pst.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading available items: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // This method either inserts a new quotation or updates an existing one.
    private void saveQuotation() {
        String customerName = tfCustomerName.getText().trim();
        String customerAddress = tfCustomerAddress.getText().trim();
        String customerContact = tfCustomerContact.getText().trim();
        String customerType = (String) cbCustomerType.getSelectedItem();
        String productCategory = (String) cbProductCategory.getSelectedItem();
        String quantityStr = tfQuantity.getText().trim();
        String deliveryOption = rbStandard.isSelected() ? "Standard" : "Express";
        boolean discount = chDiscount.isSelected();
        String additionalInfo = taAdditionalInfo.getText().trim();

        // Get selected available items as comma-separated string.
        // Extract only the item name from the string (removing the stock details).
        java.util.List<String> selected = listAvailableItems.getSelectedValuesList();
        List<String> selectedItems = new ArrayList<>();
        for (String item : selected) {
            if (item.contains(" (Stock:")) {
                selectedItems.add(item.substring(0, item.indexOf(" (Stock:")));
            } else {
                selectedItems.add(item);
            }
        }
        String availableItems = String.join(",", selectedItems);

        if (customerName.isEmpty() || customerAddress.isEmpty() || customerContact.isEmpty() || quantityStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all mandatory fields.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(quantityStr);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Quantity must be a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Connection conn = DBConnection.getConnection();
        try {
            if (!editMode) {
                // INSERT mode: Insert new customer and quotation

                // Insert customer
                String sqlCustomer = "INSERT INTO customers (name, address, contact_details) VALUES (?, ?, ?)";
                PreparedStatement pstCustomer = conn.prepareStatement(sqlCustomer, PreparedStatement.RETURN_GENERATED_KEYS);
                pstCustomer.setString(1, customerName);
                pstCustomer.setString(2, customerAddress);
                pstCustomer.setString(3, customerContact);
                int affectedRows = pstCustomer.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Creating customer failed, no rows affected.");
                }
                ResultSet generatedKeys = pstCustomer.getGeneratedKeys();
                int newCustomerId = 0;
                if (generatedKeys.next()){
                    newCustomerId = generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating customer failed, no ID obtained.");
                }
                pstCustomer.close();

                // Insert quotation (with additional fields, including available_items)
                String sqlQuotation = "INSERT INTO quotations (customer_id, sales_person_id, date_created, status, product_category, quantity, delivery_option, discount, additional_info, customer_type, available_items) VALUES (?, ?, CURRENT_DATE(), ?, ?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement pstQuotation = conn.prepareStatement(sqlQuotation);
                pstQuotation.setInt(1, newCustomerId);
                pstQuotation.setInt(2, salesPersonId);
                pstQuotation.setString(3, "Pending");
                pstQuotation.setString(4, productCategory);
                pstQuotation.setInt(5, quantity);
                pstQuotation.setString(6, deliveryOption);
                pstQuotation.setBoolean(7, discount);
                pstQuotation.setString(8, additionalInfo);
                pstQuotation.setString(9, customerType);
                pstQuotation.setString(10, availableItems);
                pstQuotation.executeUpdate();
                pstQuotation.close();

                JOptionPane.showMessageDialog(this, "Quotation submitted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                // EDIT mode: Update the existing customer and quotation records.
                // Update customer details.
                String sqlUpdateCustomer = "UPDATE customers SET name = ?, address = ?, contact_details = ? WHERE customer_id = ?";
                PreparedStatement pstCust = conn.prepareStatement(sqlUpdateCustomer);
                pstCust.setString(1, customerName);
                pstCust.setString(2, customerAddress);
                pstCust.setString(3, customerContact);
                pstCust.setInt(4, customerId);
                pstCust.executeUpdate();
                pstCust.close();

                // Update quotation details (excluding date_created and status)
                String sqlUpdateQuotation = "UPDATE quotations SET product_category = ?, quantity = ?, delivery_option = ?, discount = ?, additional_info = ?, customer_type = ?, available_items = ? WHERE quotation_id = ?";
                PreparedStatement pstQuo = conn.prepareStatement(sqlUpdateQuotation);
                pstQuo.setString(1, productCategory);
                pstQuo.setInt(2, quantity);
                pstQuo.setString(3, deliveryOption);
                pstQuo.setBoolean(4, discount);
                pstQuo.setString(5, additionalInfo);
                pstQuo.setString(6, customerType);
                pstQuo.setString(7, availableItems);
                pstQuo.setInt(8, quotationId);
                pstQuo.executeUpdate();
                pstQuo.close();

                JOptionPane.showMessageDialog(this, "Quotation updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
            clearFields();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error while saving quotation: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearFields() {
        tfCustomerName.setText("");
        tfCustomerAddress.setText("");
        tfCustomerContact.setText("");
        tfQuantity.setText("");
        taAdditionalInfo.setText("");
        cbCustomerType.setSelectedIndex(0);
        cbProductCategory.setSelectedIndex(0);
        rbStandard.setSelected(true);
        chDiscount.setSelected(false);
        listAvailableItems.clearSelection();
    }

    // For edit mode: load existing quotation data from DB and pre-populate fields.
    private void loadExistingData() {
        try {
            Connection conn = DBConnection.getConnection();
            String query = "SELECT c.customer_id, c.name, c.address, c.contact_details, " +
                           "q.product_category, q.quantity, q.delivery_option, q.discount, q.additional_info, q.customer_type, q.available_items " +
                           "FROM quotations q JOIN customers c ON q.customer_id = c.customer_id " +
                           "WHERE q.quotation_id = ?";
            PreparedStatement pst = conn.prepareStatement(query);
            pst.setInt(1, quotationId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                customerId = rs.getInt("customer_id");
                tfCustomerName.setText(rs.getString("name"));
                tfCustomerAddress.setText(rs.getString("address"));
                tfCustomerContact.setText(rs.getString("contact_details"));
                cbCustomerType.setSelectedItem(rs.getString("customer_type"));
                cbProductCategory.setSelectedItem(rs.getString("product_category"));
                tfQuantity.setText(String.valueOf(rs.getInt("quantity")));
                String delivery = rs.getString("delivery_option");
                if ("Standard".equalsIgnoreCase(delivery)) {
                    rbStandard.setSelected(true);
                } else {
                    rbExpress.setSelected(true);
                }
                chDiscount.setSelected(rs.getBoolean("discount"));
                taAdditionalInfo.setText(rs.getString("additional_info"));
                // Load available items: assume they are stored as a comma-separated string.
                String availItems = rs.getString("available_items");
                if (availItems != null && !availItems.isEmpty()) {
                    String[] items = availItems.split(",");
                    int[] indices = getIndicesForItems(items);
                    listAvailableItems.setSelectedIndices(indices);
                }
            }
            rs.close();
            pst.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading quotation data: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Helper method to get indices of available items in the list model based on plain item names.
    private int[] getIndicesForItems(String[] items) {
        List<Integer> indices = new ArrayList<>();
        for (String item : items) {
            for (int i = 0; i < listModel.getSize(); i++) {
                String listItem = listModel.get(i);
                // Extract item name (remove stock details if present)
                if (listItem.contains(" (Stock:")) {
                    listItem = listItem.substring(0, listItem.indexOf(" (Stock:"));
                }
                if (listItem.equalsIgnoreCase(item.trim())) {
                    indices.add(i);
                }
            }
        }
        int[] result = new int[indices.size()];
        for (int i = 0; i < indices.size(); i++) {
            result[i] = indices.get(i);
        }
        return result;
    }
}
