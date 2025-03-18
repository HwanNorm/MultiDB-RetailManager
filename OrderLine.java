public class OrderLine {
    private Product product;
    private int productID;
    private int orderID;
    private double quantity;
    private double cost;
    private double unitPrice;
    private boolean scanned = false;  // Add this field

    // Add these methods
    public void setScanned(boolean scanned) {
        this.scanned = scanned;
    }

    public boolean isScanned() {
        return scanned;
    }

    // Existing methods remain the same
    public Product getProduct(DataAdapter dataAdapter) {
        if (product == null) {
            product = dataAdapter.loadProduct(this.productID);
        }
        return product;
    }

    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }

    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }

    public double getCost() {     System.out.println("OrderLine getCost() called - cost: " + cost);
        return cost;  }
    public void setCost(double cost) { this.cost = cost; }

    public int getOrderID() { return orderID; }
    public void setOrderID(int orderID) { this.orderID = orderID; }

    public int getProductID() { return productID; }
    public void setProductID(int productID) { this.productID = productID; }

}
