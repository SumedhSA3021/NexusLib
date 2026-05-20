import java.util.ArrayList;

// Base Class Box: User
class User {
    public String name;
    public String id;
    // Dynamic list holding multiple active borrowed book titles
    public ArrayList<String> borrowedBooks = new ArrayList<>();
}

// Child Class Box: Student
class Student extends User {
    public String PhoneNo;
    public int Sem;
    public String Dept;

    public void display() {
        System.out.println("\n--- Student Details ---");
        System.out.println("id: " + id);
        System.out.println("name: " + name);
        System.out.println("PhoneNo: " + PhoneNo);
        System.out.println("Sem: " + Sem);
        System.out.println("Dept: " + Dept);
        System.out.println("Borrowed Books: " + (borrowedBooks.isEmpty() ? "None Currently" : borrowedBooks));
    }
}

// Child Class Box: Faculty
class Faculty extends User {
    public String PhoneNo;
    public String Dept;

    public void display() {
        System.out.println("\n--- Faculty Details ---");
        System.out.println("id: " + id);
        System.out.println("name: " + name);
        System.out.println("PhoneNo: " + PhoneNo);
        System.out.println("Dept: " + Dept);
        System.out.println("Borrowed Books: " + (borrowedBooks.isEmpty() ? "None Currently" : borrowedBooks));
    }
}