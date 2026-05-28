package com.example.library.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "faculty")
public class Faculty {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "phoneNum", nullable = false)
    private String phoneNum;

    @Column(name = "dept", nullable = false)
    private String dept;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    // Constructors
    public Faculty() {}

    public Faculty(String id, String name, String phoneNum, String dept, String passwordHash) {
        this.id = id;
        this.name = name;
        this.phoneNum = phoneNum;
        this.dept = dept;
        this.passwordHash = passwordHash;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public String getDept() {
        return dept;
    }

    public void setDept(String dept) {
        this.dept = dept;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
}
