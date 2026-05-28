package com.example.library.repository;

import com.example.library.model.IssuedBook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface IssuedBookRepository extends JpaRepository<IssuedBook, Long> {
    
    /**
     * Find an active issue record by user ID and book title.
     * @param userId the ID of the borrowing user
     * @param bookTitle the title of the borrowed book
     * @return an Optional containing the record if found
     */
    Optional<IssuedBook> findByUserIdAndBookBookTitle(String userId, String bookTitle);

    /**
     * Find all active issue records by user ID and user type.
     * @param userId the ID of the borrowing user
     * @param userType Student or Faculty
     * @return a List of active issue records
     */
    List<IssuedBook> findByUserIdAndUserType(String userId, String userType);

    /**
     * Find all active issue records by user type.
     * @param userType Student or Faculty
     * @return a List of active issue records
     */
    List<IssuedBook> findByUserType(String userType);
}
