import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.Vector;

public class AvailableOrdersView extends JDialog {
    private JTable orderTable;
    private DefaultTableModel tableModel;
    private JButton btnClaim;
    private JButton btnClose;

    public AvailableOrdersView(Window owner) {
        super(owner, "Available Orders", ModalityType.APPLICATION_MODAL);
        initializeComponents();

        // Force proper disposal when window is closed
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                System.out.println("AvailableOrdersView closing"); // Trace log
                dispose();
            }
        });
    }

    private void initializeComponents() {
        setSize(800, 500);
        setLayout(new BorderLayout(10, 10));

        tableModel = new DefaultTableModel(
                new String[]{"Order ID", "Customer", "Order Time", "Total Items", "Total Cost"},
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        orderTable = new JTable(tableModel);
        orderTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        orderTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    showOrderDetails();
                }
            }
        });

        add(new JScrollPane(orderTable), BorderLayout.CENTER);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnClaim = new JButton("Claim Order");
        btnClose = new JButton("Close");
        buttonsPanel.add(btnClaim);
        buttonsPanel.add(btnClose);

        add(buttonsPanel, BorderLayout.SOUTH);
        setLocationRelativeTo(getOwner());
    }

    private void showOrderDetails() {
        int selectedRow = orderTable.getSelectedRow();
        if (selectedRow >= 0) {
            int orderId = (Integer)orderTable.getValueAt(selectedRow, 0);
            OrderDetailView detailView = new OrderDetailView(this);
            Orders order = Application.getInstance().getDataAdapter().loadOrder(orderId);
            if (order != null) {
                detailView.displayOrderDetails(order, Application.getInstance().getDataAdapter());
                detailView.setVisible(true);
            }
        }
    }

    public void loadPendingOrders() {
        tableModel.setRowCount(0);
        try {
            Vector<Vector<Object>> orders = Application.getInstance()
                    .getDataAdapter().getPendingOrders();
            for (Vector<Object> order : orders) {
                tableModel.addRow(order);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading pending orders: " + e.getMessage());
        }
    }

    // Getters
    public JButton getBtnClaim() { return btnClaim; }
    public JButton getBtnClose() { return btnClose; }
    public JTable getOrderTable() { return orderTable; }
}