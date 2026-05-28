-- =========================================================================
-- DATABASE MIGRATION STRATEGY - PHASE 1
-- =========================================================================
-- This script adds password_hash columns to Student and Faculty tables,
-- creates the new Admin credentials table, and inserts default admin credentials.
-- =========================================================================

-- 1. Alter Student table to add password_hash (defaulting to empty string for compatibility)
ALTER TABLE student ADD COLUMN password_hash VARCHAR(255) NOT NULL DEFAULT '';

-- 2. Alter Faculty table to add password_hash (defaulting to empty string for compatibility)
ALTER TABLE faculty ADD COLUMN password_hash VARCHAR(255) NOT NULL DEFAULT '';

-- 3. Create a new Admin credentials table if it does not exist
CREATE TABLE IF NOT EXISTS admin (
    username VARCHAR(191) PRIMARY KEY,
    password_hash VARCHAR(255) NOT NULL
);

-- 4. Seed the Admin table with default credentials
-- Username: admin
-- Password: admin123
-- BCrypt Hash: $2a$10$NqRgy.H5PEjUNdp2G1deP.N31shKOHc36/IAT6tLIZYlLsQFCkhmq
INSERT INTO admin (username, password_hash) 
VALUES ('admin', '$2a$10$NqRgy.H5PEjUNdp2G1deP.N31shKOHc36/IAT6tLIZYlLsQFCkhmq')
ON DUPLICATE KEY UPDATE password_hash = VALUES(password_hash);
