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
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map.Entry;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Formatter;
import java.lang.Math;
import javax.swing.*;
import java.awt.*;
import java.util.concurrent.TimeUnit;



/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */
public class Retail {
   public static Integer HEIGHT = 800;
   public static Integer WIDTH = 900;
   public static Color myBackground = new Color(225,255,244);
   public static Color purple = new Color(230,222,255);
   public static Color darkPurple = new Color(214, 202, 250);
   public static Color pink = new Color(255, 240, 255);
   public static Color darkPink = new Color(242, 230, 255);
   public static Color blue = new Color(52, 152, 235);
   // reference to physical database connection.
   private Connection _connection = null;
   private int userId = -1;
   private double userLatitude = 0.0;
   private double userLongitude = 0.0;
   private String userType = "";
   private Boolean isGUI = false;

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

   public Boolean getIsGUI() {
      return this.isGUI;
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

   public void updateIsGUI(Boolean isGUI) {
      this.isGUI = isGUI;
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

   public static Box ContentBox(Retail esql) {
      // Menu Box
      Box content = Box.createHorizontalBox();
      content.setPreferredSize(new Dimension(650, 600));

      JPanel loginPanel = new JPanel();
      // loginPanel.setLayout(new GridBagLayout());
      loginPanel.setPreferredSize(new Dimension(600, 400));
      loginPanel.setBackground(myBackground);
      JLabel UserText = new JLabel("User");
      JTextField UserInputBox = new JTextField(10);
      JLabel PasswordText = new JLabel("Password");
      JTextField PasswordInputBox = new JTextField(10);
      JButton submitButton = new JButton("Submit");
      JButton createUserButton = new JButton("New User");

      submitButton.addActionListener(e -> {
         System.out.println("Attempting to Log In!");
         String username = UserInputBox.getText();
         String password = PasswordInputBox.getText();
         
         String verifiedUser = LogIn(esql, username, password);
         if (verifiedUser == null) {
            return;
         }

         loginPanel.setVisible(false);

         // View Stores within 30 miles
         JPanel viewStorePanel = ViewStorePanel(esql);

         // View Products
         JPanel viewProductsPanel = ViewProductsPanel(esql);

         // Place and View Orders
         JPanel placeAndViewOrders = PlaceAndViewOrders(esql);

         JTabbedPane tabbedPane = new JTabbedPane();
         tabbedPane.setBackground(purple);
         tabbedPane.addTab("Stores", null, viewStorePanel, "View Stores In Your Area");
         tabbedPane.addTab("Products", null, viewProductsPanel, "View Products List");
         tabbedPane.addTab("Order", null, placeAndViewOrders, "Place and View Recent Orders");


         // Managers and Admins
         if(esql.getUserType().equals("manager") || esql.getUserType().equals("admin")) {
            // Update Product and View Recent Updates
            JPanel productUpdatesPanel = ProductUpdatesPanel(esql);

            // View Popular Items and Customers
            JPanel popularPanel = PopularPanel(esql);

            // Place and View Supply Requests
            JPanel productSupplyRequestPanel = ProductSupplyRequestPanel(esql);

            // View The Stores You Manage
            JPanel viewManagedStoresPanel = ViewManagedStoresPanel(esql);

            tabbedPane.addTab("Updates", null, productUpdatesPanel, "Place and View Recent Product Updates");
            tabbedPane.addTab("Popular", null, popularPanel, "View Popular Items and Customers");
            tabbedPane.addTab("Supply Requests", null, productSupplyRequestPanel, "Place and View Recent Supply Requests");
            tabbedPane.addTab("My Stores", null, viewManagedStoresPanel, "View the Stores that you Mange");

            // tabbedPane.addChangeListener(u -> {
            //    if (tabbedPane.getSelectedIndex() == 1) {
            //       System.out.println("Products tab");
            //    }
            // });
         }

         content.add(tabbedPane);
      });

      createUserButton.addActionListener(e -> {
         JPanel container = new JPanel();
         JPanel inputBox = new JPanel();
         JLabel label1 = new JLabel("Name");
         JLabel label2 = new JLabel("Password");
         JLabel label3 = new JLabel("Latitude");
         JLabel label4 = new JLabel("Longitude");
         JTextField inputField1 = new JTextField(10);
         JTextField inputField2 = new JTextField(10);
         JTextField inputField3 = new JTextField(10);
         JTextField inputField4 = new JTextField(10);
         JButton submitButton1 = new JButton("Submit");
         inputBox.add(Box.createHorizontalStrut(14));
         inputBox.add(label1); inputBox.add(inputField1);
         inputBox.add(Box.createHorizontalStrut(32));
         inputBox.add(label2); inputBox.add(inputField2);
         inputBox.add(Box.createHorizontalStrut(30)); 
         inputBox.add(label3); inputBox.add(inputField3);
         inputBox.add(Box.createHorizontalStrut(30)); 
         inputBox.add(label4); inputBox.add(inputField4);
         inputBox.add(Box.createHorizontalStrut(30)); 
         inputBox.add(submitButton1);
         inputBox.setMaximumSize(new Dimension(450, 1000));
         inputBox.setPreferredSize(new Dimension(450, 1000));
         inputBox.setOpaque(false);
         container.setOpaque(false);

         submitButton1.addActionListener(u -> {
            String name = inputField1.getText();
            String password = inputField2.getText();
            String latitude = inputField3.getText();
            String longitude = inputField4.getText();

            CreateUser(esql, name, password, latitude, longitude);

            UserInputBox.setText(name);
            PasswordInputBox.setText(password);

            container.setVisible(false);
            loginPanel.setVisible(true);
         });

         loginPanel.setVisible(false);
         container.add(Box.createHorizontalStrut(50));
         container.add(inputBox);
         content.add(container);
      });
      
      loginPanel.add(UserText);
      loginPanel.add(Box.createHorizontalStrut(5));
      loginPanel.add(UserInputBox);
      loginPanel.add(Box.createHorizontalStrut(20));
      loginPanel.add(PasswordText);
      loginPanel.add(Box.createHorizontalStrut(5));
      loginPanel.add(PasswordInputBox);
      loginPanel.add(Box.createHorizontalStrut(20));
      loginPanel.add(submitButton);
      loginPanel.add(Box.createHorizontalStrut(20));
      loginPanel.add(createUserButton);
      loginPanel.setVisible(true);

      content.add(BorderLayout.NORTH, loginPanel);
      content.setVisible(true);

      return content;
   }

   public static JPanel PopularPanel(Retail esql) {
      JPanel panel = new JPanel();
      panel.setPreferredSize(new Dimension(600, 500));
      panel.setOpaque(false);
      JLabel label1 = new JLabel("Popular Items");
      JLabel label2 = new JLabel("Popular Customers");
      try {
         JTable table1 = viewPopularProductsAndMakeTable(esql);
         JTable table2 = viewPopularCustomersAndMakeTable(esql);

         JScrollPane scroll1 = new JScrollPane(table1);
         JScrollPane scroll2 = new JScrollPane(table2);

         scroll1.setPreferredSize(new Dimension((int)(WIDTH * 0.6), 200));
         scroll2.setPreferredSize(new Dimension((int)(WIDTH * 0.6), 200));
         scroll1.getViewport().setBackground(darkPink);
         scroll2.getViewport().setBackground(darkPink);
         panel.add(label1);
         panel.add(scroll1);
         panel.add(label2);
         panel.add(scroll2);
      } catch (Exception e) {
         System.err.println ("Couldn't load popular products and customers: " + e.getMessage());
      }
      return panel;
   }

   public static JPanel ViewProductsPanel(Retail esql) {
      JPanel panel = new JPanel();
      panel.setPreferredSize(new Dimension(600, 500));
      panel.setOpaque(false);

      Box inputBox = Box.createHorizontalBox();
      JLabel text = new JLabel("Store ID");
      JTextField inputField = new JTextField(10);
      JButton submitButton = new JButton("Submit");

      JPanel tableBox = new JPanel();
      tableBox.setPreferredSize(new Dimension(600, 500));
      tableBox.setOpaque(false);
      submitButton.addActionListener(e -> {
         System.out.println("Loading Tables...");
         tableBox.setVisible(false);
         tableBox.removeAll();
         String input = inputField.getText();
         List<JTable> viewTables = viewProductsAndMakeTables(esql, input);
         if (viewTables.size() != 4) {
            System.out.println("Bad Query");
            return;
         }
         JScrollPane scroll1 = new JScrollPane(viewTables.get(0));
         JScrollPane scroll2 = new JScrollPane(viewTables.get(1));
         JScrollPane scroll3 = new JScrollPane(viewTables.get(2));
         JScrollPane scroll4 = new JScrollPane(viewTables.get(3));
         scroll1.setPreferredSize(new Dimension((int)(WIDTH * 0.6), 200));
         scroll2.setPreferredSize(new Dimension((int)(WIDTH * 0.6), 200));
         scroll3.setPreferredSize(new Dimension((int)(WIDTH * 0.6), 200));
         scroll4.setPreferredSize(new Dimension((int)(WIDTH * 0.6), 200));
         scroll1.getViewport().setBackground(darkPink);
         scroll2.getViewport().setBackground(darkPink);
         scroll3.getViewport().setBackground(darkPink);
         scroll4.getViewport().setBackground(darkPink);
         JTabbedPane tabbedPane = new JTabbedPane();
         tabbedPane.setBackground(purple);
         tabbedPane.addTab("All Products", null, scroll1);
         tabbedPane.addTab("Price (High to Low)", null, scroll2);
         tabbedPane.addTab("Price (Low to High)", null, scroll3);
         tabbedPane.addTab("Popular", null, scroll4);
         tableBox.add(tabbedPane);
         tableBox.setVisible(true);
         System.out.println("Added Tables");
      });
      // submitButton.doClick();
      inputBox.add(text); inputBox.add(inputField); inputBox.add(submitButton);

      panel.add(inputBox);
      panel.add(tableBox);

      return panel;
   }

   public static JPanel PlaceAndViewOrders(Retail esql) {
      JPanel panel = new JPanel();
      panel.setPreferredSize(new Dimension(600, 500));
      panel.setOpaque(false);

      JPanel inputBox = new JPanel();
      JLabel text1 = new JLabel("Store ID");
      JLabel text2 = new JLabel("Product Name");
      JLabel text3 = new JLabel("Units");
      JTextField inputField1 = new JTextField(7);
      JTextField inputField2 = new JTextField(7);
      JTextField inputField3 = new JTextField(7);
      JButton submitButton = new JButton("Submit");
      inputBox.add(text1); inputBox.add(inputField1);
      inputBox.add(Box.createHorizontalStrut(15)); 
      inputBox.add(text2); inputBox.add(inputField2); 
      inputBox.add(Box.createHorizontalStrut(15)); 
      inputBox.add(text3); inputBox.add(inputField3); 
      inputBox.add(Box.createVerticalStrut(10));
      inputBox.add(submitButton);
      inputBox.setBorder(BorderFactory.createLineBorder(Color.black, 1));
      inputBox.setBackground(darkPink);
      inputBox.setPreferredSize(new Dimension(600, 60));

      Box tableBox = Box.createVerticalBox();
      tableBox.setPreferredSize(new Dimension((int)(WIDTH * 0.6), 200));
      try {
         JScrollPane scroll = new JScrollPane(viewRecentOrdersAndMakeTable(esql));
         scroll.getViewport().setBackground(darkPink);
         tableBox.add(scroll);
      } catch (Exception e) {
         System.err.println ("Couldn't load recent orders: " + e.getMessage());
      }

      submitButton.addActionListener(u -> {
         System.out.println("Loading Recent Orders...");
         String storeID = inputField1.getText();
         String productName = inputField2.getText();
         Integer units = Integer.parseInt(inputField3.getText());
         placeOrder(esql, storeID, productName, units);
         try {
            tableBox.setVisible(false);
            tableBox.removeAll();
            JTable table = viewRecentOrdersAndMakeTable(esql);
            JScrollPane scroll = new JScrollPane(table);
            scroll.getViewport().setBackground(darkPink);
            tableBox.add(scroll);
            tableBox.setVisible(true);
            System.out.println("Done");
         } catch (Exception e) {
            System.err.println ("Couldn't load recent orders: " + e.getMessage());
         }
      });
      panel.add(new JLabel("Recent Orders"));
      panel.add(tableBox);
      panel.add(new JLabel("Place an Order"));
      panel.add(inputBox);
      
      return panel;
   }

   public static JPanel ProductUpdatesPanel(Retail esql) {
      JPanel panel = new JPanel();
      panel.setPreferredSize(new Dimension(600, 500));
      panel.setOpaque(false);

      JPanel inputBox = new JPanel();
      JLabel text1 = new JLabel("Store ID");
      JLabel text2 = new JLabel("Product Name");
      JLabel text3 = new JLabel("New Units");
      JLabel text4 = new JLabel("New Price");
      JTextField inputField1 = new JTextField(9);
      JTextField inputField2 = new JTextField(9);
      JTextField inputField3 = new JTextField(9);
      JTextField inputField4 = new JTextField(9);
      JButton submitButton = new JButton("Submit");
      inputBox.add(Box.createHorizontalStrut(40)); 
      inputBox.add(text1); inputBox.add(inputField1);
      inputBox.add(Box.createHorizontalStrut(30)); 
      inputBox.add(text2); inputBox.add(inputField2); 
      inputBox.add(Box.createHorizontalStrut(40));
      inputBox.add(Box.createHorizontalStrut(40));  
      inputBox.add(text3); inputBox.add(inputField3); 
      inputBox.add(Box.createHorizontalStrut(30)); 
      inputBox.add(text4); inputBox.add(inputField4); 
      inputBox.add(Box.createHorizontalStrut(40)); 
      inputBox.add(Box.createVerticalStrut(10));
      inputBox.add(submitButton);
      inputBox.setBorder(BorderFactory.createLineBorder(Color.black, 1));
      inputBox.setBackground(darkPink);
      inputBox.setPreferredSize(new Dimension(550, 100));

      Box tableBox = Box.createVerticalBox();
      tableBox.setPreferredSize(new Dimension((int)(WIDTH * 0.6), 200));
      try {
         JScrollPane scroll = new JScrollPane(viewRecentUpdatesAndMakeTable(esql));
         scroll.getViewport().setBackground(darkPink);
         tableBox.add(scroll);
      } catch (Exception e) {
         System.err.println ("Couldn't load recent updates: " + e.getMessage());
      }

      submitButton.addActionListener(u -> {
         System.out.println("Loading Recent Orders...");
         String storeID = inputField1.getText();
         String productName = inputField2.getText();
         String numUnits = inputField3.getText();
         String pricePerUnit = inputField4.getText();
         updateProduct(esql, storeID, productName, numUnits, pricePerUnit);
         try {
            tableBox.setVisible(false);
            tableBox.removeAll();
            JTable table = viewRecentUpdatesAndMakeTable(esql);
            JScrollPane scroll = new JScrollPane(table);
            scroll.getViewport().setBackground(darkPink);
            tableBox.add(scroll);
            tableBox.setVisible(true);
            System.out.println("Done");
         } catch (Exception e) {
            System.err.println ("Couldn't load recent updates: " + e.getMessage());
         }
      });

      panel.add(new JLabel("Recent Updates"));
      panel.add(tableBox);
      panel.add(new JLabel("Make an Update"));
      panel.add(inputBox);

      return panel;
   }

   public static JPanel ProductSupplyRequestPanel(Retail esql) {
      JPanel panel = new JPanel();
      panel.setPreferredSize(new Dimension(600, 500));
      panel.setOpaque(false);

      JPanel inputBox = new JPanel();
      JLabel text1 = new JLabel("Store ID");
      JLabel text2 = new JLabel("Product Name");
      JLabel text3 = new JLabel("Products Needed");
      JLabel text4 = new JLabel("Warehouse ID");
      JTextField inputField1 = new JTextField(9);
      JTextField inputField2 = new JTextField(9);
      JTextField inputField3 = new JTextField(9);
      JTextField inputField4 = new JTextField(9);
      JButton submitButton = new JButton("Submit");
      inputBox.add(Box.createHorizontalStrut(60)); 
      inputBox.add(text1); inputBox.add(inputField1);
      inputBox.add(Box.createHorizontalStrut(30)); 
      inputBox.add(text2); inputBox.add(inputField2); 
      inputBox.add(Box.createHorizontalStrut(20));
      inputBox.add(Box.createHorizontalStrut(20));  
      inputBox.add(text3); inputBox.add(inputField3); 
      inputBox.add(Box.createHorizontalStrut(30)); 
      inputBox.add(text4); inputBox.add(inputField4); 
      inputBox.add(Box.createHorizontalStrut(40)); 
      inputBox.add(Box.createVerticalStrut(10));
      inputBox.add(submitButton);
      inputBox.setBorder(BorderFactory.createLineBorder(Color.black, 1));
      inputBox.setBackground(darkPink);
      inputBox.setPreferredSize(new Dimension(550, 100));

      Box tableBox = Box.createVerticalBox();
      tableBox.setPreferredSize(new Dimension((int)(WIDTH * 0.6), 200));
      try {
         JScrollPane scroll = new JScrollPane(viewRecentRequestsAndMakeTable(esql));
         scroll.getViewport().setBackground(darkPink);
         tableBox.add(scroll);
      } catch (Exception e) {
         System.err.println ("Couldn't load recent requests: " + e.getMessage());
      }

      submitButton.addActionListener(u -> {
         System.out.println("Loading Recent Supply Requests...");
         String storeID = inputField1.getText();
         String productName = inputField2.getText();
         String numProductsNeeded = inputField3.getText();
         String warehouseID = inputField4.getText();
         placeProductSupplyRequests(esql, storeID, productName, numProductsNeeded, warehouseID);
         try {
            tableBox.setVisible(false);
            tableBox.removeAll();
            JTable table = viewRecentRequestsAndMakeTable(esql);
            JScrollPane scroll = new JScrollPane(table);
            scroll.getViewport().setBackground(darkPink);
            tableBox.add(scroll);
            tableBox.setVisible(true);
            System.out.println("Done");
         } catch (Exception e) {
            System.err.println ("Couldn't load supply requests: " + e.getMessage());
         }
      });

      panel.add(new JLabel("Recent Supply Requests"));
      panel.add(tableBox);
      panel.add(new JLabel("Make a Supply Request"));
      panel.add(inputBox);

      return panel;
   }

   public static JPanel ViewStorePanel(Retail esql) {
      JPanel viewStorePanel = new JPanel();
      viewStorePanel.setPreferredSize(new Dimension(600, 500));
      viewStorePanel.setOpaque(false);
      //viewStorePanel.setBackground(myBackground);
      JLabel viewStoreLabel1 = new JLabel("Stores within 30 miles");
      JLabel viewStoreLabel2 = new JLabel("Popular Stores in Your Area");
      JLabel viewStoreLabel3 = new JLabel("Order From These Stores Again");
      List<JTable> viewTables = viewStoresAndMakeTables(esql);
      JScrollPane closeStoresScroll = new JScrollPane(viewTables.get(0));
      JScrollPane popularStoresScroll = new JScrollPane(viewTables.get(1));
      JScrollPane recentStoresScroll = new JScrollPane(viewTables.get(2));
      closeStoresScroll.setPreferredSize(new Dimension((int)(WIDTH * 0.6), 100));
      popularStoresScroll.setPreferredSize(new Dimension((int)(WIDTH * 0.6), 100));
      recentStoresScroll.setPreferredSize(new Dimension((int)(WIDTH * 0.6), 100));
      closeStoresScroll.getViewport().setBackground(darkPink);
      popularStoresScroll.getViewport().setBackground(darkPink);
      recentStoresScroll.getViewport().setBackground(darkPink);
      viewStorePanel.add(viewStoreLabel1);
      viewStorePanel.add(closeStoresScroll);
      viewStorePanel.add(viewStoreLabel2);
      viewStorePanel.add(popularStoresScroll);
      viewStorePanel.add(viewStoreLabel3);
      viewStorePanel.add(recentStoresScroll);
      return viewStorePanel;
   }

   public static JPanel ViewManagedStoresPanel(Retail esql) {
      JPanel panel = new JPanel();
      panel.setPreferredSize(new Dimension(600, 500));
      panel.setOpaque(false);
      JLabel label = new JLabel("Stores You Manage");
      try {
         JTable table1 = viewManagedStoresAndMakeTable(esql);
         JScrollPane scroll = new JScrollPane(table1);
         scroll.setPreferredSize(new Dimension((int)(WIDTH * 0.6), 200));
         scroll.getViewport().setBackground(darkPink);
         panel.add(label);
         panel.add(scroll);
      } catch (Exception e) {
         System.err.println ("Couldn't load managed stores: " + e.getMessage());
      }
      return panel;
   }

   public static void GUI (Retail esql) {
      try {
         //Creating the Frame
         JFrame frame = new JFrame("Why are you so CUTE <3");
         frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
         frame.getContentPane().setBackground(myBackground);
         frame.setLayout(new FlowLayout());
         frame.setLocationRelativeTo(null);
         frame.setSize(HEIGHT, WIDTH);

         // title 
         Box titleText = Box.createHorizontalBox();
         JLabel title = new JLabel("<html><span face='Garamond' style='color: #ffd1dc;'>My Lil' Store</span></html>");
         title.setFont (title.getFont().deriveFont(60.0f));
         titleText.add(title);

         Box contentPanel = ContentBox(esql);

         frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                  if (JOptionPane.showConfirmDialog(frame, 
                     "Are you sure you want to close this window?", "Close Window?", 
                     JOptionPane.YES_NO_OPTION,
                     JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                        try{
                           if(esql != null) {
                              System.out.print("Disconnecting from database...");
                              esql.cleanup ();
                              System.out.println("Done\n\nBye !");
                           }//end if
                        } catch (Exception e) {
                           // ignored.
                        }
                        System.exit(0);
                  }
            }
         });

         //Adding Components to the frame.
         frame.add(Box.createVerticalStrut(20));
         frame.add(titleText);
         frame.add(Box.createVerticalStrut(40));
         frame.add(contentPanel);
         frame.setVisible(true);
      } catch(Exception e) {
         System.err.println (e.getMessage ());
      }
   }


   /**
    * The main execution method
    *
    * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
    */
   public static void main (String[] args) {
      if (args.length != 3 && args.length != 4) {
         System.err.println (
            "Usage: " +
            "java [-classpath <classpath>] " +
            Retail.class.getName () +
            " <dbname> <port> <user>");
         return;
      }//end if

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

         esql.updateIsGUI(args.length == 4 && args[3].equals("--gui"));

         if (!esql.getIsGUI()) Greeting();

         if (esql.getIsGUI()) {
            GUI(esql);
            return;
         }

         Boolean keepOn = true;
         while(keepOn) {
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
               case 9: keepOn = false; break;
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
            if(esql != null && !esql.getIsGUI()) {
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
   public static void CreateUser(Retail esql) {
      try {
         System.out.print("\tEnter name: ");
         String name = in.readLine();
         System.out.print("\tEnter password: ");
         String password = in.readLine();
         System.out.print("\tEnter latitude: ");   
         String latitude = in.readLine();       //enter lat value between [0.0, 100.0]
         System.out.print("\tEnter longitude: ");  //enter long value between [0.0, 100.0]
         String longitude = in.readLine();

         CreateUser(esql, name, password, latitude, longitude);
      }catch(Exception e){
         System.err.println (e.getMessage ());
      }
   }
   public static void CreateUser(Retail esql, String name, String password, String latitude, String longitude){
      try{
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
   public static String LogIn(Retail esql) {
      try {
         System.out.print("\tEnter name: ");
         String name = in.readLine();
         System.out.print("\tEnter password: ");
         String password = in.readLine();
         return LogIn(esql, name, password);
      } catch(Exception e){
         System.err.println (e.getMessage ());
         return null;
      }
   }
   public static String LogIn(Retail esql, String name, String password){ // Andrei
      try{
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
      } catch(Exception e){
         System.err.println (e.getMessage());
      }
   }
   public static List<JTable> viewStoresAndMakeTables(Retail esql) {
      try {
         List<JTable> tableList = new ArrayList<JTable>();
         String query = String.format("SELECT s.storeID, s.name, calculate_distance(u.latitude, u.longitude, s.latitude, s.longitude) as dist FROM Users u, Store s WHERE u.userID = %d AND calculate_distance(u.latitude, u.longitude, s.latitude, s.longitude) <= 30 ORDER BY dist", esql.getUserId());
         String[][] result = esql.executeQueryAndReturnResult(query).stream().map(u -> u.stream().toArray(String[]::new)).toArray(String[][]::new);
   
         String[] columns = {"StoreID", "Name", "Dist"};
         JTable table1 = new JTable(result, columns);
         table1.setEnabled(false);
         table1.getTableHeader().setOpaque(false);
         table1.getTableHeader().setBackground(darkPurple);
         table1.setBackground(pink);
         tableList.add(table1);
         
   
         String query2 = String.format("SELECT X.storeID, X.name, Count(*) as TotalOrders FROM (SELECT s.storeID, s.name, calculate_distance(u.latitude, u.longitude, s.latitude, s.longitude) as dist FROM Users u, Store s WHERE u.userID = %d AND calculate_distance(u.latitude, u.longitude, s.latitude, s.longitude) <= 30) as X ,Orders O WHERE X.storeID = O.storeID GROUP BY X.storeID, X.name ORDER BY COUNT(*) DESC", esql.getUserId());
         String[][] result2 = esql.executeQueryAndReturnResult(query2).stream().map(u -> u.toArray(new String[0])).toArray(String[][]::new);
         String[] columns2 = {"StoreID", "Name", "Order Count"};
         JTable table2 = new JTable(result2, columns2);
         table2.setEnabled(false);
         table2.getTableHeader().setOpaque(false);
         table2.getTableHeader().setBackground(darkPurple);
         table2.setBackground(pink);
         tableList.add(table2);
   
         String query3 = String.format("SELECT s.storeID, s.name FROM Users u, (SELECT * FROM Store S WHERE S.storeID IN (SELECT O.storeID FROM Orders O WHERE O.customerID = %d)) as s WHERE u.userID = %d AND calculate_distance(u.latitude, u.longitude, s.latitude, s.longitude) <= 30", esql.getUserId(), esql.getUserId());
         String[][] result3 = esql.executeQueryAndReturnResult(query3).stream().map(u -> u.toArray(new String[0])).toArray(String[][]::new);
         String[] columns3 = {"StoreID", "Name"};
         JTable table3 = new JTable(result3, columns3);
         table3.setEnabled(false);
         table3.getTableHeader().setOpaque(false);
         table3.getTableHeader().setBackground(darkPurple);
         table3.setBackground(pink);
         tableList.add(table3);

         return tableList;
      } catch (Exception e) {
         System.err.println (e.getMessage());
         return new ArrayList<JTable>();
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
   public static List<JTable> viewProductsAndMakeTables(Retail esql, String input) {
      try {
         List<JTable> tableList = new ArrayList<JTable>();

         String[] columns1 = {"StoreID", "Name", "Units", "Price"};
         String query = "SELECT * FROM Product P WHERE P.storeID = '";
         query += (input + "'");
         String[][] result = esql.executeQueryAndReturnResult(query).stream().map(u -> u.toArray(new String[0])).toArray(String[][]::new);
         JTable table1 = new JTable(result, columns1);
         table1.setEnabled(false);
         table1.getTableHeader().setOpaque(false);
         table1.getTableHeader().setBackground(darkPurple);
         table1.setBackground(pink);
         tableList.add(table1);

         String[] columns2 = {"StoreID", "Name", "Units", "Price"};
         String query2 = String.format("SELECT * FROM Product P WHERE P.storeID = %s ORDER BY P.pricePerUnit DESC", input);
         String[][] result2 = esql.executeQueryAndReturnResult(query2).stream().map(u -> u.toArray(new String[0])).toArray(String[][]::new);
         JTable table2 = new JTable(result2, columns2);
         table2.setEnabled(false);
         table2.getTableHeader().setOpaque(false);
         table2.getTableHeader().setBackground(darkPurple);
         table2.setBackground(pink);
         tableList.add(table2);
         
         String[] columns3 = {"StoreID", "Name", "Units", "Price"};
         String query3 = String.format("SELECT * FROM Product P WHERE P.storeID = %s ORDER BY P.pricePerUnit", input);
         String[][] result3 = esql.executeQueryAndReturnResult(query3).stream().map(u -> u.toArray(new String[0])).toArray(String[][]::new);
         JTable table3 = new JTable(result3, columns3);
         table3.setEnabled(false);
         table3.getTableHeader().setOpaque(false);
         table3.getTableHeader().setBackground(darkPurple);
         table3.setBackground(pink);
         tableList.add(table3);

         String[] columns4 = {"Name", "Price"};
         String query4 = String.format("SELECT DISTINCT p.productName, p.pricePerUnit FROM Product p WHERE p.productName IN (SELECT O.productName FROM Orders O WHERE O.storeID = %s GROUP BY O.productName ORDER BY COUNT(*) DESC LIMIT 5)", input);
         String[][] result4 = esql.executeQueryAndReturnResult(query4).stream().map(u -> u.toArray(new String[0])).toArray(String[][]::new);
         JTable table4 = new JTable(result4, columns4);
         table4.setEnabled(false);
         table4.getTableHeader().setOpaque(false);
         table4.getTableHeader().setBackground(darkPurple);
         table4.setBackground(pink);
         tableList.add(table4);

         return tableList;
      } catch (Exception e) {
         System.err.println (e.getMessage());
         return new ArrayList<JTable>();
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
   public static void placeOrder(Retail esql) {
      try {
         System.out.print("\tInput Store ID: ");
         String storeID = in.readLine();
         System.out.print("\tInput Product Name: ");
         String productName = in.readLine();
         System.out.print("\tInput Number of Units: ");
         Integer unitsOrdered = Integer.parseInt(in.readLine());
         placeOrder(esql, storeID, productName, unitsOrdered);
      } catch (Exception e) {
         System.err.println (e.getMessage());
      }
   }
   public static void placeOrder(Retail esql, String storeID, String productName, Integer unitsOrdered) { // Christopher
      try{
         if (unitsOrdered < 0 ) {
            throw new IOException("Can't order negative amounts");
         }

         // check that the store is within 30 miles of the user
         String query = String.format("SELECT latitude, longitude FROM Store WHERE storeID = %s", storeID);
         List<List<String>> stores = esql.executeQueryAndReturnResult(query);
         if (stores.size() == 0) {
            throw new IOException("StoreID not found");
         }
         double storeLatitude = Double.parseDouble(stores.get(0).get(0));
         double storeLongitude = Double.parseDouble(stores.get(0).get(1));
         if (30.0 <= esql.calculateDistance(esql.getUserLatitude(), esql.getUserLongitude(), storeLatitude, storeLongitude)) {
            throw new IOException("Store too far away");
         }

         query = String.format("SELECT numberOfUnits FROM Product WHERE storeID = %s AND productName = '%s'", storeID, productName);
         List<List<String>> result = esql.executeQueryAndReturnResult(query);
         if (result.size() == 0) {
            throw new IOException("Product does not exist at this store");
         }
         if (Integer.parseInt(result.get(0).get(0)) < unitsOrdered) {
            throw new IOException(String.format("Not enough inventory. Store only has %s units available.", result.get(0).get(0)));
         }

         query = String.format (
            "INSERT INTO ORDERS (customerID, storeID, productName, unitsOrdered, orderTime) VALUES (%d, %s, '%s', %d, current_timestamp)",
            esql.getUserId(),
            storeID,
            productName,
            unitsOrdered
         );
         esql.executeUpdate(query);

         System.out.println(String.format("Ordered %s from Store: %s.\nQuantity: %s", productName, storeID, unitsOrdered));

         // then update product quantity using a trigger
      } catch(Exception e){
         System.err.println (e.getMessage ());
      }
   }
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
   public static JTable viewRecentOrdersAndMakeTable(Retail esql) throws Exception {
      String query = String.format("SELECT O.storeID, O.productName, O.unitsOrdered, O.orderTime FROM Orders O WHERE O.customerID = %d ORDER BY O.orderTime DESC LIMIT 5", esql.getUserId());
      String[] columns = {"StoreID", "Name", "Units", "Order Time"};
      String[][] result = esql.executeQueryAndReturnResult(query).stream().map(u -> u.toArray(new String[0])).toArray(String[][]::new);
      JTable table = new JTable(result, columns);
      table.setEnabled(false);
      table.getTableHeader().setOpaque(false);
      table.getTableHeader().setBackground(darkPurple);
      table.setBackground(pink);
      
      return table;      
   }

   // check if they are manager or admin for below:
   public static void updateProduct(Retail esql) {
      try {
         System.out.print("\tInput Store ID: ");
         String storeID = in.readLine();
         System.out.print("\tInput Product Name: ");
         String productName = in.readLine();
         System.out.print("\tInput New Number of Units: ");
         String numUnits = in.readLine();
         System.out.print("\tInput New Price Per Unit: ");
         String pricePerUnit = in.readLine();
         updateProduct(esql, storeID, productName, numUnits, pricePerUnit);
      } catch (Exception e) {
         System.err.println (e.getMessage());
      }
   }
   public static void updateProduct(Retail esql, String storeID, String productName, String numUnits, String pricePerUnit) { // Christopher
      try {
         if (!(esql.getUserType().equals("manager") || esql.getUserType().equals("admin"))) {
            throw new IOException("You do not have permission to perform this action");
         }

         // make sure the manager manages this store
         String query = String.format("SELECT S.managerID FROM Store S WHERE S.storeID = %s AND S.managerID = %d", storeID, esql.getUserId());
         List<List<String>> result = esql.executeQueryAndReturnResult(query);
         if (result.size() == 0 && esql.getUserType().equals("manager")) {
            throw new IOException(String.format("You do not manage the store with ID: %s", storeID));
         }

         query = String.format("SELECT * FROM Product P WHERE P.productName = '%s'", productName);
         //make sure the Product exists in the provided store
         result = esql.executeQueryAndReturnResult(query);
         if (result.size() == 0) {
            throw new IOException(String.format("Product '%s' does not exist in this store", productName));
         }

         query = String.format (
            "UPDATE Product SET numberOfUnits = %s, pricePerUnit = %s WHERE storeID = %s AND productName = '%s'",
            numUnits,
            pricePerUnit,
            storeID,
            productName
         );
         esql.executeUpdate(query);

         query = String.format (
            "INSERT INTO ProductUpdates (managerID, storeID, productName, updatedOn) VALUES (%d, %s, '%s', current_timestamp)",
            esql.getUserId(),
            storeID,
            productName
         );
         esql.executeUpdate(query);

         System.out.println("Updated Product " + productName);

      } catch (Exception e) {
         System.err.println (e.getMessage());
      }
   }
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
   public static JTable viewRecentUpdatesAndMakeTable(Retail esql) throws Exception {
      String query = String.format("SELECT * FROM ProductUpdates PU, Product P WHERE PU.storeID = P.storeID AND PU.productName = P.productName AND PU.managerID = %d ORDER BY PU.updatedOn DESC LIMIT 5", esql.getUserId());
      String[] columns = {"Update Number", "ManagerID", "StoreID", "Product", "Date"};
      String[][] result = esql.executeQueryAndReturnResult(query).stream().map(u -> u.toArray(new String[0])).toArray(String[][]::new);
      JTable table = new JTable(result, columns);
      table.setEnabled(false);
      table.getTableHeader().setOpaque(false);
      table.getTableHeader().setBackground(darkPurple);
      table.setBackground(pink);
      
      return table;  
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
   public static JTable viewPopularProductsAndMakeTable(Retail esql) throws Exception {
      String query = String.format("SELECT O.productName, COUNT(*) FROM Orders O WHERE O.storeID IN (SELECT S.storeID FROM Store S WHERE S.managerID = %d) GROUP BY O.productName ORDER BY COUNT(*) DESC LIMIT 5", esql.getUserId());
      String[] columns = {"Product Name", "Number of Orders"};
      String[][] result = esql.executeQueryAndReturnResult(query).stream().map(u -> u.toArray(new String[0])).toArray(String[][]::new);
      JTable table = new JTable(result, columns);
      table.setEnabled(false);
      table.getTableHeader().setOpaque(false);
      table.getTableHeader().setBackground(darkPurple);
      table.setBackground(pink);

      return table;
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
   public static JTable viewPopularCustomersAndMakeTable(Retail esql) throws Exception {
      String query = String.format("SELECT O.customerID, COUNT(*) FROM Orders O WHERE O.storeID IN (SELECT S.storeID FROM Store S WHERE S.managerID = %d) GROUP BY O.customerID ORDER BY COUNT(*) DESC LIMIT 5", esql.getUserId());
      String[] columns = {"CustomerID", "Number of Orders"};
      String[][] result = esql.executeQueryAndReturnResult(query).stream().map(u -> u.toArray(new String[0])).toArray(String[][]::new);
      JTable table = new JTable(result, columns);
      table.setEnabled(false);
      table.getTableHeader().setOpaque(false);
      table.getTableHeader().setBackground(darkPurple);
      table.setBackground(pink);

      return table;
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
   public static JTable viewManagedStoresAndMakeTable(Retail esql) throws Exception {
      String query = String.format("SELECT * FROM Store S WHERE S.managerID = %d", esql.getUserId());
      String[] columns = {"StoreID", "Name", "Lat", "Long", "ManagerID", "Established"};
      String[][] result = esql.executeQueryAndReturnResult(query).stream().map(u -> u.toArray(new String[0])).toArray(String[][]::new);
      JTable table = new JTable(result, columns);
      table.setEnabled(false);
      table.getTableHeader().setOpaque(false);
      table.getTableHeader().setBackground(darkPurple);
      table.setBackground(pink);

      return table;
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
         String query = "SELECT * FROM Users U ORDER BY U.userID";	
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
            "UPDATE Users SET name = '%s', password = '%s', latitude = %s, longitude = %s, type = '%s' WHERE userID = %s",	
            name,	
            password,	
            latitude,	
            longitude,	
            type,
            userID
         );	
         esql.executeUpdate(query);	
         System.out.println("User information succesfully updated");	
      } catch (Exception e) {	
         System.err.println (e.getMessage());	
      }	
   }
   public static void placeProductSupplyRequests(Retail esql) {
      try {
         System.out.print("\tInput Store ID: ");
         String storeID = in.readLine();
         System.out.print("\tInput Product Name: ");
         String productName = in.readLine();
         System.out.print("\tInput Number of Products Needed: ");
         String numProductsNeeded = in.readLine();
         System.out.print("\tInput Warehouse ID: ");
         String warehouseID = in.readLine();
         placeProductSupplyRequests(esql, storeID, productName, numProductsNeeded, warehouseID);

      }catch(Exception e) {
         System.err.println(e.getMessage());
      }
   }
   public static void placeProductSupplyRequests(Retail esql, String storeID, String productName, String numProductsNeeded, String warehouseID) { // Christopher
      try {
         if (Integer.parseInt(numProductsNeeded) < 0 ) {
            throw new IOException("Can't order negative amounts");
         }

         if (!(esql.getUserType().equals("manager") || esql.getUserType().equals("admin"))) {
            throw new IOException("You do not have permission to perform this action");
         }

         // make sure the manager manages this store
         String query = String.format("SELECT S.managerID FROM Store S WHERE S.storeID = %s AND S.managerID = %s", storeID, esql.getUserId());
         List<List<String>> result = esql.executeQueryAndReturnResult(query);
         if (result.size() == 0 && esql.getUserType().equals("manager")) {
            throw new IOException(String.format("You do not manage the store with ID: %s", storeID));
         }
         
         query = String.format("SELECT * FROM Product P WHERE P.productName = '%s'", productName);
         //make sure the Product exists in the provided store
         result = esql.executeQueryAndReturnResult(query);
         if (result.size() == 0) {
            throw new IOException(String.format("Product '%s' does not exist in this store", productName));
         }

         query = String.format(
            "INSERT INTO ProductSupplyRequests (managerID, warehouseID, storeID, productName, unitsRequested) VALUES (%d, %s, %s, '%s', %s)",
            esql.getUserId(),
            warehouseID,
            storeID,
            productName,
            numProductsNeeded
         );
         esql.executeUpdate(query);

         //this should trigger an event that updates the quantities in the Product table
      } catch (Exception e) {
         System.err.println (e.getMessage());
      }
   }
   public static JTable viewRecentRequestsAndMakeTable(Retail esql) throws Exception {
      String query = String.format("SELECT * FROM ProductSupplyRequests PS, Product P WHERE PS.storeID = P.storeID AND PS.productName = P.productName AND PS.managerID = %d ORDER BY PS.requestNumber DESC", esql.getUserId());
      String[] columns = {"Request #", "Manager", "Warehouse", "Store", "Name", "Units"};
      String[][] result = esql.executeQueryAndReturnResult(query).stream().map(u -> u.toArray(new String[0])).toArray(String[][]::new);
      JTable table = new JTable(result, columns);
      table.setEnabled(false);
      table.getTableHeader().setOpaque(false);
      table.getTableHeader().setBackground(darkPurple);
      table.setBackground(pink);
      
      return table;  
   }
}//end Retail

