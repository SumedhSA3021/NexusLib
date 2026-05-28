package com.example.library.controller;

import com.example.library.model.*;
import com.example.library.repository.*;
import com.example.library.utility.PasswordHasher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // Allows the frontend HTML page to connect from any local origin
public class LibraryController {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private FacultyRepository facultyRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private IssuedBookRepository issuedBookRepository;

    @Autowired
    private AdminRepository adminRepository;

    // =========================================================================
    //  WEB FRONTEND COMPATIBILITY ENDPOINTS (x-www-form-urlencoded)
    // =========================================================================

    // 1. GET /api/catalog -> Full Book Catalog
    @GetMapping("/catalog")
    public ResponseEntity<?> getCatalog() {
        try {
            List<Book> books = bookRepository.findAll();
            List<Map<String, Object>> booksResponse = new ArrayList<>();
            int totalCopies = 0;

            for (Book b : books) {
                totalCopies += b.getQuantity();
                String status = b.getQuantity() > 0 ? "In Stock" : "Out of Stock";
                booksResponse.add(Map.of(
                        "title", b.getBookTitle(),
                        "author", b.getAuthor(),
                        "quantity", b.getQuantity(),
                        "status", status
                ));
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "totalTitles", books.size(),
                    "totalCopies", totalCopies,
                    "books", booksResponse
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", "Error: " + e.getMessage()));
        }
    }

    // 2. GET /api/search -> Search book availability
    @GetMapping("/search")
    public ResponseEntity<?> searchBook(@RequestParam("title") String title) {
        Optional<Book> bookOpt = bookRepository.findById(title);
        if (bookOpt.isPresent()) {
            Book b = bookOpt.get();
            String status = b.getQuantity() > 0 ? "Available for borrowing" : "Out of Stock";
            return ResponseEntity.ok(Map.of(
                    "found", true,
                    "title", b.getBookTitle(),
                    "author", b.getAuthor(),
                    "quantity", b.getQuantity(),
                    "status", status
            ));
        } else {
            return ResponseEntity.ok(Map.of(
                    "found", false,
                    "message", "Book not found in database catalog."
            ));
        }
    }

    // 3. POST /api/student -> Student Transactions (Profile, Borrow, Return)
    @PostMapping(value = "/student", consumes = "application/x-www-form-urlencoded")
    @Transactional
    public ResponseEntity<?> handleStudentAction(@RequestParam Map<String, String> params) {
        String action = params.get("action");
        String stuId = params.get("stuId");
        String bookTitle = params.get("bookTitle");

        if ("profile".equalsIgnoreCase(action)) {
            return getStudentProfile(stuId);
        } else if ("login".equalsIgnoreCase(action)) {
            String password = params.get("password");
            return studentLogin(stuId, password);
        } else if ("borrow".equalsIgnoreCase(action)) {
            return borrowBook("Student", stuId, bookTitle);
        } else if ("return".equalsIgnoreCase(action)) {
            return returnBook(stuId, bookTitle);
        }
        return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Invalid action"));
    }

    // 4. POST /api/faculty -> Faculty Transactions (Profile, Borrow, Return)
    @PostMapping(value = "/faculty", consumes = "application/x-www-form-urlencoded")
    @Transactional
    public ResponseEntity<?> handleFacultyAction(@RequestParam Map<String, String> params) {
        String action = params.get("action");
        String facId = params.get("facId");
        String bookTitle = params.get("bookTitle");

        if ("profile".equalsIgnoreCase(action)) {
            return getFacultyProfile(facId);
        } else if ("login".equalsIgnoreCase(action)) {
            String password = params.get("password");
            return facultyLogin(facId, password);
        } else if ("borrow".equalsIgnoreCase(action)) {
            return borrowBook("Faculty", facId, bookTitle);
        } else if ("return".equalsIgnoreCase(action)) {
            return returnBook(facId, bookTitle);
        }
        return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Invalid action"));
    }

    // 5. POST /api/register -> Register Student/Faculty
    @PostMapping(value = "/register", consumes = "application/x-www-form-urlencoded")
    public ResponseEntity<?> registerUser(@RequestParam Map<String, String> params) {
        try {
            String userType = params.get("userType");
            String id = params.get("id");
            String name = params.get("name");
            String phoneNum = params.get("phoneNum");
            String semStr = params.getOrDefault("sem", "0");
            String dept = params.get("dept");
            String password = params.get("password");

            boolean success = false;
            String message = "";
            String activePassword = (password != null && !password.trim().isEmpty()) ? password.trim() : id;

            if ("Student".equalsIgnoreCase(userType)) {
                if (studentRepository.existsById(id)) {
                    message = "Failed to register Student (ID already exists).";
                } else {
                    int sem = Integer.parseInt(semStr);
                    String hashedPw = PasswordHasher.hashPassword(activePassword);
                    Student student = new Student(id, name, phoneNum, sem, dept, hashedPw);
                    studentRepository.save(student);
                    success = true;
                    message = "Student registered successfully! (Password: " + activePassword + ")";
                }
            } else if ("Faculty".equalsIgnoreCase(userType)) {
                if (facultyRepository.existsById(id)) {
                    message = "Failed to register Faculty (ID already exists).";
                } else {
                    String hashedPw = PasswordHasher.hashPassword(activePassword);
                    Faculty faculty = new Faculty(id, name, phoneNum, dept, hashedPw);
                    facultyRepository.save(faculty);
                    success = true;
                    message = "Faculty registered successfully! (Password: " + activePassword + ")";
                }
            } else {
                message = "Invalid registration type.";
            }

            return ResponseEntity.ok(Map.of("success", success, "message", message));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Registration error: " + e.getMessage()));
        }
    }

    // 6. POST /api/addbook -> Add Book / Update stock
    @PostMapping(value = "/addbook", consumes = "application/x-www-form-urlencoded")
    @Transactional
    public ResponseEntity<?> addBookForm(@RequestParam Map<String, String> params) {
        try {
            String bookTitle = params.get("bookTitle");
            String author = params.get("author");
            int quantity = Integer.parseInt(params.get("quantity"));

            Optional<Book> bookOpt = bookRepository.findById(bookTitle);
            int currentStock = bookOpt.map(Book::getQuantity).orElse(0);

            if (currentStock + quantity > 10) {
                return ResponseEntity.ok(Map.of(
                        "success", false,
                        "message", "Operation Denied: System inventory storage limit exceeded. Cannot hold more than 10 copies of any title."
                ));
            }

            Book book = bookOpt.orElse(new Book(bookTitle, author, 0));
            book.setQuantity(currentStock + quantity);
            bookRepository.save(book);

            return ResponseEntity.ok(Map.of("success", true, "message", "Book stock updated successfully!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Error: " + e.getMessage()));
        }
    }

    // 7. GET /api/admin/loans -> Fetch active loans for Admin View
    @GetMapping("/admin/loans")
    public ResponseEntity<?> getActiveLoans() {
        try {
            List<IssuedBook> studentLoans = issuedBookRepository.findByUserType("Student");
            List<IssuedBook> facultyLoans = issuedBookRepository.findByUserType("Faculty");

            List<Map<String, Object>> studentLoansList = new ArrayList<>();
            for (IssuedBook loan : studentLoans) {
                Optional<Student> studentOpt = studentRepository.findById(loan.getUserId());
                String name = studentOpt.map(Student::getName).orElse("--");
                String branch = studentOpt.map(Student::getDept).orElse("--");
                int sem = studentOpt.map(Student::getSem).orElse(0);

                studentLoansList.add(Map.of(
                        "usn", loan.getUserId(),
                        "name", name,
                        "branch", branch,
                        "sem", sem,
                        "bookTitle", loan.getBook().getBookTitle(),
                        "issueDate", loan.getIssueDate().toString(),
                        "returnDate", loan.getReturnDate().toString()
                ));
            }

            List<Map<String, Object>> facultyLoansList = new ArrayList<>();
            for (IssuedBook loan : facultyLoans) {
                Optional<Faculty> facultyOpt = facultyRepository.findById(loan.getUserId());
                String name = facultyOpt.map(Faculty::getName).orElse("--");

                facultyLoansList.add(Map.of(
                        "facId", loan.getUserId(),
                        "name", name,
                        "bookTitle", loan.getBook().getBookTitle()
                ));
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "studentLoans", studentLoansList,
                    "facultyLoans", facultyLoansList
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", "Error: " + e.getMessage()));
        }
    }

    // 8. POST /api/admin/login -> Validate Admin credentials (x-www-form-urlencoded)
    @PostMapping(value = "/admin/login", consumes = "application/x-www-form-urlencoded")
    public ResponseEntity<?> adminLogin(@RequestParam Map<String, String> params) {
        String username = params.getOrDefault("username", "admin");
        String password = params.get("password");

        if (password == null || password.trim().isEmpty()) {
            return ResponseEntity.ok(Map.of("success", false, "message", "Password is required."));
        }

        Optional<Admin> adminOpt = adminRepository.findById(username);
        if (adminOpt.isPresent()) {
            Admin admin = adminOpt.get();
            if (PasswordHasher.checkPassword(password, admin.getPasswordHash())) {
                return ResponseEntity.ok(Map.of("success", true, "message", "Admin login successful."));
            }
        }
        return ResponseEntity.ok(Map.of("success", false, "message", "Invalid admin credentials."));
    }

    // =========================================================================
    //  INTERNAL HELPER METHODS FOR BUSINESS LOGIC
    // =========================================================================

    private ResponseEntity<?> studentLogin(String id, String password) {
        Optional<Student> studentOpt = studentRepository.findById(id);
        if (studentOpt.isEmpty()) {
            return ResponseEntity.ok(Map.of("success", false, "message", "Student profile not found."));
        }

        Student student = studentOpt.get();
        if (password == null || !PasswordHasher.checkPassword(password, student.getPasswordHash())) {
            return ResponseEntity.ok(Map.of("success", false, "message", "Invalid password."));
        }

        return getStudentProfile(id);
    }

    private ResponseEntity<?> facultyLogin(String id, String password) {
        Optional<Faculty> facultyOpt = facultyRepository.findById(id);
        if (facultyOpt.isEmpty()) {
            return ResponseEntity.ok(Map.of("success", false, "message", "Faculty profile not found."));
        }

        Faculty faculty = facultyOpt.get();
        if (password == null || !PasswordHasher.checkPassword(password, faculty.getPasswordHash())) {
            return ResponseEntity.ok(Map.of("success", false, "message", "Invalid password."));
        }

        return getFacultyProfile(id);
    }

    private ResponseEntity<?> getStudentProfile(String id) {
        Optional<Student> studentOpt = studentRepository.findById(id);
        if (studentOpt.isEmpty()) {
            return ResponseEntity.ok(Map.of("success", false, "message", "Student profile not found."));
        }

        Student student = studentOpt.get();
        List<IssuedBook> loans = issuedBookRepository.findByUserIdAndUserType(id, "Student");
        List<Map<String, String>> borrowedList = new ArrayList<>();

        for (IssuedBook loan : loans) {
            borrowedList.add(Map.of(
                    "title", loan.getBook().getBookTitle(),
                    "issuedDate", loan.getIssueDate().toString(),
                    "returnDate", loan.getReturnDate().toString()
            ));
        }

        Map<String, Object> response = Map.of(
                "id", student.getId(),
                "name", student.getName(),
                "phoneNum", student.getPhoneNum(),
                "sem", student.getSem(),
                "dept", student.getDept(),
                "borrowedBooks", borrowedList
        );

        return ResponseEntity.ok(Map.of("success", true, "student", response));
    }

    private ResponseEntity<?> getFacultyProfile(String id) {
        Optional<Faculty> facultyOpt = facultyRepository.findById(id);
        if (facultyOpt.isEmpty()) {
            return ResponseEntity.ok(Map.of("success", false, "message", "Faculty profile not found."));
        }

        Faculty faculty = facultyOpt.get();
        List<IssuedBook> loans = issuedBookRepository.findByUserIdAndUserType(id, "Faculty");
        List<Map<String, String>> borrowedList = new ArrayList<>();

        for (IssuedBook loan : loans) {
            borrowedList.add(Map.of(
                    "title", loan.getBook().getBookTitle(),
                    "issuedDate", loan.getIssueDate().toString(),
                    "returnDate", loan.getReturnDate().toString()
            ));
        }

        Map<String, Object> response = Map.of(
                "id", faculty.getId(),
                "name", faculty.getName(),
                "phoneNum", faculty.getPhoneNum(),
                "dept", faculty.getDept(),
                "borrowedBooks", borrowedList
        );

        return ResponseEntity.ok(Map.of("success", true, "faculty", response));
    }

    private ResponseEntity<?> borrowBook(String userType, String userId, String bookTitle) {
        Optional<Book> bookOpt = bookRepository.findById(bookTitle);
        if (bookOpt.isEmpty() || bookOpt.get().getQuantity() <= 0) {
            return ResponseEntity.ok(Map.of("success", false, "message", "Book is currently unavailable or out of stock."));
        }

        Book book = bookOpt.get();
        book.setQuantity(book.getQuantity() - 1);
        bookRepository.save(book);

        LocalDate issueDate = LocalDate.now();
        int limitDays = "Student".equalsIgnoreCase(userType) ? 14 : 30;
        LocalDate returnDate = issueDate.plusDays(limitDays);

        IssuedBook loan = new IssuedBook(userId, userType, book, issueDate, returnDate);
        issuedBookRepository.save(loan);

        // Fetch updated profile
        Object profileData;
        if ("Student".equalsIgnoreCase(userType)) {
            profileData = ((Map<?, ?>) Objects.requireNonNull(getStudentProfile(userId).getBody())).get("student");
        } else {
            profileData = ((Map<?, ?>) Objects.requireNonNull(getFacultyProfile(userId).getBody())).get("faculty");
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Success! Book borrowed successfully.",
                "issuedDate", issueDate.toString(),
                "returnDate", returnDate.toString(),
                userType.toLowerCase(), profileData
        ));
    }

    private ResponseEntity<?> returnBook(String userId, String bookTitle) {
        Optional<IssuedBook> loanOpt = issuedBookRepository.findByUserIdAndBookBookTitle(userId, bookTitle);
        if (loanOpt.isEmpty()) {
            return ResponseEntity.ok(Map.of("success", false, "message", "Match Error: No active borrowing record found."));
        }

        IssuedBook loan = loanOpt.get();
        Book book = loan.getBook();
        book.setQuantity(book.getQuantity() + 1);
        bookRepository.save(book);

        issuedBookRepository.delete(loan);

        LocalDate returnDueDate = loan.getReturnDate();
        LocalDate today = LocalDate.now();
        long daysLate = 0;
        double fine = 0.0;

        if ("Student".equalsIgnoreCase(loan.getUserType()) && today.isAfter(returnDueDate)) {
            daysLate = ChronoUnit.DAYS.between(returnDueDate, today);
            fine = daysLate * 0.5;
        }

        Object updatedProfile;
        if ("Student".equalsIgnoreCase(loan.getUserType())) {
            updatedProfile = getStudentProfile(userId).getBody();
        } else {
            updatedProfile = getFacultyProfile(userId).getBody();
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Book returned successfully.",
                "daysLate", daysLate,
                "fine", fine,
                "profile", updatedProfile
        ));
    }
}
