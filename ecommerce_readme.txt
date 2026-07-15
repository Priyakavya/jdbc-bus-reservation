===========================================
        E-COMMERCE MANAGEMENT SYSTEM-PROJECT
===========================================

1. PROJECT DESCRIPTION
This is a Console based E-Commerce Application developed using Java, JDBC and MySQL.
It uses Cart concept before placing order. All operations are done with Transaction Management,
Row Count Validation, Batch Processing and 4 Custom SQL Queries.
All output is saved to "ECommerce_Output.txt" file.

2. FEATURES IMPLEMENTED AS PER TN REQUIREMENTS
[1] DATABASE: MySQL
    - 4 Tables: products, customers, orders, order_items
    - Constraints: PRIMARY KEY, FOREIGN KEY, UNIQUE, NOT NULL, ON DELETE CASCADE

[2] CRUD OPERATIONS
    C - Create: Add to Cart, Place Order
    R - Read : View Products, Track Order
    U - Update: Update Product
    D - Delete: Delete Product

[3] TRANSACTION MANAGEMENT
    - con.setAutoCommit(false)
    - con.commit() on success
    - con.rollback() on error

[4] VALIDATION
    - Row Count checked after Insert/Update/Delete/Batch
    - Java validation for price>0, stock>=0, quantity>0

[5] BATCH PROCESSING
    - Batch Insert for Sample Products
    - Batch Insert for order_items
    - Batch Update for stock

[6] 4 CUSTOMISED QUERIES
    Q1: WHERE + ORDER BY -> Products with low stock
    Q2: GROUP BY + AGGREGATE -> Total Quantity Sold per Product
    Q3: JOIN 3+ TABLES -> Customer Order History
    Q4: UNION -> All Names from Products and Customers

[7] FILE OUTPUT
    - All console output is also written to "ECommerce_Output.txt"

3. SOFTWARE REQUIREMENTS
- JDK 8 or above
- MySQL 8.0
- MySQL Connector/J 8.0.x jar
- Eclipse / NetBeans / VS Code

4. HOW TO RUN
Step 1: Open MySQL and start service.
Step 2: Change MySQL password in code:
        static final String PASS = "your_password";
Step 3: Add mysql-connector-j jar to project Build Path.
Step 4: Compile: javac ECommerceSystem.java
Step 5: Run: java ECommerceSystem
Step 6: Follow menu options.

5. DEMO FLOW FOR VIVA
9 -> Insert Sample Data
1 -> View Products
2 -> Add ProductID 1, Qty 2 to Cart
2 -> Add ProductID 2, Qty 1 to Cart 
3 -> View Cart
4 -> Place Order. Name: Priya, Email: priya@gmail.com
5 -> Track Order. Enter OrderID: 1
6 -> Update Product
7 -> Delete Product
8 -> Show 4 Custom Queries
10 -> Exit

6. FILES INCLUDED
1. ECommerceSystem.java -> Main Source Code
2. ECommerce_Output.txt -> Output Log File
3. ecommerce_readme.txt -> This File

7. DEVELOPED BY
Name: [PRIYA K N]

===========================================