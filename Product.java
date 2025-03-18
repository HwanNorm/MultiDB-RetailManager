public class Product {
    private int productID;

    private int sellerID;
    private String name;
    private double price;
    private double quantity;
    private String barcode;
    private String category;
    private String description;

    public double getPrice() {
        return price;
    }
    public String getCategory() {
        return category;
    }
    public String getDescription() {
        return description;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getProductID() {
        return productID;
    }

    public void setProductID(int productID) {
        this.productID = productID;
    }

    public String getName() {
        return name;
    }

    public String getBarcode() { return barcode; }

    public void setName(String name) {
        this.name = name;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public int getSellerID() {
        return sellerID;
    }

    public void setSellerID(int sellerID) {
        this.sellerID = sellerID;
    }
    public void setBarcode(String barcode) { this.barcode = barcode; }
    public void setCategory(String category) {
        this.category = category;
    }
    public void setDescription(String description) {
        this.description = description;
    }
}
