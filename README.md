  # NexusLib 📚 | Enterprise Library Management Data Engine

A high-performance, secure, and decoupled full-stack Web Application built using a modern Spring Boot REST API and a premium Tailwind CSS interface.

---

## 🛠️ Tech Stack & Architecture

NexusLib is built on a decoupled, **Three-Tier Client-Server Architecture** designed to completely isolate user interactions from core database transactions:

* **Frontend (Client Layer)**: A modern, single-page dashboard built with **HTML5, ES6+ JavaScript (Fetch API)**, and styled using the **Tailwind CSS** utility framework for a premium dark-mode/glassmorphic user experience.
* **Backend Framework**: A headless **Spring Boot** Web API server processing incoming payloads exclusively via asynchronous network JSON streams.
* **Data Access & ORM**: **Spring Data JPA (Hibernate)** acting as the object-relational mapping engine to handle relational database schema states as native Java entities.
* **Database & Pooling**: **MySQL** optimized with a high-performance **HikariCP connection pool** to maximize throughput and ensure zero-latency data operations.

---

## ⚡ Production-Grade Engineering Highlights

Unlike standard academic projects, NexusLib was engineered around industrial security and efficiency principles:

* **🔒 Cryptographic Security**: Integrated the **BCrypt hashing engine** to secure user and administrative account profiles, ensuring passwords are securely salted and encrypted before touching database disk storage.
* **🚀 Database Connection Pooling**: Leveraged **HikariCP** to maintain a warm pool of reusable connections to port `3306`, dropping transaction latency to near-zero and preventing execution bottlenecks.
* **🛡️ Data Validation Rules**: Implemented strict backend business logic verification gates, including database transaction tracking, automated loan duration calculation (+14 days), and a hard **10-copy inventory cap per book title** to guarantee database integrity.

---

## 🚀 Local Deployment Guide

### 1. Prerequisites
Ensure you have the following software infrastructure installed:
* Java Development Kit (JDK 17 or higher)
* Apache Maven
* Local MySQL server instance (via XAMPP, WampServer, or Docker)

### 2. Database Set Up
1. Start your local MySQL service on port `3306`.
2. Access your database management tool (e.g., phpMyAdmin) and create an empty schema instance named:
   ```sql
   CREATE DATABASE library_db;
