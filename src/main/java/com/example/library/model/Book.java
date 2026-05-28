package com.example.library.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "bookdb")
public class Book {

    @Id
    @Column(name = "book_title")
    private String bookTitle;

    @Column(name = "author", nullable = false)
    private String author;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    // Constructors
    public Book() {}

    public Book(String bookTitle, String author, int quantity) {
        this.bookTitle = bookTitle;
        this.author = author;
        this.quantity = quantity;
    }

    // Getters and Setters
    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
