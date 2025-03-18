import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Vector;

public class CustomerView extends JFrame {
    private JButton btnViewOrders = new JButton("My Orders");
    private JButton btnLogout = new JButton("Logout");

    // Product catalog components
    private DefaultTableModel catalogModel;
    private JTable catalogTable;
    private JTextField searchField;
    private JButton btnSearch = new JButton("Search");
    private JButton btnBuy = new JButton("Buy Selected Product");
    private JLabel welcomeLabel = new JLabel();

    public CustomerView() {
        this.setTitle("Store Management System - Welcome");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(1000, 700);
        this.setMinimumSize(new Dimension(800, 600));

        // Create main panel with BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Create header panel
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Create catalog panel
        JPanel catalogPanel = createCatalogPanel();
        mainPanel.add(catalogPanel, BorderLayout.CENTER);

        // Create footer panel
        JPanel footerPanel = createFooterPanel();
        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        this.add(mainPanel);
        this.setLocationRelativeTo(null);

        // Add window listener to load data when window becomes visible
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowActivated(java.awt.event.WindowEvent windowEvent) {
                if (isVisible()) {
                    loadCatalogData();
                }
            }
        });
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout(10, 10));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        // Welcome message on the left
        welcomeLabel.setFont(new Font("Sans Serif", Font.BOLD, 24));
        headerPanel.add(welcomeLabel, BorderLayout.WEST);

        // Search panel on the right
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchField = new JTextField(20);
        searchField.setPreferredSize(new Dimension(200, 30));
        btnSearch.setPreferredSize(new Dimension(80, 30));
        searchPanel.add(new JLabel("Search Products: "));
        searchPanel.add(searchField);
        searchPanel.add(btnSearch);

        headerPanel.add(searchPanel, BorderLayout.EAST);
        return headerPanel;
    }

    private JPanel createCatalogPanel() {
        JPanel catalogPanel = new JPanel(new BorderLayout(10, 10));
        catalogPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Product Catalog"),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // Initialize catalog table with expanded columns
        catalogModel = new DefaultTableModel(
                new String[]{"Product ID", "Name", "Price", "Quantity", "Category", "Stock Status", "Details"},
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6; // Only Details column is editable
            }
        };

        catalogTable = new JTable(catalogModel);
        catalogTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        catalogTable.setRowHeight(25);
        catalogTable.getTableHeader().setReorderingAllowed(false);

        // Add Details button column
        Action showDetails = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                int modelRow = Integer.parseInt(e.getActionCommand());
                Application.getInstance().getMainScreenController().showProductDetails(modelRow);
            }
        };
        new ButtonColumn(catalogTable, showDetails, 6);

        // Add tooltip for description
        catalogTable.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseMoved(MouseEvent e) {
                Point p = e.getPoint();
                int row = catalogTable.rowAtPoint(p);
                if (row >= 0) {
                    int productId = (Integer)catalogTable.getValueAt(row, 0);
                    Product product = Application.getInstance().getDataAdapter().loadProduct(productId);
                    if (product != null && product.getDescription() != null) {
                        catalogTable.setToolTipText("<html><body width='300'>" +
                                product.getDescription() + "</body></html>");
                    } else {
                        catalogTable.setToolTipText(null);
                    }
                }
            }
        });

        // Center align all columns except Name and Description
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < catalogTable.getColumnCount(); i++) {
            if (i != 1) { // Skip Name column
                catalogTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }
        }

        // Set column widths
        catalogTable.getColumnModel().getColumn(0).setPreferredWidth(80);   // ID
        catalogTable.getColumnModel().getColumn(1).setPreferredWidth(250);  // Name
        catalogTable.getColumnModel().getColumn(2).setPreferredWidth(100);  // Price
        catalogTable.getColumnModel().getColumn(3).setPreferredWidth(80);   // Quantity
        catalogTable.getColumnModel().getColumn(4).setPreferredWidth(120);  // Category
        catalogTable.getColumnModel().getColumn(5).setPreferredWidth(100);  // Stock Status
        catalogTable.getColumnModel().getColumn(6).setPreferredWidth(80);   // Details

        // Add double-click listener
        catalogTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    Application.getInstance().getMainScreenController().buySelectedProduct();
                }
            }
        });

        // Add table to scroll pane
        JScrollPane scrollPane = new JScrollPane(catalogTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        catalogPanel.add(scrollPane, BorderLayout.CENTER);

        return catalogPanel;
    }

    private JPanel createFooterPanel() {
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        // Buy button on the left
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnBuy.setPreferredSize(new Dimension(150, 35));
        btnBuy.setFont(new Font("Sans Serif", Font.BOLD, 12));
        leftPanel.add(btnBuy);

        // Other buttons on the right
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnViewOrders.setPreferredSize(new Dimension(120, 35));
        btnLogout.setPreferredSize(new Dimension(100, 35));
        rightPanel.add(btnViewOrders);
        rightPanel.add(btnLogout);

        footerPanel.add(leftPanel, BorderLayout.WEST);
        footerPanel.add(rightPanel, BorderLayout.EAST);

        return footerPanel;
    }

    public void loadCatalogData() {
        try {
            // Get current user
            Users currentUser = Application.getInstance().getCurrentUser();
            if (currentUser != null) {
                welcomeLabel.setText("Welcome, " + currentUser.getFullName());
            }

            // Clear existing data
            catalogModel.setRowCount(0);

            // Updated query without seller reference
            String query = """
            SELECT 
                p.ProductID,
                p.Name,
                p.Price,
                p.Quantity,
                p.Category,
                CASE 
                    WHEN p.Quantity = 0 THEN 'Out of Stock'
                    WHEN p.Quantity <= p.ReorderPoint THEN 'Low Stock'
                    ELSE 'In Stock'
                END as StockStatus
            FROM Products p
            WHERE p.Quantity > 0
            ORDER BY p.Name
            """;

            try (PreparedStatement stmt = Application.getConnection().prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    Vector<Object> row = new Vector<>();
                    row.add(rs.getInt("ProductID"));
                    row.add(rs.getString("Name"));
                    row.add(String.format("$%.2f", rs.getDouble("Price")));
                    row.add(rs.getDouble("Quantity"));
                    row.add(rs.getString("Category"));
                    row.add(rs.getString("StockStatus"));
                    catalogModel.addRow(row);
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error loading product catalog: " + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // Add method to refresh catalog
    public void refreshCatalog() {
        loadCatalogData();
    }

    // Getters for components
    public JButton getBtnLogout() { return btnLogout; }
    public JButton getBtnViewOrders() { return btnViewOrders; }
    public JButton getBtnBuy() { return btnBuy; }
    public JButton getBtnSearch() { return btnSearch; }
    public JTextField getSearchField() { return searchField; }
    public JTable getCatalogTable() { return catalogTable; }
}