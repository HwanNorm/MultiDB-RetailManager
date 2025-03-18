import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Vector;

public class CustomerViewController implements ActionListener {
    private CustomerView view;
    private DataAdapter dataAdapter;

    public CustomerViewController(CustomerView view, DataAdapter dataAdapter) {
        this.view = view;
        this.dataAdapter = dataAdapter;

        // Add action listeners
        this.view.getBtnLogout().addActionListener(this);
        this.view.getBtnViewOrders().addActionListener(this);
        this.view.getBtnBuy().addActionListener(this);
        this.view.getBtnSearch().addActionListener(this);

        // Add search field listener for real-time search
        this.view.getSearchField().addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                searchProducts();
            }
        });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == view.getBtnLogout()) {
            handleLogout();
        } else if (e.getSource() == view.getBtnViewOrders()) {
            showOrderHistory();
        } else if (e.getSource() == view.getBtnBuy()) {
            buySelectedProduct();
        } else if (e.getSource() == view.getBtnSearch()) {
            searchProducts();
        }
    }

    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(
                view,
                "Are you sure you want to logout?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            Application.getInstance().setCurrentUser(null);
        }
    }

    public void buySelectedProduct() {
        processSelectedProduct();
    }

    private void processSelectedProduct() {
        int selectedRow = view.getCatalogTable().getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(view,
                    "Please select a product to buy.",
                    "No Product Selected",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try {
            // Get product information from the selected row
            int productId = (Integer) view.getCatalogTable().getValueAt(selectedRow, 0);
            String productName = (String) view.getCatalogTable().getValueAt(selectedRow, 1);
            String priceStr = (String) view.getCatalogTable().getValueAt(selectedRow, 2);
            double availableQuantity = (Double) view.getCatalogTable().getValueAt(selectedRow, 3);

            // Create quantity input panel with text field
            JPanel quantityPanel = new JPanel(new BorderLayout(5, 5));
            JTextField quantityField = new JTextField("1");

            // Make the text field select all text when focused
            quantityField.addFocusListener(new java.awt.event.FocusAdapter() {
                public void focusGained(java.awt.event.FocusEvent evt) {
                    quantityField.selectAll();
                }
            });

            // Create info panel with product name and available quantity
            JPanel infoPanel = new JPanel(new GridLayout(2, 1, 5, 5));
            infoPanel.add(new JLabel("Enter quantity for " + productName + ":"));
            infoPanel.add(new JLabel("Available: " + availableQuantity));

            // Add components to panel
            quantityPanel.add(infoPanel, BorderLayout.NORTH);
            quantityPanel.add(quantityField, BorderLayout.CENTER);

            int result = JOptionPane.showConfirmDialog(view,
                    quantityPanel,
                    "Enter Quantity",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE);

            if (result == JOptionPane.OK_OPTION) {
                try {
                    double quantity = Double.parseDouble(quantityField.getText().trim());

                    if (quantity <= 0) {
                        JOptionPane.showMessageDialog(view,
                                "Please enter a valid quantity greater than 0.",
                                "Invalid Quantity",
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    if (quantity > availableQuantity) {
                        JOptionPane.showMessageDialog(view,
                                "Not enough stock available. Maximum available: " + availableQuantity,
                                "Insufficient Stock",
                                JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    // Load the actual product from database
                    Product product = dataAdapter.loadProduct(productId);
                    if (product == null) {
                        JOptionPane.showMessageDialog(view,
                                "Error loading product details.",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    // Use the singleton CheckoutController instance
                    CheckoutController checkoutController = Application.getInstance().getCheckoutController();
                    checkoutController.addProductToOrder(product, quantity);

                    // Show checkout screen and hide main screen
                    Application.getInstance().getCheckoutScreen().setVisible(true);
                    view.setVisible(false);

                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(view,
                            "Please enter a valid number for quantity.",
                            "Invalid Input",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(view,
                    "Error processing purchase: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    public void showProductDetails(int row) {
        int productId = (Integer)view.getCatalogTable().getValueAt(row, 0);
        Product product = Application.getInstance().getDataAdapter().loadProduct(productId);

        if (product != null) {
            JDialog dialog = new JDialog(view, "Product Details", true);
            dialog.setLayout(new BorderLayout(10, 10));

            JPanel contentPanel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.insets = new Insets(5, 5, 5, 5);

            // Add product details
            contentPanel.add(new JLabel("<html><h2>" + product.getName() + "</h2></html>"), gbc);

            gbc.gridy++;
            contentPanel.add(new JLabel("<html><b>Price:</b> $" +
                    String.format("%.2f", product.getPrice()) + "</html>"), gbc);

            gbc.gridy++;
            contentPanel.add(new JLabel("<html><b>Category:</b> " +
                    product.getCategory() + "</html>"), gbc);

            gbc.gridy++;
            contentPanel.add(new JLabel("<html><b>Description:</b><br>" +
                    product.getDescription() + "</html>"), gbc);

            dialog.add(contentPanel, BorderLayout.CENTER);

            JButton closeButton = new JButton("Close");
            closeButton.addActionListener(e -> dialog.dispose());

            JPanel buttonPanel = new JPanel();
            buttonPanel.add(closeButton);
            dialog.add(buttonPanel, BorderLayout.SOUTH);

            dialog.setSize(400, 300);
            dialog.setLocationRelativeTo(view);
            dialog.setVisible(true);
        }
    }

    private void searchProducts() {
        String searchTerm = view.getSearchField().getText().trim().toLowerCase();

        try {
            String query = """
           SELECT 
               p.ProductID,
               p.Name, 
               p.Price,
               p.Quantity,
               p.Category,
               p.Description,
               CASE 
                   WHEN p.Quantity = 0 THEN 'Out of Stock'
                   WHEN p.Quantity <= p.ReorderPoint THEN 'Low Stock'
                   ELSE 'In Stock'
               END as StockStatus
           FROM Products p
           WHERE p.Quantity > 0
           AND (
               LOWER(p.Name) LIKE ? 
               OR LOWER(p.Category) LIKE ?
               OR LOWER(p.Description) LIKE ?
           )
           ORDER BY p.Name
           """;

            try (PreparedStatement stmt = Application.getConnection().prepareStatement(query)) {
                String searchPattern = "%" + searchTerm + "%";
                stmt.setString(1, searchPattern);
                stmt.setString(2, searchPattern);
                stmt.setString(3, searchPattern);

                try (ResultSet rs = stmt.executeQuery()) {
                    DefaultTableModel model = (DefaultTableModel) view.getCatalogTable().getModel();
                    model.setRowCount(0);

                    while (rs.next()) {
                        Vector<Object> row = new Vector<>();
                        row.add(rs.getInt("ProductID"));
                        row.add(rs.getString("Name"));
                        row.add(String.format("$%.2f", rs.getDouble("Price")));
                        row.add(rs.getDouble("Quantity"));
                        row.add(rs.getString("Category"));
                        row.add(rs.getString("StockStatus"));
                        row.add("Details"); // For details button
                        model.addRow(row);
                    }
                }
            }
        } catch (SQLException ex) {
            System.err.println("Error searching products: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void showOrderHistory() {
        try {
            Users currentUser = Application.getInstance().getCurrentUser();
            Vector<Vector<Object>> orderHistory = dataAdapter.getBuyerOrderHistory(currentUser.getUserID());

            final JDialog historyDialog = new JDialog(view, "My Order History", true);
            historyDialog.setSize(800, 500);

            Vector<String> columnNames = new Vector<>();
            columnNames.add("Order ID");
            columnNames.add("Date");
            columnNames.add("Total Cost");
            columnNames.add("Status");

            DefaultTableModel tableModel = new DefaultTableModel(orderHistory, columnNames) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }

                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    if (columnIndex == 0) return Integer.class;
                    return super.getColumnClass(columnIndex);
                }
            };

            JTable orderTable = new JTable(tableModel);
            orderTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            orderTable.setRowHeight(25);

            // Add double-click listener for order details
            orderTable.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        int row = orderTable.getSelectedRow();
                        if (row >= 0) {
                            try {
                                Integer orderId = (Integer) orderTable.getValueAt(row, 0);
                                Orders order = dataAdapter.loadOrder(orderId);

                                if (order != null) {
                                    OrderDetailView detailView = new OrderDetailView(historyDialog);
                                    detailView.displayOrderDetails(order, dataAdapter);
                                    detailView.setVisible(true);
                                }
                            } catch (Exception ex) {
                                JOptionPane.showMessageDialog(historyDialog,
                                        "Error loading order details: " + ex.getMessage());
                            }
                        }
                    }
                }
            });

            JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
            mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            JLabel instructionsLabel = new JLabel("Double-click an order to view details");
            instructionsLabel.setHorizontalAlignment(JLabel.CENTER);
            mainPanel.add(instructionsLabel, BorderLayout.NORTH);
            mainPanel.add(new JScrollPane(orderTable), BorderLayout.CENTER);

            JButton closeButton = new JButton("Close");
            closeButton.addActionListener(e -> historyDialog.dispose());
            JPanel buttonPanel = new JPanel();
            buttonPanel.add(closeButton);
            mainPanel.add(buttonPanel, BorderLayout.SOUTH);

            historyDialog.add(mainPanel);
            historyDialog.setLocationRelativeTo(view);
            historyDialog.setVisible(true);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(view,
                    "Error loading order history: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public void refreshCatalog() {
        view.loadCatalogData();
    }
}