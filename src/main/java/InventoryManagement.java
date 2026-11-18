
import java.sql.*;
import java.util.Scanner;

public class InventoryManagement {

    static final String URL =
            "jdbc:mysql://localhost:3306/retail_db";
    static final String USER = "root";
    static final String PASSWORD = "Athar@99356";

    static Connection con;
    static Scanner sc = new Scanner(System.in);

    // Database Connection
    static void connect() throws SQLException {
        con = DriverManager.getConnection(URL, USER, PASSWORD);
        System.out.println("Database Connected Successfully!");
    }

    // Input methods
    static int readInt(String message) {
        while (true) {
            try {
                System.out.print(message);
                return Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid integer.");
            }
        }
    }

    static double readDouble(String message) {
        while (true) {
            try {
                System.out.print(message);
                return Double.parseDouble(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    // (a) Insert at least three product records
    static void insertProducts() {

        String sql = "INSERT INTO Product VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            for (int i = 1; i <= 3; i++) {

                System.out.println("\nEnter Product " + i + " Details");

                int id = readInt("Product ID: ");

                System.out.print("Product Name: ");
                String name = sc.nextLine();

                System.out.print("Category: ");
                String category = sc.nextLine();

                int quantity = readInt("Quantity: ");
                double price = readDouble("Price: ");

                if (quantity < 0 || price <= 0) {
                    System.out.println("Invalid quantity or price. Record skipped.");
                    continue;
                }

                ps.setInt(1, id);
                ps.setString(2, name);
                ps.setString(3, category);
                ps.setInt(4, quantity);
                ps.setDouble(5, price);

                try {
                    ps.executeUpdate();
                    System.out.println("Product inserted successfully!");

                } catch (SQLIntegrityConstraintViolationException e) {
                    System.out.println("Duplicate Product ID! Record not inserted.");
                }
            }

        } catch (SQLException e) {
            System.out.println("Insert Error: " + e.getMessage());
        }
    }

    // (b) Retrieve products by category and quantity
    static void retrieveProducts() {

        String sql = "SELECT * FROM Product " +
                "WHERE category = ? AND quantity < 20 " +
                "ORDER BY price DESC";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            System.out.print("Enter Category: ");
            String category = sc.nextLine();

            ps.setString(1, category);

            try (ResultSet rs = ps.executeQuery()) {

                System.out.println("\nProduct Details:");
                System.out.printf("%-10s %-20s %-15s %-10s %-10s%n",
                        "ID", "Name", "Category", "Quantity", "Price");

                boolean found = false;

                while (rs.next()) {
                    found = true;

                    System.out.printf("%-10d %-20s %-15s %-10d %-10.2f%n",
                            rs.getInt("product_id"),
                            rs.getString("product_name"),
                            rs.getString("category"),
                            rs.getInt("quantity"),
                            rs.getDouble("price"));
                }

                if (!found) {
                    System.out.println("No matching products found.");
                }
            }

        } catch (SQLException e) {
            System.out.println("Retrieval Error: " + e.getMessage());
        }
    }

    // (c) Update product stock
    static void updateStock() {

        String sql = "UPDATE Product " +
                "SET quantity = quantity + ? " +
                "WHERE product_id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            int id = readInt("Enter Product ID: ");
            int quantity = readInt("Enter Quantity to Add: ");

            if (quantity <= 0) {
                System.out.println("Quantity must be positive.");
                return;
            }

            ps.setInt(1, quantity);
            ps.setInt(2, id);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                System.out.println("Stock updated successfully!");
            } else {
                System.out.println("Product does not exist.");
            }

        } catch (SQLException e) {
            System.out.println("Update Error: " + e.getMessage());
        }
    }

    // (d) Search and modify product price
    static void modifyProduct() {

        int id = readInt("Enter Product ID: ");

        String search = "SELECT * FROM Product WHERE product_id = ?";

        try (PreparedStatement ps = con.prepareStatement(search)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    System.out.println("\nProduct Found!");
                    System.out.println("Name: " +
                            rs.getString("product_name"));
                    System.out.println("Category: " +
                            rs.getString("category"));
                    System.out.println("Quantity: " +
                            rs.getInt("quantity"));
                    System.out.println("Old Price: " +
                            rs.getDouble("price"));

                    double newPrice = readDouble("Enter New Price: ");

                    if (newPrice <= 0) {
                        System.out.println("Price must be greater than zero.");
                        return;
                    }

                    String update = "UPDATE Product SET price = ? " +
                            "WHERE product_id = ?";

                    try (PreparedStatement ps2 =
                                 con.prepareStatement(update)) {

                        ps2.setDouble(1, newPrice);
                        ps2.setInt(2, id);

                        ps2.executeUpdate();
                    }

                    System.out.println("\nPrice updated successfully!");

                    // Display updated product details
                    try (PreparedStatement ps3 =
                                 con.prepareStatement(search)) {

                        ps3.setInt(1, id);

                        try (ResultSet rs2 = ps3.executeQuery()) {

                            if (rs2.next()) {
                                System.out.println("\nUpdated Product Details:");
                                System.out.println("ID: " +
                                        rs2.getInt("product_id"));
                                System.out.println("Name: " +
                                        rs2.getString("product_name"));
                                System.out.println("Category: " +
                                        rs2.getString("category"));
                                System.out.println("Quantity: " +
                                        rs2.getInt("quantity"));
                                System.out.println("New Price: " +
                                        rs2.getDouble("price"));
                            }
                        }
                    }

                } else {
                    System.out.println("Product does not exist.");
                }
            }

        } catch (SQLException e) {
            System.out.println("Modification Error: " + e.getMessage());
        }
    }

    // (e) Aggregate query and conditional deletion
    static void calculateAndDelete() {

        String totalSQL =
                "SELECT SUM(quantity * price) AS total FROM Product";

        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(totalSQL)) {

            if (rs.next()) {
                double total = rs.getDouble("total");
                System.out.println("Total Inventory Value: " + total);
            }

        } catch (SQLException e) {
            System.out.println("Aggregate Error: " + e.getMessage());
        }

        int id = readInt("\nEnter Product ID to Delete: ");

        String deleteSQL =
                "DELETE FROM Product " +
                        "WHERE product_id = ? AND quantity = 0";

        try (PreparedStatement ps = con.prepareStatement(deleteSQL)) {

            ps.setInt(1, id);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                System.out.println("Product deleted successfully!");
            } else {
                System.out.println(
                        "Product cannot be deleted. It does not exist " +
                                "or its quantity is not zero."
                );
            }

        } catch (SQLException e) {
            System.out.println("Deletion Error: " + e.getMessage());
        }
    }

    // Main Menu
    public static void main(String[] args) {

        try {
            connect();

            int choice;

            do {
                System.out.println("\n===== INVENTORY MANAGEMENT SYSTEM =====");
                System.out.println("1. Insert Product Records");
                System.out.println("2. Retrieve Products by Category");
                System.out.println("3. Update Product Stock");
                System.out.println("4. Search and Modify Product Price");
                System.out.println("5. Calculate Inventory Value and Delete");
                System.out.println("6. Exit");

                choice = readInt("Enter Your Choice: ");

                switch (choice) {

                    case 1:
                        insertProducts();
                        break;

                    case 2:
                        retrieveProducts();
                        break;

                    case 3:
                        updateStock();
                        break;

                    case 4:
                        modifyProduct();
                        break;

                    case 5:
                        calculateAndDelete();
                        break;

                    case 6:
                        System.out.println("Exiting Application...");
                        break;

                    default:
                        System.out.println("Invalid Choice!");
                }

            } while (choice != 6);

        } catch (SQLException e) {
            System.out.println("Database Connection Failed!");
            System.out.println(e.getMessage());

        } finally {
            try {
                if (con != null) {
                    con.close();
                    System.out.println("Database Connection Closed.");
                }
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
