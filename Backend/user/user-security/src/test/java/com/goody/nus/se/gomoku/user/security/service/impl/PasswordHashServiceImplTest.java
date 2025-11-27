package com.goody.nus.se.gomoku.user.security.service.impl;

import com.goody.nus.se.gomoku.user.security.service.IPasswordHashService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test for {@link PasswordHashServiceImpl}
 *
 * @author Haotian
 * @version 1.0, 2025/10/4
 */
class PasswordHashServiceImplTest {

    private IPasswordHashService passwordHashService;

    @BeforeEach
    void setUp() {
        passwordHashService = new PasswordHashServiceImpl();
    }

    @Test
    void generateSalt_shouldReturnNonNullSalt() {
        String salt = passwordHashService.generateSalt();
        assertNotNull(salt);
    }

    @Test
    void generateSalt_shouldReturn32CharacterString() {
        String salt = passwordHashService.generateSalt();
        assertEquals(32, salt.length());
    }

    @Test
    void generateSalt_shouldReturnDifferentSalts() {
        String salt1 = passwordHashService.generateSalt();
        String salt2 = passwordHashService.generateSalt();
        assertNotEquals(salt1, salt2);
    }

    @Test
    void hashPassword_shouldReturnNonNullHash() {
        String plainPassword = "testPassword123";
        String salt = passwordHashService.generateSalt();

        String hash = passwordHashService.hashPassword(plainPassword, salt);

        assertNotNull(hash);
        assertFalse(hash.isEmpty());
    }

    @Test
    void hashPassword_shouldReturnBCryptHash() {
        String plainPassword = "testPassword123";
        String salt = passwordHashService.generateSalt();

        String hash = passwordHashService.hashPassword(plainPassword, salt);

        // BCrypt hashes start with $2a$, $2b$, or $2y$
        assertTrue(hash.startsWith("$2"));
    }

    @Test
    void hashPassword_samePlainPasswordAndSalt_shouldReturnDifferentHashes() {
        String plainPassword = "testPassword123";
        String salt = passwordHashService.generateSalt();

        String hash1 = passwordHashService.hashPassword(plainPassword, salt);
        String hash2 = passwordHashService.hashPassword(plainPassword, salt);

        // BCrypt includes its own internal salt, so hashes should be different
        assertNotEquals(hash1, hash2);
    }

    @Test
    void verifyPassword_correctPassword_shouldReturnTrue() {
        String plainPassword = "testPassword123";
        String salt = passwordHashService.generateSalt();
        String hash = passwordHashService.hashPassword(plainPassword, salt);

        boolean result = passwordHashService.verifyPassword(plainPassword, hash, salt);

        assertTrue(result);
    }

    @Test
    void verifyPassword_incorrectPassword_shouldReturnFalse() {
        String plainPassword = "testPassword123";
        String wrongPassword = "wrongPassword456";
        String salt = passwordHashService.generateSalt();
        String hash = passwordHashService.hashPassword(plainPassword, salt);

        boolean result = passwordHashService.verifyPassword(wrongPassword, hash, salt);

        assertFalse(result);
    }

    @Test
    void verifyPassword_incorrectSalt_shouldReturnFalse() {
        String plainPassword = "testPassword123";
        String salt1 = passwordHashService.generateSalt();
        String salt2 = passwordHashService.generateSalt();
        String hash = passwordHashService.hashPassword(plainPassword, salt1);

        boolean result = passwordHashService.verifyPassword(plainPassword, hash, salt2);

        assertFalse(result);
    }

    @Test
    void hashPassword_emptyPassword_shouldWork() {
        String emptyPassword = "";
        String salt = passwordHashService.generateSalt();

        String hash = passwordHashService.hashPassword(emptyPassword, salt);

        assertNotNull(hash);
        assertTrue(passwordHashService.verifyPassword(emptyPassword, hash, salt));
    }

    @Test
    void hashPassword_specialCharacters_shouldWork() {
        String specialPassword = "P@ssw0rd!#$%^&*()";
        String salt = passwordHashService.generateSalt();

        String hash = passwordHashService.hashPassword(specialPassword, salt);

        assertNotNull(hash);
        assertTrue(passwordHashService.verifyPassword(specialPassword, hash, salt));
    }

    @Test
    void hashPassword_unicodeCharacters_shouldWork() {
        String unicodePassword = "密码测试123";
        String salt = passwordHashService.generateSalt();

        String hash = passwordHashService.hashPassword(unicodePassword, salt);

        assertNotNull(hash);
        assertTrue(passwordHashService.verifyPassword(unicodePassword, hash, salt));
    }

    @Test
    void hashPassword_longPassword_shouldWork() {
        String longPassword = "a".repeat(20);
        String salt = passwordHashService.generateSalt();

        String hash = passwordHashService.hashPassword(longPassword, salt);

        assertNotNull(hash);
        assertTrue(passwordHashService.verifyPassword(longPassword, hash, salt));
    }
}
