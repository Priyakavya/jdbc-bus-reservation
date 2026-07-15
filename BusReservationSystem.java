package com.busreservaton.jdbc;
import java.sql.*;
import java.util.Scanner;
import java.io.FileWriter;   // TN: FOR TXT OUTPUT
import java.io.IOException;  // TN: FOR TXT OUTPUT
import java.time.LocalDateTime; // TN: FOR TIMESTAMP

	public class BusReservationSystem {

	    static final String URL = "jdbc:mysql://localhost:3306/";
	    static final String DB = "bus_db";
	    static final String USER = "root";
	    static final String PASS = "Priya@02"; 

	    static Connection con;
	    static FileWriter fw; 

	    
	    static void log(String message) throws IOException {
	        System.out.println(message); 
	        fw.write(message + "\n");    
	        fw.flush(); 
	    }

	    public static void main(String[] args) {
	        try {
	            Class.forName("com.mysql.cj.jdbc.Driver");
	            con = DriverManager.getConnection(URL, USER, PASS);
	            
	            fw = new FileWriter("BusReservation_Output.txt", true);
	            log("====== BUS RESERVATION SYSTEM LOG STARTED AT: " + LocalDateTime.now() + " ======");

	            createDatabaseAndTables();
	            con.setAutoCommit(false); 

	            Scanner sc = new Scanner(System.in);
	            int choice;

	            do {
	                log("\n===== BUS RESERVATION =====");
	                log("1. Add Bus");
	                log("2. Search Bus");
	                log("3. Book Seat");
	                log("4. Update Bus");
	                log("5. Cancel Ticket");
	                log("6. Passenger Details");
	                log("7. Custom Queries");
	                log("8. Exit");
	                log("Enter choice: ");

	                choice = sc.nextInt();

	                try {
	                    switch(choice) {
	                    case 1: addBus(); break;
	                    case 2: searchBus(sc); break;
	                    case 3: bookSeat(sc); break;
	                    case 4: updateBus(sc); break;
	                    case 5: cancelTicket(sc); break;
	                    case 6: passengerDetails(sc); break;
	                    case 7: customQueries(); break; 
	                    case 8: log("Exit. Connection Closed."); break;
	                    default: log("Invalid Choice!");
	                    }

	                    if(choice >=1 && choice <=7) {
	                        con.commit(); // TN: COMMIT
	                        log("Transaction Committed");
	                    }

	                }
	                catch(Exception e) {
	                    con.rollback(); // TN: ROLLBACK
	                    log("Transaction Rolled Back! Error: " + e.getMessage());
	                }

	            } while(choice != 8);

	            con.close();
	            sc.close();
	            fw.close(); // TN: CLOSE FILE

	        }
	        catch(Exception e) {
	            e.printStackTrace();
	        }
	    }

	    // CREATE DATABASE & TABLES
	    static void createDatabaseAndTables() throws SQLException, IOException {
	        Statement stmt = con.createStatement();
	        stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB);
	        stmt.executeUpdate("USE " + DB);

	        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS buses("
	            + "bus_id INT PRIMARY KEY AUTO_INCREMENT,"
	            + "bus_name VARCHAR(50) NOT NULL,"
	            + "source VARCHAR(50) NOT NULL,"
	            + "destination VARCHAR(50) NOT NULL,"
	            + "total_seats INT CHECK(total_seats>0))");

	        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS passengers("
	            + "passenger_id INT PRIMARY KEY AUTO_INCREMENT,"
	            + "name VARCHAR(50) NOT NULL,"
	            + "email VARCHAR(100) UNIQUE NOT NULL)");

	        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS bookings("
	            + "booking_id INT PRIMARY KEY AUTO_INCREMENT,"
	            + "bus_id INT,"
	            + "passenger_id INT,"
	            + "seat_no INT NOT NULL,"
	            + "booking_date DATE,"
	            + "FOREIGN KEY(bus_id) REFERENCES buses(bus_id) ON DELETE CASCADE," 
	            + "FOREIGN KEY(passenger_id) REFERENCES passengers(passenger_id),"
	            + "UNIQUE(bus_id,seat_no))");

	        log("Tables Created Successfully!");
	    }

	    // CREATE
	    static void addBus() throws SQLException, IOException {
	        String sql = "INSERT INTO buses(bus_name,source,destination,total_seats) VALUES(?,?,?,?)";
	        PreparedStatement ps = con.prepareStatement(sql);

	        ps.setString(1,"KPN"); ps.setString(2,"Bangalore"); ps.setString(3,"Chennai"); ps.setInt(4,40);
	        int r1 = ps.executeUpdate();

	        ps.setString(1,"SRM"); ps.setString(2,"Bangalore"); ps.setString(3,"Mysore"); ps.setInt(4,35);
	        int r2 = ps.executeUpdate();

	        log("Inserted Rows = " + (r1+r2)); 

	        ResultSet rs = con.createStatement().executeQuery("SELECT COUNT(*) FROM buses");
	        rs.next();
	        log("Total Buses in DB = " + rs.getInt(1));
	    }

	    // READ
	 // READ
	    static void searchBus(Scanner sc) throws SQLException, IOException {
	        log("Source : ");
	        String source = sc.next();
	        log("Destination : ");
	        String dest = sc.next();

	        String sql = "SELECT * FROM buses WHERE source LIKE ? AND destination LIKE ?";
	        PreparedStatement ps = con.prepareStatement(sql);
	        ps.setString(1, source + "%"); // TN: LIKE PATTERN
	        ps.setString(2, dest + "%");

	        ResultSet rs = ps.executeQuery();
	        int count = 0;
	        while(rs.next()) {
	            log(rs.getInt("bus_id") + " | " + rs.getString("bus_name") + " | " + rs.getString("source") + " to " + rs.getString("destination") + " | Seats: " + rs.getInt("total_seats")); // FIXED LINE
	            count++;
	        }
	        log("Rows Found = " + count);
	    }
	    // CREATE BOOKING + TN: SEAT VALIDATION
	    static void bookSeat(Scanner sc) throws SQLException, IOException {
	        log("Bus Id : ");
	        int busId = sc.nextInt();
	        log("Name : ");
	        String name = sc.next();
	        log("Email : ");
	        String email = sc.next();
	        log("Seat No : ");
	        int seat = sc.nextInt();

	        // TN: VALIDATION - CHECK SEAT AVAILABILITY
	        PreparedStatement check = con.prepareStatement("SELECT COUNT(*) FROM bookings WHERE bus_id=? AND seat_no=?");
	        check.setInt(1, busId);
	        check.setInt(2, seat);
	        ResultSet rsCheck = check.executeQuery();
	        rsCheck.next();
	        if(rsCheck.getInt(1) > 0) {
	            throw new SQLException("Seat Already Booked!"); 
	        }

	        PreparedStatement p1 = con.prepareStatement("INSERT INTO passengers(name,email) VALUES(?,?)", Statement.RETURN_GENERATED_KEYS);
	        p1.setString(1,name); p1.setString(2,email);
	        int pRows = p1.executeUpdate();

	        ResultSet key = p1.getGeneratedKeys();
	        key.next();
	        int pid = key.getInt(1);

	        PreparedStatement p2 = con.prepareStatement("INSERT INTO bookings(bus_id,passenger_id,seat_no,booking_date) VALUES(?,?,?,CURDATE())");
	        p2.setInt(1,busId); p2.setInt(2,pid); p2.setInt(3,seat);
	        int rows = p2.executeUpdate();

	        log("Booking Rows = " + rows + ", Passenger Rows = " + pRows);

	        ResultSet rs = con.createStatement().executeQuery("SELECT COUNT(*) FROM bookings");
	        rs.next();
	        log("Total Booking Count = " + rs.getInt(1));
	    }

	    // UPDATE
	    static void updateBus(Scanner sc) throws SQLException, IOException {
	        log("Bus ID : ");
	        int id = sc.nextInt();
	        log("New Seats : ");
	        int seats = sc.nextInt();

	        PreparedStatement ps = con.prepareStatement("UPDATE buses SET total_seats=? WHERE bus_id=?");
	        ps.setInt(1,seats); ps.setInt(2,id);
	        int rows = ps.executeUpdate();

	        if(rows == 0) throw new SQLException("Invalid Bus ID");
	        log("Updated Rows = " + rows); 

	        ResultSet rs = con.createStatement().executeQuery("SELECT COUNT(*) FROM buses WHERE bus_id=" + id);
	        rs.next();
	        log("Validation Count = " + rs.getInt(1));
	    }

	    // DELETE
	    static void cancelTicket(Scanner sc) throws SQLException, IOException {
	        log("Booking ID : ");
	        int id = sc.nextInt();

	        PreparedStatement ps = con.prepareStatement("DELETE FROM bookings WHERE booking_id=?");
	        ps.setInt(1,id);
	        int rows = ps.executeUpdate();

	        if(rows == 0) throw new SQLException("Invalid Booking ID");
	        log("Deleted Rows = " + rows);

	        ResultSet rs = con.createStatement().executeQuery("SELECT COUNT(*) FROM bookings");
	        rs.next();
	        log("Remaining Bookings = " + rs.getInt(1));
	    }

	    // PASSENGER DETAILS
	    static void passengerDetails(Scanner sc) throws SQLException, IOException {
	        log("Email : ");
	        String email = sc.next();

	        String sql = "SELECT p.name,p.email,b.booking_id,b.bus_id,b.seat_no,b.booking_date "
	                   + "FROM passengers p "
	                   + "JOIN bookings b "
	                   + "ON p.passenger_id=b.passenger_id "
	                   + "WHERE p.email=?";
	        PreparedStatement ps = con.prepareStatement(sql);
	        ps.setString(1,email);
	        ResultSet rs = ps.executeQuery();

	        int count = 0;
	        while(rs.next()) {
	            log("BookingID: " + rs.getInt("booking_id") + " | Name: " + rs.getString("name") + " | Bus: " + rs.getInt("bus_id") + " | Seat: " + rs.getInt("seat_no") + " | Date: " + rs.getDate("booking_date"));
	            count++;
	        }
	        log("Total Rows = " + count);
	    }


	    static void customQueries() throws SQLException, IOException {
	        log("\n--- 4 CUSTOMISED QUERIES ---");
	        Statement stmt = con.createStatement();

	        // Q1: TN: UNION - Same column name 'name' from 2 tables
	        log("\nQUERY 1: All Names - Bus and Passenger [UNION]");
	        ResultSet rs1 = stmt.executeQuery("SELECT bus_name as name FROM buses UNION SELECT name FROM passengers");
	        while(rs1.next()) 
	            log(" - " + rs1.getString(1));

	        // Q2: TN: WHERE CLAUSE
	        log("\nQUERY 2: Buses with more than 30 seats [WHERE]");
	        ResultSet rs2 = stmt.executeQuery("SELECT bus_id, bus_name, total_seats FROM buses WHERE total_seats > 30");
	        while(rs2.next()) 
	            log("ID: " + rs2.getInt("bus_id") + " | " + rs2.getString("bus_name") + " - " + rs2.getInt("total_seats") + " Seats");

	        // Q3: TN: GROUP BY + JOIN
	        log("\nQUERY 3: Booking Count per Bus [GROUP BY]");
	        ResultSet rs3 = stmt.executeQuery("SELECT b.bus_id, b.bus_name, COUNT(*) total FROM bookings bk JOIN buses b ON bk.bus_id=b.bus_id GROUP BY b.bus_id, b.bus_name");
	        while(rs3.next()) 
	            log("Bus: " + rs3.getString("bus_name") + " | Total Bookings: " + rs3.getInt("total"));

	        // Q4: TN: JOIN + DATE
	        log("\nQUERY 4: Today's Bookings [JOIN + CURDATE]");
	        ResultSet rs4 = stmt.executeQuery("SELECT p.name, p.email, b.booking_id, b.seat_no FROM bookings b JOIN passengers p ON b.passenger_id=p.passenger_id WHERE b.booking_date=CURDATE()");
	        int todayCount = 0;
	        while(rs4.next()) {
	            log("Name: " + rs4.getString("name") + " | Email: " + rs4.getString("email") + " | BookingID: " + rs4.getInt("booking_id") + " | Seat: " + rs4.getInt("seat_no"));
	            todayCount++;
	        }
	        log("Total Bookings Today = " + todayCount);
	    }
	}

		    	