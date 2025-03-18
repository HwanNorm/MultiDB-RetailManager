public class Users {
    public enum Role {
        CUSTOMER,  // Changed from BUYER
        CASHIER,   // Changed from SELLER
        MANAGER
    }

    private int userID;
    private String username;
    private String password;
    private String fullName;
    private String email;    // New field
    private Role role;

    public Users() {
        // Default constructor
    }

    // Getters and setters
    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    // Role check methods
    public boolean isCustomer() {
        return role == Role.CUSTOMER;
    }

    public boolean isCashier() {
        return role == Role.CASHIER;
    }

    public boolean isManager() {
        return role == Role.MANAGER;
    }

    // Utility method for role-based access control
    public boolean hasPermission(String action) {
        switch (action.toUpperCase()) {
            case "VIEW_PRODUCTS":
                return true; // All roles can view products
            case "PROCESS_SALE":
                return role == Role.CASHIER || role == Role.MANAGER;
            case "MANAGE_INVENTORY":
            case "MANAGE_USERS":
            case "VIEW_REPORTS":
                return role == Role.MANAGER;
            default:
                return false;
        }
    }

    @Override
    public String toString() {
        return "User{" +
                "userID=" + userID +
                ", username='" + username + '\'' +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                '}';
    }
}
