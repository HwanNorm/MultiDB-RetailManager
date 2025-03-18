import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class CashierController implements ActionListener {
    private CashierView view;
    private DataAdapter dataAdapter;
    private Connection connection;
    private Timer pendingOrdersTimer;

    public CashierController(CashierView view, DataAdapter dataAdapter) {
        this.view = view;
        this.dataAdapter = dataAdapter;
        this.connection = Application.getConnection();

        // Add action listeners
        view.getBtnScan().addActionListener(this);
        view.getBtnProcessPayment().addActionListener(this);
        view.getBtnClearCart().addActionListener(this);
        view.getBtnLogout().addActionListener(this);
        view.getBtnAvailableOrders().addActionListener(this);

        // Start timer to check pending orders periodically
        pendingOrdersTimer = new Timer(5000, e -> updatePendingOrdersCount());
        pendingOrdersTimer.start();

        // Initial update
        updatePendingOrdersCount();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == view.getBtnScan()) {
            processBarcode();
        } else if (e.getSource() == view.getBtnProcessPayment()) {
            processSale();
        } else if (e.getSource() == view.getBtnClearCart()) {
            clearCart();
        } else if (e.getSource() == view.getBtnLogout()) {
            handleLogout();
        } else if (e.getSource() == view.getBtnAvailableOrders()) {
            showAvailableOrders();
        }
    }

    private void processBarcode() {
        String barcode = view.getBarcodeField().getText().trim();
        try {
            if (barcode.isEmpty()) {
                return; // Remove error message for empty barcode
            }

            Orders currentOrder = view.getCurrentOrder();
            if (currentOrder == null) {
                showError("Please claim an order first");
                return;
            }

            Product product = dataAdapter.loadProductByBarcode(barcode);
            boolean productFound = false;
            double runningTotal = 0;

            // Update table and calculate running total
            for (int i = 0; i < view.getCartTableModel().getRowCount(); i++) {
                int productId = (Integer) view.getCartTableModel().getValueAt(i, 0);
                if (product != null && product.getProductID() == productId) {
                    OrderLine line = currentOrder.getLines().get(i);
                    if (!line.isScanned()) {
                        line.setScanned(true);
                        double lineTotal = line.getCost();
                        double lineTax = lineTotal * 0.1;
                        double lineTotalWithTax = lineTotal + lineTax;
                        view.getCartTableModel().setValueAt(
                                String.format("$%.2f", lineTotalWithTax), i, 4);
                        view.getCartTableModel().setValueAt("✓", i, 5);
                        productFound = true;
                    }
                }
                // Add to running total if item is scanned
                String costStr = ((String)view.getCartTableModel().getValueAt(i, 4))
                        .replace("$", "").replace(",", "");
                if (!costStr.equals("-")) {  // Changed from "0.00" to "-"
                    runningTotal += Double.parseDouble(costStr) / 1.1; // Remove tax to get base total
                }
            }

            if (!productFound) {
                showError("Invalid product or already scanned");
                return;
            }

            // Update totals with proper tax calculation
            double tax = runningTotal * 0.1;
            view.updateTotals(runningTotal, tax, runningTotal + tax);

            view.getBarcodeField().setText("");
            view.getBarcodeField().requestFocus();
            view.getCartTable().clearSelection();
            checkAllProductsScanned();

        } catch (SQLException ex) {
            showError("Error scanning product: " + ex.getMessage());
        }
    }

    private void handleLogout() {
        if (!view.isVisible()) {
            return; // Prevent multiple logout attempts
        }

        int confirm = JOptionPane.showConfirmDialog(view,
                "Are you sure you want to logout?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            Orders currentOrder = view.getCurrentOrder();
            if (currentOrder != null) {
                try {
                    String query = """
                    UPDATE Orders 
                    SET Status = 'PENDING',
                        CashierID = NULL,
                        AssignedTime = NULL 
                    WHERE OrderID = ? 
                    AND Status = 'PROCESSING'
                """;

                    try (PreparedStatement stmt = dataAdapter.getConnection().prepareStatement(query)) {
                        stmt.setInt(1, currentOrder.getOrderID());
                        stmt.executeUpdate();
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(view,
                            "Error releasing order: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            cleanup();
            view.setVisible(false);
            Application.getInstance().setCurrentUser(null);
        }
    }

    private void releaseOrder(int orderId) {
        try {
            String query = "UPDATE Orders SET Status = 'PENDING', CashierID = NULL, AssignedTime = NULL " +
                    "WHERE OrderID = ? AND Status = 'PROCESSING' AND CashierID = ?";
            try (PreparedStatement stmt = dataAdapter.getConnection().prepareStatement(query)) {
                stmt.setInt(1, orderId);
                stmt.setInt(2, Application.getInstance().getCurrentUser().getUserID());
                stmt.executeUpdate();
            }
        } catch (SQLException ex) {
            System.err.println("Error releasing order: " + ex.getMessage());
        }
    }

    private void updateDisplayedOrder() {
        Orders currentOrder = view.getCurrentOrder();
        if (currentOrder == null) return;

        DefaultTableModel model = view.getCartTableModel();
        model.setRowCount(0);

        double subtotal = 0;
        for (OrderLine line : currentOrder.getLines()) {
            Product product = dataAdapter.loadProduct(line.getProductID());
            if (product != null) {
                double lineTotal = line.getCost();
                double lineTax = lineTotal * 0.1;
                double totalWithTax = lineTotal + lineTax;

                model.addRow(new Object[]{
                        product.getProductID(),
                        product.getName(),
                        String.format("$%.2f", product.getPrice()),
                        line.getQuantity(),
                        String.format("$%.2f", totalWithTax),
                        line.isScanned() ? "✓" : ""
                });
                subtotal += lineTotal;
            }
        }

        double tax = subtotal * 0.1;
        view.updateTotals(subtotal, tax, subtotal + tax);
    }

    private void checkAllProductsScanned() {
        Orders currentOrder = view.getCurrentOrder();
        if (currentOrder == null) return;

        boolean allScanned = true;
        for (OrderLine line : currentOrder.getLines()) {
            if (!line.isScanned()) {
                allScanned = false;
                break;
            }
        }

        view.getBtnProcessPayment().setEnabled(allScanned);
    }

    private void processSale() {
        Orders currentOrder = view.getCurrentOrder();
        if (currentOrder == null) {
            // Remove this error message since it appears after successful order completion
            return;
        }

        try {
            if (dataAdapter.completeOrder(currentOrder.getOrderID(), currentOrder.getPaymentMethod())) {
                JOptionPane.showMessageDialog(view, "Order completed successfully!");
                printReceipt(currentOrder);
                // Add this to properly reset view state after completion
                view.setCurrentOrder(null);  // Add this setter in CashierView
                clearCart();
            } else {
                showError("Failed to complete order");
            }
        } catch (SQLException ex) {
            showError("Error processing sale: " + ex.getMessage());
        }
    }

    private void showAvailableOrders() {
        // Check if any AvailableOrdersView is already open
        for (Window window : Window.getWindows()) {
            if (window instanceof AvailableOrdersView && window.isVisible()) {
                window.requestFocus(); // Bring existing window to front
                return; // Don't open another window
            }
        }

        AvailableOrdersView ordersView = new AvailableOrdersView(view);

        ordersView.getBtnClaim().addActionListener(e -> {
            int selectedRow = ordersView.getOrderTable().getSelectedRow();
            if (selectedRow >= 0) {
                int orderId = (Integer) ordersView.getOrderTable().getValueAt(selectedRow, 0);
                claimOrder(orderId);
                ordersView.dispose(); // Ensure window closes after claiming
            } else {
                JOptionPane.showMessageDialog(ordersView, "Please select an order to claim");
            }
        });

        ordersView.getBtnClose().addActionListener(e -> ordersView.dispose());
        ordersView.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent windowEvent) {
                ordersView.dispose(); // Ensure proper cleanup
            }
        });

        ordersView.loadPendingOrders();
        ordersView.setVisible(true);
    }

    private void claimOrder(int orderId) {
        try {
            System.out.println("Attempting to claim order: " + orderId); // Trace log

            if (dataAdapter.claimOrder(orderId, Application.getInstance().getCurrentUser().getUserID())) {
                Orders order = dataAdapter.loadOrder(orderId);
                view.loadOrder(order);
                updatePendingOrdersCount();

                // Close ALL AvailableOrdersView windows forcefully
                SwingUtilities.invokeLater(() -> {
                    System.out.println("Finding and closing AvailableOrdersView windows"); // Trace log
                    Window[] windows = Window.getWindows();
                    for (Window window : windows) {
                        if (window instanceof AvailableOrdersView) {
                            System.out.println("Found AvailableOrdersView window - disposing"); // Trace log
                            window.setVisible(false);
                            window.dispose();
                        }
                    }
                });

            } else {
                JOptionPane.showMessageDialog(view, "Failed to claim order. It may have been claimed by another cashier.");
            }
        } catch (SQLException ex) {
            System.err.println("Error in claimOrder: " + ex.getMessage()); // Error log
            JOptionPane.showMessageDialog(view, "Error claiming order: " + ex.getMessage());
        }
    }
    private void clearCart() {
        Orders currentOrder = view.getCurrentOrder();

        if (currentOrder != null) {
            try {
                // Only revert to pending if the order wasn't completed
                if (!currentOrder.getStatus().equals("COMPLETED")) {
                    String query = "UPDATE Orders SET Status = 'PENDING', CashierID = NULL, AssignedTime = NULL WHERE OrderID = ? AND Status = 'PROCESSING'";

                    try (PreparedStatement stmt = dataAdapter.getConnection().prepareStatement(query)) {
                        stmt.setInt(1, currentOrder.getOrderID());
                        stmt.executeUpdate();
                    }
                }
                updatePendingOrdersCount();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(view,
                        "Error clearing cart: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }

        // Clear view state
        view.clearCart();
        view.getBarcodeField().setText("");
        view.getBarcodeField().requestFocus();
        view.getBtnProcessPayment().setEnabled(false);
        view.getBtnScan().setEnabled(false);
    }

    private void updatePendingOrdersCount() {
        try {
            int count = dataAdapter.getPendingOrdersCount();
            view.updatePendingOrderCount(count);
        } catch (SQLException ex) {
            System.err.println("Error updating pending orders count: " + ex.getMessage());
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(view,
                message,
                "Error",
                JOptionPane.ERROR_MESSAGE);
    }

    private void printReceipt(Orders order) {
        StringBuilder receipt = new StringBuilder();
        receipt.append("=== STORE RECEIPT ===\n\n");
        receipt.append("Date: ").append(order.getDate()).append("\n");
        receipt.append("Order ID: ").append(order.getOrderID()).append("\n");
        receipt.append("Cashier: ").append(Application.getInstance().getCurrentUser().getFullName()).append("\n\n");
        receipt.append("Items:\n");
        receipt.append("----------------------------------------\n");

        for (OrderLine line : order.getLines()) {
            Product product = dataAdapter.loadProduct(line.getProductID());
            if (product != null) {
                receipt.append(String.format("%-30s\n", product.getName()));
                receipt.append(String.format("  %.0f @ $%.2f = $%.2f\n",
                        line.getQuantity(),
                        line.getUnitPrice(),
                        line.getCost()));
            }
        }

        receipt.append("----------------------------------------\n");
        receipt.append(String.format("Subtotal: $%.2f\n", order.getTotalCost() - order.getTotalTax()));
        receipt.append(String.format("Tax (10%%): $%.2f\n", order.getTotalTax()));
        receipt.append(String.format("Total: $%.2f\n\n", order.getTotalCost()));
        receipt.append("Payment Method: ").append(order.getPaymentMethod()).append("\n\n");
        receipt.append("Thank you for shopping with us!\n");

        JTextArea textArea = new JTextArea(receipt.toString());
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(400, 500));

        JOptionPane.showMessageDialog(view,
                scrollPane,
                "Receipt",
                JOptionPane.INFORMATION_MESSAGE);
    }

    public void cleanup() {
        if (pendingOrdersTimer != null) {
            pendingOrdersTimer.stop();
        }
    }
}