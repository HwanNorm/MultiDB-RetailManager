import java.util.List;
import java.util.ArrayList;

public class Orders {
    private int orderID;
    private int customerID;    // Changed from buyerID
    private int cashierID;     // New field
    private PaymentMethod paymentMethod;  // New field
    private double totalCost;
    private double totalTax;
    private String date;
    private String status;
    private List<OrderLine> lines;

    public Orders() {
        lines = new ArrayList<>();
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }

    public double getTotalTax() {
        return totalTax;
    }

    public void setTotalTax(double totalTax) {
        this.totalTax = totalTax;
    }

    public int getOrderID() {
        return orderID;
    }

    public void setOrderID(int orderID) {
        this.orderID = orderID;
    }

    public int getCustomerID() {
        return customerID;
    }

    public void setCustomerID(int customerID) {
        this.customerID = customerID;
    }

    public int getCashierID() {
        return cashierID;
    }

    public void setCashierID(int cashierID) {
        this.cashierID = cashierID;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public void addLine(OrderLine line) {
        lines.add(line);
    }

    public void removeLine(OrderLine line) {
        lines.remove(line);
    }

    public List<OrderLine> getLines() {
        return lines;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Helper method to calculate subtotal (before tax)
    public double getSubtotal() {
        double subtotal = 0.0;
        for (OrderLine line : lines) {
            subtotal += line.getCost();
        }
        return subtotal;
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderID=" + orderID +
                ", customerID=" + customerID +
                ", cashierID=" + cashierID +
                ", paymentMethod=" + paymentMethod +
                ", totalCost=" + totalCost +
                ", totalTax=" + totalTax +
                ", date='" + date + '\'' +
                ", status='" + status + '\'' +
                ", lines=" + lines.size() +
                '}';
    }
}