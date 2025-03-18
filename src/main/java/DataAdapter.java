import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

public class DataAdapter {
    private Connection connection;

    public DataAdapter(Connection connection) {
        this.connection = connection;
    }

    private PreparedStatement prepareStatement(String sql) throws SQLException {
        // Get connection from Application if current connection is closed
        if (connection == null || connection.isClosed()) {
            connection = Application.getConnection();
        }
        return connection.prepareStatement(sql);
    }

    // Helper method for preparing statements with generated keys
    private PreparedStatement prepareStatement(String sql, int flag) throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = Application.getConnection();
        }
        return connection.prepareStatement(sql, flag);
    }

    // User-related methods
    public Users loadUser(String username, String password) {
        try (PreparedStatement statement = prepareStatement(
                "SELECT * FROM Users WHERE UserName = ? AND Password = ?")) {
            statement.setString(1, username);
            statement.setString(2, password);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Users user = new Users();
                    user.setUserID(resultSet.getInt("UserID"));
                    user.setUsername(resultSet.getString("UserName"));
                    user.setPassword(resultSet.getString("Password"));
                    user.setFullName(resultSet.getString("DisplayName"));
                    user.setRole(Users.Role.valueOf(resultSet.getString("Role")));
                    return user;
                }
            }
        } catch (SQLException e) {
            System.out.println("Database access error!");
            e.printStackTrace();
        }
        return null;
    }

    public Users loadUserById(int userId) throws SQLException {
        try (PreparedStatement stmt = prepareStatement("SELECT * FROM Users WHERE UserID = ?")) {
            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Users user = new Users();
                    user.setUserID(rs.getInt("UserID"));
                    user.setUsername(rs.getString("UserName"));
                    user.setFullName(rs.getString("DisplayName"));
                    user.setRole(Users.Role.valueOf(rs.getString("Role")));
                    return user;
                }
            }
        }
        return null;
    }
    public void deleteProduct(int productId) throws SQLException {
        connection.setAutoCommit(false);
        try {
            // First delete related inventory logs
            String deleteLogsQuery = "DELETE FROM InventoryLog WHERE ProductID = ?";
            try (PreparedStatement stmt = prepareStatement(deleteLogsQuery)) {
                stmt.setInt(1, productId);
                stmt.executeUpdate();
            }

            // Then delete the product
            String deleteProductQuery = "DELETE FROM Products WHERE ProductID = ?";
            try (PreparedStatement stmt = prepareStatement(deleteProductQuery)) {
                stmt.setInt(1, productId);
                stmt.executeUpdate();
            }

            connection.commit();
        } catch (SQLException ex) {
            connection.rollback();
            throw ex;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    public Vector<Vector<Object>> getAllUsers() throws SQLException {
        Vector<Vector<Object>> userData = new Vector<>();
        String query = "SELECT UserID, UserName, DisplayName, Role FROM Users";

        try (PreparedStatement stmt = prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("UserID"));
                row.add(rs.getString("UserName"));
                row.add(rs.getString("DisplayName"));
                row.add(rs.getString("Role"));
                userData.add(row);
            }
        }
        return userData;
    }

    public void saveUser(Users user) throws SQLException {
        String query = "INSERT INTO Users (UserName, Password, DisplayName, Role) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = prepareStatement(query)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getFullName());
            stmt.setString(4, user.getRole().toString());
            stmt.executeUpdate();
        }
    }

    public void updateUser(Users user) throws SQLException {
        String query = "UPDATE Users SET UserName = ?, DisplayName = ?, Role = ? WHERE UserID = ?";
        try (PreparedStatement stmt = prepareStatement(query)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getFullName());
            stmt.setString(3, user.getRole().toString());
            stmt.setInt(4, user.getUserID());
            stmt.executeUpdate();
        }
    }

    public void deleteUser(int userId) throws SQLException {
        if (hasRelatedRecords(userId)) {
            throw new SQLException("Cannot delete user with existing orders or products");
        }

        String query = "DELETE FROM Users WHERE UserID = ?";
        try (PreparedStatement stmt = prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
    }

    private boolean hasRelatedRecords(int userId) throws SQLException {
// Check Orders
        try (PreparedStatement stmt = prepareStatement(
                "SELECT COUNT(*) FROM Orders WHERE CustomerID = ?")) {  // Changed from BuyerID
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return true;
                }
            }
        }

        // Check Products
//        try (PreparedStatement stmt = prepareStatement("SELECT COUNT(*) FROM Products WHERE SellerID = ?")) {
//            stmt.setInt(1, userId);
//            try (ResultSet rs = stmt.executeQuery()) {
//                if (rs.next() && rs.getInt(1) > 0) {
//                    return true;
//                }
//            }
//        }

        return false;
    }
    public Product loadProduct ( int id){
        try (PreparedStatement statement = prepareStatement(
                "SELECT * FROM Products WHERE ProductID = ?")) {
            statement.setInt(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    Product product = new Product();
                    product.setProductID(rs.getInt("ProductID"));
                    product.setName(rs.getString("Name"));
                    product.setPrice(rs.getDouble("Price"));
                    product.setQuantity(rs.getDouble("Quantity"));
                    product.setBarcode(rs.getString("Barcode"));
                    product.setDescription(rs.getString("Description"));
                    product.setCategory(rs.getString("Category"));
                    return product;
                }
            }
        } catch (SQLException e) {
            System.err.println("Database access error in loadProduct: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public Product loadProductByName (String name){
        try (PreparedStatement statement = prepareStatement("SELECT * FROM Products WHERE Name = ?")) {
            statement.setString(1, name);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Product product = new Product();
                    product.setProductID(resultSet.getInt("ProductID"));
                    product.setSellerID(resultSet.getInt("SellerID"));
                    product.setName(resultSet.getString("Name"));
                    product.setPrice(resultSet.getDouble("Price"));
                    product.setQuantity(resultSet.getDouble("Quantity"));
                    return product;
                }
            }
        } catch (SQLException e) {
            System.out.println("Database access error!");
            e.printStackTrace();
        }
        return null;
    }

    public boolean saveProduct (Product product){
        try {
            PreparedStatement statement;
            if (product.getProductID() == 0) {
                // New product
                statement = prepareStatement(
                        "INSERT INTO Products (Name, Description, Price, Quantity, Category, Barcode) " +
                                "VALUES (?, ?, ?, ?, ?, ?)"
                );
                statement.setString(1, product.getName());
                statement.setString(2, "");
                statement.setDouble(3, product.getPrice());
                statement.setDouble(4, product.getQuantity());
                statement.setString(5, product.getCategory());
                statement.setString(6, product.getBarcode());
            } else {
                // For existing products, don't update barcode
                statement = prepareStatement(
                        "UPDATE Products SET Name = ?, Price = ?, Quantity = ?, " +
                                "Category = ? WHERE ProductID = ?"
                );
                statement.setString(1, product.getName());
                statement.setDouble(2, product.getPrice());
                statement.setDouble(3, product.getQuantity());
                statement.setString(4, product.getCategory());
                statement.setInt(5, product.getProductID());
            }
            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Database access error!");
            e.printStackTrace();
            return false;
        }
    }

    public Vector<Vector<Object>> getSellerProducts ( int sellerId) throws SQLException {
        Vector<Vector<Object>> productData = new Vector<>();
        String query = """
                    SELECT ProductID, Name, Price, Quantity,
                        CASE 
                            WHEN Quantity = 0 THEN 'Out of Stock'
                            WHEN Quantity < 10 THEN 'Low Stock'
                            ELSE 'In Stock'
                        END as Status 
                    FROM Products 
                    WHERE SellerID = ?
                    ORDER BY ProductID
                    """;

        try (PreparedStatement stmt = prepareStatement(query)) {
            stmt.setInt(1, sellerId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Vector<Object> row = new Vector<>();
                    row.add(rs.getInt("ProductID"));
                    row.add(rs.getString("Name"));
                    row.add(String.format("$%.2f", rs.getDouble("Price")));
                    row.add(rs.getDouble("Quantity"));
                    row.add(rs.getString("Status"));
                    productData.add(row);
                }
            }
        }
        return productData;
    }
    public Orders loadOrder ( int id){
        Orders order = null;
        try {
            // Load the order details including tax
            try (PreparedStatement statement = prepareStatement("SELECT * FROM Orders WHERE OrderID = ?")) {
                statement.setInt(1, id);

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        order = new Orders();
                        order.setOrderID(resultSet.getInt("OrderID"));
                        order.setCustomerID(resultSet.getInt("CustomerID")); // Changed from setBuyerID
                        order.setCashierID(resultSet.getInt("CashierID"));
                        order.setTotalCost(resultSet.getDouble("TotalCost"));
                        order.setTotalTax(resultSet.getDouble("TotalTax"));
                        order.setDate(resultSet.getString("OrderDate"));
                        order.setStatus(resultSet.getString("Status"));
                        order.setPaymentMethod(PaymentMethod.valueOf(resultSet.getString("PaymentMethod")));
                    }
                }
            }

            // Load the order lines
            if (order != null) {
                loadOrderLines(order);
            }

        } catch (SQLException e) {
            System.out.println("Database access error!");
            e.printStackTrace();
        }
        return order;
    }

    private void loadOrderLines(Orders order) throws SQLException {
        try (PreparedStatement statement = prepareStatement("SELECT * FROM OrderLine WHERE OrderID = ?")) {
            statement.setInt(1, order.getOrderID());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    OrderLine line = new OrderLine();
                    line.setOrderID(resultSet.getInt("OrderID"));
                    line.setProductID(resultSet.getInt("ProductID"));
                    line.setQuantity(resultSet.getDouble("Quantity"));
                    line.setUnitPrice(resultSet.getDouble("UnitPrice"));
                    line.setCost(resultSet.getDouble("Cost"));
                    order.addLine(line);
                }
            }
        }
    }
    public boolean saveOrder (Orders order){
        try {
            String sql = "INSERT INTO Orders (CustomerID, CashierID, OrderDate, TotalCost, TotalTax, PaymentMethod, Status) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement statement = prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                statement.setInt(1, order.getCustomerID());  // Changed from getBuyerID
                statement.setInt(2, order.getCashierID());
                statement.setString(3, order.getDate());
                statement.setDouble(4, order.getTotalCost());
                statement.setDouble(5, order.getTotalTax());
                statement.setString(6, order.getPaymentMethod().toString());
                statement.setString(7, "PENDING");

                statement.executeUpdate();

                // Get the generated OrderID
                try (ResultSet rs = statement.getGeneratedKeys()) {
                    if (rs.next()) {
                        order.setOrderID(rs.getInt(1));
                    }
                }
            }

            // Save the OrderLines
            for (OrderLine line : order.getLines()) {
                try (PreparedStatement statement = prepareStatement(
                        "INSERT INTO OrderLine (OrderID, ProductID, Quantity, UnitPrice, Cost) VALUES (?, ?, ?, ?, ?)"
                )) {
                    statement.setInt(1, order.getOrderID());
                    statement.setInt(2, line.getProductID());
                    statement.setDouble(3, line.getQuantity());
                    statement.setDouble(4, line.getUnitPrice());
                    statement.setDouble(5, line.getCost());
                    statement.executeUpdate();
                }
            }
            return true;
        } catch (SQLException e) {
            System.out.println("Database access error!");
            e.printStackTrace();
            return false;
        }
    }

    public Vector<Vector<Object>> getBuyerOrderHistory ( int customerId) throws SQLException {
        Vector<Vector<Object>> orderHistory = new Vector<>();
        String query = """
                SELECT OrderID, OrderDate, TotalCost, Status 
                FROM Orders 
                WHERE CustomerID = ? 
                ORDER BY OrderDate DESC
                """;

        try (PreparedStatement stmt = prepareStatement(query)) {
            stmt.setInt(1, customerId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Vector<Object> row = new Vector<>();
                    row.add(rs.getInt("OrderID"));
                    row.add(rs.getTimestamp("OrderDate"));
                    row.add(String.format("$%.2f", rs.getDouble("TotalCost")));
                    row.add(rs.getString("Status"));
                    orderHistory.add(row);
                }
            }
        }
        return orderHistory;
    }

    public Vector<Vector<Object>> getSellerOrders ( int sellerId) throws SQLException {
        Vector<Vector<Object>> orderData = new Vector<>();
        String query = """
                    SELECT DISTINCT o.OrderID, p.Name, ol.Quantity, ol.Cost, o.Status, o.OrderDate
                    FROM Orders o
                    JOIN OrderLine ol ON o.OrderID = ol.OrderID
                    JOIN Products p ON ol.ProductID = p.ProductID
                    WHERE p.SellerID = ?
                    ORDER BY o.OrderDate DESC
                    """;

        try (PreparedStatement stmt = prepareStatement(query)) {
            stmt.setInt(1, sellerId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Vector<Object> row = new Vector<>();
                    row.add(rs.getInt("OrderID"));
                    row.add(rs.getString("Name"));
                    row.add(rs.getDouble("Quantity"));
                    row.add(String.format("$%.2f", rs.getDouble("Cost")));
                    row.add(rs.getString("Status"));
                    row.add(rs.getTimestamp("OrderDate"));
                    orderData.add(row);
                }
            }
        }
        return orderData;
    }

    public boolean updateOrderStatus ( int orderId, String newStatus) throws SQLException {
        try (PreparedStatement stmt = prepareStatement("UPDATE Orders SET Status = ? WHERE OrderID = ?")) {
            stmt.setString(1, newStatus);
            stmt.setInt(2, orderId);
            return stmt.executeUpdate() > 0;
        }
    }
    // Report-related methods
    public Vector<Vector<Object>> generateSalesReport (Date startDate, Date endDate) throws SQLException {
        Vector<Vector<Object>> reportData = new Vector<>();
        String query = """
            SELECT 
                DATE(o.OrderDate) as SaleDate,
                COUNT(DISTINCT o.OrderID) as TotalOrders,
                SUM(o.TotalCost) as TotalRevenue,
                (
                    SELECT p.Name
                    FROM OrderLine ol2
                    JOIN Products p ON ol2.ProductID = p.ProductID
                    WHERE DATE(o.OrderDate) = DATE(OrderDate)
                    GROUP BY p.ProductID
                    ORDER BY COUNT(*) DESC, SUM(ol2.Quantity) DESC
                    LIMIT 1
                ) as TopProduct
            FROM Orders o
            WHERE o.OrderDate BETWEEN ? AND ?
                AND o.Status != 'CANCELLED'
            GROUP BY DATE(o.OrderDate)
            ORDER BY SaleDate DESC
            """;

        try (PreparedStatement stmt = prepareStatement(query)) {
            // Add time to dates to include full day
            stmt.setTimestamp(1, new Timestamp(startDate.getTime()));
            Timestamp endTs = new Timestamp(endDate.getTime());
            endTs.setHours(23);
            endTs.setMinutes(59);
            endTs.setSeconds(59);
            stmt.setTimestamp(2, endTs);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Vector<Object> row = new Vector<>();
                    // Format date
                    Date saleDate = rs.getDate("SaleDate");
                    row.add(new SimpleDateFormat("yyyy-MM-dd").format(saleDate));

                    // Add order count
                    row.add(rs.getInt("TotalOrders"));

                    // Format revenue with currency
                    double revenue = rs.getDouble("TotalRevenue");
                    row.add(String.format("$%,.2f", revenue));

                    // Add top product
                    String topProduct = rs.getString("TopProduct");
                    row.add(topProduct != null ? topProduct : "N/A");

                    reportData.add(row);
                }
            }
        }
        return reportData;
    }

    public Vector<Vector<Object>> generateInventoryReport () throws SQLException {
        Vector<Vector<Object>> reportData = new Vector<>();
        String query = """
                    SELECT p.Name, p.Quantity as CurrentStock,
                        CASE 
                            WHEN p.Quantity < 10 THEN 'Low'
                            WHEN p.Quantity < 20 THEN 'Medium'
                            ELSE 'Good'
                        END as StockLevel,
                        u.DisplayName as SellerName
                    FROM Products p
                    JOIN Users u ON p.SellerID = u.UserID
                    ORDER BY p.Quantity ASC
                    """;

        try (PreparedStatement stmt = prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getString("Name"));
                row.add(rs.getDouble("CurrentStock"));
                row.add(rs.getString("StockLevel"));
                row.add(rs.getString("SellerName"));
                reportData.add(row);
            }
        }
        return reportData;
    }

    public Vector<Vector<Object>> getOrderDetails ( int orderId) throws SQLException {
        Vector<Vector<Object>> details = new Vector<>();
        String query = """
                    SELECT p.Name, ol.Quantity, p.Price, ol.Cost
                    FROM OrderLine ol
                    JOIN Products p ON ol.ProductID = p.ProductID
                    WHERE ol.OrderID = ?
                    """;

        try (PreparedStatement stmt = prepareStatement(query)) {
            stmt.setInt(1, orderId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Vector<Object> row = new Vector<>();
                    row.add(rs.getString("Name"));
                    row.add(rs.getDouble("Quantity"));
                    row.add(rs.getDouble("Price"));
                    row.add(rs.getDouble("Cost"));
                    details.add(row);
                }
            }
        }
        return details;
    }

    public Vector<Vector<Object>> getAllOrders () throws SQLException {
        Vector<Vector<Object>> orderData = new Vector<>();
        String query = """
        SELECT DISTINCT 
            o.OrderID,
            u.DisplayName as CustomerName,
            o.OrderDate,
            o.TotalCost,
            o.TotalTax,
            o.Status,
            (
                SELECT GROUP_CONCAT(p.Name SEPARATOR ', ')
                FROM OrderLine ol2
                JOIN Products p ON ol2.ProductID = p.ProductID
                WHERE ol2.OrderID = o.OrderID
            ) as Products,
            (
                SELECT SUM(ol2.Quantity)
                FROM OrderLine ol2
                WHERE ol2.OrderID = o.OrderID
            ) as TotalQuantity
        FROM Orders o
        JOIN Users u ON o.CustomerID = u.UserID
        LEFT JOIN OrderLine ol ON o.OrderID = ol.OrderID
        LEFT JOIN Products p ON ol.ProductID = p.ProductID
        GROUP BY o.OrderID
        ORDER BY o.OrderDate DESC
        """;

        try (PreparedStatement stmt = prepareStatement(query)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Vector<Object> row = new Vector<>();
                    row.add(rs.getInt("OrderID"));
                    row.add(rs.getString("CustomerName"));
                    row.add(rs.getTimestamp("OrderDate"));
                    row.add(String.format("$%,.2f", rs.getDouble("TotalCost")));
                    row.add(rs.getString("Status"));
                    row.add(rs.getString("Products"));
                    row.add(rs.getDouble("TotalQuantity"));
                    orderData.add(row);
                }
            }
        }
        return orderData;
    }

    // Utility methods
    public Connection getConnection () {
        return this.connection;
    }
    public boolean updateSellerOrderStatus(int orderId, String newStatus, int sellerId) throws SQLException {
        // First verify that the order contains products from this seller
        String verifyQuery = """
            SELECT COUNT(*) 
            FROM OrderLine ol 
            JOIN Products p ON ol.ProductID = p.ProductID 
            WHERE ol.OrderID = ? AND p.SellerID = ?
            """;

        try (PreparedStatement verifyStmt = prepareStatement(verifyQuery)) {
            verifyStmt.setInt(1, orderId);
            verifyStmt.setInt(2, sellerId);

            try (ResultSet rs = verifyStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    // Order belongs to seller, proceed with update
                    try (PreparedStatement updateStmt = prepareStatement(
                            "UPDATE Orders SET Status = ? WHERE OrderID = ?")) {
                        updateStmt.setString(1, newStatus);
                        updateStmt.setInt(2, orderId);
                        return updateStmt.executeUpdate() > 0;
                    }
                }
            }
        }
        return false;  // Order doesn't belong to this seller
    }
    public Product loadProductByBarcode(String barcode) throws SQLException {
        String query = "SELECT * FROM Products WHERE Barcode = ?";
        try (PreparedStatement stmt = prepareStatement(query)) {
            stmt.setString(1, barcode);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Product product = new Product();
                    product.setProductID(rs.getInt("ProductID"));
                    product.setName(rs.getString("Name"));
                    product.setPrice(rs.getDouble("Price"));
                    product.setQuantity(rs.getDouble("Quantity"));
                    return product;
                }
            }
        }
        return null;
    }

    public void updateProductQuantity(int productId, double newQuantity)
            throws SQLException {
        String query = "UPDATE Products SET Quantity = ? WHERE ProductID = ?";
        try (PreparedStatement stmt = prepareStatement(query)) {
            stmt.setDouble(1, newQuantity);
            stmt.setInt(2, productId);
            int result = stmt.executeUpdate();
            if (result != 1) {
                throw new SQLException("Failed to update product quantity");
            }
        }
    }

    public void addInventoryLog(int productId, double quantity, String type,
                                int userId) throws SQLException {
        String query = "INSERT INTO InventoryLog (ProductID, Quantity, Type, " +
                "UserID, LogDate) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";
        try (PreparedStatement stmt = prepareStatement(query)) {
            stmt.setInt(1, productId);
            stmt.setDouble(2, quantity);
            stmt.setString(3, type);
            stmt.setInt(4, userId);
            stmt.executeUpdate();
        }
    }
    public Vector<Vector<Object>> getPendingOrders() throws SQLException {
        Vector<Vector<Object>> orders = new Vector<>();
        String query = """
        SELECT o.OrderID, u.DisplayName, o.OrderDate, 
               COUNT(ol.ProductID) as ItemCount,
               o.TotalCost
        FROM Orders o
        JOIN Users u ON o.CustomerID = u.UserID
        LEFT JOIN OrderLine ol ON o.OrderID = ol.OrderID
        WHERE o.Status = 'PENDING'
        GROUP BY o.OrderID
        ORDER BY o.OrderDate ASC
    """;

        try (PreparedStatement stmt = prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("OrderID"));
                row.add(rs.getString("DisplayName"));
                row.add(rs.getTimestamp("OrderDate"));
                row.add(rs.getInt("ItemCount"));
                row.add(String.format("$%.2f", rs.getDouble("TotalCost")));
                orders.add(row);
            }
        }
        return orders;
    }

    public boolean claimOrder(int orderId, int cashierId) throws SQLException {
        String query = """
        UPDATE Orders 
        SET Status = 'PROCESSING',
            CashierID = ?,
            AssignedTime = CURRENT_TIMESTAMP
        WHERE OrderID = ? 
        AND Status = 'PENDING'
    """;

        try (PreparedStatement stmt = prepareStatement(query)) {
            stmt.setInt(1, cashierId);
            stmt.setInt(2, orderId);
            return stmt.executeUpdate() > 0;
        }
    }
    public int getPendingOrdersCount() throws SQLException {
        String query = "SELECT COUNT(*) FROM Orders WHERE Status = 'PENDING'";
        try (PreparedStatement stmt = prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public boolean completeOrder(int orderId, PaymentMethod paymentMethod) throws SQLException {
        String query = """
        UPDATE Orders 
        SET Status = 'COMPLETED',
            PaymentMethod = ?,
            CompletedTime = CURRENT_TIMESTAMP
        WHERE OrderID = ? 
        AND Status = 'PROCESSING'
    """;

        connection.setAutoCommit(false);
        try {
            // Update order status
            try (PreparedStatement stmt = prepareStatement(query)) {
                stmt.setString(1, paymentMethod.toString());
                stmt.setInt(2, orderId);
                if (stmt.executeUpdate() == 0) {
                    return false;
                }
            }

            // Update inventory
            Orders order = loadOrder(orderId);
            for (OrderLine line : order.getLines()) {
                Product product = loadProduct(line.getProductID());
                updateProductQuantity(line.getProductID(),
                        product.getQuantity() - line.getQuantity());
            }
            // Update Redis stats
            RedisDataAdapter redis = RedisDataAdapter.getInstance();
            redis.addRecentCustomer(order.getCustomerID(),
                    loadUserById(order.getCustomerID()).getFullName());

            for (OrderLine line : order.getLines()) {
                redis.incrementProductSales(line.getProductID(), line.getQuantity());
            }
            connection.commit();
            return true;
        } catch (SQLException ex) {
            connection.rollback();
            throw ex;
        } finally {
            connection.setAutoCommit(true);
        }
    }
}

