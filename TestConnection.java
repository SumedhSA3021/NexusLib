import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class TestConnection {
    public static void main(String[] args) {
        System.out.println("Connecting to WAMP MySQL...");

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            // 1. Establish connection
            conn = DatabaseConnection.getConnection(); 
            System.out.println("Success! Connected to library_db.");

            // 2. Run query on your exact table name
            String query = "SELECT * FROM bookdb";
            stmt = conn.createStatement();
            rs = stmt.executeQuery(query);

            System.out.println("\n--- Current Books in Database ---");
            
            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                
                // Matched exactly to your phpMyAdmin column headers!
                String title = rs.getString("book_title");
                String author = rs.getString("author");
                int qty = rs.getInt("quantity");
                
                System.out.println("Title: " + title + " | Author: " + author + " | Qty: " + qty);
            }

            if (!hasData) {
                System.out.println("The table is currently empty.");
            }

        } catch (Exception e) {
            System.out.println("Connection failed! Error details below:");
            e.printStackTrace();
        } finally {
            // 3. Clean up resources
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}