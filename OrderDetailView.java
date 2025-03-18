import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class OrderDetailView extends JDialog {
    private DefaultTableModel items;
    private JTable tblItems;
    private JLabel labDate;
    private JLabel labStatus;
    private JLabel labSubtotal;
    private JLabel labTax;
    private JLabel labTotal;

    // Add new constructor that accepts Window as parent (can be either JFrame or JDialog)
    public OrderDetailView(Window parent) {
        // Make the dialog modal and set its parent
        super(parent, "Order Details", ModalityType.APPLICATION_MODAL);

        this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
        this.setSize(500, 450);

        // Set default close operation
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        // Initialize components
        initializeComponents();

        // Center on parent
        this.setLocationRelativeTo(parent);
    }

    // Keep the old constructor for backward compatibility
    public OrderDetailView(JDialog parent) {
        this((Window) parent);
    }

    private void initializeComponents() {
        // Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        labDate = new JLabel("Date: ");
        labStatus = new JLabel("Status: ");
        headerPanel.add(labDate);
        headerPanel.add(Box.createHorizontalStrut(20));
        headerPanel.add(labStatus);
        this.getContentPane().add(headerPanel);

        // Initialize Table Model and Table
        items = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Add columns to table model
        items.addColumn("Product ID");
        items.addColumn("Name");
        items.addColumn("Price");
        items.addColumn("Quantity");
        items.addColumn("Cost");

        tblItems = new JTable(items);
        JScrollPane scrollPane = new JScrollPane(tblItems);
        scrollPane.setPreferredSize(new Dimension(480, 300));
        this.getContentPane().add(scrollPane);

        // Cost Breakdown Panel
        JPanel costPanel = new JPanel();
        costPanel.setLayout(new BoxLayout(costPanel, BoxLayout.Y_AXIS));
        costPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create a panel for cost details with right alignment
        JPanel costDetailsPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        costDetailsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Cost Summary"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        // Initialize cost labels with fonts
        Font regularFont = new Font("Arial", Font.PLAIN, 14);
        Font boldFont = new Font("Arial", Font.BOLD, 14);

        labSubtotal = new JLabel("Subtotal: $0.00", SwingConstants.RIGHT);
        labSubtotal.setFont(regularFont);

        labTax = new JLabel("Tax (10%): $0.00", SwingConstants.RIGHT);
        labTax.setFont(regularFont);

        labTotal = new JLabel("Total: $0.00", SwingConstants.RIGHT);
        labTotal.setFont(boldFont);

        costDetailsPanel.add(labSubtotal);
        costDetailsPanel.add(labTax);
        costDetailsPanel.add(labTotal);

        // Wrap cost details in alignment panel
        JPanel alignmentPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        alignmentPanel.add(costDetailsPanel);
        costPanel.add(alignmentPanel);

        this.getContentPane().add(costPanel);

        // Button Panel
        JPanel buttonPanel = new JPanel();
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> this.dispose());
        buttonPanel.add(closeButton);
        this.getContentPane().add(buttonPanel);
    }

    public void displayOrderDetails(Orders order, DataAdapter dataAdapter) {
        // Clear existing items
        while (items.getRowCount() > 0) {
            items.removeRow(0);
        }

        // Set order info
        labDate.setText("Date: " + order.getDate());
        labStatus.setText("Status: " + order.getStatus());

        // Calculate subtotal from order lines
        double subtotal = 0.0;
        for(OrderLine line : order.getLines()) {
            Product product = line.getProduct(dataAdapter);
            if (product != null) {
                subtotal += line.getCost();
                displayProductInfo(product, line.getQuantity(), line.getCost());
            }
        }

        // Get tax and total from the order
        double tax = order.getTotalTax();  // This should now be properly loaded from the database
        double total = order.getTotalCost();

        System.out.println("Displaying order details:");
        System.out.println("Subtotal: $" + subtotal);
        System.out.println("Tax: $" + tax);
        System.out.println("Total: $" + total);

        // Update cost labels with properly formatted values
        labSubtotal.setText(String.format("Subtotal: $%,.2f", subtotal));
        labTax.setText(String.format("Tax (10%%): $%,.2f", tax));
        labTotal.setText(String.format("Total: $%,.2f", total));

        // Adjust column widths
        tblItems.getColumnModel().getColumn(0).setPreferredWidth(70);  // Product ID
        tblItems.getColumnModel().getColumn(1).setPreferredWidth(200); // Name
        tblItems.getColumnModel().getColumn(2).setPreferredWidth(80);  // Price
        tblItems.getColumnModel().getColumn(3).setPreferredWidth(70);  // Quantity
        tblItems.getColumnModel().getColumn(4).setPreferredWidth(80);  // Cost

        // Update UI
        this.revalidate();
        this.repaint();
    }

    private void displayProductInfo(Product product, double quantity, double cost) {
        Object[] row = new Object[5];
        row[0] = product.getProductID();
        row[1] = product.getName();
        row[2] = String.format("$%,.2f", product.getPrice());
        row[3] = quantity;
        row[4] = String.format("$%,.2f", cost);
        items.addRow(row);
    }
}