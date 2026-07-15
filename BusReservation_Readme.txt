=====================================================================
           BUS RESERVATION SYSTEM - JDBC PROJECT
           Developed By: Priya K N
=====================================================================

1. PROJECT DESCRIPTION
----------------------
This is a Console based Bus Reservation System using Java and JDBC.
Passengers can book seats online, search buses, cancel tickets and 
view passenger details. All data is stored in MySQL Database.
All output is also saved to "BusReservation_Output.txt" file.

2. MODULES IMPLEMENTED
----------------------
1.  Search Buses       : Search buses by Source and Destination using LIKE
2.  Seat Booking       : Book seat with validation. Checks if seat is already booked
3.  Ticket Cancellation: Cancel ticket by Booking ID
4.  Passenger Details  : View all bookings of a passenger using Email + JOIN
5.  Add Bus            : Admin can add new buses - Multiple Insertion
6.  Update Bus         : Admin can update total seats
7.  Custom Queries     : 4 Custom Reports - UNION, WHERE, GROUP BY, JOIN

3. DATABASE TABLES
------------------
Table 1: buses
  bus_id      - INT, PRIMARY KEY, AUTO_INCREMENT
  bus_name    - VARCHAR(50), NOT NULL
  source      - VARCHAR(50), NOT NULL
  destination - VARCHAR(50), NOT NULL
  total_seats - INT, CHECK > 0

Table 2: passengers
  passenger_id - INT, PRIMARY KEY, AUTO_INCREMENT
  name         - VARCHAR(50), NOT NULL
  email        - VARCHAR(100), UNIQUE, NOT NULL

Table 3: bookings
  booking_id   - INT, PRIMARY KEY, AUTO_INCREMENT
  bus_id       - INT, FOREIGN KEY REFERENCES buses(bus_id) ON DELETE CASCADE
  passenger_id - INT, FOREIGN KEY REFERENCES passengers(passenger_id)
  seat_no      - INT, NOT NULL
  booking_date - DATE
  CONSTRAINT   - UNIQUE(bus_id, seat_no) -- Prevents double booking

4. JDBC CONCEPTS USED
---------------------
1.  Database and Table Creation in Application
2.  PreparedStatement for all CRUD operations
3.  Transactions: con.setAutoCommit(false), commit(), rollback()
4.  Seat Availability Validation before booking
5.  Row Count Validation after insert/update/delete
6.  ResultSet for fetching data
7.  JOIN, UNION, GROUP BY, WHERE, LIKE queries
8.  FileWriter for saving output to TXT file

5. HOW TO RUN
-------------
Step 1: Change DB Password in code: static final String PASS = "your_password";
Step 2: Add mysql-connector-j-9.7.0.jar to Build Path
Step 3: Run BusReservationSystem.java as Java Application
Step 4: Follow menu options 1 to 8
Step 5: Check "BusReservation_Output.txt" in project folder for output log

6. SAMPLE TEST CASE
-------------------
1.  Press 1 -> Add 2 sample buses
2.  Press 2 -> Search Source:Bangalore Destination:Chennai
3.  Press 3 -> Book Seat: BusID=1, Name=Priya, Email=priya@gmail.com, Seat=5
4.  Press 3 -> Try booking same seat again -> Transaction Rollback
5.  Press 6 -> View Passenger Details by Email
6.  Press 7 -> View 4 Custom Reports
7.  Press 5 -> Cancel Ticket by BookingID
8.  Press 8 -> Exit

7. OUTPUT FILE
--------------
All console output is automatically saved to: BusReservation_Output.txt

