================================================================
         EMPLOYEE LEAVE MANAGEMENT SYSTEM PROJECT
               JAVA + JDBC + MYSQL + TRANSACTION
================================================================

1. AIM
To develop a Console based Employee Leave Management System using Java, JDBC and 
MySQL with modules for Employee Registration, Leave Application, Approval and 
Leave Balance Tracking.

2. OBJECTIVES
1. Implement CRUD operations for Employee and Leave data
2. Use JDBC to connect Java with MySQL Database
3. Implement Transaction Management - Commit and Rollback
4. Implement Date Handling for leave dates
5. Use Batch Processing for multiple employee insertion
6. Execute 4 Custom SQL Queries
7. Write all output to a text file "Leave_Output.txt"

3. SOFTWARE & HARDWARE REQUIREMENTS
Software:
   - JDK 1.8 or above
   - MySQL Server 8.0
   - MySQL Connector/J 8.0.x
   - Eclipse / NetBeans / VS Code
Hardware:
   - RAM: 4GB minimum
   - OS: Windows 10 / 11

4. DATABASE DESIGN
Database Name: leave_db

Table 1: employees
emp_id       INT  PK, AUTO_INCREMENT
emp_name     VARCHAR(100) NOT NULL
email        VARCHAR(100) UNIQUE NOT NULL
department   VARCHAR(50) NOT NULL
join_date    DATE NOT NULL

Table 2: leave_balance
balance_id   INT PK, AUTO_INCREMENT
emp_id       INT FK ON DELETE CASCADE
emp_name     VARCHAR(100)        -- TN: SAME COLUMN NAME AS employees
total_leaves INT DEFAULT 20
leaves_taken INT DEFAULT 0

Table 3: leave_requests
leave_id     INT PK, AUTO_INCREMENT
emp_id       INT FK ON DELETE CASCADE
leave_type   VARCHAR(20) NOT NULL  -- CL, SL, PL
start_date   DATE NOT NULL         -- TN: DATE HANDLING
end_date     DATE NOT NULL
reason       VARCHAR(200)
status       VARCHAR(20) DEFAULT 'PENDING'
applied_date DATE

5. MODULES DESCRIPTION
Module 1: EMPLOYEE REGISTRATION
   - Register new employee with validation
   - Auto create leave balance = 20

Module 2: APPLY LEAVE
   - Employee can apply for CL/SL/PL
   - Date validation using YYYY-MM-DD format

Module 3: APPROVE/REJECT LEAVE
   - HR/Admin can APPROVE or REJECT
   - If APPROVED, auto deduct days from leave_balance using DATEDIFF

Module 4: LEAVE BALANCE TRACKING
   - View total leaves, taken leaves, remaining leaves
   - Update total leaves

6. JDBC CONCEPTS IMPLEMENTED
1. CRUD OPERATIONS
   C - Create: Register Employee, Apply Leave
   R - Read  : View Employees, View Requests, View Balance
   U - Update: Approve Leave, Update Balance
   D - Delete: Delete Employee
   TN: Row Count validation after every insert/update/delete

2. TRANSACTION MANAGEMENT
   - con.setAutoCommit(false)
   - con.commit() on success
   - con.rollback() on error

3. BATCH PROCESSING
   - Multiple Employee Insert using addBatch() and executeBatch()

4. DATE HANDLING
   - CURDATE(), Date.valueOf(), DATEDIFF()

5. CONSTRAINTS
   - PRIMARY KEY, FOREIGN KEY, UNIQUE, NOT NULL, ON DELETE CASCADE

6. 4 CUSTOMISED QUERIES
   Q1: WHERE -> Pending Leaves
   Q2: GROUP BY + COUNT -> Leave Count by Type
   Q3: JOIN 2 TABLES -> Employee Leave Details
   Q4: BETWEEN -> Recent Leaves in last 30 days

7. STEPS TO EXECUTE
Step 1: Start MySQL Server
Step 2: Change DB password in code: PASS = "your_password"
Step 3: Add mysql-connector-j.jar to project Build Path
Step 4: Compile: javac EmployeeLeaveSystem.java
Step 5: Run: java EmployeeLeaveSystem
Step 6: Follow menu options
Step 7: Check "Leave_Output.txt" for saved output

8. SAMPLE DEMO FLOW FOR VIVA
10 -> Batch Insert Employees
2  -> View Employees
1  -> Register Employee
3  -> Apply Leave
5  -> View Leave Requests
4  -> Approve Leave
6  -> View Leave Balance
9  -> Custom Queries
7  -> Update Balance
8  -> Delete Employee
11 -> Exit

9. OUTPUT
All console output is duplicated in "Leave_Output.txt" file.

10. CONCLUSION
This project successfully implements a real-world Leave Management System with
CRUD, Transaction, Batch, Date Handling and Custom Queries using Java and MySQL.

11. FUTURE ENHANCEMENT
- Add GUI using JavaFX/Swing
- Add Email Notification on Leave Approval
- Add Login for Admin and Employee

================================================================
DEVELOPED BY  : [PRIYA K N]
================================================================