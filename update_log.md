# Project Update Log & Phase Transitions

This document tracks the incremental upgrades and architectural transitions of the Library Management System from a basic school project to a premium personal portfolio backend.

---

## 📋 Initial Project State (Legacy System)
Before starting the upgrade, the library system was configured with the following parameters:
- **UI Interfaces**: A command-line root portal (`MainApplication.java`) and a basic HTML web dashboard (`frontend/index.html`).
- **Security**: 
  - Zero password security on the CLI. Students and Faculty logged in simply by typing their user ID (anyone could access any user's profile with their ID card number).
  - Admins had no authentication check in the CLI. The web portal used a hardcoded frontend-only check for `admin123`.
- **Database**: Local MySQL database (`library_db`) containing basic tables (`student`, `faculty`, `bookdb`, `issued_books`).

---

## 🔐 Phase 1: Robust Cryptographic User Authentication (Completed)
**Goal**: Upgrade the backend to secure user accounts using cryptographically salted BCrypt hashing.

### Major Changes Implemented:
1. **BCrypt Utility Integration**:
   - Added [PasswordHasher.java](file:///c:/Users/sumed/Desktop/Code/WE--Desi/src/PasswordHasher.java) to wrap `jbcrypt`'s password hashing and validation functions.
   - Downloaded `jbcrypt-0.4.jar` and integrated it into the classpath config [.classpath](file:///c:/Users/sumed/Desktop/Code/WE--Desi/.classpath).
2. **Database Schema Enhancements**:
   - Added a `password_hash` column (`VARCHAR(255)`) to both the `student` and `faculty` tables.
   - Created a dedicated `admin` credentials table with an index-safe primary key size (`VARCHAR(191)`).
   - Seeded default admin credentials: Username: `admin`, Password: `admin123`.
3. **Core Services Upgrade**:
   - Upgraded [LibraryService.java](file:///c:/Users/sumed/Desktop/Code/WE--Desi/src/LibraryService.java) to support hashing on new user registration and validation on logins.
   - Implemented overloaded registration methods to preserve backward-compatibility with the web dashboard, ensuring it compiles without modification.
4. **CLI Entry Upgrade**:
   - Updated [MainApplication.java](file:///c:/Users/sumed/Desktop/Code/WE--Desi/src/MainApplication.java) to prompt for password credentials on every portal entry (Student, Faculty, and Admin) and verify them live against the database.
5. **Migration Runner**:
   - Created a standalone Java utility [RunMigration.java](file:///c:/Users/sumed/Desktop/Code/WE--Desi/src/RunMigration.java) to automate SQL table modifications.

---

## ⚡ Phase 2: High-Performance Connection Pooling (Completed)
**Goal**: Replace standard, slow `DriverManager`-based JDBC connections with a production-grade connection pool using HikariCP, optimizing concurrent database access.

### Major Changes Implemented:
1. **HikariCP Configuration**:
   - Replaced legacy JDBC connection code in [DBConnection.java](file:///c:/Users/sumed/Desktop/Code/WE--Desi/src/DBConnection.java) with a static, optimized `HikariDataSource` pool instance.
   - Configured premium tuning parameters:
     - `maximumPoolSize`: 10 (Restricts maximum concurrent background connections)
     - `minimumIdle`: 2 (Keeps at least 2 connections warmed up and ready)
     - `idleTimeout`: 300,000 ms (5 minutes before cleaning up idle connections)
     - `connectionTimeout`: 20,000 ms (20 seconds max wait time for a connection before timeout exception)
2. **Dependency Management Integration**:
   - Downloaded and placed the HikariCP pool engine and its logging dependencies (`HikariCP-5.1.0.jar`, `slf4j-api-2.0.12.jar`, and `slf4j-simple-2.0.12.jar`) into the `lib/` directory.
   - Updated the Eclipse [.classpath](file:///c:/Users/sumed/Desktop/Code/WE--Desi/.classpath) file to reference the new libraries.
3. **Resource Management Audit**:
   - Refactored transactional database query methods (`addNewBook`, `borrowBook`, and `returnBook`) in [LibraryService.java](file:///c:/Users/sumed/Desktop/Code/WE--Desi/src/LibraryService.java) to use Java's try-with-resources syntax.
   - Used a transaction-safe nested try-catch block structure to guarantee that connections are immediately released and returned to the HikariCP pool, even if a runtime sql query exception triggers a rollback.

---

## 🚀 Phase 3: Spring Boot & Spring Data JPA Migration (Completed)
**Goal**: Migrate away from raw socket-based web servers and custom JDBC helper logic into a production-grade REST API architecture using Spring Boot and Spring Data JPA.

### Major Changes Implemented:
1. **Maven Configuration (`pom.xml`)**:
   - Configured [pom.xml](file:///c:/Users/sumed/Desktop/Code/WE--Desi/pom.xml) with core Spring Boot Starters: `spring-boot-starter-web` for HTTP MVC capabilities, `spring-boot-starter-data-jpa` for ORM database operations, and standard database drivers and security dependencies.
2. **Spring Properties (`application.properties`)**:
   - Set database pooling properties (`HikariCP` configured natively via Spring configuration prefix) and Hibernate DDL updates in [application.properties](file:///c:/Users/sumed/Desktop/Code/WE--Desi/src/main/resources/application.properties).
3. **ORM JPA Entities**:
   - Mapped domain model classes to their database counterparts using standard JPA annotations (`@Entity`, `@Table`, `@Id`, `@ManyToOne`, etc.):
     - [Student.java](file:///c:/Users/sumed/Desktop/Code/WE--Desi/src/main/java/com/example/library/model/Student.java)
     - [Faculty.java](file:///c:/Users/sumed/Desktop/Code/WE--Desi/src/main/java/com/example/library/model/Faculty.java)
     - [Admin.java](file:///c:/Users/sumed/Desktop/Code/WE--Desi/src/main/java/com/example/library/model/Admin.java)
     - [Book.java](file:///c:/Users/sumed/Desktop/Code/WE--Desi/src/main/java/com/example/library/model/Book.java)
     - [IssuedBook.java](file:///c:/Users/sumed/Desktop/Code/WE--Desi/src/main/java/com/example/library/model/IssuedBook.java) (with standard `@ManyToOne` foreign key association to the book DB).
4. **Spring Data Repository Interfaces**:
   - Built JpaRepository interfaces (`StudentRepository`, `FacultyRepository`, `AdminRepository`, `BookRepository`, `IssuedBookRepository`) to handle data query mapping and retrieval operations automatically.
5. **Headless Web API Controller**:
   - Implemented [LibraryController.java](file:///c:/Users/sumed/Desktop/Code/WE--Desi/src/main/java/com/example/library/controller/LibraryController.java) to expose REST API endpoints (`/api/admin/students`, `/api/admin/books`, `/api/books`, `/api/loans/borrow`, `/api/loans/return`) processing data exclusively in structured JSON format.
6. **Main Spring Boot Application**:
   - Created [LibraryApplication.java](file:///c:/Users/sumed/Desktop/Code/WE--Desi/src/main/java/com/example/library/LibraryApplication.java) to run the application context.


