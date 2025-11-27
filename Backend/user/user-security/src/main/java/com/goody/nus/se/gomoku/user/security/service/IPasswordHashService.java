package com.goody.nus.se.gomoku.user.security.service;

/**
 * Password hashing service interface
 *
 * @author HaoTian
 * @version 1.0, 2025/10/4
 */
public interface IPasswordHashService {

    /**
     * Generate a UUID-based salt (32 characters without hyphens)
     *
     * @return UUID salt string
     */
    String generateSalt();

    /**
     * Hash a plain text password with the provided salt
     *
     * @param plainPassword plain text password (already decrypted from RSA)
     * @param salt UUID-based salt
     * @return hashed password in BCrypt format
     */
    String hashPassword(String plainPassword, String salt);

    /**
     * Verify a password against a stored hash and salt
     *
     * @param plainPassword plain text password (already decrypted from RSA)
     * @param hashedPassword stored BCrypt hash
     * @param salt stored UUID salt
     * @return true if password matches, false otherwise
     */
    boolean verifyPassword(String plainPassword, String hashedPassword, String salt);

    /**
     * Decrypt RSA-encrypted password from client
     *
     * @param encryptedPassword RSA encrypted password (Base64 encoded)
     * @return decrypted plain text password
     * @throws Exception if decryption fails
     */
    String decryptPassword(String encryptedPassword) throws Exception;
}
