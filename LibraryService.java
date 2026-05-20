import java.sql.*;
import java.time.LocalDate;

public class LibraryService {

    // Path: BookDB -> Book Search()
    public void BookSearch(String title) {
        String query = "SELECT * FROM BookDB WHERE book_title = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, title);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("\n--- Book Found ---");
                    System.out.println("Book Title: " + rs.getString("book_title"));
                    System.out.println("Author: " + rs.getString("author"));
                    System.out.println("Quantity Available: " + rs.getInt("quantity"));
                    
                    if (rs.getInt("quantity") > 0) {
                        System.out.println("Status: Available for borrowing");
                    } else {
                        System.out.println("Status: Out of Stock");
                    }
                } else {
                    System.out.println("Book not found in BookDB.");
                }
            }
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
        }
    }

    // Matches 'Student -> Student_Search(String ID, BookDB obj)' (Issue Multiple Books)
    public void Student_Search(String targetID, String targetBookTitle) {
        String userQuery = "SELECT * FROM student WHERE id = ?";
        String bookQuery = "SELECT quantity FROM BookDB WHERE book_title = ?";
        String currentIssuesQuery = "SELECT book_title FROM issued_books WHERE user_id = ? AND user_type = 'student'";
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(userQuery)) {
                ps.setString(1, targetID);
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) {
                    System.out.println("Student profile not found.");
                    return;
                }

                Student student = new Student();
                student.id = rs.getString("id");
                student.name = rs.getString("name");
                student.PhoneNo = rs.getString("phoneNum");
                student.Sem = rs.getInt("sem");
                student.Dept = rs.getString("dept");

                try (PreparedStatement psBook = conn.prepareStatement(bookQuery)) {
                    psBook.setString(1, targetBookTitle);
                    ResultSet rsBook = psBook.executeQuery();
                    if (!rsBook.next() || rsBook.getInt("quantity") <= 0) {
                        System.out.println("Book is currently unavailable or out of stock.");
                        return;
                    }
                }

                String insertIssue = "INSERT INTO issued_books (user_id, book_title, user_type, issue_date, return_date) VALUES (?, ?, 'student', ?, ?)";
                String reduceStock = "UPDATE BookDB SET quantity = quantity - 1 WHERE book_title = ?";

                try (PreparedStatement iStmt = conn.prepareStatement(insertIssue);
                     PreparedStatement rStmt = conn.prepareStatement(reduceStock)) {
                    
                    Date today = Date.valueOf(LocalDate.now());
                    Date returnDue = Date.valueOf(LocalDate.now().plusDays(14)); 

                    iStmt.setString(1, targetID);
                    iStmt.setString(2, targetBookTitle);
                    iStmt.setDate(3, today);
                    iStmt.setDate(4, returnDue);
                    iStmt.executeUpdate();

                    rStmt.setString(1, targetBookTitle);
                    rStmt.executeUpdate();

                    try (PreparedStatement cIssues = conn.prepareStatement(currentIssuesQuery)) {
                        cIssues.setString(1, targetID);
                        ResultSet rsIssues = cIssues.executeQuery();
                        while (rsIssues.next()) {
                            student.borrowedBooks.add(rsIssues.getString("book_title"));
                        }
                    }

                    System.out.println("🎉 Success! Book borrowed successfully.");
                    student.display();
                }
            }
        } catch (SQLException e) {
            System.out.println("Transaction process failed: " + e.getMessage());
        }
    }

    // Student Return Book Engine with 0.5 RS Per Day Penalty Computation
    public void Student_Return(String targetID, String targetBookTitle) {
        String issueQuery = "SELECT return_date FROM issued_books WHERE user_id = ? AND book_title = ? AND user_type = 'student'";
        String deleteIssue = "DELETE FROM issued_books WHERE user_id = ? AND book_title = ? AND user_type = 'student'";
        String restoreStock = "UPDATE BookDB SET quantity = quantity + 1 WHERE book_title = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            try (PreparedStatement psIssue = conn.prepareStatement(issueQuery)) {
                psIssue.setString(1, targetID);
                psIssue.setString(2, targetBookTitle);
                ResultSet rs = psIssue.executeQuery();

                if (rs.next()) {
                    Date returnDueSQL = rs.getDate("return_date");
                    LocalDate returnDueDate = returnDueSQL.toLocalDate();
                    LocalDate today = LocalDate.now();

                    System.out.println("\n--- Processing Return Track ---");
                    System.out.println("Due Date: " + returnDueDate);
                    System.out.println("Return Date: " + today);

                    double fine = 0.0;
                    if (today.isAfter(returnDueDate)) {
                        long daysLate = java.time.temporal.ChronoUnit.DAYS.between(returnDueDate, today);
                        fine = daysLate * 0.5; 
                        System.out.println("⚠️ Warning: Book is late by " + daysLate + " days.");
                        System.out.println("💰 Penalty Charges Accumulated: " + fine + " RS");
                    } else {
                        System.out.println("✅ Book returned on time! No penalty charges applied.");
                    }

                    try (PreparedStatement delStmt = conn.prepareStatement(deleteIssue);
                         PreparedStatement addStmt = conn.prepareStatement(restoreStock)) {
                        
                        delStmt.setString(1, targetID);
                        delStmt.setString(2, targetBookTitle);
                        delStmt.executeUpdate();

                        addStmt.setString(1, targetBookTitle);
                        addStmt.executeUpdate();

                        System.out.println("🎉 Success! Stock updated cleanly (+1 available).");
                    }
                } else {
                    System.out.println("❌ Match Error: No active record found for this student and book title combo.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Return processing failed: " + e.getMessage());
        }
    }

    // Matches 'Faculty -> Faculty_Search(String ID, BookDB obj)'
    public void Faculty_Search(String targetID, String targetBookTitle) {
        String userQuery = "SELECT * FROM faculty WHERE id = ?";
        String bookQuery = "SELECT quantity FROM BookDB WHERE book_title = ?";
        String currentIssuesQuery = "SELECT book_title FROM issued_books WHERE user_id = ? AND user_type = 'faculty'";

        try (Connection conn = DatabaseConnection.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(userQuery)) {
                ps.setString(1, targetID);
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) {
                    System.out.println("Faculty profile not found.");
                    return;
                }

                Faculty faculty = new Faculty();
                faculty.id = rs.getString("id");
                faculty.name = rs.getString("name");
                faculty.PhoneNo = rs.getString("phoneNum");
                faculty.Dept = rs.getString("dept");

                try (PreparedStatement psBook = conn.prepareStatement(bookQuery)) {
                    psBook.setString(1, targetBookTitle);
                    ResultSet rsBook = psBook.executeQuery();
                    if (!rsBook.next() || rsBook.getInt("quantity") <= 0) {
                        System.out.println("Book is currently unavailable or out of stock.");
                        return;
                    }
                }

                String insertIssue = "INSERT INTO issued_books (user_id, book_title, user_type, issue_date, return_date) VALUES (?, ?, 'faculty', ?, ?)";
                String reduceStock = "UPDATE BookDB SET quantity = quantity - 1 WHERE book_title = ?";

                try (PreparedStatement iStmt = conn.prepareStatement(insertIssue);
                     PreparedStatement rStmt = conn.prepareStatement(reduceStock)) {
                    
                    Date today = Date.valueOf(LocalDate.now());
                    Date returnDue = Date.valueOf(LocalDate.now().plusDays(30)); 

                    iStmt.setString(1, targetID);
                    iStmt.setString(2, targetBookTitle);
                    iStmt.setDate(3, today);
                    iStmt.setDate(4, returnDue);
                    iStmt.executeUpdate();

                    rStmt.setString(1, targetBookTitle);
                    rStmt.executeUpdate();

                    try (PreparedStatement cIssues = conn.prepareStatement(currentIssuesQuery)) {
                        cIssues.setString(1, targetID);
                        ResultSet rsIssues = cIssues.executeQuery();
                        while (rsIssues.next()) {
                            faculty.borrowedBooks.add(rsIssues.getString("book_title"));
                        }
                    }

                    System.out.println("🎉 Success! Book borrowed successfully.");
                    faculty.display();
                }
            }
        } catch (SQLException e) {
            System.out.println("Transaction process failed: " + e.getMessage());
        }
    }

    // NEW: Faculty Return Book Engine (Restocks inventory safely with No Fine Calculation)
    public void Faculty_Return(String targetID, String targetBookTitle) {
        String issueQuery = "SELECT issue_id FROM issued_books WHERE user_id = ? AND book_title = ? AND user_type = 'faculty'";
        String deleteIssue = "DELETE FROM issued_books WHERE user_id = ? AND book_title = ? AND user_type = 'faculty'";
        String restoreStock = "UPDATE BookDB SET quantity = quantity + 1 WHERE book_title = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            try (PreparedStatement psIssue = conn.prepareStatement(issueQuery)) {
                psIssue.setString(1, targetID);
                psIssue.setString(2, targetBookTitle);
                ResultSet rs = psIssue.executeQuery();

                if (rs.next()) {
                    System.out.println("\n--- Processing Faculty Return Track ---");

                    try (PreparedStatement delStmt = conn.prepareStatement(deleteIssue);
                         PreparedStatement addStmt = conn.prepareStatement(restoreStock)) {
                        
                        delStmt.setString(1, targetID);
                        delStmt.setString(2, targetBookTitle);
                        delStmt.executeUpdate();

                        addStmt.setString(1, targetBookTitle);
                        addStmt.executeUpdate();

                        System.out.println("✅ Success! '" + targetBookTitle + "' returned on time. Stock updated cleanly (+1 available).");
                    }
                } else {
                    System.out.println("❌ Match Error: No active tracking record found for this faculty ID and book title combo.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Faculty return processing failed: " + e.getMessage());
        }
    }
}