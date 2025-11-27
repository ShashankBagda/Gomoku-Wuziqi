package com.goody.nus.se.gomoku.user.security.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for {@link RsaSecurityServiceImpl}
 *
 * @author Claude
 * @version 1.0
 */
class RsaSecurityServiceImplTest {

    @InjectMocks
    private RsaSecurityServiceImpl rsaSecurityService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void init_shouldInitializeWithoutException() {
        // When & Then
        assertDoesNotThrow(() -> rsaSecurityService.init());
    }

    @Test
    void publicKey_shouldReturnNonNullKey() throws Exception {
        // When
        String publicKey = rsaSecurityService.publicKey();

        // Then
        assertNotNull(publicKey);
        assertFalse(publicKey.isEmpty());
    }

    @Test
    void publicKey_shouldReturnValidPemFormat() throws Exception {
        // When
        String publicKey = rsaSecurityService.publicKey();

        // Then
        assertTrue(publicKey.startsWith("-----BEGIN PUBLIC KEY-----") ||
                   publicKey.contains("BEGIN PUBLIC KEY"));
    }

    @Test
    void publicKey_shouldReturnConsistentKey() throws Exception {
        // When
        String key1 = rsaSecurityService.publicKey();
        String key2 = rsaSecurityService.publicKey();

        // Then
        assertEquals(key1, key2, "Public key should be consistent across calls");
    }

    @Test
    void publicKey_shouldReturnBase64EncodedContent() throws Exception {
        // When
        String publicKey = rsaSecurityService.publicKey();

        // Then
        // Remove PEM headers/footers and check if content is base64
        String content = publicKey
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replace("\n", "")
                .replace("\r", "")
                .trim();

        assertFalse(content.isEmpty());
        // Base64 characters: A-Z, a-z, 0-9, +, /, =
        assertTrue(content.matches("[A-Za-z0-9+/=]+"),
                   "Public key content should be base64 encoded");
    }
}
