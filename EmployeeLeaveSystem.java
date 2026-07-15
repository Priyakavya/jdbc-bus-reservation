package employee;
import java.sql.*;
import java.util.Scanner;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;

public class EmployeeLeaveSystem {

    static final String URL = "jdbc:mysql://localhost:3306/";
    static final String DB = "leave_db";
    static final String USER = "root";
    static final String PASS = "Priya@02"; // CHANGE YOUR MYSQL PASSWORD

    static Connection con;
    static FileWriter fw;

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection(URL, USER, PASS);
            fw = new FileWriter("Leave_Output.txt", true);
            
            printConsole("====== EMPLOYEE LEAVE SYSTEM STARTED: " + LocalDate.now() + " ======");
            writeFile("====== EMPLOYEE LEAVE SYSTEM STARTED: " + LocalDate.now() + " ======");

            setupDatabase();
            con.setAutoCommit(false);
            int ch;

            do {
                printConsole("\n--- LEAVE MANAGEMENT MENU ---");
                printConsole("1. Register Employee");
                printConsole("2. View Employees");
                printConsole("3. Apply Leave");
                printConsole("4. Approve/Reject Leave");
                printConsole("5. View Leave Requests");
                printConsole("6. View Leave Balance");
                printConsole("7. Update Leave Balance");
                printConsole("8. Delete Employee");
                printConsole("9. Custom Queries");
                printConsole("10. Batch Insert Employees");
                printConsole("11. Exit");
                System.out.print("Enter choice: ");
                ch = sc.nextInt();

                try {
                    switch(ch) {
                        case 1: registerEmployee(sc); break;
                        case 2: viewEmployees(); break;
                        case 3: applyLeave(sc); break;
                        case 4: approveLeave(sc); break;
                        case 5: viewLeaveRequests(); break;
                        case 6: viewLeaveBalance(); break;
                        case 7: updateLeaveBalance(sc); break;
                        case 8: deleteEmployee(sc); break;
                        case 9: customQueries(); break;
                        case 10: batchInsertEmployees(); break;
                        case 11: printConsole("Thank You!"); writeFile("Thank You!"); break;
                        default: printConsole("Wrong choice");
                    }
                    if(ch>=1 && ch<=10) {
                        con.commit();
                        printConsole("Transaction Committed");
                        writeFile("Transaction Committed");
                    }
                } catch(Exception e) {
                    con.rollback();
                    printConsole("Transaction Rolled Back! Error: " + e.getMessage());
                    writeFile("Transaction Rolled Back! Error: " + e.getMessage());
                    sc.nextLine();
                }
            } while(ch != 11);

        } catch(Exception e) { e.printStackTrace(); } 
        finally {
            try{ if(con!=null) con.close(); if(sc!=null) sc.close(); if(fw!=null) fw.close(); } catch(Exception e){}
        }
    }

    // METHOD 1: FOR CONSOLE ONLY
    static void printConsole(String msg){
        System.out.println(msg);
    }
    
    // METHOD 2: FOR FILE ONLY
    static void writeFile(String msg) throws IOException{
        fw.write(msg + "\n");
    }

    // CREATE TABLES IN APPLICATION WITH CONSTRAINTS
    static void setupDatabase() throws SQLException, IOException {
        Statement st = con.createStatement();
        st.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB);
        st.executeUpdate("USE " + DB);

        st.executeUpdate("DROP TABLE IF EXISTS leave_requests");
        st.executeUpdate("DROP TABLE IF EXISTS leave_balance");
        st.executeUpdate("DROP TABLE IF EXISTS employees");

        st.executeUpdate("CREATE TABLE employees(emp_id INT AUTO_INCREMENT PRIMARY KEY, emp_name VARCHAR(100) NOT NULL, email VARCHAR(100) UNIQUE NOT NULL, department VARCHAR(50) NOT NULL, join_date DATE NOT NULL)");
        
        st.executeUpdate("CREATE TABLE leave_balance(balance_id INT AUTO_INCREMENT PRIMARY KEY, emp_id INT, emp_name VARCHAR(100), total_leaves INT DEFAULT 20, leaves_taken INT DEFAULT 0, FOREIGN KEY(emp_id) REFERENCES employees(emp_id) ON DELETE CASCADE)");

        st.executeUpdate("CREATE TABLE leave_requests(leave_id INT AUTO_INCREMENT PRIMARY KEY, emp_id INT, leave_type VARCHAR(20) NOT NULL, start_date DATE NOT NULL, end_date DATE NOT NULL, reason VARCHAR(200), status VARCHAR(20) DEFAULT 'PENDING', applied_date DATE, FOREIGN KEY(emp_id) REFERENCES employees(emp_id) ON DELETE CASCADE)");
        
        printConsole("DB and 3 Tables Created Successfully!");
        writeFile("DB and 3 Tables Created Successfully!");
    }

    // CRUD - CREATE
    static void registerEmployee(Scanner sc) throws SQLException, IOException {
        printConsole("Enter Employee Name: "); 
        String name = sc.next();
        printConsole("Enter Email: "); 
        String email = sc.next();
        printConsole("Enter Department: "); 
        String dept = sc.next();

        PreparedStatement ps = con.prepareStatement("INSERT INTO employees(emp_name,email,department,join_date) VALUES(?,?,?,CURDATE())", Statement.RETURN_GENERATED_KEYS);
        ps.setString(1,name); ps.setString(2,email); ps.setString(3,dept);
        int rows = ps.executeUpdate(); // TN: ROW COUNT VALIDATION
        printConsole("Employee Inserted Rows = " + rows);
        writeFile("Employee Inserted Rows = " + rows);
        if(rows == 0) throw new SQLException("Insert Failed");

        ResultSet rs = ps.getGeneratedKeys(); 
        if(rs.next()){
            int empId = rs.getInt(1);
            PreparedStatement ps2 = con.prepareStatement("INSERT INTO leave_balance(emp_id,emp_name,total_leaves,leaves_taken) VALUES(?,?,20,0)");
            ps2.setInt(1,empId); ps2.setString(2,name);
            ps2.executeUpdate();
            printConsole("Leave Balance Created for EmpID: " + empId);
            writeFile("Leave Balance Created for EmpID: " + empId);
        }
    }

    // CRUD - READ
    static void viewEmployees() throws SQLException, IOException {
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery("SELECT * FROM employees");
        int count = 0;
        while(rs.next()) {
            String data = rs.getInt(1) + ". " + rs.getString(2) + " | " + rs.getString(3) + " | " + rs.getString(4);
            printConsole(data);
            writeFile(data);
            count++;
        }
        printConsole("Total Employees = " + count);
        writeFile("Total Employees = " + count);
    }

    // CRUD - CREATE
    static void applyLeave(Scanner sc) throws SQLException, IOException {
        printConsole("Enter Employee ID: "); int id = sc.nextInt();
        printConsole("Enter Leave Type [CL/SL/PL]: "); String type = sc.next();
        printConsole("Enter Start Date [YYYY-MM-DD]: "); String sdate = sc.next();
        printConsole("Enter End Date [YYYY-MM-DD]: "); String edate = sc.next();
        sc.nextLine();
        printConsole("Enter Reason: "); String reason = sc.nextLine();

        PreparedStatement ps = con.prepareStatement("INSERT INTO leave_requests(emp_id,leave_type,start_date,end_date,reason,applied_date) VALUES(?,?,?,?,?,CURDATE())");
        ps.setInt(1,id); ps.setString(2,type); ps.setDate(3,Date.valueOf(sdate)); 
        ps.setDate(4,Date.valueOf(edate)); ps.setString(5,reason);
        int rows = ps.executeUpdate();
        printConsole("Leave Applied. Rows = " + rows);
        writeFile("Leave Applied. Rows = " + rows);
        if(rows == 0) throw new SQLException("Apply Leave Failed");
    }

    // CRUD - UPDATE
    static void approveLeave(Scanner sc) throws SQLException, IOException {
        printConsole("Enter Leave ID: "); int lid = sc.nextInt();
        printConsole("Enter Status [APPROVED/REJECTED]: "); String status = sc.next();

        PreparedStatement ps = con.prepareStatement("UPDATE leave_requests SET status=? WHERE leave_id=?");
        ps.setString(1,status); ps.setInt(2,lid);
        int rows = ps.executeUpdate();
        printConsole("Updated Rows = " + rows);
        writeFile("Updated Rows = " + rows);
        if(rows == 0) throw new SQLException("Invalid Leave ID");

        if(status.equalsIgnoreCase("APPROVED")) {
            PreparedStatement ps2 = con.prepareStatement("UPDATE leave_balance lb JOIN leave_requests lr ON lb.emp_id=lr.emp_id SET lb.leaves_taken = lb.leaves_taken + DATEDIFF(lr.end_date, lr.start_date)+1 WHERE lr.leave_id=?");
            ps2.setInt(1,lid);
            ps2.executeUpdate();
            printConsole("Leave Balance Updated");
            writeFile("Leave Balance Updated");
        }
    }

    // READ
    static void viewLeaveRequests() throws SQLException, IOException {
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery("SELECT lr.leave_id, e.emp_name, lr.leave_type, lr.start_date, lr.end_date, lr.status FROM leave_requests lr JOIN employees e ON lr.emp_id=e.emp_id");
        int count = 0;
        while(rs.next()) {
            String data = "LeaveID: " + rs.getInt(1) + " | Emp: " + rs.getString(2) + " | Status: " + rs.getString(6);
            printConsole(data); writeFile(data);
            count++;
        }
        printConsole("Total Requests = " + count); writeFile("Total Requests = " + count);
    }

    // READ
    static void viewLeaveBalance() throws SQLException, IOException {
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery("SELECT * FROM leave_balance");
        while(rs.next()) {
            int remaining = rs.getInt(4) - rs.getInt(5);
            String data = "EmpID: " + rs.getInt(2) + " | Name: " + rs.getString(3) + " | Remaining: " + remaining;
            printConsole(data); writeFile(data);
        }
    }

    // UPDATE
    static void updateLeaveBalance(Scanner sc) throws SQLException, IOException {
        printConsole("Enter Employee ID: "); int id = sc.nextInt();
        printConsole("Enter New Total Leaves: "); int total = sc.nextInt();
        PreparedStatement ps = con.prepareStatement("UPDATE leave_balance SET total_leaves=? WHERE emp_id=?");
        ps.setInt(1,total); ps.setInt(2,id);
        int rows = ps.executeUpdate();
        printConsole("Updated Rows = " + rows); writeFile("Updated Rows = " + rows);
        if(rows == 0) throw new SQLException("Invalid Emp ID");
    }

    // DELETE
    static void deleteEmployee(Scanner sc) throws SQLException, IOException {
        printConsole("Enter Employee ID to Delete: "); int id = sc.nextInt();
        PreparedStatement ps = con.prepareStatement("DELETE FROM employees WHERE emp_id=?");
        ps.setInt(1,id);
        int rows = ps.executeUpdate();
        printConsole("Deleted Rows = " + rows); writeFile("Deleted Rows = " + rows);
        if(rows == 0) throw new SQLException("Invalid Emp ID");
    }

    // 4 CUSTOMISED QUERIES
    static void customQueries() throws SQLException, IOException {
        printConsole("\n--- 4 CUSTOM QUERIES ---"); writeFile("\n--- 4 CUSTOM QUERIES ---");
        Statement st = con.createStatement();

        printConsole("\nQ1: Pending Leaves [WHERE]"); 
        ResultSet rs1 = st.executeQuery("SELECT leave_id FROM leave_requests WHERE status='PENDING'");
        while(rs1.next()) { printConsole("LeaveID: " + rs1.getInt(1)); writeFile("LeaveID: " + rs1.getInt(1)); }

        printConsole("\nQ2: Leave Count by Type [GROUP BY]");
        ResultSet rs2 = st.executeQuery("SELECT leave_type, COUNT(*) FROM leave_requests GROUP BY leave_type");
        while(rs2.next()) { printConsole(rs2.getString(1) + " = " + rs2.getInt(2)); writeFile(rs2.getString(1) + " = " + rs2.getInt(2)); }

        printConsole("\nQ3: Employee + Balance [JOIN]");
        ResultSet rs3 = st.executeQuery("SELECT e.emp_name, lb.total_leaves FROM employees e JOIN leave_balance lb ON e.emp_id=lb.emp_id");
        while(rs3.next()) { printConsole("Name: " + rs3.getString(1)); writeFile("Name: " + rs3.getString(1)); }

        printConsole("\nQ4: Recent Leaves [BETWEEN]");
        ResultSet rs4 = st.executeQuery("SELECT leave_id FROM leave_requests WHERE applied_date BETWEEN CURDATE()-30 AND CURDATE()");
        while(rs4.next()) { printConsole("LeaveID: " + rs4.getInt(1)); writeFile("LeaveID: " + rs4.getInt(1)); }
    }

    // BATCH INSERT - MULTIPLE INSERTION
    static void batchInsertEmployees() throws SQLException, IOException {
        PreparedStatement ps = con.prepareStatement("INSERT INTO employees(emp_name,email,department,join_date) VALUES(?,?,?,CURDATE())");
        
        ps.setString(1,"Ravi"); ps.setString(2,"ravi" + System.currentTimeMillis() + "@comp.com"); ps.setString(3,"IT"); ps.addBatch();
        ps.setString(1,"Sneha"); ps.setString(2,"sneha" + System.currentTimeMillis() + "@comp.com"); ps.setString(3,"HR"); ps.addBatch();
        ps.setString(1,"Arun"); ps.setString(2,"arun" + System.currentTimeMillis() + "@comp.com"); ps.setString(3,"Finance"); ps.addBatch();
        
        int[] rows = ps.executeBatch(); // TN: BATCH
        printConsole("Batch Inserted " + rows.length + " Employees");
        writeFile("Batch Inserted " + rows.length + " Employees");
    }
}
