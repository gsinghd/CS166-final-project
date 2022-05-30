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
import java.util.ArrayList;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */
public class Cafe {

   // reference to physical database connection.
   private Connection _connection = null;

   // handling the keyboard inputs through a BufferedReader
   // This variable can be global for convenience.
   static BufferedReader in = new BufferedReader(
                                new InputStreamReader(System.in));

   /**
    * Creates a new instance of Cafe
    *
    * @param hostname the MySQL or PostgreSQL server hostname
    * @param database the name of the database
    * @param username the user name used to login to the database
    * @param password the user login password
    * @throws java.sql.SQLException when failed to make a connection.
    */
   public Cafe(String dbname, String dbport, String user, String passwd) throws SQLException {

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
   }//end Cafe

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
      while (rs.next()){
		 if(outputHeader){
			for(int i = 1; i <= numCol; i++){
			System.out.print(rsmd.getColumnName(i) + "\t");
			}
			System.out.println();
			outputHeader = false;
		 }
         for (int i=1; i<=numCol; ++i)
            System.out.print (rs.getString (i) + "\t");
         System.out.println ();
         ++rowCount;
      }//end while
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
            Cafe.class.getName () +
            " <dbname> <port> <user>");
         return;
      }//end if

      Greeting();
      Cafe esql = null;
      try{
         // use postgres JDBC driver.
         Class.forName ("org.postgresql.Driver").newInstance ();
         // instantiate the Cafe object and creates a physical
         // connection.
         String dbname = args[0];
         String dbport = args[1];
         String user = args[2];
         esql = new Cafe (dbname, dbport, user, "");

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
              boolean usermenu = true;
              while(usermenu) {
                System.out.println("MAIN MENU");
                System.out.println("---------");
                System.out.println("1. Goto Menu");
                System.out.println("2. Update Profile");
                System.out.println("3. Place a Order");
                System.out.println("4. Update a Order");
                System.out.println(".........................");
                System.out.println("9. Log out");
                switch (readChoice()){
                   case 1: Menu(esql); break;
                   case 2: UpdateProfile(esql); break;
                   case 3: PlaceOrder(esql); break;
                   case 4: UpdateOrder(esql); break;
                   case 9: usermenu = false; break;
                   default : System.out.println("Unrecognized choice!"); break;
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
    * Creates a new user with privided login, passowrd and phoneNum
    **/
   public static void CreateUser(Cafe esql){
      try{
         System.out.print("\tEnter user login: ");
         String login = in.readLine();
         System.out.print("\tEnter user password: ");
         String password = in.readLine();
         System.out.print("\tEnter user phone: ");
         String phone = in.readLine();

	    String type="Customer";
	    String favItems="";

				 String query = String.format("INSERT INTO USERS (phoneNum, login, password, favItems, type) VALUES ('%s','%s','%s','%s','%s')", phone, login, password, favItems, type);

         esql.executeUpdate(query);
         System.out.println ("User successfully created!");
      }catch(Exception e){
         System.err.println (e.getMessage ());
      }
   }//end CreateUser


   /*
    * Check log in credentials for an existing user
    * @return User login or null is the user does not exist
    **/
   public static String LogIn(Cafe esql){
      try{
         System.out.print("\tEnter user login: ");
         String login = in.readLine();
         System.out.print("\tEnter user password: ");
         String password = in.readLine();

         String query = String.format("SELECT * FROM USERS WHERE login = '%s' AND password = '%s'", login, password);
         int userNum = esql.executeQuery(query);
	 if (userNum > 0)
		return login;
         return null;
      }catch(Exception e){
         System.err.println (e.getMessage ());
         return null;
      }
   }//end

// Rest of the functions definition go in here


public static void Menu(Cafe esql)
{

  try{
    boolean In_Menu = true;
    while(In_Menu){
      System.out.println("Menu List");
      System.out.println("---------");
      System.out.println("1. Search Item by name");
      System.out.println("2. Search Item by type");
      System.out.println("---------");
      System.out.println("Manager Only");
      System.out.println("6. Add items");
      System.out.println("7. Delete items");
      System.out.println("8. Update items");
      System.out.println("9. Go back to main menu");
      switch(readChoice()){
        case 1: Search_Item_By_Name(esql); break;
        case 2: Search_Item_By_Type(esql); break;
        case 6: Add_Items(esql); break;
        case 7: Delete_Items(esql); break;
        case 8: Update_Items(esql); break;
        case 9: In_Menu = false; break;
        default : System.out.println("Unrecognized choice!"); break;
      }
    }//end while
  }//end try
  catch(Exception e){
    System.err.println (e.getMessage());
  }//endcatch

}

public static void Search_Item_By_Name(Cafe esql)
{
  boolean Search_ItemName_Menu = true;


  while(Search_ItemName_Menu) {

    try{
      System.out.println("Enter the name of item you are looking for : ");
      String Item_Name = in.readLine();

      String query = "SELECT * FROM Menu WHERE itemName = '";
      query += Item_Name;
      String quotation = "'";
      query += quotation;

      List<List<String>> output = esql.executeQueryAndReturnResult(query);

      //output list
      for(int i = 0; i < output.size(); i++)
      {
        for(int j = 0; j < output.get(i).size(); j++)
        {
          System.out.print(output.get(i).get(j));
        }
        System.out.println("");
      }

      System.out.print(output.toString());

    }catch(Exception e){
      System.err.println(e.getMessage());
    }

    System.out.println("----------Search Finished-----------");
    System.out.println("Do You Want Search another Item?");
    System.out.println("1. Yes 2.No(go back to menu)");
    switch(readChoice()){
      case 1: break;
      case 2: Search_ItemName_Menu = false; break;
    }
  }
}//end Search Item By name;

public static void Search_Item_By_Type(Cafe esql)
{
  boolean Search_ItemType_Menu = true;

  while(Search_ItemType_Menu)
  {
    try{
      System.out.println("Enter the type of name you are looking for : ");
      String Item_Type = in.readLine();

      String query = "SELECT * FROM Menu WHERE type = '";
      String quotation = "'";
      query += Item_Type + quotation;

      List<List<String>> output = esql.executeQueryAndReturnResult(query);

      System.out.print(output);

    }catch(Exception e){
      System.err.println (e.getMessage());
    }

    System.out.println("----------Search Finished-----------");
    System.out.println("Do You Want Search another Item?");
    System.out.println("1. Yes 2.No (go back to menu)");
    switch(readChoice()){
      case 1: break;
      case 2: Search_ItemType_Menu = false; break;
    }

  }
}

//end Searh Item By type

public static void Add_Items(Cafe esql)
{
  boolean add = true;

  try {
    while(add)
    {
      System.out.println("------------------------");
      System.out.println("-------ADD ITEMS--------");
      System.out.println("Enter the Name of Item");
      String Item_Name = in.readLine();
      System.out.println("Enter the type of Item");
      String Item_Type = in.readLine();
      System.out.println("Enter the price of Item");
      String Price_str = in.readLine();
      double Price = Double.parseDouble(Price_str);
      System.out.println("Enter the description of Item");
      String Description = in.readLine();
      System.out.println("Enter the URL of image");
      String URL = in.readLine();

      String query = String.format("INSERT INTO Menu (ItemName, type, price, description, imageURL) VALUES ('%s', '%s','%s','%s','%s')", Item_Name, Item_Type, Price, Description, URL);

      esql.executeUpdate(query);
      System.out.println("Item Added!");
      System.out.println("Do you want to add more items?");
      System.out.println("1. Yes 2. No(go back to menu)");

      switch(readChoice()){
        case 1: break;
        case 2: add = false; break;
      }

    }
  }catch(Exception e){
    System.err.println (e.getMessage());
  }

}

public static void Delete_Items(Cafe esql)
{
  boolean delete = true;

  try {
    while(delete)
    {
      System.out.println("---------------------------");
      System.out.println("-------DELETE ITEMS--------");
      System.out.println("Enter the name of Item to delete");
      String Item_Name = in.readLine();

      String query = "DELETE FROM Menu WHERE itemName = '";
      String quotation = "'";
      query += Item_Name + quotation;

      esql.executeUpdate(query);
      System.out.println("Item removed!");
      System.out.println("Do you want to delete more items?");
      System.out.println("1. Yes 2. No(go back to menu)");

      switch(readChoice()){
        case 1: break;
        case 2: delete = false; break;
      }
    }
  }catch(Exception e){
    System.err.println (e.getMessage());
  }

}

public static void Update_Items(Cafe esql)
{
  boolean update = true;

  try {
    while(update)
    {
      System.out.println("---------------------------");
      System.out.println("-------UPDATE ITEMS--------");
      System.out.println("Enter the name of Item to update");
      String Item_Name = in.readLine();
      System.out.println("Which one should be updated?");
      System.out.println("1. Item Name 2. Item Type 3. Price 4. Description 5. image URL");

      String quotation = "'";
      String query = "";
      String value = "";
      String where = "WHERE itemName = '" + Item_Name + quotation;
      switch(readChoice()){
        case 1: System.out.println("Enter the change name of Item");
        value = in.readLine();
        query = "UPDATE Menu set itemName = '";
        query += value + quotation + where;
        break;
        case 2: System.out.println("Enter the change type of Item");
        value = in.readLine();
        query = "UPDATE Menu set type = '";
        query += value + quotation + where;
        break;
        case 3: System.out.println("Enter the change price of Item");
        value = in.readLine();
        double Price = Double.parseDouble(value);
        query = "UPDATE Menu set price = ";
        query += Price + where;
        break;
        case 4: System.out.println("Enter the change description of Item");
        value = in.readLine();
        query = "UPDATE Menu set description = '";
        query += value + quotation + where;
        break;
        case 5: System.out.println("Enter the change URL of Item");
        value = in.readLine();
        query = "UPDATE Menu set imageURL = '";
        query += value + quotation + where;
        break;
        default : System.out.println("Invalid Value");

      }
      esql.executeUpdate(query);
      System.out.println("Item Updated!");
      System.out.println("Do you want to update more items?");
      System.out.println("1. Yes 2. No(go back to menu)");

      switch(readChoice()){
        case 1: break;
        case 2: update = false; break;
      }
    }
  }
  catch(Exception e){
    System.err.println (e.getMessage());
  }

}



  public static void UpdateProfile(Cafe esql){}

  public static void PlaceOrder(Cafe esql){}

  public static void UpdateOrder(Cafe esql){}

}//end Cafe
