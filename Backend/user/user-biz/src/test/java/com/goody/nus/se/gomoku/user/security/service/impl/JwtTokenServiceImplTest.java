package com.goody.nus.se.gomoku.user.security.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link JwtTokenServiceImpl}
 * Tests all branches including token generation, verification, and extraction
 */
class JwtTokenServiceImplTest {

    private JwtTokenServiceImpl jwtTokenService;
    private Long testUserId;
    private String testEmail;
    private String testNickname;
    private String testRefreshToken;

    @BeforeEach
    void setUp() {
        jwtTokenService = new JwtTokenServiceImpl();
        // Set expiration to 24 hours (default)
        ReflectionTestUtils.setField(jwtTokenService, "expiration", 86400000L);

        testUserId = 12345L;
        testEmail = "test@example.com";
        testNickname = "TestUser";
        testRefreshToken = "test-refresh-token-uuid-1234567890";
    }

    // ==================== generateToken() Tests ====================

    @Test
    void generateToken_Success() {
        // When
        String token = jwtTokenService.generateToken(testUserId, testEmail, testNickname, testRefreshToken);

        // Then
        assertNotNull(token);
        assertTrue(token.split("\\.").length == 3); // JWT has 3 parts

        // Verify token contains userId in payload
        String payload = new String(java.util.Base64.getUrlDecoder().decode(token.split("\\.")[1]));
        assertTrue(payload.contains("\"userId\":" + testUserId));
        assertTrue(payload.contains("\"email\":\"" + testEmail + "\""));
        assertTrue(payload.contains("\"nickname\":\"" + testNickname + "\""));
    }

    @Test
    void generateToken_WithDifferentRefreshTokens_GeneratesDifferentTokens() {
        // Given
        String refreshToken1 = "refresh-token-1";
        String refreshToken2 = "refresh-token-2";

        // When
        String token1 = jwtTokenService.generateToken(testUserId, testEmail, testNickname, refreshToken1);
        String token2 = jwtTokenService.generateToken(testUserId, testEmail, testNickname, refreshToken2);

        // Then
        assertNotNull(token1);
        assertNotNull(token2);
        assertNotEquals(token1, token2); // Different refresh tokens should produce different signatures
    }

    @Test
    void generateToken_WithShortRefreshToken_Success() {
        // Given - short refresh token that needs padding
        String shortRefreshToken = "short";

        // When
        String token = jwtTokenService.generateToken(testUserId, testEmail, testNickname, shortRefreshToken);

        // Then
        assertNotNull(token);
        assertTrue(token.split("\\.").length == 3);
    }

    @Test
    void generateToken_WithLongRefreshToken_Success() {
        // Given - very long refresh token
        String longRefreshToken = "very-long-refresh-token-that-exceeds-minimum-length-requirements-1234567890-abcdefgh";

        // When
        String token = jwtTokenService.generateToken(testUserId, testEmail, testNickname, longRefreshToken);

        // Then
        assertNotNull(token);
        assertTrue(token.split("\\.").length == 3);
    }

    @Test
    void generateToken_WithCustomExpiration_Success() {
        // Given - set custom expiration (1 hour)
        ReflectionTestUtils.setField(jwtTokenService, "expiration", 3600000L);

        // When
        String token = jwtTokenService.generateToken(testUserId, testEmail, testNickname, testRefreshToken);

        // Then
        assertNotNull(token);
        assertTrue(token.split("\\.").length == 3);
    }

    // ==================== verifyToken() Tests ====================

    @Test
    void verifyToken_WithValidToken_ReturnsUserId() {
        // Given - generate a valid token
        String token = jwtTokenService.generateToken(testUserId, testEmail, testNickname, testRefreshToken);

        // When
        Long verifiedUserId = jwtTokenService.verifyToken(token, testRefreshToken);

        // Then
        assertNotNull(verifiedUserId);
        assertEquals(testUserId, verifiedUserId);
    }

    @Test
    void verifyToken_WithWrongRefreshToken_ReturnsNull() {
        // Given - generate token with one refresh token
        String token = jwtTokenService.generateToken(testUserId, testEmail, testNickname, testRefreshToken);

        // When - verify with different refresh token
        Long verifiedUserId = jwtTokenService.verifyToken(token, "wrong-refresh-token");

        // Then - signature verification should fail
        assertNull(verifiedUserId);
    }

    @Test
    void verifyToken_WithMalformedToken_ReturnsNull() {
        // Given - malformed token (not 3 parts)
        String malformedToken = "malformed.token";

        // When
        Long verifiedUserId = jwtTokenService.verifyToken(malformedToken, testRefreshToken);

        // Then
        assertNull(verifiedUserId);
    }

    @Test
    void verifyToken_WithInvalidBase64_ReturnsNull() {
        // Given - invalid base64 encoding
        String invalidToken = "invalid!!!.base64!!!.encoding!!!";

        // When
        Long verifiedUserId = jwtTokenService.verifyToken(invalidToken, testRefreshToken);

        // Then
        assertNull(verifiedUserId);
    }

    @Test
    void verifyToken_WithExpiredToken_ReturnsNull() {
        // Given - set very short expiration (1 millisecond)
        ReflectionTestUtils.setField(jwtTokenService, "expiration", 1L);
        String token = jwtTokenService.generateToken(testUserId, testEmail, testNickname, testRefreshToken);

        // Wait for token to expire
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // When
        Long verifiedUserId = jwtTokenService.verifyToken(token, testRefreshToken);

        // Then - should return null due to expiration
        assertNull(verifiedUserId);
    }

    @Test
    void verifyToken_WithTamperedPayload_ReturnsNull() {
        // Given - generate valid token then tamper with it
        String token = jwtTokenService.generateToken(testUserId, testEmail, testNickname, testRefreshToken);
        String[] parts = token.split("\\.");

        // Tamper with payload by changing userId
        String tamperedPayload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]))
                .replace("\"userId\":" + testUserId, "\"userId\":99999");
        String tamperedToken = parts[0] + "." +
                java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(tamperedPayload.getBytes()) +
                "." + parts[2];

        // When
        Long verifiedUserId = jwtTokenService.verifyToken(tamperedToken, testRefreshToken);

        // Then - signature verification should fail
        assertNull(verifiedUserId);
    }

    @Test
    void verifyToken_WithNullToken_ReturnsNull() {
        // When
        Long verifiedUserId = jwtTokenService.verifyToken(null, testRefreshToken);

        // Then
        assertNull(verifiedUserId);
    }

    @Test
    void verifyToken_WithEmptyToken_ReturnsNull() {
        // When
        Long verifiedUserId = jwtTokenService.verifyToken("", testRefreshToken);

        // Then
        assertNull(verifiedUserId);
    }

    // ==================== extractUserId() Tests ====================

    @Test
    void extractUserId_WithValidToken_ReturnsUserId() {
        // Given - generate a valid token
        String token = jwtTokenService.generateToken(testUserId, testEmail, testNickname, testRefreshToken);

        // When - extract userId without verification
        Long extractedUserId = jwtTokenService.extractUserId(token);

        // Then
        assertNotNull(extractedUserId);
        assertEquals(testUserId, extractedUserId);
    }

    @Test
    void extractUserId_WithValidTokenButDifferentRefreshToken_StillReturnsUserId() {
        // Given - generate token with one refresh token
        String token = jwtTokenService.generateToken(testUserId, testEmail, testNickname, testRefreshToken);

        // When - extract userId (no verification, so refresh token doesn't matter)
        Long extractedUserId = jwtTokenService.extractUserId(token);

        // Then - should still extract userId successfully
        assertNotNull(extractedUserId);
        assertEquals(testUserId, extractedUserId);
    }

    @Test
    void extractUserId_WithMalformedToken_ReturnsNull() {
        // Given - malformed token (not 3 parts)
        String malformedToken = "malformed.token";

        // When
        Long extractedUserId = jwtTokenService.extractUserId(malformedToken);

        // Then
        assertNull(extractedUserId);
    }

    @Test
    void extractUserId_WithInvalidBase64_ReturnsNull() {
        // Given - invalid base64 encoding in payload
        String invalidToken = "valid.invalid!!!base64.signature";

        // When
        Long extractedUserId = jwtTokenService.extractUserId(invalidToken);

        // Then
        assertNull(extractedUserId);
    }

    @Test
    void extractUserId_WithMissingUserId_ReturnsNull() {
        // Given - manually create token without userId claim
        String header = java.util.Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"alg\":\"HS256\"}".getBytes());
        String payload = java.util.Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"email\":\"test@example.com\"}".getBytes());
        String token = header + "." + payload + ".signature";

        // When
        Long extractedUserId = jwtTokenService.extractUserId(token);

        // Then
        assertNull(extractedUserId);
    }

    @Test
    void extractUserId_WithInvalidUserIdFormat_ReturnsNull() {
        // Given - token with non-numeric userId
        String header = java.util.Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"alg\":\"HS256\"}".getBytes());
        String payload = java.util.Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"userId\":\"not-a-number\"}".getBytes());
        String token = header + "." + payload + ".signature";

        // When
        Long extractedUserId = jwtTokenService.extractUserId(token);

        // Then
        assertNull(extractedUserId);
    }

    @Test
    void extractUserId_WithUserIdAtEnd_Success() {
        // Given - token with userId at the end of payload (no comma after)
        String header = java.util.Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"alg\":\"HS256\"}".getBytes());
        String payload = java.util.Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"email\":\"test@example.com\",\"userId\":12345}".getBytes());
        String token = header + "." + payload + ".signature";

        // When
        Long extractedUserId = jwtTokenService.extractUserId(token);

        // Then
        assertNotNull(extractedUserId);
        assertEquals(12345L, extractedUserId);
    }

    @Test
    void extractUserId_WithNullToken_ReturnsNull() {
        // When
        Long extractedUserId = jwtTokenService.extractUserId(null);

        // Then
        assertNull(extractedUserId);
    }

    @Test
    void extractUserId_WithEmptyToken_ReturnsNull() {
        // When
        Long extractedUserId = jwtTokenService.extractUserId("");

        // Then
        assertNull(extractedUserId);
    }

    @Test
    void extractUserId_WithOnlyOnePart_ReturnsNull() {
        // Given
        String invalidToken = "onlyonepart";

        // When
        Long extractedUserId = jwtTokenService.extractUserId(invalidToken);

        // Then
        assertNull(extractedUserId);
    }

    // ==================== Integration Tests ====================

    @Test
    void fullFlow_GenerateVerifyExtract_Success() {
        // Given
        Long userId = 98765L;
        String email = "integration@test.com";
        String nickname = "IntegrationUser";
        String refreshToken = "integration-refresh-token";

        // When - generate token
        String token = jwtTokenService.generateToken(userId, email, nickname, refreshToken);

        // Then - extract userId
        Long extractedUserId = jwtTokenService.extractUserId(token);
        assertEquals(userId, extractedUserId);

        // And - verify token
        Long verifiedUserId = jwtTokenService.verifyToken(token, refreshToken);
        assertEquals(userId, verifiedUserId);
    }

    @Test
    void fullFlow_GenerateWithOneTokenVerifyWithAnother_VerificationFails() {
        // Given
        String refreshToken1 = "refresh-token-1";
        String refreshToken2 = "refresh-token-2";

        // When - generate with token1
        String token = jwtTokenService.generateToken(testUserId, testEmail, testNickname, refreshToken1);

        // Then - extract should work
        Long extractedUserId = jwtTokenService.extractUserId(token);
        assertEquals(testUserId, extractedUserId);

        // But verify with token2 should fail
        Long verifiedUserId = jwtTokenService.verifyToken(token, refreshToken2);
        assertNull(verifiedUserId);
    }

    @Test
    void createSigningKey_WithShortKey_PadsCorrectly() {
        // Given - very short refresh token (less than 32 bytes)
        String shortToken = "abc";

        // When - generate token (internally tests padding)
        String token = jwtTokenService.generateToken(testUserId, testEmail, testNickname, shortToken);

        // Then - should successfully create token
        assertNotNull(token);

        // And - should be able to verify with same short token
        Long verifiedUserId = jwtTokenService.verifyToken(token, shortToken);
        assertEquals(testUserId, verifiedUserId);
    }

    @Test
    void createSigningKey_WithExactly32Bytes_Success() {
        // Given - refresh token with exactly 32 bytes
        String token32Bytes = "12345678901234567890123456789012"; // 32 chars = 32 bytes

        // When
        String token = jwtTokenService.generateToken(testUserId, testEmail, testNickname, token32Bytes);

        // Then
        assertNotNull(token);
        Long verifiedUserId = jwtTokenService.verifyToken(token, token32Bytes);
        assertEquals(testUserId, verifiedUserId);
    }

    @Test
    void generateToken_WithVeryLargeUserId_Success() {
        // Given - very large userId
        Long largeUserId = Long.MAX_VALUE;

        // When
        String token = jwtTokenService.generateToken(largeUserId, testEmail, testNickname, testRefreshToken);

        // Then
        assertNotNull(token);
        Long extractedUserId = jwtTokenService.extractUserId(token);
        assertEquals(largeUserId, extractedUserId);
    }

    @Test
    void generateToken_WithSpecialCharactersInEmail_Success() {
        // Given - email with special characters
        String specialEmail = "test+special@sub-domain.example.com";

        // When
        String token = jwtTokenService.generateToken(testUserId, specialEmail, testNickname, testRefreshToken);

        // Then
        assertNotNull(token);
        String payload = new String(java.util.Base64.getUrlDecoder().decode(token.split("\\.")[1]));
        assertTrue(payload.contains(specialEmail));
    }

    @Test
    void generateToken_WithUnicodeNickname_Success() {
        // Given - nickname with Unicode characters
        String unicodeNickname = "用户测试";

        // When
        String token = jwtTokenService.generateToken(testUserId, testEmail, unicodeNickname, testRefreshToken);

        // Then
        assertNotNull(token);
        Long verifiedUserId = jwtTokenService.verifyToken(token, testRefreshToken);
        assertEquals(testUserId, verifiedUserId);
    }
}
