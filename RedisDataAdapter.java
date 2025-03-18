import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.resps.Tuple;


import java.time.Duration;
import java.util.*;


public class RedisDataAdapter {
    private static RedisDataAdapter instance;
    private JedisPool jedisPool;
    private static final String REDIS_HOST = "redis-11107.c323.us-east-1-2.ec2.redns.redis-cloud.com";
    private static final int REDIS_PORT = 11107;
    private static final String REDIS_PASSWORD = "fm92mhziczac";


    // Inner classes first
    public static class ProductSalesData {
        private final int productId;
        private final double quantity;


        public ProductSalesData(int productId, double quantity) {
            this.productId = productId;
            this.quantity = quantity;
        }


        public int getProductId() {
            return productId;
        }

        public double getQuantity() {
            return quantity;
        }
    }


    public static class RecentCustomerData {
        private final int customerId;
        private final String name;
        private final long lastVisit;


        public RecentCustomerData(int customerId, String name, long lastVisit) {
            this.customerId = customerId;
            this.name = name;
            this.lastVisit = lastVisit;
        }


        public int getCustomerId() {
            return customerId;
        }

        public String getName() {
            return name;
        }

        public long getLastVisit() {
            return lastVisit;
        }
    }


    private RedisDataAdapter() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(10);
        poolConfig.setMaxIdle(5);
        poolConfig.setMinIdle(1);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestWhileIdle(true);
        poolConfig.setMinEvictableIdleTimeMillis(Duration.ofSeconds(60).toMillis());
        poolConfig.setTimeBetweenEvictionRunsMillis(Duration.ofSeconds(30).toMillis());
        poolConfig.setNumTestsPerEvictionRun(3);
        poolConfig.setBlockWhenExhausted(true);


        jedisPool = new JedisPool(poolConfig, REDIS_HOST, REDIS_PORT, 2000, REDIS_PASSWORD);
    }


    public static RedisDataAdapter getInstance() {
        if (instance == null) {
            instance = new RedisDataAdapter();
        }
        return instance;
    }


    public boolean isConnected() {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.ping().equals("PONG");
        } catch (Exception e) {
            System.err.println("Redis connection error: " + e.getMessage());
            return false;
        }
    }


    public void cleanup() {
        if (jedisPool != null && !jedisPool.isClosed()) {
            jedisPool.close();
        }
    }


    // Method to get Jedis resource (for use in other methods)
    protected Jedis getResource() {
        return jedisPool.getResource();
    }

    // Redis operations methods
    public void incrementProductSales(int productId, double quantity) {
        try (Jedis jedis = getResource()) {
            jedis.zincrby("best_selling_products", quantity, String.valueOf(productId));
        }
    }


    public List<ProductSalesData> getBestSellingProducts(int limit) {
        try (Jedis jedis = getResource()) {
            List<ProductSalesData> result = new ArrayList<>();
            List<Tuple> scores = new ArrayList<>(jedis.zrevrangeWithScores("best_selling_products", 0, limit - 1));

            for (Tuple tuple : scores) {
                result.add(new ProductSalesData(
                        Integer.parseInt(tuple.getElement()),
                        tuple.getScore()
                ));
            }
            return result;
        }
    }


    public void addRecentCustomer(int customerId, String customerName) {
        try (Jedis jedis = getResource()) {
            Map<String, String> customerData = new HashMap<>();
            customerData.put("name", customerName);
            customerData.put("last_visit", String.valueOf(System.currentTimeMillis()));
            jedis.hset("customer:" + customerId, customerData);
            jedis.zadd("recent_customers", System.currentTimeMillis(), String.valueOf(customerId));
            jedis.zremrangeByRank("recent_customers", 0, -101);
        }
    }


    public List<RecentCustomerData> getRecentCustomers(int limit) {
        try (Jedis jedis = getResource()) {
            List<RecentCustomerData> result = new ArrayList<>();
            List<Tuple> scores = new ArrayList<>(jedis.zrevrangeWithScores("recent_customers", 0, limit - 1));

            for (Tuple tuple : scores) {
                int customerId = Integer.parseInt(tuple.getElement());
                Map<String, String> data = jedis.hgetAll("customer:" + customerId);
                if (!data.isEmpty()) {
                    result.add(new RecentCustomerData(
                            customerId,
                            data.get("name"),
                            Long.parseLong(data.get("last_visit"))
                    ));
                }
            }
            return result;
        }
    }
}
