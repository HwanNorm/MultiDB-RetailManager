import org.bson.Document;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;
import java.util.List;
import java.util.ArrayList;
import org.bson.Document;
import java.util.stream.Collectors;
public class ManagerView extends JFrame {
    private JTabbedPane tabbedPane = new JTabbedPane();
    private JButton btnLogout = new JButton("Logout");
    private JButton btnDeleteProduct = new JButton("Delete Product");
    // Users Panel Components
    private DefaultTableModel userTableModel;
    private JTable userTable;
    private JButton btnAddUser = new JButton("Add User");
    private JButton btnEditUser = new JButton("Edit User");
    private JButton btnDeleteUser = new JButton("Delete User");

    // Orders Panel Components
    private DefaultTableModel orderTableModel;
    private JTable orderTable;
    private JButton btnViewOrder = new JButton("View Details");
    private JButton btnUpdateStatus = new JButton("Update Status");

    // Reports Panel Components
    private JTextField txtProductName = new JTextField(30);
    private JTextField txtProductPrice = new JTextField(10);
    private JTextField txtProductQuantity = new JTextField(10);
    private JTextField txtProductBarcode = new JTextField(20);
    private JTextField txtCategory = new JTextField(20);
    private JButton btnAddProduct = new JButton("Add Product");
    private JButton btnEditProduct = new JButton("Edit Product");
    private JButton btnSaveProduct = new JButton("Save Product");
    private DefaultTableModel productTableModel;
    private JTable productTable;

    public ManagerView() {
        this.setTitle("Store Management System - Manager View");
        this.setSize(1000, 700);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Create main panel to hold everything
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Add logout button panel at the top
        JPanel logoutPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        logoutPanel.add(btnLogout);
        mainPanel.add(logoutPanel, BorderLayout.NORTH);

        // Initialize tabs
        JPanel usersPanel = createUsersPanel();
        tabbedPane.addTab("Users", usersPanel);

        JPanel ordersPanel = createOrdersPanel();
        tabbedPane.addTab("Orders", ordersPanel);

        JPanel productsPanel = createProductsPanel();
        tabbedPane.addTab("Products", productsPanel);

        tabbedPane.addTab("MongoDB & Redis Analytics", createMongoAnalyticsPanel());

        tabbedPane.addChangeListener(e -> {
            if (tabbedPane.getSelectedIndex() == 2) { // Index for Products tab
                Application.getInstance().getManagerController().loadProducts();
            }
        });

        // Add tabbed pane to main panel
        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        // Add main panel to frame
        this.add(mainPanel);

        // Add logout action listener
        btnLogout.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                handleLogout();
            }
        });

        // Center the frame on screen
        this.setLocationRelativeTo(null);
    }

    private JPanel createUsersPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create table model with columns
        userTableModel = new DefaultTableModel(
                new String[]{"ID", "Username", "Full Name", "Role"},
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        userTable = new JTable(userTableModel);

        // Configure table properties
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userTable.setRowHeight(25);
        userTable.getTableHeader().setReorderingAllowed(false);

        // Set column widths
        userTable.getColumnModel().getColumn(0).setPreferredWidth(50);   // ID
        userTable.getColumnModel().getColumn(1).setPreferredWidth(150);  // Username
        userTable.getColumnModel().getColumn(2).setPreferredWidth(200);  // Full Name
        userTable.getColumnModel().getColumn(3).setPreferredWidth(100);  // Role

        // Add table to scroll pane
        JScrollPane scrollPane = new JScrollPane(userTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Create buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        buttonPanel.add(btnAddUser);
        buttonPanel.add(btnEditUser);
        buttonPanel.add(btnDeleteUser);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createProductsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Product form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Product Details"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Add form fields
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3;
        formPanel.add(txtProductName, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Price:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtProductPrice, gbc);
        gbc.gridx = 2;
        formPanel.add(new JLabel("Quantity:"), gbc);
        gbc.gridx = 3;
        formPanel.add(txtProductQuantity, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Barcode:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtProductBarcode, gbc);
        gbc.gridx = 2;
        formPanel.add(new JLabel("Category:"), gbc);
        gbc.gridx = 3;
        formPanel.add(txtCategory, gbc);

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        buttonPanel.add(btnAddProduct);
        buttonPanel.add(btnEditProduct);
        buttonPanel.add(btnSaveProduct);
        buttonPanel.add(btnDeleteProduct);

        // Product table
        productTableModel = new DefaultTableModel(
                new String[]{"ID", "Name", "Price", "Quantity", "Category", "Status"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        productTable = new JTable(productTableModel);
        productTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Add components to panel
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(formPanel, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(productTable), BorderLayout.CENTER);

        return panel;
    }

    private JPanel createOrdersPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create table model with columns
        orderTableModel = new DefaultTableModel(
                new String[]{"Order ID", "Buyer", "Date", "Total Cost", "Status", "Products", "Quantity"},
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    case 0:
                        return Integer.class;  // Order ID
                    case 2:
                        return java.util.Date.class;  // Date
                    case 6:
                        return Double.class;  // Quantity
                    default:
                        return String.class;
                }
            }
        };

        orderTable = new JTable(orderTableModel);

        // Set column widths
        orderTable.getColumnModel().getColumn(0).setPreferredWidth(70);   // Order ID
        orderTable.getColumnModel().getColumn(1).setPreferredWidth(150);  // Buyer
        orderTable.getColumnModel().getColumn(2).setPreferredWidth(150);  // Date
        orderTable.getColumnModel().getColumn(3).setPreferredWidth(100);  // Total Cost
        orderTable.getColumnModel().getColumn(4).setPreferredWidth(100);  // Status
        orderTable.getColumnModel().getColumn(5).setPreferredWidth(250);  // Products
        orderTable.getColumnModel().getColumn(6).setPreferredWidth(80);   // Quantity

        // Set up date renderer
        orderTable.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                if (value instanceof java.util.Date) {
                    value = dateFormat.format(value);
                }
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        });

        // Configure table properties
        orderTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        orderTable.setRowHeight(25);
        orderTable.getTableHeader().setReorderingAllowed(false);

        // Add table to scroll pane
        JScrollPane scrollPane = new JScrollPane(orderTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Create buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        buttonPanel.add(btnViewOrder);
        buttonPanel.add(btnUpdateStatus);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }


    private JPanel createMongoAnalyticsPanel() {
        if (!MongoDataAdapter.getInstance().isConnected()) {
            JPanel errorPanel = new JPanel(new BorderLayout());
            errorPanel.add(new JLabel("Cannot connect to MongoDB"), BorderLayout.CENTER);
            return errorPanel;
        }
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create tabs for different MongoDB data
        JTabbedPane mongoTabs = new JTabbedPane();

        // Customers tab
        JPanel customersPanel = new JPanel(new BorderLayout());
        DefaultTableModel customerModel = new DefaultTableModel(
                new String[]{"MySQL ID", "Name", "Email", "Phone", "City", "State", "Favorite Categories", "Member Since"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable customerTable = new JTable(customerModel);
        addTableDetailListeners(customerTable);
        JScrollPane scrollPane = new JScrollPane(customerTable); // Make sure table is in ScrollPane
        customersPanel.add(scrollPane, BorderLayout.CENTER);

        // Orders tab
        JPanel ordersPanel = new JPanel(new BorderLayout());
        DefaultTableModel orderModel = new DefaultTableModel(
                new String[]{"MySQL Order ID", "Customer ID", "Order Date", "Status", "Total Amount", "Processing Time", "Time of Day"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable orderTable = new JTable(orderModel);
        addTableDetailListeners(orderTable);
        JScrollPane orderScroll = new JScrollPane(orderTable);
        orderTable.setFillsViewportHeight(true);
        ordersPanel.add(orderScroll);

        // Reviews tab
        JPanel reviewsPanel = new JPanel(new BorderLayout());
        DefaultTableModel reviewModel = new DefaultTableModel(
                new String[]{"Product ID", "Customer ID", "Rating", "Review Text", "Helpful Votes", "Status", "Review Date"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable reviewTable = new JTable(reviewModel);
        addTableDetailListeners(reviewTable);
        JScrollPane reviewScroll = new JScrollPane(reviewTable);
        reviewTable.setFillsViewportHeight(true);
        reviewsPanel.add(reviewScroll);

        // Add Redis Analytics tab
        JPanel redisAnalyticsPanel = createRedisAnalyticsPanel();
        mongoTabs.addTab("Real-Time Analytics", redisAnalyticsPanel);

        // Add refresh buttons for each tab
        JButton refreshCustomers = new JButton("Refresh");
        refreshCustomers.addActionListener(e -> loadCustomerData(customerModel));

        JButton refreshOrders = new JButton("Refresh");
        refreshOrders.addActionListener(e -> loadOrderData(orderModel));

        JButton refreshReviews = new JButton("Refresh");
        refreshReviews.addActionListener(e -> loadReviewData(reviewModel));

        customersPanel.add(refreshCustomers, BorderLayout.SOUTH);
        ordersPanel.add(refreshOrders, BorderLayout.SOUTH);
        reviewsPanel.add(refreshReviews, BorderLayout.SOUTH);

        // Add tabs
        mongoTabs.addTab("Customers", customersPanel);
        mongoTabs.addTab("Orders", ordersPanel);
        mongoTabs.addTab("Reviews", reviewsPanel);

        panel.add(mongoTabs, BorderLayout.CENTER);

        // Initial data load
        loadCustomerData(customerModel);
        loadOrderData(orderModel);
        loadReviewData(reviewModel);

        return panel;
    }

    private void loadCustomerData(DefaultTableModel model) {
        model.setRowCount(0);
        try {
            System.out.println("Starting to load customer data..."); // Debug print
            List<Document> customers = MongoDataAdapter.getInstance().getAllCustomers();
            System.out.println("Number of customers retrieved: " + customers.size()); // Debug print

            for (Document customer : customers) {
                Document address = (Document) customer.get("address");
                Document preferences = (Document) customer.get("preferences");
                @SuppressWarnings("unchecked")
                List<String> categories = (List<String>) preferences.get("favoriteCategories");

                model.addRow(new Object[]{
                        customer.getInteger("mysqlUserId"),
                        customer.getString("name"),
                        customer.getString("email"),
                        customer.getString("phone"),
                        address.getString("city"),
                        address.getString("state"),
                        categories.stream().collect(Collectors.joining(", ")),
                        customer.getDate("memberSince")
                });
                System.out.println("Added customer: " + customer.getString("name")); // Debug print
            }
        } catch (Exception e) {
            System.err.println("Error loading customer data: " + e.getMessage()); // Debug print
            e.printStackTrace(); // Print full stack trace
            JOptionPane.showMessageDialog(this,
                    "Error loading customer data: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadOrderData(DefaultTableModel model) {
        model.setRowCount(0);
        try {
            List<Document> orders = MongoDataAdapter.getInstance().getAllOrders();
            for (Document order : orders) {
                Document timeline = (Document) order.get("timeline");
                Document orderDetails = (Document) order.get("orderDetails");
                Document analytics = (Document) order.get("analytics");

                model.addRow(new Object[]{
                        order.getInteger("mysqlOrderId"),
                        order.getInteger("customerId"),
                        timeline.getDate("ordered"),
                        orderDetails.getString("status"),
                        orderDetails.getDouble("totalAmount"),
                        analytics.getString("processingTime"),
                        analytics.getString("timeOfDay")
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading order data: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadReviewData(DefaultTableModel model) {
        model.setRowCount(0);
        try {
            List<Document> reviews = MongoDataAdapter.getInstance().getAllReviews();
            for (Document review : reviews) {
                model.addRow(new Object[]{
                        review.getInteger("productId"),
                        review.getInteger("mysqlCustomerId"),
                        review.getInteger("rating"),
                        review.getString("reviewText"),
                        review.getInteger("helpfulVotes"),
                        review.getString("status"),
                        review.getDate("reviewDate")
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading review data: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addDateValidation(JTextField dateField) {
        dateField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                String date = dateField.getText().trim();
                if (!isValidDate(date)) {
                    JOptionPane.showMessageDialog(null,
                            "Please enter a valid date in YYYY-MM-DD format",
                            "Invalid Date",
                            JOptionPane.WARNING_MESSAGE);
                    dateField.requestFocus();
                }
            }
        });
    }

    private boolean isValidDate(String date) {
        if (date == null || date.trim().isEmpty()) {
            return false;
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            sdf.setLenient(false);
            sdf.parse(date);
            return true;
        } catch (ParseException ex) {
            return false;
        }
    }

    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to logout?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            Application.getInstance().setCurrentUser(null);
        }
    }

    // Methods to update tables
    public void updateUserTable(Vector<Vector<Object>> data) {
        userTableModel.setRowCount(0);
        for (Vector<Object> row : data) {
            userTableModel.addRow(row);
        }
    }

    public void updateOrderTable(Vector<Vector<Object>> data) {
        orderTableModel.setRowCount(0);
        for (Vector<Object> row : data) {
            orderTableModel.addRow(row);
        }
    }
    private void addTableDetailListeners(JTable table) {
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {  // Double click
                    JTable target = (JTable) e.getSource();
                    int row = target.getSelectedRow();
                    int col = target.getSelectedColumn();
                    Object value = target.getValueAt(row, col);

                    if (value != null) {
                        String columnName = target.getColumnModel().getColumn(col).getHeaderValue().toString();
                        showDetailDialog(columnName, value);
                    }
                }
            }
        });
    }

    private void showDetailDialog(String title, Object content) {
        JDialog dialog = new JDialog(this, title, true);

        // For dates, format them nicely
        String displayContent;
        if (content instanceof Date) {
            displayContent = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format((Date)content);
        } else {
            displayContent = content.toString();
        }

        JTextArea textArea = new JTextArea(displayContent);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(400, 300));

        dialog.add(scrollPane);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private JPanel createRedisAnalyticsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create split pane for two sections
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

        // Best Selling Products section
        DefaultTableModel bestProductsModel = new DefaultTableModel(
                new String[]{"Product ID", "Name", "Total Quantity Sold"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable bestProductsTable = new JTable(bestProductsModel);
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createTitledBorder("Best Selling Products"));

        // Style the table
        bestProductsTable.setRowHeight(25);
        bestProductsTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        bestProductsTable.setFont(new Font("Arial", Font.PLAIN, 12));

        // Center align numeric columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        bestProductsTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        bestProductsTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);

        topPanel.add(new JScrollPane(bestProductsTable), BorderLayout.CENTER);

        // Recent Customers section
        DefaultTableModel recentCustomersModel = new DefaultTableModel(
                new String[]{"Customer ID", "Name", "Last Visit"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable recentCustomersTable = new JTable(recentCustomersModel);
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createTitledBorder("Recent Customers"));

        // Style the table
        recentCustomersTable.setRowHeight(25);
        recentCustomersTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        recentCustomersTable.setFont(new Font("Arial", Font.PLAIN, 12));

        // Center align ID and timestamp columns
        recentCustomersTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        recentCustomersTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);

        bottomPanel.add(new JScrollPane(recentCustomersTable), BorderLayout.CENTER);

        // Add refresh controls
        JPanel refreshPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton refreshButton = new JButton("Refresh Analytics");
        JLabel lastUpdatedLabel = new JLabel("Last updated: Never");
        refreshButton.addActionListener(e -> {
            System.out.println("Manual refresh requested");
            if (Application.getInstance() != null && Application.getInstance().getDataAdapter() != null) {
                refreshRedisAnalytics(bestProductsModel, recentCustomersModel);
                lastUpdatedLabel.setText("Last updated: " +
                        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            } else {
                System.out.println("Application or DataAdapter not yet initialized");
            }
        });
        refreshPanel.add(refreshButton);
        refreshPanel.add(lastUpdatedLabel);

        splitPane.setTopComponent(topPanel);
        splitPane.setBottomComponent(bottomPanel);
        splitPane.setDividerLocation(300);

        panel.add(splitPane, BorderLayout.CENTER);
        panel.add(refreshPanel, BorderLayout.SOUTH);

        // Schedule initial load
        SwingUtilities.invokeLater(() -> {
            System.out.println("Attempting initial Redis analytics refresh...");
            if (Application.getInstance() != null && Application.getInstance().getDataAdapter() != null) {
                System.out.println("Application and DataAdapter available, refreshing...");
                refreshRedisAnalytics(bestProductsModel, recentCustomersModel);
                lastUpdatedLabel.setText("Last updated: " +
                        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            } else {
                System.out.println("Application or DataAdapter not yet initialized");
            }
        });

        return panel;
    }
    private void refreshRedisAnalytics(DefaultTableModel productsModel, DefaultTableModel customersModel) {
        RedisDataAdapter redis = RedisDataAdapter.getInstance();
        DataAdapter dataAdapter = Application.getInstance().getDataAdapter();

        // Update best selling products
        productsModel.setRowCount(0);
        List<RedisDataAdapter.ProductSalesData> bestProducts = redis.getBestSellingProducts(10);
        for (RedisDataAdapter.ProductSalesData data : bestProducts) {
            Product product = dataAdapter.loadProduct(data.getProductId());
            if (product != null) {
                productsModel.addRow(new Object[]{
                        data.getProductId(),
                        product.getName(),
                        data.getQuantity()
                });
            }
        }

        // Update recent customers
        customersModel.setRowCount(0);
        List<RedisDataAdapter.RecentCustomerData> recentCustomers = redis.getRecentCustomers(10);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (RedisDataAdapter.RecentCustomerData data : recentCustomers) {
            customersModel.addRow(new Object[]{
                    data.getCustomerId(),
                    data.getName(),
                    sdf.format(new Date(data.getLastVisit()))
            });
        }
    }

    // Getters for components
    public JButton getBtnAddUser() {
        return btnAddUser;
    }

    public JButton getBtnEditUser() {
        return btnEditUser;
    }

    public JButton getBtnDeleteUser() {
        return btnDeleteUser;
    }

    public JButton getBtnViewOrder() {
        return btnViewOrder;
    }

    public JButton getBtnUpdateStatus() {
        return btnUpdateStatus;
    }

    public JTable getUserTable() {
        return userTable;
    }

    public JTable getOrderTable() {
        return orderTable;
    }
    public JButton getBtnLogout() {
        return btnLogout;
    }
    public JButton getBtnDeleteProduct() { return btnDeleteProduct; }

    public JTextField getTxtProductName() { return txtProductName; }
    public JTextField getTxtProductPrice() { return txtProductPrice; }
    public JTextField getTxtProductQuantity() { return txtProductQuantity; }
    public JTextField getTxtProductBarcode() { return txtProductBarcode; }
    public JTextField getTxtCategory() { return txtCategory; }
    public JButton getBtnAddProduct() { return btnAddProduct; }
    public JButton getBtnEditProduct() { return btnEditProduct; }
    public JButton getBtnSaveProduct() { return btnSaveProduct; }
    public JTable getProductTable() { return productTable; }
}

