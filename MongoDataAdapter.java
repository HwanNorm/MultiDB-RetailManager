import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

public class MongoDataAdapter {
    private static MongoDataAdapter instance;
    private MongoClient mongoClient;
    private MongoDatabase database;
    private String databaseName;

    private MongoDataAdapter(String databaseName) {
        this.databaseName = databaseName;
        try {
            mongoClient = MongoClients.create("mongodb://localhost:27017");
            database = mongoClient.getDatabase(databaseName);
            System.out.println("Connected to MongoDB database: " + databaseName);
        } catch (Exception e) {
            System.err.println("MongoDB Connection Error: " + e.getMessage());
            throw e;
        }
    }

    public static void initialize(String databaseName) {
        if (instance == null) {
            instance = new MongoDataAdapter(databaseName);
        } else if (!instance.databaseName.equals(databaseName)) {
            instance.cleanup();
            instance = new MongoDataAdapter(databaseName);
        }
    }

    public static MongoDataAdapter getInstance() {
        if (instance == null) {
            throw new IllegalStateException("MongoDataAdapter not initialized. Call initialize() first.");
        }
        return instance;
    }

    // Customer Methods
    public List<Document> getAllCustomers() {
        List<Document> customers = new ArrayList<>();
        try {
            database.getCollection("customers").find()
                    .sort(new Document("memberSince", -1))
                    .into(customers);
            return customers;
        } catch (Exception e) {
            System.err.println("Error retrieving customers: " + e.getMessage());
            throw e;
        }
    }

    public Document getCustomerByMySQLId(int mysqlUserId) {
        try {
            return database.getCollection("customers")
                    .find(Filters.eq("mysqlUserId", mysqlUserId))
                    .first();
        } catch (Exception e) {
            System.err.println("Error retrieving customer: " + e.getMessage());
            throw e;
        }
    }

    public void saveCustomer(Document customerDoc) {
        try {
            database.getCollection("customers").insertOne(customerDoc);
        } catch (Exception e) {
            System.err.println("Error saving customer: " + e.getMessage());
            throw e;
        }
    }

    public void updateCustomer(int mysqlUserId, Document updateDoc) {
        try {
            database.getCollection("customers")
                    .updateOne(
                            Filters.eq("mysqlUserId", mysqlUserId),
                            new Document("$set", updateDoc)
                    );
        } catch (Exception e) {
            System.err.println("Error updating customer: " + e.getMessage());
            throw e;
        }
    }

    // Order Methods
    public List<Document> getAllOrders() {
        List<Document> orders = new ArrayList<>();
        try {
            database.getCollection("orders")
                    .find()
                    .sort(new Document("timeline.ordered", -1))
                    .into(orders);
            return orders;
        } catch (Exception e) {
            System.err.println("Error retrieving orders: " + e.getMessage());
            throw e;
        }
    }

    public Document getOrderByMySQLId(int mysqlOrderId) {
        try {
            return database.getCollection("orders")
                    .find(Filters.eq("mysqlOrderId", mysqlOrderId))
                    .first();
        } catch (Exception e) {
            System.err.println("Error retrieving order: " + e.getMessage());
            throw e;
        }
    }

    public List<Document> getCustomerOrders(int customerId) {
        List<Document> orders = new ArrayList<>();
        try {
            database.getCollection("orders")
                    .find(Filters.eq("customerId", customerId))
                    .sort(new Document("timeline.ordered", -1))
                    .into(orders);
            return orders;
        } catch (Exception e) {
            System.err.println("Error retrieving customer orders: " + e.getMessage());
            throw e;
        }
    }

    public void saveOrder(Document orderDoc) {
        try {
            database.getCollection("orders").insertOne(orderDoc);
        } catch (Exception e) {
            System.err.println("Error saving order: " + e.getMessage());
            throw e;
        }
    }

    public void updateOrder(int mysqlOrderId, Document updateDoc) {
        try {
            database.getCollection("orders")
                    .updateOne(
                            Filters.eq("mysqlOrderId", mysqlOrderId),
                            new Document("$set", updateDoc)
                    );
        } catch (Exception e) {
            System.err.println("Error updating order: " + e.getMessage());
            throw e;
        }
    }

    // Review Methods
    public List<Document> getAllReviews() {
        List<Document> reviews = new ArrayList<>();
        try {
            database.getCollection("reviews")
                    .find()
                    .sort(new Document("reviewDate", -1))
                    .into(reviews);
            return reviews;
        } catch (Exception e) {
            System.err.println("Error retrieving reviews: " + e.getMessage());
            throw e;
        }
    }

    public List<Document> getProductReviews(int productId) {
        List<Document> reviews = new ArrayList<>();
        try {
            database.getCollection("reviews")
                    .find(Filters.eq("productId", productId))
                    .sort(new Document("reviewDate", -1))
                    .into(reviews);
            return reviews;
        } catch (Exception e) {
            System.err.println("Error retrieving product reviews: " + e.getMessage());
            throw e;
        }
    }

    public List<Document> getCustomerReviews(int mysqlCustomerId) {
        List<Document> reviews = new ArrayList<>();
        try {
            database.getCollection("reviews")
                    .find(Filters.eq("mysqlCustomerId", mysqlCustomerId))
                    .sort(new Document("reviewDate", -1))
                    .into(reviews);
            return reviews;
        } catch (Exception e) {
            System.err.println("Error retrieving customer reviews: " + e.getMessage());
            throw e;
        }
    }

    public void saveReview(Document reviewDoc) {
        try {
            database.getCollection("reviews").insertOne(reviewDoc);
        } catch (Exception e) {
            System.err.println("Error saving review: " + e.getMessage());
            throw e;
        }
    }

    public void updateReview(Document query, Document updateDoc) {
        try {
            database.getCollection("reviews")
                    .updateOne(query, new Document("$set", updateDoc));
        } catch (Exception e) {
            System.err.println("Error updating review: " + e.getMessage());
            throw e;
        }
    }

    // Analytics Methods
    public Document getOrderAnalytics() {
        try {
            List<Document> pipeline = new ArrayList<>();
            pipeline.add(new Document("$group", new Document()
                    .append("_id", null)
                    .append("totalOrders", new Document("$sum", 1))
                    .append("avgProcessingTime", new Document("$avg", "$processingTime"))
                    .append("totalRevenue", new Document("$sum", "$orderDetails.totalAmount"))
            ));

            return database.getCollection("orders")
                    .aggregate(pipeline)
                    .first();
        } catch (Exception e) {
            System.err.println("Error generating order analytics: " + e.getMessage());
            throw e;
        }
    }

    public Document getReviewAnalytics() {
        try {
            List<Document> pipeline = new ArrayList<>();
            pipeline.add(new Document("$group", new Document()
                    .append("_id", "$productId")
                    .append("avgRating", new Document("$avg", "$rating"))
                    .append("totalReviews", new Document("$sum", 1))
                    .append("helpfulVotes", new Document("$sum", "$helpfulVotes"))
            ));

            return database.getCollection("reviews")
                    .aggregate(pipeline)
                    .first();
        } catch (Exception e) {
            System.err.println("Error generating review analytics: " + e.getMessage());
            throw e;
        }
    }

    // Utility Methods
    public void cleanup() {
        if (mongoClient != null) {
            try {
                mongoClient.close();
                System.out.println("MongoDB connection closed successfully");
            } catch (Exception e) {
                System.err.println("Error closing MongoDB connection: " + e.getMessage());
            }
        }
    }

    public boolean isConnected() {
        try {
            database.runCommand(new Document("ping", 1));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void initializeCollections() {
        try {
            // Create collections if they don't exist
            if (!collectionExists("customers")) {
                database.createCollection("customers");
            }
            if (!collectionExists("orders")) {
                database.createCollection("orders");
            }
            if (!collectionExists("reviews")) {
                database.createCollection("reviews");
            }
            System.out.println("MongoDB collections initialized successfully");
        } catch (Exception e) {
            System.err.println("Error initializing MongoDB collections: " + e.getMessage());
            throw e;
        }
    }

    private boolean collectionExists(String collectionName) {
        for (String name : database.listCollectionNames()) {
            if (name.equals(collectionName)) {
                return true;
            }
        }
        return false;
    }
}