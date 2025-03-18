import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;

public class CashierView extends JFrame {
    private JTextField barcodeField;
    private JButton btnScan;
    private JButton btnAvailableOrders;
    private JLabel paymentMethodLabel;
    private JButton btnProcessPayment;
    private JButton btnClearCart;
    private JButton btnLogout;
    private int pendingOrderCount = 0;
    private DefaultTableModel cartTableModel;
    private JTable cartTable;
    private JLabel labSubtotal;
    private JLabel labTax;
    private JLabel labTotal;
    private Orders currentOrder;

    public CashierView() {
        this.setTitle("Store Management System - Cashier View");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(1000, 700);
        this.setMinimumSize(new Dimension(800, 600));

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        initializeComponents();
        setupLayout(mainPanel);
        setupTableProperties();

        this.add(mainPanel);
        this.setLocationRelativeTo(null);
    }

    private void initializeComponents() {
        Dimension buttonSize = new Dimension(150, 35);
        Font buttonFont = new Font("Arial", Font.BOLD, 12);

        barcodeField = new JTextField(20);
        barcodeField.setPreferredSize(new Dimension(200, 35));
        barcodeField.setFont(new Font("Arial", Font.PLAIN, 14));

        btnScan = createStyledButton("Scan Product", buttonSize, buttonFont);
        btnScan.setEnabled(false);

        btnAvailableOrders = createStyledButton("Available Orders: (0)", buttonSize, buttonFont);
        btnProcessPayment = createStyledButton("Process Payment", buttonSize, buttonFont);
        btnProcessPayment.setEnabled(false);

        btnClearCart = createStyledButton("Clear Cart", buttonSize, buttonFont);
        btnLogout = createStyledButton("Logout", new Dimension(100, 35), buttonFont);

        paymentMethodLabel = new JLabel("Payment Method: -");
        paymentMethodLabel.setFont(new Font("Arial", Font.BOLD, 14));

        cartTableModel = new DefaultTableModel(
                new String[]{"Product ID", "Name", "Price", "Quantity", "Total", "Scanned"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        cartTable = new JTable(cartTableModel);

        Font summaryFont = new Font("Arial", Font.BOLD, 14);
        labSubtotal = new JLabel("Subtotal: $0.00");
        labTax = new JLabel("Tax (10%): $0.00");
        labTotal = new JLabel("Total: $0.00");

        labSubtotal.setFont(summaryFont);
        labTax.setFont(summaryFont);
        labTotal.setFont(summaryFont);
    }

    private void setupLayout(JPanel mainPanel) {
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        JPanel cartPanel = createCartPanel();
        mainPanel.add(cartPanel, BorderLayout.CENTER);

        JPanel summaryPanel = createSummaryPanel();
        mainPanel.add(summaryPanel, BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout(10, 10));

        JPanel topHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        topHeader.add(btnAvailableOrders);
        topHeader.add(btnLogout);

        JPanel scanPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        scanPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Scan Product"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        scanPanel.add(new JLabel("Barcode:"));
        scanPanel.add(barcodeField);
        scanPanel.add(btnScan);

        headerPanel.add(topHeader, BorderLayout.NORTH);
        headerPanel.add(scanPanel, BorderLayout.SOUTH);

        return headerPanel;
    }

    private JPanel createCartPanel() {
        JPanel cartPanel = new JPanel(new BorderLayout(10, 10));
        cartPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                "Current Order",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14)));

        cartTable.setFillsViewportHeight(true);
        JScrollPane scrollPane = new JScrollPane(cartTable);
        cartPanel.add(scrollPane, BorderLayout.CENTER);

        return cartPanel;
    }

    private JPanel createSummaryPanel() {
        JPanel summaryPanel = new JPanel(new BorderLayout(10, 10));
        summaryPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JPanel totalsPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        totalsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Order Summary"),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        totalsPanel.add(labSubtotal);
        totalsPanel.add(labTax);
        totalsPanel.add(labTotal);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        actionPanel.add(paymentMethodLabel);
        actionPanel.add(btnProcessPayment);
        actionPanel.add(btnClearCart);

        summaryPanel.add(totalsPanel, BorderLayout.EAST);
        summaryPanel.add(actionPanel, BorderLayout.SOUTH);

        return summaryPanel;
    }

    private void setupTableProperties() {
        cartTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        cartTable.setRowHeight(30);
        cartTable.getTableHeader().setReorderingAllowed(false);
        cartTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        cartTable.setFont(new Font("Arial", Font.PLAIN, 12));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);

        for (int i = 0; i < cartTable.getColumnCount(); i++) {
            if (i != 1) {
                cartTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }
        }

        cartTable.getColumnModel().getColumn(0).setPreferredWidth(70);
        cartTable.getColumnModel().getColumn(1).setPreferredWidth(250);
        cartTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        cartTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        cartTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        cartTable.getColumnModel().getColumn(5).setPreferredWidth(60);

        cartTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = cartTable.getSelectedRow();
                if (row >= 0) {
                    int productId = (Integer) cartTableModel.getValueAt(row, 0);
                    Product product = Application.getInstance().getDataAdapter().loadProduct(productId);
                    if (product != null) {
                        barcodeField.setText(product.getBarcode());
                        barcodeField.selectAll();
                        btnScan.requestFocus();
                    }
                }
            }
        });
        Font tableFont = new Font("Dialog", Font.PLAIN, 12);
        cartTable.setFont(tableFont);
        cartTable.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(JLabel.CENTER);
                if (value != null && value.toString().contains("✓")) {
                    setFont(new Font("Dialog", Font.PLAIN, 14));
                }
                return this;
            }
        });
    }

    private JButton createStyledButton(String text, Dimension size, Font font) {
        JButton button = new JButton(text);
        button.setPreferredSize(size);
        button.setFont(font);
        return button;
    }

    public void clearCart() {
        cartTableModel.setRowCount(0);
        updateTotals(0, 0, 0);
        currentOrder = null;
        btnScan.setEnabled(false);
        btnProcessPayment.setEnabled(false);
    }

    public void updateTotals(double subtotal, double tax, double total) {
        labSubtotal.setText(String.format("Subtotal: $%.2f", subtotal));
        labTax.setText(String.format("Tax (10%%): $%.2f", tax));
        labTotal.setText(String.format("Total: $%.2f", total));
    }

    public void updatePendingOrderCount(int count) {
        this.pendingOrderCount = count;
        btnAvailableOrders.setText("Available Orders: (" + count + ")");
    }

    public void loadOrder(Orders order) {
        System.out.println("===== DEBUG: Loading order in CashierView =====");
        System.out.println("Order details: " + order);

        this.currentOrder = order;
        cartTableModel.setRowCount(0);

        if (order != null && order.getLines() != null) {
            System.out.println("Number of order lines: " + order.getLines().size());

            for (OrderLine line : order.getLines()) {
                Product product = line.getProduct(Application.getInstance().getDataAdapter());
                System.out.println("Processing product: " + product);

                if (product != null) {
                    Object[] row = {
                            product.getProductID(),
                            product.getName(),
                            String.format("$%.2f", product.getPrice()),
                            line.getQuantity(),
                            "-",
                            line.isScanned() ? "✓" : ""
                    };
                    cartTableModel.addRow(row);
                }
            }
        } else {
            System.out.println("Order or order lines is null");
        }

        btnScan.setEnabled(true);
        updateTotals(0, 0, 0);
        paymentMethodLabel.setText("Payment Method: " + order.getPaymentMethod());
        System.out.println("Order loading complete");
    }

    public JTextField getBarcodeField() { return barcodeField; }
    public JButton getBtnScan() { return btnScan; }
    public JButton getBtnAvailableOrders() { return btnAvailableOrders; }
    public JButton getBtnProcessPayment() { return btnProcessPayment; }
    public JButton getBtnClearCart() { return btnClearCart; }
    public JButton getBtnLogout() { return btnLogout; }
    public DefaultTableModel getCartTableModel() { return cartTableModel; }
    public Orders getCurrentOrder() { return currentOrder; }
    public JTable getCartTable() { return cartTable; }

    public void updatePaymentMethod(PaymentMethod method) {
        paymentMethodLabel.setText("Payment Method: " + method);
    }
    public void setCurrentOrder(Orders order) {
        this.currentOrder = order;
        // Update UI state based on order
        btnScan.setEnabled(order != null);
        btnProcessPayment.setEnabled(false); // Reset until items are scanned
    }
}