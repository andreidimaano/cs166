/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Formatter;
import java.lang.Math;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */
public class Retail {

   // reference to physical database connection.
   private Connection _connection = null;
   private int userId = -1;
   private double userLatitude = 0.0;
   private double userLongitude = 0.0;
   private String userType = "";

   // handling the keyboard inputs through a BufferedReader
   // This variable can be global for convenience.
   static BufferedReader in = new BufferedReader(
                                new InputStreamReader(System.in));

   /**
    * Creates a new instance of Retail shop
    *
    * @param hostname the MySQL or PostgreSQL server hostname
    * @param database the name of the database
    * @param username the user name used to login to the database
    * @param password the user login password
    * @throws java.sql.SQLException when failed to make a connection.
    */
   public Retail(String dbname, String dbport, String user, String passwd) throws SQLException {

      System.out.print("Connecting to database...");
      try{
         // constructs the connection URL
         String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
         System.out.println ("Connection URL: " + url + "\n");

         // obtain a physical connection
         this._connection = DriverManager.getConnection(url, user, passwd);
         System.out.println("Done");
      }catch (Exception e){
         System.err.println("Error - Unable to Connect to Database: " + e.getMessage() );
         System.out.println("Make sure you started postgres on this machine");
         System.exit(-1);
      }//end catch
   }//end Retail

   public int getUserId() {
      return this.userId;      
   }

   public double getUserLatitude() {
      return this.userLatitude;      
   }

   public double getUserLongitude() {
      return this.userLongitude;      
   }

   public String getUserType() {
      return this.userType;
   }

   public void updateUserId(int userId) {
      this.userId = userId;
   }

   public void updateUserLatitude(double userLatitude) {
      this.userLatitude = userLatitude;
   }

   public void updateUserLongitude(double userLongitude) {
      this.userLongitude = userLongitude;
   }
   
   public void updateUserType(String userType) {
      this.userType = userType;
   }

   // Method to calculate euclidean distance between two latitude, longitude pairs. 
   public double calculateDistance (double lat1, double long1, double lat2, double long2){
      double t1 = (lat1 - lat2) * (lat1 - lat2);
      double t2 = (long1 - long2) * (long1 - long2);
      return Math.sqrt(t1 + t2); 
   }
   /**
    * Method to execute an update SQL statement.  Update SQL instructions
    * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
    *
    * @param sql the input SQL string
    * @throws java.sql.SQLException when update failed
    */
   public void executeUpdate (String sql) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the update instruction
      stmt.executeUpdate (sql);

      // close the instruction
      stmt.close ();
   }//end executeUpdate

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and outputs the results to
    * standard out.
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQueryAndPrintResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and output them to standard out.
      boolean outputHeader = true;
      List<Integer> maxColumnSizes = new ArrayList<Integer>();
      for(int i = 1; i <= numCol; i++) {
         maxColumnSizes.add(rsmd.getColumnName(i).length());
      }

      List<List<String>> results = new ArrayList<List<String>>();
      List<String> header = new ArrayList<String>();
      for(int i = 1; i <= numCol; i++) {
         header.add(rsmd.getColumnName(i));
      }
      results.add(header);

      while (rs.next()){
         List<String> row = new ArrayList<String>();
         for (int i=1; i<=numCol; ++i){
            row.add(rs.getString(i).trim());
            maxColumnSizes.set(i - 1, Math.max(maxColumnSizes.get(i-1), rs.getString(i).trim().length()));
         }
         results.add(row);
         ++rowCount;
      }//end while

      for(List<String> r: results) {
         for(int i = 0; i < r.size(); i++) {
            String space = "";
            for(int j = 0; j < maxColumnSizes.get(i)- r.get(i).length() + 2; j++) {
               space += " ";
            }
            System.out.print(r.get(i)+space);
         }
         System.out.println();
      }
      stmt.close ();
      return rowCount;
   }//end executeQuery

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the results as
    * a list of records. Each record in turn is a list of attribute values
    *
    * @param query the input query string
    * @return the query result as a list of records
    * @throws java.sql.SQLException when failed to execute the query
    */
   public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and saves the data returned by the query.
      boolean outputHeader = false;
      List<List<String>> result  = new ArrayList<List<String>>();
      while (rs.next()){
        List<String> record = new ArrayList<String>();
		for (int i=1; i<=numCol; ++i)
			record.add(rs.getString (i));
        result.add(record);
      }//end while
      stmt.close ();
      return result;
   }//end executeQueryAndReturnResult

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the number of results
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQuery (String query) throws SQLException {
       // creates a statement object
       Statement stmt = this._connection.createStatement ();

       // issues the query instruction
       ResultSet rs = stmt.executeQuery (query);

       int rowCount = 0;

       // iterates through the result set and count nuber of results.
       while (rs.next()){
          rowCount++;
       }//end while
       stmt.close ();
       return rowCount;
   }

   /**
    * Method to fetch the last value from sequence. This
    * method issues the query to the DBMS and returns the current
    * value of sequence used for autogenerated keys
    *
    * @param sequence name of the DB sequence
    * @return current value of a sequence
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int getCurrSeqVal(String sequence) throws SQLException {
	Statement stmt = this._connection.createStatement ();

	ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
	if (rs.next())
		return rs.getInt(1);
	return -1;
   }

   /**
    * Method to close the physical connection if it is open.
    */
   public void cleanup(){
      try{
         if (this._connection != null){
            this._connection.close ();
         }//end if
      }catch (SQLException e){
         // ignored.
      }//end try
   }//end cleanup


   /**
    * The main execution method
    *
    * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
    */
   public static void main (String[] args) {
      if (args.length != 3) {
         System.err.println (
            "Usage: " +
            "java [-classpath <classpath>] " +
            Retail.class.getName () +
            " <dbname> <port> <user>");
         return;
      }//end if

      Greeting();
      Retail esql = null;
      try{
         // use postgres JDBC driver.
         Class.forName ("org.postgresql.Driver").newInstance ();
         // instantiate the Retail object and creates a physical
         // connection.
         String dbname = args[0];
         String dbport = args[1];
         String user = args[2];
         esql = new Retail (dbname, dbport, user, "");

         boolean keepon = true;
         while(keepon) {
            // These are sample SQL statements
            System.out.println("MAIN MENU");
            System.out.println("---------");
            System.out.println("1. Create user");
            System.out.println("2. Log in");
            System.out.println("9. < EXIT");
            String authorisedUser = null;
            switch (readChoice()){
               case 1: CreateUser(esql); break;
               case 2: authorisedUser = LogIn(esql); break;
               case 9: keepon = false; break;
               default : System.out.println("Unrecognized choice!"); break;
            }//end switch
            if (authorisedUser != null) {
               System.out.println(esql.getUserType());
              boolean usermenu = true;
              while(usermenu) {
                System.out.println("MAIN MENU");
                System.out.println("---------");
                System.out.println("1. View Stores within 30 miles");
                System.out.println("2. View Product List");
                System.out.println("3. Place a Order");
                System.out.println("4. View 5 recent orders");

                if(esql.getUserType().equals("manager") || esql.getUserType().equals("admin")) {
                  System.out.println("5. Update Product");
                  System.out.println("6. View 5 recent Product Updates Info");
                  System.out.println("7. View 5 Popular Items");
                  System.out.println("8. View 5 Popular Customers");
                  System.out.println("9. Place Product Supply Request to Warehouse");
                  System.out.println("10. View Managed Stores");
                  System.out.println("11. View Customer Orders");
                  System.out.println("12. View Managed Products");
                }

                if(esql.getUserType().equals("admin")) {
                  System.out.println("13. View All Products");
                  System.out.println("14. View All Users");
                  System.out.println("15. Update User");
                }


                
                System.out.println(".........................");
                System.out.println("20. Log out");
                
                int choice = readChoice();
                if((esql.getUserType().equals("customer") && choice >= 5 && choice != 20) || (esql.getUserType().equals("manager") && choice >= 13 && choice != 20)) {
                  System.out.println("Unauthorized access");
                  continue;
                } else {
                  switch (choice){
                     case 1: viewStores(esql); break;
                     case 2: viewProducts(esql); break;
                     case 3: placeOrder(esql); break;
                     case 4: viewRecentOrders(esql); break;

                     // managers / admins
                     case 5: updateProduct(esql); break;
                     case 6: viewRecentUpdates(esql); break;
                     case 7: viewPopularProducts(esql); break;
                     case 8: viewPopularCustomers(esql); break;
                     case 9: placeProductSupplyRequests(esql); break;
                     case 10: viewManagedStores(esql); break;
                     case 11: viewCustomerOrders(esql); break;
                     case 12: viewManagedProducts(esql); break;

                     // admin
                     case 13: viewAllProducts(esql); break;
                     case 14: viewUsers(esql); break;
                     case 15: updateUser(esql); break;

                     case 20: 
                        usermenu = false;
                        esql.updateUserId(-1);
                        esql.updateUserLatitude(0.0);
                        esql.updateUserLongitude(0.0);
                        esql.updateUserType("");
                        break;
                     default : System.out.println("Unrecognized choice!"); break;
                  }
                }
              }
            }
         }//end while
      }catch(Exception e) {
         System.err.println (e.getMessage ());
      }finally{
         // make sure to cleanup the created table and close the connection.
         try{
            if(esql != null) {
               System.out.print("Disconnecting from database...");
               esql.cleanup ();
               System.out.println("Done\n\nBye !");
            }//end if
         }catch (Exception e) {
            // ignored.
         }//end try
      }//end try
   }//end main

   public static void Greeting(){
      System.out.println(
         "\n\n*******************************************************\n" +
         "              User Interface      	               \n" +
         "*******************************************************\n");
   }//end Greeting

   /*
    * Reads the users choice given from the keyboard
    * @int
    **/
   public static int readChoice() {
      int input;
      // returns only if a correct value is given.
      do {
         System.out.print("Please make your choice: ");
         try { // read the integer, parse it and break.
            input = Integer.parseInt(in.readLine());
            break;
         }catch (Exception e) {
            System.out.println("Your input is invalid!");
            continue;
         }//end try
      }while (true);
      return input;
   }//end readChoice

   /*
    * Creates a new user
    **/
   public static void CreateUser(Retail esql){
      try{
         System.out.print("\tEnter name: ");
         String name = in.readLine();
         System.out.print("\tEnter password: ");
         String password = in.readLine();
         System.out.print("\tEnter latitude: ");   
         String latitude = in.readLine();       //enter lat value between [0.0, 100.0]
         System.out.print("\tEnter longitude: ");  //enter long value between [0.0, 100.0]
         String longitude = in.readLine();
         
         String type="Customer";

			String query = String.format("INSERT INTO USERS (name, password, latitude, longitude, type) VALUES ('%s','%s', %s, %s,'%s')", name, password, latitude, longitude, type);

         esql.executeUpdate(query);
         System.out.println ("User successfully created!");
      }catch(Exception e){
         System.err.println (e.getMessage ());
      }
   }//end CreateUser


   /*
    * Check log in credentials for an existing user
    * @return User login or null if the user does not exist
    **/
   public static String LogIn(Retail esql){ // Andrei
      try{
         System.out.print("\tEnter name: ");
         String name = in.readLine();
         System.out.print("\tEnter password: ");
         String password = in.readLine();

         String query = String.format("SELECT userId, name, latitude, longitude, type FROM USERS WHERE name = '%s' AND password = '%s'", name, password);
         List<List<String>> queryResult = esql.executeQueryAndReturnResult(query);
	      if (queryResult.size() > 0) {
            esql.updateUserId(Integer.parseInt(queryResult.get(0).get(0)));
		      esql.updateUserLatitude(Double.parseDouble(queryResult.get(0).get(2)));
            esql.updateUserLongitude(Double.parseDouble(queryResult.get(0).get(3)));;
            esql.updateUserType(queryResult.get(0).get(4).trim());
            
            return queryResult.get(0).get(1);
         }
         // if it doesn't exit, print user login doesn't exist
         System.out.println("User does not exist");
         return null;
      } catch(Exception e){
         System.err.println (e.getMessage ());
         return null;
      }
   }//end

// Rest of the functions definition go in here

   public static void viewStores(Retail esql) { // Andrei
      try{
         String query = String.format("SELECT s.storeID, s.name, calculate_distance(u.latitude, u.longitude, s.latitude, s.longitude) as dist FROM Users u, Store s WHERE u.userID = %d AND calculate_distance(u.latitude, u.longitude, s.latitude, s.longitude) <= 30 ORDER BY dist", esql.getUserId());
         System.out.println ("Stores in your area: ");
         int rowCount = esql.executeQueryAndPrintResult(query);
         System.out.println ("total stores: " + rowCount);

         System.out.println ();
         System.out.println("Popular Stores in your area:");
         String query2 = String.format("SELECT X.storeID, X.name, Count(*) as TotalOrders FROM (SELECT s.storeID, s.name, calculate_distance(u.latitude, u.longitude, s.latitude, s.longitude) as dist FROM Users u, Store s WHERE u.userID = %d AND calculate_distance(u.latitude, u.longitude, s.latitude, s.longitude) <= 30) as X ,Orders O WHERE X.storeID = O.storeID GROUP BY X.storeID, X.name ORDER BY COUNT(*) DESC", esql.getUserId());
         int rowCount2 = esql.executeQueryAndPrintResult(query2);
         System.out.println ("total stores: " + rowCount2);

         System.out.println ();
         System.out.println("Order from these stores again:");
         String query3 = String.format("SELECT s.storeID, s.name FROM Users u, (SELECT * FROM Store S WHERE S.storeID IN (SELECT O.storeID FROM Orders O WHERE O.customerID = %d)) as s WHERE u.userID = %d AND calculate_distance(u.latitude, u.longitude, s.latitude, s.longitude) <= 30", esql.getUserId(), esql.getUserId());
         int rowCount3 = esql.executeQueryAndPrintResult(query3);
         System.out.println ("total stores: " + rowCount3);

         /* TODO
          1. Recently Shopped at Stores
         */
      } catch(Exception e){
         System.err.println (e.getMessage());
      }
   }
   public static void viewProducts(Retail esql) {
      try{
         String query = "SELECT * FROM Product P WHERE P.storeID = '";
         System.out.print("\tEnter s: $");
         String input = in.readLine();
         query += (input + "'");
         System.out.println ("Products at store " + input + ":");
         int rowCount = esql.executeQueryAndPrintResult(query);
         System.out.println ("total products(s): " + rowCount);
         System.out.println();

         System.out.println("Products filtered by price (High to Low):");
         String query2 = String.format("SELECT * FROM Product P WHERE P.storeID = %s ORDER BY P.pricePerUnit DESC", input);
         int rowCount2 = esql.executeQueryAndPrintResult(query2);
         System.out.println ("total products(s): " + rowCount2);
         System.out.println();
         
         System.out.println("Products filtered by price (Low to High):");
         String query3 = String.format("SELECT * FROM Product P WHERE P.storeID = %s ORDER BY P.pricePerUnit", input);
         int rowCount3 = esql.executeQueryAndPrintResult(query3);
         System.out.println ("total products(s): " + rowCount3);
         System.out.println();

         System.out.println("Frequently Bought Products:");
         String query4 = String.format("SELECT DISTINCT p.productName, p.pricePerUnit FROM Product p WHERE p.productName IN (SELECT O.productName FROM Orders O WHERE O.storeID = %s GROUP BY O.productName ORDER BY COUNT(*) DESC LIMIT 5)", input);
         int rowCount4 = esql.executeQueryAndPrintResult(query4);
         System.out.println ("total products(s): " + rowCount4);
         System.out.println();

      }catch(Exception e){
         System.err.println (e.getMessage());
      }
   }
   public static void viewUserInformation(Retail esql) { // Andrei
      // for admins
      try{
         String query = "SELECT * FROM Users";
         System.out.println ("User Information: ");
         int rowCount = esql.executeQueryAndPrintResult(query);
         System.out.println ("total users(s): " + rowCount);
      }catch(Exception e){
         System.err.println (e.getMessage());
      }
   }
   public static void placeOrder(Retail esql) {}
   public static void viewRecentOrders(Retail esql) {
      try{
         String query = String.format("SELECT O.storeID, O.productName, O.unitsOrdered, O.orderTime FROM Orders O WHERE O.customerID = %d ORDER BY O.orderTime DESC LIMIT 5", esql.getUserId());
         System.out.println ("Recent Orders: ");
         int rowCount = esql.executeQueryAndPrintResult(query);
         System.out.println ("total products(s) in order history: " + rowCount);
      }catch(Exception e){
         System.err.println (e.getMessage());
      }
   }

   // check if they are manager or admin for below:

   public static void updateProduct(Retail esql) {}
   public static void viewRecentUpdates(Retail esql) {
      try{
         String query = String.format("SELECT * FROM ProductUpdates PU, Product P WHERE PU.storeID = P.storeID AND PU.productName = P.productName AND PU.managerID = %d ORDER BY PU.updatedOn DESC LIMIT 5", esql.getUserId());
         System.out.println ("Recent Updates: ");
         int rowCount = esql.executeQueryAndPrintResult(query);
         System.out.println ("total product(s) recently updated(5 max shown): " + rowCount);
      }catch(Exception e){
         System.err.println (e.getMessage());
      }
   }
   public static void viewPopularProducts(Retail esql) {
      try{
         String query = String.format("SELECT O.productName, COUNT(*) FROM Orders O WHERE O.storeID IN (SELECT S.storeID FROM Store S WHERE S.managerID = %d) GROUP BY O.productName ORDER BY COUNT(*) DESC LIMIT 5", esql.getUserId());
         System.out.println ("Popular Products at stores you manage: ");
         int rowCount = esql.executeQueryAndPrintResult(query);
         System.out.println ("total popular product(s)(5 max shown): " + rowCount);
      }catch(Exception e){
         System.err.println (e.getMessage());
      }
   }
   public static void viewPopularCustomers(Retail esql) {
      try{
         String query = String.format("SELECT O.customerID, COUNT(*) FROM Orders O WHERE O.storeID IN (SELECT S.storeID FROM Store S WHERE S.managerID = %d) GROUP BY O.customerID ORDER BY COUNT(*) DESC LIMIT 5", esql.getUserId());
         System.out.println ("Popular Customers at stores you manage: ");
         int rowCount = esql.executeQueryAndPrintResult(query);
         System.out.println ("total popular customer(s)(5 max shown): " + rowCount);
      }catch(Exception e){
         System.err.println (e.getMessage());
      }
   }
   public static void viewManagedStores(Retail esql) {
      try{
         String query = String.format("SELECT * FROM Store S WHERE S.managerID = %d", esql.getUserId());
         System.out.println ("Stores you manage: ");
         int rowCount = esql.executeQueryAndPrintResult(query);
         System.out.println ("total Managed Stores: " + rowCount);
      }catch(Exception e) {
         System.err.println(e.getMessage());
      }
   }
   public static void viewCustomerOrders(Retail esql) {
      try{
         String query = String.format("SELECT O.orderNumber, U.name as customer_name, O.storeID, O.productName, O.orderTime FROM Orders O, Users U WHERE O.storeID IN (SELECT s.storeID FROM Store s WHERE s.managerID = %d) AND U.userID = O.customerID", esql.getUserId());
         System.out.println ("Customer Orders from stores you manage: ");
         int rowCount = esql.executeQueryAndPrintResult(query);
         System.out.println ("total orders: " + rowCount);
      }catch(Exception e) {
         System.err.println(e.getMessage());
      }
   }
   public static void viewUsers(Retail esql) {
      try{
         String query = "SELECT * FROM Users U";
         System.out.println ("User Information: ");
         int rowCount = esql.executeQueryAndPrintResult(query);
         System.out.println ("total users: " + rowCount);
      }catch(Exception e) {
         System.err.println(e.getMessage());
      }
   }
   public static void viewAllProducts(Retail esql) {
      try{
         String query = "SELECT * FROM Product P";
         System.out.println ("User Information: ");
         int rowCount = esql.executeQueryAndPrintResult(query);
         System.out.println ("total products: " + rowCount);
      }catch(Exception e) {
         System.err.println(e.getMessage());
      }
   }

   public static void viewManagedProducts(Retail esql) {
      try{
         String query = String.format("SELECT * FROM Product P WHERE P.storeID IN (SELECT s.storeID FROM Store s WHERE s.managerID = %d)", esql.getUserId());
         System.out.println ("Product information of stores you manage: ");
         int rowCount = esql.executeQueryAndPrintResult(query);
         System.out.println ("total products: " + rowCount);

         query = String.format("SELECT * FROM Product P WHERE P.storeID IN (SELECT s.storeID FROM Store s WHERE s.managerID = %d) ORDER BY P.numberOfUnits", esql.getUserId());
         System.out.println ("Filtered by inventory: ");
         rowCount = esql.executeQueryAndPrintResult(query);
         System.out.println ("total products: " + rowCount);
      }catch(Exception e) {
         System.err.println(e.getMessage());
      }
   }

   public static void updateUser(Retail esql) {
      try {
         System.out.print("\tInput User ID: ");
         String userID = in.readLine();
         
         String query = String.format("SELECT * FROM Users U WHERE U.userID = %s", userID);
         //make sure the Product exists in the provided store
         List<List<String>> result = esql.executeQueryAndReturnResult(query);
         if (result.size() == 0) {
            System.out.println((String.format("User '%s' does not exist", userID)));
            return;
         }

         System.out.print("\tInput new name: ");
         String name = in.readLine();

         System.out.print("\tInput new password: ");
         String password = in.readLine();

         System.out.print("\tInput new latitude: ");
         String latitude = in.readLine();

         System.out.print("\tInput new longitude: ");
         String longitude = in.readLine();

         System.out.print("\tInput new type: ");
         String type = in.readLine();

         query = String.format (
            "UPDATE User SET name = %s, password = %s, latitude = %s, longitude = %s, type = %s WHERE userID = %s",
            name,
            password,
            latitude,
            longitude,
            type
         );
         esql.executeUpdate(query);

         System.out.println("User information succesfully updated");


      } catch (Exception e) {
         System.err.println (e.getMessage());
      }
   }
   public static void placeProductSupplyRequests(Retail esql) {}
}//end Retail

