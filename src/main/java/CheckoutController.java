import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CheckoutController implements ActionListener {
    private CheckoutView view;
    private DataAdapter dataAdapter;
    private Orders order;

    public CheckoutController(CheckoutView view, DataAdapter dataAdapter) {
        this.dataAdapter = dataAdapter;
        this.view = view;

        view.getBtnAdd().addActionListener(this);
        view.getBtnPay().addActionListener(this);
        view.getBtnCancel().addActionListener(this);

        // Initialize new order
        this.order = new Orders();
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == view.getBtnAdd()) {
            // Store current order items before returning to main screen
            storeCurrentOrderItems();
            Application.getInstance().getMainScreen().setVisible(true);
            view.setVisible(false);
        } else if (e.getSource() == view.getBtnPay()) {
            makeOrder();
        } else if (e.getSource() == view.getBtnCancel()) {
            cancelOrder();
        }
    }

    private void storeCurrentOrderItems() {
        // Clear existing order lines to prevent duplicates
        order.getLines().clear();

        // Store all items from the view table into the order
        for (int i = 0; i < view.getItems().getRowCount(); i++) {
            int productId = (Integer) view.getItems().getValueAt(i, 0);
            double quantity = (Double) view.getItems().getValueAt(i, 3);
            String costStr = ((String) view.getItems().getValueAt(i, 4)).replace("$", "").replace(",", "");
            double cost = Double.parseDouble(costStr);

            OrderLine line = new OrderLine();
            line.setOrderID(order.getOrderID());
            line.setProductID(productId);
            line.setQuantity(quantity);
            line.setCost(cost);
            order.getLines().add(line);
        }
    }

    private void makeOrder() {
        try {
            System.out.println("Starting makeOrder()");
            if (order.getLines().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Cannot create empty order!");
                return;
            }

            PaymentMethod[] methods = PaymentMethod.values();
            PaymentMethod selectedMethod = (PaymentMethod) JOptionPane.showInputDialog(
                    view,
                    "Select Payment Method:",
                    "Payment Method",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    methods,
                    methods[0]
            );

            if (selectedMethod == null) {
                System.out.println("Payment method selection cancelled");
                return;
            }

            System.out.println("Selected payment method: " + selectedMethod);
            order.setPaymentMethod(selectedMethod);

            int confirmation = showOrderConfirmation();
            System.out.println("Confirmation result: " + confirmation);

            if (confirmation != JOptionPane.YES_OPTION) {
                System.out.println("Order not confirmed by user");
                return;
            }

            // Set customer and order info
            order.setCustomerID(Application.getInstance().getCurrentUser().getUserID());
            order.setDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));

            // Find and set a cashier for the order
            try (PreparedStatement stmt = dataAdapter.getConnection().prepareStatement(
                    "SELECT UserID FROM Users WHERE Role = 'CASHIER' LIMIT 1")) {
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    order.setCashierID(rs.getInt("UserID"));
                } else {
                    throw new SQLException("No cashier available in the system");
                }
            }

            // Calculate totals
            double subtotal = order.getSubtotal();
            double tax = subtotal * 0.1;
            order.setTotalTax(tax);
            order.setTotalCost(subtotal + tax);

            System.out.println("Order details set: " + order);

            if (dataAdapter.saveOrder(order)) {
                JOptionPane.showMessageDialog(view, "Order completed successfully!");
                resetOrder();
                view.setVisible(false);
                Application.getInstance().getMainScreen().setVisible(true);
            } else {
                JOptionPane.showMessageDialog(view, "Error saving order", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            System.err.println("Error in makeOrder(): " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(view, "Error processing order: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private int showOrderConfirmation() {
        StringBuilder message = new StringBuilder();
        message.append("Please confirm your order:\n\n");

        // Add each item in the order
        for (int i = 0; i < view.getItems().getRowCount(); i++) {
            String name = (String) view.getItems().getValueAt(i, 1);
            double quantity = (Double) view.getItems().getValueAt(i, 3);
            String cost = (String) view.getItems().getValueAt(i, 4);
            message.append(String.format("%s x %.2f = %s\n", name, quantity, cost));
        }

        // Add cost summary
        message.append("\n");
        message.append(view.getLabSubtotal().getText()).append("\n");
        message.append(view.getLabTax().getText()).append("\n");
        message.append(view.getLabTotal().getText()).append("\n\n");
        message.append("Do you want to proceed with this order?");

        return JOptionPane.showConfirmDialog(
                view,
                message.toString(),
                "Confirm Order",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
    }

    private void cancelOrder() {
        if (view.getItems().getRowCount() > 0) {
            int confirm = JOptionPane.showConfirmDialog(
                    view,
                    "Are you sure you want to cancel this order?",
                    "Cancel Order",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

            if (confirm == JOptionPane.YES_OPTION) {
                resetOrder();
                view.setVisible(false);
                // Show main screen again
                Application.getInstance().getMainScreen().setVisible(true);
            }
        } else {
            view.setVisible(false);
            // Show main screen again even if cart was empty
            Application.getInstance().getMainScreen().setVisible(true);
        }
    }

    private void resetOrder() {
        order = new Orders();
        view.clearOrder();
    }

    public void addProductToOrder(Product product, double quantity) {
        OrderLine line = new OrderLine();
        line.setOrderID(this.order.getOrderID());
        line.setProductID(product.getProductID());
        line.setQuantity(quantity);
        line.setCost(quantity * product.getPrice());
        order.getLines().add(line);

        // Also add to the view table
        Object[] row = new Object[5];
        row[0] = product.getProductID();
        row[1] = product.getName();
        row[2] = String.format("$%.2f", product.getPrice());
        row[3] = quantity;
        row[4] = String.format("$%.2f", line.getCost());

        view.addRow(row);

        // Restore any previously existing items
        restoreOrderItems();
    }
    private void restoreOrderItems() {
        // Update the view to show all items in the order
        view.getItems().setRowCount(0);
        for (OrderLine line : order.getLines()) {
            Product product = dataAdapter.loadProduct(line.getProductID());
            if (product != null) {
                Object[] row = new Object[5];
                row[0] = product.getProductID();
                row[1] = product.getName();
                row[2] = String.format("$%.2f", product.getPrice());
                row[3] = line.getQuantity();
                row[4] = String.format("$%.2f", line.getCost());
                view.addRow(row);
            }
        }
    }

}