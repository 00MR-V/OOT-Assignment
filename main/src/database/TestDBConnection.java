package database;
import java.sql.Connection;

public class TestDBConnection {
    public static void main(String[] args) {
        // Try to get a connection from the DBConnection class.
        Connection conn = DBConnection.getConnection();
        if (conn != null) {
            System.out.println("Database connection is working.");
        } else {
            System.out.println("Failed to establish database connection.");
        }
        
        // Close the connection when finished.
        DBConnection.closeConnection();
    }
}
