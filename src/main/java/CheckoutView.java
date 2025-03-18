import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class CheckoutView extends JFrame {

    private JButton btnAdd = new JButton("Add Another Item");
    private JButton btnPay = new JButton("Complete Order");
    private JButton btnCancel = new JButton("Cancel Order");

    private DefaultTableModel items = new DefaultTableModel();
    private JTable tblItems = new JTable(items);

    private JLabel labSubtotal = new JLabel("Subtotal: $0.00");
    private JLabel labTax = new JLabel("Tax (10%): $0.00");
    private JLabel labTotal = new JLabel("Total: $0.00");

    public CheckoutView() {
        this.setTitle("Checkout");
        this.setLayout(new BorderLayout(10, 10));
        this.setSize(800, 600);
        this.setMinimumSize(new Dimension(800, 600));

        // Set up table columns
        items.addColumn("Product ID");
        items.addColumn("Name");
        items.addColumn("Price");
        items.addColumn("Quantity");
        items.addColumn("Cost");

        // Create main panel with padding
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Create header
        JLabel headerLabel = new JLabel("Order Summary");
        headerLabel.setFont(new Font("Arial", Font.BOLD, 24));
        headerLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        mainPanel.add(headerLabel, BorderLayout.NORTH);

        // Create order panel
        JPanel orderPanel = new JPanel(new BorderLayout(10, 10));

        // Add table to a scroll pane with proper size
        JScrollPane scrollPane = new JScrollPane(tblItems);
        scrollPane.setPreferredSize(new Dimension(760, 350));
        orderPanel.add(scrollPane, BorderLayout.CENTER);

        // Create cost breakdown panel
        JPanel costPanel = createCostPanel();
        orderPanel.add(costPanel, BorderLayout.SOUTH);

        mainPanel.add(orderPanel, BorderLayout.CENTER);

        // Create button panel
        JPanel buttonPanel = createButtonPanel();
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        this.add(mainPanel);

        // Set up table properties
        setupTable();

        // Center on screen
        this.setLocationRelativeTo(null);
    }

    private JPanel createCostPanel() {
        JPanel costPanel = new JPanel();
        costPanel.setLayout(new BoxLayout(costPanel, BoxLayout.Y_AXIS));
        costPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create a panel for the cost breakdown with right alignment
        JPanel costBreakdown = new JPanel(new GridLayout(3, 1, 5, 5));
        costBreakdown.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Order Summary"),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // Set up fonts
        Font regularFont = new Font("Arial", Font.PLAIN, 16);
        Font boldFont = new Font("Arial", Font.BOLD, 18);

        // Configure labels with larger fonts
        labSubtotal.setFont(regularFont);
        labSubtotal.setHorizontalAlignment(SwingConstants.RIGHT);

        labTax.setFont(regularFont);
        labTax.setHorizontalAlignment(SwingConstants.RIGHT);

        labTotal.setFont(boldFont);
        labTotal.setHorizontalAlignment(SwingConstants.RIGHT);

        // Add labels to the cost breakdown
        costBreakdown.add(labSubtotal);
        costBreakdown.add(labTax);
        costBreakdown.add(labTotal);

        // Wrap cost breakdown in a panel for right alignment
        JPanel alignmentPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        alignmentPanel.add(costBreakdown);
        costPanel.add(alignmentPanel);

        return costPanel;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));

        // Style buttons
        btnAdd.setPreferredSize(new Dimension(150, 40));
        btnPay.setPreferredSize(new Dimension(150, 40));
        btnCancel.setPreferredSize(new Dimension(150, 40));

        // Set fonts
        Font buttonFont = new Font("Arial", Font.BOLD, 14);
        btnAdd.setFont(buttonFont);
        btnPay.setFont(buttonFont);
        btnCancel.setFont(buttonFont);

        // Add buttons with spacing
        buttonPanel.add(btnAdd);
        buttonPanel.add(btnPay);
        buttonPanel.add(btnCancel);

        return buttonPanel;
    }

    private void setupTable() {
        // Set column widths
        tblItems.getColumnModel().getColumn(0).setPreferredWidth(70);  // Product ID
        tblItems.getColumnModel().getColumn(1).setPreferredWidth(250); // Name
        tblItems.getColumnModel().getColumn(2).setPreferredWidth(100); // Price
        tblItems.getColumnModel().getColumn(3).setPreferredWidth(100); // Quantity
        tblItems.getColumnModel().getColumn(4).setPreferredWidth(100); // Cost

        // Center align all columns except the Name column
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < tblItems.getColumnCount(); i++) {
            if (i != 1) { // Skip the Name column
                tblItems.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }
        }

        // Make table read-only
        tblItems.setDefaultEditor(Object.class, null);

        // Enable row selection
        tblItems.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Set row height
        tblItems.setRowHeight(25);

        // Set table font
        tblItems.setFont(new Font("Arial", Font.PLAIN, 14));
        tblItems.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
    }

    public void updateCosts(double subtotal) {
        // Calculate tax and total
        double tax = subtotal * 0.1; // 10% tax rate
        double total = subtotal + tax;

        // Update labels with formatted values
        labSubtotal.setText(String.format("Subtotal: $%,.2f", subtotal));
        labTax.setText(String.format("Tax (10%%): $%,.2f", tax));
        labTotal.setText(String.format("Total: $%,.2f", total));
    }

    // Add cancel order functionality
    public void clearOrder() {
        items.setRowCount(0);
        updateCosts(0.0);
    }

    // Getters
    public JButton getBtnAdd() { return btnAdd; }
    public JButton getBtnPay() { return btnPay; }
    public JButton getBtnCancel() { return btnCancel; }
    public DefaultTableModel getItems() { return items; }
    public JLabel getLabSubtotal() { return labSubtotal; }
    public JLabel getLabTax() { return labTax; }
    public JLabel getLabTotal() { return labTotal; }

    public void addRow(Object[] row) {
        items.addRow(row);

        // Calculate new subtotal
        double subtotal = 0.0;
        for (int i = 0; i < items.getRowCount(); i++) {
            Object costObj = items.getValueAt(i, 4);
            if (costObj instanceof String) {
                // Remove "$" and "," from the string and parse
                String costStr = ((String) costObj).replace("$", "").replace(",", "");
                subtotal += Double.parseDouble(costStr);
            } else if (costObj instanceof Double) {
                subtotal += (Double) costObj;
            }
        }

        // Update all cost labels
        updateCosts(subtotal);
    }
}
