package com.goody.nus.se.gomoku.user.security.service.impl;

import com.goody.nus.se.gomoku.user.security.service.IJwtTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Test class for JwtTokenServiceImpl
 * Tests the JWT token generation and verification using refresh token as signing key
 *
 * @author Goody
 * @version 1.0, 2025/10/8
 */
class JwtTokenServiceImplTest {

    private IJwtTokenService jwtTokenService;
    private String refreshToken;

    @BeforeEach
    void setUp() {
        JwtTokenServiceImpl service = new JwtTokenServiceImpl();

        // Inject property values using ReflectionTestUtils
        ReflectionTestUtils.setField(service, "expiration", 3600000L); // 1 hour

        this.jwtTokenService = service;
        this.refreshToken = UUID.randomUUID().toString();
    }

    @Test
    void generateToken_shouldReturnValidToken() {
        // Given
        Long userId = 123456L;
        String email = "test@example.com";
        String nickname = "testuser";

        // When
        String token = jwtTokenService.generateToken(userId, email, nickname, refreshToken);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertEquals(3, token.split("\\.").length); // JWT has 3 parts separated by dots
    }

    @Test
    void verifyToken_withValidToken_shouldReturnUserId() {
        // Given
        Long userId = 123456L;
        String email = "test@example.com";
        String nickname = "testuser";
        String token = jwtTokenService.generateToken(userId, email, nickname, refreshToken);

        // When
        Long verifiedUserId = jwtTokenService.verifyToken(token, refreshToken);

        // Then
        assertNotNull(verifiedUserId);
        assertEquals(userId, verifiedUserId);
    }

    @Test
    void verifyToken_withWrongRefreshToken_shouldReturnNull() {
        // Given
        Long userId = 123456L;
        String email = "test@example.com";
        String nickname = "testuser";
        String token = jwtTokenService.generateToken(userId, email, nickname, refreshToken);

        // Use a different refresh token for verification
        String wrongRefreshToken = UUID.randomUUID().toString();

        // When
        Long verifiedUserId = jwtTokenService.verifyToken(token, wrongRefreshToken);

        // Then - should fail because signature was created with different key
        assertNull(verifiedUserId);
    }

    @Test
    void verifyToken_withInvalidToken_shouldReturnNull() {
        // Given
        String invalidToken = "invalid.token.here";

        // When
        Long userId = jwtTokenService.verifyToken(invalidToken, refreshToken);

        // Then
        assertNull(userId);
    }

    @Test
    void verifyToken_withExpiredToken_shouldReturnNull() throws InterruptedException {
        // Given - create service with very short expiration
        JwtTokenServiceImpl service = new JwtTokenServiceImpl();
        ReflectionTestUtils.setField(service, "expiration", 1L); // 1 millisecond

        Long userId = 123456L;
        String token = service.generateToken(userId, "test@example.com", "testuser", refreshToken);

        // Wait for token to expire
        Thread.sleep(10);

        // When
        Long verifiedUserId = service.verifyToken(token, refreshToken);

        // Then
        assertNull(verifiedUserId);
    }

    @Test
    void verifyToken_withNullToken_shouldReturnNull() {
        // When
        Long userId = jwtTokenService.verifyToken(null, refreshToken);

        // Then
        assertNull(userId);
    }

    @Test
    void verifyToken_withEmptyToken_shouldReturnNull() {
        // When
        Long userId = jwtTokenService.verifyToken("", refreshToken);

        // Then
        assertNull(userId);
    }

    @Test
    void generateToken_withDifferentUsers_shouldGenerateDifferentTokens() {
        // Given
        Long userId1 = 123456L;
        Long userId2 = 789012L;

        // When
        String token1 = jwtTokenService.generateToken(userId1, "user1@example.com", "user1", refreshToken);
        String token2 = jwtTokenService.generateToken(userId2, "user2@example.com", "user2", refreshToken);

        // Then
        assertNotEquals(token1, token2);

        // Verify each token returns correct userId
        assertEquals(userId1, jwtTokenService.verifyToken(token1, refreshToken));
        assertEquals(userId2, jwtTokenService.verifyToken(token2, refreshToken));
    }

    @Test
    void verifyToken_afterTokenGeneration_shouldMaintainConsistency() {
        // Given
        Long userId = 999888777L;
        String email = "consistency@example.com";
        String nickname = "consistencytest";

        // When
        String token = jwtTokenService.generateToken(userId, email, nickname, refreshToken);
        Long verifiedUserId1 = jwtTokenService.verifyToken(token, refreshToken);
        Long verifiedUserId2 = jwtTokenService.verifyToken(token, refreshToken);

        // Then
        assertEquals(userId, verifiedUserId1);
        assertEquals(userId, verifiedUserId2);
        assertEquals(verifiedUserId1, verifiedUserId2);
    }

    @Test
    void extractUserId_withValidToken_shouldReturnUserId() {
        // Given
        Long userId = 123456L;
        String email = "test@example.com";
        String nickname = "testuser";
        String token = jwtTokenService.generateToken(userId, email, nickname, refreshToken);

        // When
        Long extractedUserId = jwtTokenService.extractUserId(token);

        // Then
        assertNotNull(extractedUserId);
        assertEquals(userId, extractedUserId);
    }

    @Test
    void extractUserId_withInvalidToken_shouldReturnNull() {
        // Given
        String invalidToken = "invalid.token.here";

        // When
        Long userId = jwtTokenService.extractUserId(invalidToken);

        // Then
        assertNull(userId);
    }

    @Test
    void extractUserId_withMalformedToken_shouldReturnNull() {
        // Given - token with only 2 parts
        String malformedToken = "header.payload";

        // When
        Long userId = jwtTokenService.extractUserId(malformedToken);

        // Then
        assertNull(userId);
    }

    @Test
    void extractUserId_withNullToken_shouldReturnNull() {
        // When
        Long userId = jwtTokenService.extractUserId(null);

        // Then
        assertNull(userId);
    }

    @Test
    void extractUserId_withEmptyToken_shouldReturnNull() {
        // When
        Long userId = jwtTokenService.extractUserId("");

        // Then
        assertNull(userId);
    }

    @Test
    void extractUserId_shouldWorkWithoutRefreshToken() {
        // Given - extractUserId should work even if we don't have the refresh token
        Long userId = 123456L;
        String email = "test@example.com";
        String nickname = "testuser";
        String token = jwtTokenService.generateToken(userId, email, nickname, refreshToken);

        // When - extract userId without needing the refresh token
        Long extractedUserId = jwtTokenService.extractUserId(token);

        // Then
        assertNotNull(extractedUserId);
        assertEquals(userId, extractedUserId);
    }

    @Test
    void tokenLifecycle_shouldWorkCorrectly() {
        // Simulate the complete token lifecycle

        // 1. User logs in - generate token with new refresh token
        Long userId = 555666777L;
        String email = "lifecycle@example.com";
        String nickname = "lifecycleuser";
        String refreshToken1 = UUID.randomUUID().toString();
        String token1 = jwtTokenService.generateToken(userId, email, nickname, refreshToken1);

        // 2. Extract userId from token (without verification)
        Long extractedUserId = jwtTokenService.extractUserId(token1);
        assertEquals(userId, extractedUserId);

        // 3. Verify token with correct refresh token
        Long verifiedUserId1 = jwtTokenService.verifyToken(token1, refreshToken1);
        assertEquals(userId, verifiedUserId1);

        // 4. User logs in again - new refresh token
        String refreshToken2 = UUID.randomUUID().toString();
        String token2 = jwtTokenService.generateToken(userId, email, nickname, refreshToken2);

        // 5. Old token should fail verification with new refresh token
        Long verifiedOldToken = jwtTokenService.verifyToken(token1, refreshToken2);
        assertNull(verifiedOldToken);

        // 6. New token should work with new refresh token
        Long verifiedNewToken = jwtTokenService.verifyToken(token2, refreshToken2);
        assertEquals(userId, verifiedNewToken);
    }

    @Test
    void differentRefreshTokens_shouldCreateDifferentSignatures() {
        // Given - same user data, different refresh tokens
        Long userId = 123456L;
        String email = "test@example.com";
        String nickname = "testuser";
        String refreshToken1 = UUID.randomUUID().toString();
        String refreshToken2 = UUID.randomUUID().toString();

        // When - generate tokens with different refresh tokens
        String token1 = jwtTokenService.generateToken(userId, email, nickname, refreshToken1);
        String token2 = jwtTokenService.generateToken(userId, email, nickname, refreshToken2);

        // Then - tokens should be different (different signatures)
        assertNotEquals(token1, token2);

        // And - each token can only be verified with its own refresh token
        assertNotNull(jwtTokenService.verifyToken(token1, refreshToken1));
        assertNull(jwtTokenService.verifyToken(token1, refreshToken2));

        assertNotNull(jwtTokenService.verifyToken(token2, refreshToken2));
        assertNull(jwtTokenService.verifyToken(token2, refreshToken1));
    }
}
