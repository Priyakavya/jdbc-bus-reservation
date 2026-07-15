package ecommanagement;
import java.sql.*;
import java.util.Scanner;
import java.util.ArrayList;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

class CartItem {
    int productId;
    int quantity;
    CartItem(int p, int q) { productId = p; quantity = q; }
}

public class ECommerceSystem {

    static final String URL = "jdbc:mysql://localhost:3306/";
    static final String DB = "ecommerce_db";
    static final String USER = "root";
    static final String PASS = "Priya@02"; // CHANGE YOUR MYSQL PASSWORD HERE

    static Connection con;
    static FileWriter fw;
    static ArrayList<CartItem> cart = new ArrayList<>(); 

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection(URL, USER, PASS);
            
            fw = new FileWriter("ECommerce_Output.txt", true);
            String startMsg = "====== E-COMMERCE SYSTEM LOG STARTED AT: " + LocalDateTime.now() + " ======\n";
            System.out.print(startMsg);
            fw.write(startMsg);

            setupDatabase();
            con.setAutoCommit(false);
            int ch;

            do {
                String menu = "\n--- MENU ---\n1. View Products\n2. Add to Cart\n3. View Cart\n4. Place Order\n5. Track Order\n6. Update Product\n7. Delete Product\n8. Custom Queries\n9. Insert Sample Data\n10. Exit\nEnter choice: ";
                System.out.print(menu);
                fw.write(menu);
                ch = sc.nextInt();

                try {
                    switch(ch) {
                        case 1: viewProducts(sc); break; // R
                        case 2: addToCart(sc); break;    // C
                        case 3: viewCart(); break;
                        case 4: placeOrder(sc); break;   // C
                        case 5: trackOrder(sc); break;   // R
                        case 6: updateProduct(sc); break;// U
                        case 7: deleteProduct(sc); break;// D
                        case 8: customQueries(); break;  // 4 QUERIES
                        case 9: insertSampleData(); break;
                        case 10: 
                            String exitMsg = "Thank You!\n";
                            System.out.print(exitMsg);
                            fw.write(exitMsg);
                            break;
                        default: 
                            String defMsg = "Wrong choice\n";
                            System.out.print(defMsg);
                            fw.write(defMsg);
                    }
                    if(ch>=1 && ch<=8) {
                        con.commit();
                        String commitMsg = "Transaction Committed\n";
                        System.out.print(commitMsg);
                        fw.write(commitMsg);
                    }
                } catch(Exception e) {
                    con.rollback();
                    String errMsg = "Transaction Rolled Back! Error: " + e.getMessage() + "\n";
                    System.out.print(errMsg);
                    fw.write(errMsg);
                    sc.nextLine(); // clear buffer
                }
            } while(ch != 10);

        } catch(Exception e) { 
            e.printStackTrace(); 
        } finally {
            try{ if(con!=null) con.close(); if(sc!=null) sc.close(); if(fw!=null) fw.close(); } catch(Exception e){}
        }
    }

    // CREATE DB + 4 TABLES WITH CONSTRAINTS
    static void setupDatabase() throws SQLException, IOException {
        Statement st = con.createStatement();
        st.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB);
        st.executeUpdate("USE " + DB);

        st.executeUpdate("DROP TABLE IF EXISTS order_items");
        st.executeUpdate("DROP TABLE IF EXISTS orders");
        st.executeUpdate("DROP TABLE IF EXISTS customers");
        st.executeUpdate("DROP TABLE IF EXISTS products");

        st.executeUpdate("CREATE TABLE products(product_id INT AUTO_INCREMENT PRIMARY KEY, product_name VARCHAR(100) NOT NULL, price DOUBLE NOT NULL, stock INT NOT NULL)");
        st.executeUpdate("CREATE TABLE customers(customer_id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(50) NOT NULL, email VARCHAR(100) UNIQUE NOT NULL)");
        st.executeUpdate("CREATE TABLE orders(order_id INT AUTO_INCREMENT PRIMARY KEY, customer_id INT, order_date DATE, status VARCHAR(20) DEFAULT 'PLACED', FOREIGN KEY(customer_id) REFERENCES customers(customer_id))");
        st.executeUpdate("CREATE TABLE order_items(item_id INT AUTO_INCREMENT PRIMARY KEY, order_id INT, product_id INT, quantity INT NOT NULL, FOREIGN KEY(order_id) REFERENCES orders(order_id) ON DELETE CASCADE, FOREIGN KEY(product_id) REFERENCES products(product_id))");
        
        String msg = "DB and Tables Created Successfully!\n";
        System.out.print(msg);
        fw.write(msg);
    }

    // R - READ
    static void viewProducts(Scanner sc) throws SQLException, IOException {
        System.out.print("Search Product Name: ");
        fw.write("Search Product Name: ");
        String s = sc.next();
        PreparedStatement ps = con.prepareStatement("SELECT * FROM products WHERE product_name LIKE ?");
        ps.setString(1, s+"%");
        ResultSet rs = ps.executeQuery();
        int count = 0;
        while(rs.next()) {
            String row = rs.getInt(1) + ". " + rs.getString(2) + " - Rs." + rs.getDouble(3) + " - Stock:" + rs.getInt(4) + "\n";
            System.out.print(row);
            fw.write(row);
            count++;
        }
        String countMsg = "Total Products Found = " + count + "\n";
        System.out.print(countMsg);
        fw.write(countMsg);
    }

    // C - CREATE CART
    static void addToCart(Scanner sc) throws SQLException, IOException {
        System.out.print("Enter Product ID: ");
        fw.write("Enter Product ID: ");
        int pid = sc.nextInt();
        System.out.print("Enter Quantity: ");
        fw.write("Enter Quantity: ");
        int qty = sc.nextInt();
        
        if(qty <= 0) throw new SQLException("Quantity must be > 0");

        PreparedStatement ps = con.prepareStatement("SELECT stock FROM products WHERE product_id=?");
        ps.setInt(1,pid);
        ResultSet rs = ps.executeQuery();
        if(rs.next() && rs.getInt(1) >= qty) {
            cart.add(new CartItem(pid, qty));
            String msg = "Added to Cart\n";
            System.out.print(msg);
            fw.write(msg);
        } else {
            throw new SQLException("Stock not available for ProductID: " + pid);
        }
    }

    static void viewCart() throws IOException {
        if(cart.isEmpty()) { 
            String msg = "Cart is Empty\n";
            System.out.print(msg);
            fw.write(msg);
            return; 
        }
        String head = "--- YOUR CART ---\n";
        System.out.print(head);
        fw.write(head);
        for(CartItem c : cart) {
            String item = "ProductID: " + c.productId + " Qty: " + c.quantity + "\n";
            System.out.print(item);
            fw.write(item);
        }
    }

    // C - CREATE ORDER + BATCH + TRANSACTION
    static void placeOrder(Scanner sc) throws SQLException, IOException {
        if(cart.isEmpty()) throw new SQLException("Cart is empty");

        System.out.print("Enter Customer Name: ");
        fw.write("Enter Customer Name: ");
        String name = sc.next();
        System.out.print("Enter Email: ");
        fw.write("Enter Email: ");
        String email = sc.next();

        PreparedStatement ps1;
        int cid;
        try{
            ps1 = con.prepareStatement("INSERT INTO customers(name,email) VALUES(?,?)", Statement.RETURN_GENERATED_KEYS);
            ps1.setString(1,name); ps1.setString(2,email);
            int cRows = ps1.executeUpdate();
            System.out.print("Customer Inserted Rows = " + cRows + "\n");
            fw.write("Customer Inserted Rows = " + cRows + "\n");
            ResultSet rs = ps1.getGeneratedKeys(); rs.next();
            cid = rs.getInt(1);
        } catch(SQLIntegrityConstraintViolationException e) {
            PreparedStatement psGet = con.prepareStatement("SELECT customer_id FROM customers WHERE email=?");
            psGet.setString(1,email);
            ResultSet rs = psGet.executeQuery();
            rs.next();
            cid = rs.getInt(1);
            String msg = "Customer Already Exists. Using CustomerID = " + cid + "\n";
            System.out.print(msg);
            fw.write(msg);
        }

        PreparedStatement ps2 = con.prepareStatement("INSERT INTO orders(customer_id,order_date,status) VALUES(?,CURDATE(),'PLACED')", Statement.RETURN_GENERATED_KEYS);
        ps2.setInt(1,cid);
        int oRows = ps2.executeUpdate();
        System.out.print("Order Inserted Rows = " + oRows + "\n");
        fw.write("Order Inserted Rows = " + oRows + "\n");
        ResultSet rs = ps2.getGeneratedKeys(); rs.next();
        int oid = rs.getInt(1);

        PreparedStatement ps3 = con.prepareStatement("INSERT INTO order_items(order_id,product_id,quantity) VALUES(?,?,?)");
        PreparedStatement ps4 = con.prepareStatement("UPDATE products SET stock=stock-? WHERE product_id=?");

        for(CartItem item : cart) {
            ps3.setInt(1,oid); ps3.setInt(2,item.productId); ps3.setInt(3,item.quantity);
            ps3.addBatch();
            ps4.setInt(1,item.quantity); ps4.setInt(2,item.productId);
            ps4.addBatch();
        }
        int[] itemRows = ps3.executeBatch();
        int[] stockRows = ps4.executeBatch();
        String batchMsg = "OrderItems Batch Inserted = " + itemRows.length + " | Stock Updated = " + stockRows.length + "\n";
        System.out.print(batchMsg);
        fw.write(batchMsg);
        cart.clear();
        String finalMsg = "Order Placed! Order ID: " + oid + "\n";
        System.out.print(finalMsg);
        fw.write(finalMsg);
    }

    // R - READ + JOIN
    static void trackOrder(Scanner sc) throws SQLException, IOException {
        System.out.print("Enter Order ID: ");
        fw.write("Enter Order ID: ");
        int oid = sc.nextInt();
        String sql = "SELECT o.order_id, c.name, p.product_name, oi.quantity, p.price, o.status " +
                     "FROM orders o JOIN customers c ON o.customer_id=c.customer_id " +
                     "JOIN order_items oi ON o.order_id=oi.order_id " +
                     "JOIN products p ON oi.product_id=p.product_id WHERE o.order_id=?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1,oid);
        ResultSet rs = ps.executeQuery();
        int count=0; double total=0;
        while(rs.next()) {
            if(count==0) {
                String head = "Customer: " + rs.getString(2) + " | Status: " + rs.getString(6) + "\n";
                System.out.print(head);
                fw.write(head);
            }
            String row = "Product: " + rs.getString(3) + " | Qty: " + rs.getInt(4) + " | Price: " + rs.getDouble(5) + "\n";
            System.out.print(row);
            fw.write(row);
            total += rs.getInt(4) * rs.getDouble(5);
            count++;
        }
        String totalMsg = "Total Items = " + count + " | Total Bill: Rs." + total + "\n";
        System.out.print(totalMsg);
        fw.write(totalMsg);
    }
    
    // U - UPDATE
    static void updateProduct(Scanner sc) throws SQLException, IOException {
        System.out.print("Enter Product ID to Update: ");
        fw.write("Enter Product ID to Update: ");
        int id = sc.nextInt();
        System.out.print("Enter New Price: ");
        fw.write("Enter New Price: ");
        double price = sc.nextDouble();
        System.out.print("Enter New Stock: ");
        fw.write("Enter New Stock: ");
        int stock = sc.nextInt();
        
        if(price <= 0 || stock < 0) throw new SQLException("Invalid Price/Stock");
        
        PreparedStatement ps = con.prepareStatement("UPDATE products SET price=?, stock=? WHERE product_id=?");
        ps.setDouble(1,price); ps.setInt(2,stock); ps.setInt(3,id);
        int rows = ps.executeUpdate();
        if(rows == 0) throw new SQLException("Invalid Product ID");
        String msg = "Updated Rows = " + rows + "\n";
        System.out.print(msg);
        fw.write(msg);
    }
    
    // D - DELETE
    static void deleteProduct(Scanner sc) throws SQLException, IOException {
        System.out.print("Enter Product ID to Delete: ");
        fw.write("Enter Product ID to Delete: ");
        int id = sc.nextInt();
        PreparedStatement ps = con.prepareStatement("DELETE FROM products WHERE product_id=?");
        ps.setInt(1,id);
        int rows = ps.executeUpdate();
        if(rows == 0) throw new SQLException("Invalid Product ID");
        String msg = "Deleted Rows = " + rows + "\n";
        System.out.print(msg);
        fw.write(msg);
    }

    // 4 CUSTOMISED QUERIES
    static void customQueries() throws SQLException, IOException {
        String head = "\n--- 4 CUSTOMISED QUERIES ---\n";
        System.out.print(head);
        fw.write(head);
        Statement stmt = con.createStatement();

        System.out.println("QUERY 1: Products with stock < 10 [WHERE + ORDER BY]");
        fw.write("QUERY 1: Products with stock < 10 [WHERE + ORDER BY]\n");
        ResultSet rs1 = stmt.executeQuery("SELECT product_id, product_name, stock FROM products WHERE stock < 10 ORDER BY stock");
        while(rs1.next()) {
            String r = "ID: " + rs1.getInt(1) + " | " + rs1.getString(2) + " - Stock: " + rs1.getInt(3) + "\n";
            System.out.print(r);
            fw.write(r);
        }

        System.out.println("QUERY 2: Total Quantity Sold per Product [GROUP BY]");
        fw.write("QUERY 2: Total Quantity Sold per Product [GROUP BY]\n");
        ResultSet rs2 = stmt.executeQuery("SELECT p.product_name, SUM(oi.quantity) as total_sold FROM order_items oi JOIN products p ON oi.product_id=p.product_id GROUP BY p.product_name");
        while(rs2.next()) {
            String r = "Product: " + rs2.getString(1) + " | Total Sold: " + rs2.getInt(2) + "\n";
            System.out.print(r);
            fw.write(r);
        }

        System.out.println("QUERY 3: Customer Order History [JOIN]");
        fw.write("QUERY 3: Customer Order History [JOIN]\n");
        ResultSet rs3 = stmt.executeQuery("SELECT c.name, o.order_id, o.order_date, o.status FROM customers c JOIN orders o ON c.customer_id=o.customer_id");
        while(rs3.next()) {
            String r = "Customer: " + rs3.getString(1) + " | OrderID: " + rs3.getInt(2) + " | Date: " + rs3.getDate(3) + "\n";
            System.out.print(r);
            fw.write(r);
        }

        System.out.println("QUERY 4: All Names - Product and Customer [UNION]");
        fw.write("QUERY 4: All Names - Product and Customer [UNION]\n");
        ResultSet rs4 = stmt.executeQuery("SELECT product_name as name FROM products UNION SELECT name FROM customers");
        while(rs4.next()) {
            String r = " - " + rs4.getString(1) + "\n";
            System.out.print(r);
            fw.write(r);
        }
    }

    // BATCH INSERT
    static void insertSampleData() throws SQLException, IOException {
        PreparedStatement ps = con.prepareStatement("INSERT INTO products(product_name,price,stock) VALUES(?,?,?)");
        ps.setString(1,"Laptop"); ps.setDouble(2,60000); ps.setInt(3,15); ps.addBatch();
        ps.setString(1,"Phone"); ps.setDouble(2,30000); ps.setInt(3,25); ps.addBatch();
        ps.setString(1,"Mouse"); ps.setDouble(2,800); ps.setInt(3,100); ps.addBatch();
        int[] rows = ps.executeBatch();
        String msg = "Batch Inserted " + rows.length + " Sample Products\n";
        System.out.print(msg);
        fw.write(msg);
    }
}