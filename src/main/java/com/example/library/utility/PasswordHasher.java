package com.example.library.utility;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordHasher {

    /**
     * Hashes a plain-text password using BCrypt.
     * @param plainTextPassword the plain-text password to hash
     * @return the secure hashed password
     */
    public static String hashPassword(String plainTextPassword) {
        if (plainTextPassword == null) {
            throw new IllegalArgumentException("Password cannot be null");
        }
        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt());
    }

    /**
     * Verifies a plain-text password against a stored BCrypt cryptographic hash.
     * @param plainTextPassword the plain-text password to verify
     * @param hashedWithBCrypt the stored secure BCrypt hash
     * @return true if the credentials match, false otherwise
     */
    public static boolean checkPassword(String plainTextPassword, String hashedWithBCrypt) {
        if (plainTextPassword == null || hashedWithBCrypt == null) {
            return false;
        }
        try {
            return BCrypt.checkpw(plainTextPassword, hashedWithBCrypt);
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid BCrypt hash format: " + e.getMessage());
            return false;
        }
    }
}
