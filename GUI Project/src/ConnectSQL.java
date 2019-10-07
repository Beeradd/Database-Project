//ConnectSQL.java communicates with mysql server directly with JDBC functions
import java.sql.Statement;
import java.util.StringJoiner;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

public class ConnectSQL 
{
	private Connection connect = null;
	private Statement statement = null;
	private ResultSet results = null;
	private ResultSetMetaData rsmd = null;

	final String driver = "com.mysql.jdbc.Driver";
	final String url="jdbc:mysql://dbserv.cs.siu.edu:3306/bsprinkle";
	final String user="bsprinkle";
	final String pswd="v6MH4s4j";
	
	public ConnectSQL() {}

	//Insert function that allows the addition of new entries into tables
	public boolean insert(String data, String relation) throws Exception
	{
		int success = 0;
		boolean valid = false;
		String sql ="insert into "+relation+
					" values" +data;
		try
		{
			System.out.println("Connecting to database..."); 
			Class.forName(driver);
			connect = DriverManager.getConnection(url, user, pswd);
			statement = connect.createStatement();
			success = statement.executeUpdate(sql);
			if(success > 0)
			{
				System.out.println("Success");
				valid = true;
				return valid;
			}
				

		}catch(Exception e)
		{
			System.err.println("Couldn't connect to database: " + e);
			AlertBox.printError("Insert Error", "Error inserting new entry: \n" + e);
			connect.close();
			throw e;
		}
		
		connect.close();
		return valid;
	}
	
	//Update function that allows for one attribute to be updated at a time based on one comparison
	public boolean update(String relation, String changeAtt, String statAtt, String newData, String oldData) throws Exception
	{
		int success = 0;
		boolean valid = false;
		
		String sql = "update " +relation+
					 " set " +changeAtt+"='"+newData+"' "
					 +"where "+statAtt+"='"+oldData+"'";
		
		try
		{
			System.out.println("Connecting to database...");
			Class.forName(driver);
			connect = DriverManager.getConnection(url, user, pswd);
			statement = connect.createStatement();
			success = statement.executeUpdate(sql);
			if(success > 0)
			{
				System.out.println("Success");
				valid = true;
				return valid;
			}
			
			
		}
		catch(Exception e)
		{
			System.err.println("Couldn't connect to database: " + e);
			AlertBox.printError("Update Error", "Error updating previous entry: \n" + e);
			connect.close();
			throw e;
		}
		
		
		return valid;
	}
	
	//Delete function that deletes an entry based on what attribute is given
	public boolean delete(String relation, String attribute, String value) throws Exception
	{
		boolean valid = false;
		int success = 0;
		
		String sql = "delete from " +relation+
					 " where "+attribute+"='"+value+"'";
		try
		{
			System.out.println("Connecting to database...");
			Class.forName(driver);
			connect = DriverManager.getConnection(url, user, pswd);
			statement = connect.createStatement();
			success = statement.executeUpdate(sql);
			if(success > 0)
			{
				System.out.println("Success");
				valid = true;
				return valid;
			}
		}
		catch(Exception e)
		{
			System.err.println("Couldn't connect to database: " + e);
			AlertBox.printError("Delete Error", "Error deleting entry: \n" + e);
			connect.close();
			throw e;	
		}
		
		return valid;
	}
	
	//Basic search function that doesn't use the "where" clause
	public ResultSet searchBasic(String select, String relation) throws Exception
	{
		ResultSet search_results = null;
		String sql = "select " + select+
					 " from " +relation;
		try
		{
			System.out.println("Connecting to database...");
			Class.forName(driver);
			connect = DriverManager.getConnection(url, user, pswd);
			statement = connect.createStatement();
			search_results = statement.executeQuery(sql);
			
		}
		catch(Exception e)
		{
			System.err.println("Error: " + e);
			AlertBox.printError("Search Error", "Error searching for data: \n" + e + "\nPlease check your formatting");
			connect.close();
			throw e;
		}
		
		return search_results;
	}
	
	//Advanced search function that uses the "where" clause
	public ResultSet searchWhere(String select, String relation, String where) throws Exception
	{
		ResultSet search_results = null;
		String sql = "select " + select+
					 " from " + relation +
					 " where "+ where;
		try
		{
			System.out.println("Connecting to database...");
			Class.forName(driver);
			connect = DriverManager.getConnection(url, user, pswd);
			statement = connect.createStatement();
			search_results = statement.executeQuery(sql);
			
		}
		catch(Exception e)
		{
			System.err.println("Error: " + e);
			AlertBox.printError("Search Error", "Error searching for data: \n" + e + "\nPlease check your formatting");
			connect.close();
			throw e;
		}
		
		return search_results;
	}
	//Checks login information is in the database and valid
	public boolean ssnValidator(String type, String ssn, String relation) throws Exception
	{
		boolean valid = false;
		String sql = "select * "
					+"from " +relation+ 
					" where "+type+"_ssn = " +ssn;
		
		try 
		{
			System.out.println("Verifying login credentials...");
			Class.forName(driver);
			connect = DriverManager.getConnection(url, user, pswd);
			statement = connect.createStatement();
			results = statement.executeQuery(sql);
			
			if(results.next())
			{
				valid = true;
				return valid;
			}	
			
		}catch(Exception e){
			System.err.println("Couldn't connect to database: " + e);
			AlertBox.printError("Login Error", "Error finding user: \nPlease try your ID number");
			connect.close();
			throw e;
		}
		
		return valid;
	}

	//Checks to see what user is logged on, retrieves name for populating GUI labels
	public ResultSet loggedOn(String ssn, String relation, String att) throws Exception
	{
		ResultSet search_results = null;
		String sql ="select name from " +relation+
					" where " +att+"='"+ssn+"'";
		try
		{
			System.out.println("Who logged on...");
			Class.forName(driver);
			connect = DriverManager.getConnection(url, user, pswd);
			statement = connect.createStatement();
			search_results = statement.executeQuery(sql);
			
		}
		catch(Exception e)
		{
			System.err.println("Error: " + e);
			AlertBox.printError("Login Error", "Error finding user: \n" + e + "\nPlease check your formatting");
			connect.close();
			throw e;
		}
		
		return search_results;
	}
	
	//Collects all of the user's information for GUI use
	public ResultSet userInfo(String relation, String name) throws Exception
	{
		ResultSet search_results = null;
		String sql ="select * from " +relation+
					" where name='"+name+"'";
		try
		{
			System.out.println("Collecting user data...");
			Class.forName(driver);
			connect = DriverManager.getConnection(url, user, pswd);
			statement = connect.createStatement();
			search_results = statement.executeQuery(sql);
			
		}
		catch(Exception e)
		{
			System.err.println("Error: " + e);
			AlertBox.printError("Info Error", "Error finding user info: \n" + e + "\nPlease check your formatting");
			connect.close();
			throw e;
		}
		
		
		return search_results;
	}
	
	//Updates the enroll integer for each course
	public boolean enrollUpdate(String c_name) throws Exception
	{
		int success = 0;
		boolean valid = false;
		
		String sql= "update courses "
					 		+"set c_enrolled=c_enrolled+1 "
					 		+"where c_name='" +c_name+"'" ;
		try
		{
			System.out.println("Updating enrolled int...");
			Class.forName(driver);
			connect = DriverManager.getConnection(url, user, pswd);
			statement = connect.createStatement();
			success = statement.executeUpdate(sql);
			if(success > 0)
			{
				System.out.println("Success");
				valid = true;
				return valid;
			}
			
			
		}
		catch(Exception e)
		{
			System.err.println("Couldn't connect to database: " + e);
			AlertBox.printError("Update Error", "Error updating previous entry: \n" + e);
			connect.close();
			throw e;
		}
		
		return valid;
	}
	
	//Inserts students and info into enrollment table
	public boolean enrollInsert(String data, String relation) throws Exception
	{
		int success = 0;
		boolean valid = false;
		
		String sql = "insert into " +relation+
					 " values " + data;
		
		try
		{
			System.out.println("Trying to insert enrollment..."); 
			Class.forName(driver);
			connect = DriverManager.getConnection(url, user, pswd);
			statement = connect.createStatement();
			success = statement.executeUpdate(sql);
			if(success > 0)
			{
				System.out.println("Success");
				valid = true;
				return valid;
			}
				

		}catch(Exception e)
		{
			System.err.println("Couldn't connect to database: " + e);
			AlertBox.printError("Insert Error", "Error inserting new entry: \n" + e);
			connect.close();
			throw e;
		}
		
		connect.close();
		
		return valid;
	}
	
	//Close function to end connections 
	public void close()
	{
		try
		{
			if(results != null)
			{
				results.close();
				System.out.println("ResultSet closed.");
			}
			if(statement != null)
			{
				statement.close();
				System.out.println("Statement closed.");
			}
			if(connect != null)
			{
				connect.close();
				System.out.println("Connection closed.");
			}
		} catch (Exception e)
		{
			
		}
		
	}
		
}