package database;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    // JDBC URL for connecting to the "Textile_Factory" database on localhost.
    private static final String URL = "jdbc:mysql://localhost:3306/Textile_Factory?useSSL=false";
    // Default XAMPP credentials (adjust if you have changed these)
    private static final String USER = "root";
    private static final String PASSWORD = "";
    
    // Singleton instance for the connection.
    private static Connection connection = null;
    
    // Returns a Connection object to be used in other parts of your application.
    public static Connection getConnection() {
        if (connection == null) {
            try {
                // Load the MySQL Connector/J driver.
                Class.forName("com.mysql.cj.jdbc.Driver");
                // Establish the connection.
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Connection established successfully.");
            } catch (ClassNotFoundException e) {
                System.err.println("MySQL JDBC Driver not found.");
                e.printStackTrace();
            } catch (SQLException e) {
                System.err.println("Error connecting to the database.");
                e.printStackTrace();
            }
        }
        return connection;
    }
    
    // Closes the database connection.
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
                System.out.println("Connection closed successfully.");
            } catch (SQLException e) {
                System.err.println("Error closing the connection.");
                e.printStackTrace();
            }
        }
    }
}
