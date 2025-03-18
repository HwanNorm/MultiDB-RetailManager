import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Vector;
import java.util.Date;
public class ManagerController implements ActionListener {
    private ManagerView view;
    private DataAdapter dataAdapter;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private Timer refreshTimer;
    private boolean isEditingProduct = false;

    public ManagerController(ManagerView view, DataAdapter dataAdapter) {
        this.view = view;
        this.dataAdapter = dataAdapter;

        // Add action listeners
        view.getBtnAddUser().addActionListener(this);
        view.getBtnEditUser().addActionListener(this);
        view.getBtnDeleteUser().addActionListener(this);
        view.getBtnViewOrder().addActionListener(this);
        view.getBtnUpdateStatus().addActionListener(this);
        view.getBtnAddProduct().addActionListener(this);
        view.getBtnEditProduct().addActionListener(this);
        view.getBtnSaveProduct().addActionListener(this);
        view.getBtnDeleteProduct().addActionListener(this);

        // Load initial data
        loadUsers();
        loadOrders();
        loadProducts();

        // Set up auto-refresh timer (refresh every 5 seconds)
        refreshTimer = new Timer(5000, e -> {
            loadOrders(); // Reload orders
        });
        refreshTimer.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == view.getBtnAddUser()) {
            addUser();
        } else if (e.getSource() == view.getBtnEditUser()) {
            editUser();
        } else if (e.getSource() == view.getBtnDeleteUser()) {
            deleteUser();
        } else if (e.getSource() == view.getBtnViewOrder()) {
            viewOrderDetails();
        } else if (e.getSource() == view.getBtnUpdateStatus()) {
            updateOrderStatus();
        }
        if (e.getSource() == view.getBtnAddProduct()) {
            handleAddProduct();
        } else if (e.getSource() == view.getBtnEditProduct()) {
            handleEditProduct();
        } else if (e.getSource() == view.getBtnSaveProduct()) {
            handleSaveProduct();
        }
        else if (e.getSource() == view.getBtnDeleteProduct()) {
            deleteProduct();
        }
    }

    // Method to stop the timer when closing the view
    public void cleanup() {
        if (refreshTimer != null && refreshTimer.isRunning()) {
            refreshTimer.stop();
        }
    }

    private void handleAddProduct() {
        clearProductForm();
        isEditingProduct = false;
        view.getTxtProductName().requestFocus();
    }

    private void handleEditProduct() {
        int row = view.getProductTable().getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(view, "Please select a product to edit");
            return;
        }

        int productId = (Integer)view.getProductTable().getValueAt(row, 0);
        Product product = dataAdapter.loadProduct(productId);
        if (product != null) {
            view.getTxtProductName().setText(product.getName());
            view.getTxtProductPrice().setText(String.format("%.2f", product.getPrice()));
            view.getTxtProductQuantity().setText(String.format("%.2f", product.getQuantity()));
            view.getTxtProductBarcode().setText(product.getBarcode());
            view.getTxtCategory().setText((String)view.getProductTable().getValueAt(row, 4)); // Get category from table
            isEditingProduct = true;
        }
    }

    private void handleSaveProduct() {
        try {
            // Get selected row for editing case
            int selectedRow = view.getProductTable().getSelectedRow();

            Product product = new Product();
            if (selectedRow >= 0) {
                product.setProductID((Integer)view.getProductTable().getValueAt(selectedRow, 0));
            }

            product.setName(view.getTxtProductName().getText().trim());
            product.setPrice(Double.parseDouble(view.getTxtProductPrice().getText().trim()));
            product.setQuantity(Double.parseDouble(view.getTxtProductQuantity().getText().trim()));
            product.setCategory(view.getTxtCategory().getText().trim());
            product.setBarcode(view.getTxtProductBarcode().getText().trim());

            if (dataAdapter.saveProduct(product)) {
                JOptionPane.showMessageDialog(view, "Product saved successfully");
                clearProductForm();
                loadProducts();
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(view, "Please enter valid numbers for price and quantity");
        }
    }
    private void deleteProduct() {
        int selectedRow = view.getProductTable().getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(view, "Please select a product to delete");
            return;
        }

        int productId = (Integer)view.getProductTable().getValueAt(selectedRow, 0);
        String productName = (String)view.getProductTable().getValueAt(selectedRow, 1);

        int confirm = JOptionPane.showConfirmDialog(view,
                "Are you sure you want to delete product: " + productName + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                dataAdapter.deleteProduct(productId);
                loadProducts();
                JOptionPane.showMessageDialog(view, "Product deleted successfully");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(view,
                        "Error deleting product: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearProductForm() {
        view.getTxtProductName().setText("");
        view.getTxtProductPrice().setText("");
        view.getTxtProductQuantity().setText("");
        view.getTxtProductBarcode().setText("");
        view.getTxtCategory().setText("");
    }

    private void populateProductForm(Product product) {
        view.getTxtProductName().setText(product.getName());
        view.getTxtProductPrice().setText(String.format("%.2f", product.getPrice()));
        view.getTxtProductQuantity().setText(String.format("%.2f", product.getQuantity()));
        view.getTxtProductBarcode().setText(product.getBarcode());
        view.getTxtCategory().setText(product.getCategory());
    }

    public void loadProducts() {
        try {
            System.out.println("Loading products..."); // Debug print
            String query = """
            SELECT p.ProductID, p.Name, p.Price, p.Quantity, p.Category,
            CASE 
                WHEN p.Quantity = 0 THEN 'Out of Stock'
                WHEN p.Quantity <= p.ReorderPoint THEN 'Low Stock'
                ELSE 'In Stock'
            END as Status
            FROM Products p
            """;

            try (PreparedStatement stmt = dataAdapter.getConnection().prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {

                DefaultTableModel model = (DefaultTableModel) view.getProductTable().getModel();
                model.setRowCount(0);

                while (rs.next()) {
                    model.addRow(new Object[]{
                            rs.getInt("ProductID"),
                            rs.getString("Name"),
                            String.format("$%.2f", rs.getDouble("Price")),
                            rs.getDouble("Quantity"),
                            rs.getString("Category"),
                            rs.getString("Status")
                    });
                }
            }
        } catch (SQLException ex) {
            System.out.println("Error loading products: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    private void loadUsers() {
        try {
            Vector<Vector<Object>> userData = dataAdapter.getAllUsers();
            view.updateUserTable(userData);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Error loading users: " + ex.getMessage());
        }
    }

    private void loadOrders() {
        try {
            Vector<Vector<Object>> orderData = dataAdapter.getAllOrders();
            view.updateOrderTable(orderData);
        } catch (SQLException ex) {
            System.err.println("Error loading orders: " + ex.getMessage());
            // Don't show error dialog during auto-refresh to avoid annoying popups
        }
    }

    private void addUser() {
        // Create a dialog to get user information
        JTextField userName = new JTextField();
        JTextField password = new JTextField();
        JTextField displayName = new JTextField();
        JComboBox<Users.Role> role = new JComboBox<>(Users.Role.values());

        Object[] message = {
                "Username:", userName,
                "Password:", password,
                "Display Name:", displayName,
                "Role:", role
        };

        int option = JOptionPane.showConfirmDialog(null, message, "Add New User", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try {
                Users newUser = new Users();
                newUser.setUsername(userName.getText());
                newUser.setPassword(password.getText());
                newUser.setFullName(displayName.getText());
                newUser.setRole((Users.Role) role.getSelectedItem());

                dataAdapter.saveUser(newUser);
                loadUsers(); // Refresh the user table
                JOptionPane.showMessageDialog(null, "User added successfully!");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(null, "Error adding user: " + ex.getMessage());
            }
        }
    }

    private void editUser() {
        int selectedRow = view.getUserTable().getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(null, "Please select a user to edit.");
            return;
        }

        try {
            int userId = (Integer) view.getUserTable().getValueAt(selectedRow, 0);
            Users user = dataAdapter.loadUserById(userId);

            JTextField userName = new JTextField(user.getUsername());
            JTextField displayName = new JTextField(user.getFullName());
            JComboBox<Users.Role> role = new JComboBox<>(Users.Role.values());
            role.setSelectedItem(user.getRole());

            Object[] message = {
                    "Username:", userName,
                    "Display Name:", displayName,
                    "Role:", role
            };

            int option = JOptionPane.showConfirmDialog(null, message, "Edit User", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                user.setUsername(userName.getText());
                user.setFullName(displayName.getText());
                user.setRole((Users.Role) role.getSelectedItem());

                dataAdapter.updateUser(user);
                loadUsers(); // Refresh the user table
                JOptionPane.showMessageDialog(null, "User updated successfully!");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Error updating user: " + ex.getMessage());
        }
    }

    private void deleteUser() {
        int selectedRow = view.getUserTable().getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(null, "Please select a user to delete.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(null,
                "Are you sure you want to delete this user?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                int userId = (Integer) view.getUserTable().getValueAt(selectedRow, 0);
                dataAdapter.deleteUser(userId);
                loadUsers(); // Refresh the user table
                JOptionPane.showMessageDialog(null, "User deleted successfully!");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(null, "Error deleting user: " + ex.getMessage());
            }
        }
    }

    private void viewOrderDetails() {
        int selectedRow = view.getOrderTable().getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(null, "Please select an order to view.");
            return;
        }

        try {
            int orderId = (Integer) view.getOrderTable().getValueAt(selectedRow, 0);

            // Load the complete order including tax information
            Orders order = dataAdapter.loadOrder(orderId);
            if (order == null) {
                JOptionPane.showMessageDialog(null, "Error loading order details!");
                return;
            }

            // Create and show order details dialog
            JDialog dialog = new JDialog(view, "Order Details - Order #" + orderId, true);
            dialog.setSize(600, 400);

            // Create panels
            JPanel mainPanel = new JPanel();
            mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
            mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            // Create table with order details
            Vector<String> columns = new Vector<>();
            columns.add("Product");
            columns.add("Quantity");
            columns.add("Price");
            columns.add("Cost");

            Vector<Vector<Object>> orderDetails = dataAdapter.getOrderDetails(orderId);
            JTable detailsTable = new JTable(orderDetails, columns);
            JScrollPane scrollPane = new JScrollPane(detailsTable);
            scrollPane.setPreferredSize(new Dimension(550, 250));
            mainPanel.add(scrollPane);

            // Add cost summary panel
            JPanel costPanel = new JPanel(new GridLayout(3, 1, 5, 5));
            costPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createTitledBorder("Cost Summary"),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)
            ));

            // Calculate subtotal
            double subtotal = 0.0;
            for (Vector<Object> row : orderDetails) {
                subtotal += (Double) row.get(3); // Cost column
            }

            // Get tax and total
            double tax = order.getTotalTax();
            double total = order.getTotalCost();

            // Create labels with right alignment
            JLabel labSubtotal = new JLabel(String.format("Subtotal: $%,.2f", subtotal), SwingConstants.RIGHT);
            JLabel labTax = new JLabel(String.format("Tax (10%%): $%,.2f", tax), SwingConstants.RIGHT);
            JLabel labTotal = new JLabel(String.format("Total: $%,.2f", total), SwingConstants.RIGHT);

            // Set fonts
            Font regularFont = new Font("Arial", Font.PLAIN, 14);
            Font boldFont = new Font("Arial", Font.BOLD, 14);

            labSubtotal.setFont(regularFont);
            labTax.setFont(regularFont);
            labTotal.setFont(boldFont);

            costPanel.add(labSubtotal);
            costPanel.add(labTax);
            costPanel.add(labTotal);

            // Add cost panel to a right-aligned wrapper
            JPanel costWrapperPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            costWrapperPanel.add(costPanel);
            mainPanel.add(costWrapperPanel);

            // Add close button
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            JButton closeButton = new JButton("Close");
            closeButton.addActionListener(e -> dialog.dispose());
            buttonPanel.add(closeButton);
            mainPanel.add(buttonPanel);

            dialog.add(mainPanel);
            dialog.setLocationRelativeTo(view);
            dialog.setVisible(true);

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Error loading order details: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void updateOrderStatus() {
        int selectedRow = view.getOrderTable().getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(null, "Please select an order to update.");
            return;
        }

        try {
            int orderId = (Integer) view.getOrderTable().getValueAt(selectedRow, 0);
            String[] statuses = {"PENDING", "PROCESSING", "COMPLETED", "CANCELLED"};
            String currentStatus = (String) view.getOrderTable().getValueAt(selectedRow, 4);

            JComboBox<String> statusCombo = new JComboBox<>(statuses);
            statusCombo.setSelectedItem(currentStatus);

            int option = JOptionPane.showConfirmDialog(null,
                    new Object[]{"Select new status:", statusCombo},
                    "Update Order Status",
                    JOptionPane.OK_CANCEL_OPTION);

            if (option == JOptionPane.OK_OPTION) {
                String newStatus = (String) statusCombo.getSelectedItem();
                dataAdapter.updateOrderStatus(orderId, newStatus);
                loadOrders(); // Refresh the orders table
                JOptionPane.showMessageDialog(null, "Order status updated successfully!");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Error updating order status: " + ex.getMessage());
        }
    }

    private boolean isValidDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return false;
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            sdf.setLenient(false);
            sdf.parse(dateStr);
            return true;
        } catch (ParseException ex) {
            return false;
        }
    }
}