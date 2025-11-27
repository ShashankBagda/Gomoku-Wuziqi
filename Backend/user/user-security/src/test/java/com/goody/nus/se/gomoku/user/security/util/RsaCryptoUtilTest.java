package com.goody.nus.se.gomoku.user.security.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link RsaCryptoUtil} test
 *
 * @author Haotian
 * @version 1.0, 2025/10/3
 */
class RsaCryptoUtilTest {

    @Test
    public void test_GetPublicKeyPem() throws Exception {
        String publicKeyPem = RsaCryptoUtil.getPublicKeyPem();
        assertNotNull(publicKeyPem);
        assertTrue(publicKeyPem.startsWith("-----BEGIN PUBLIC KEY-----"));
        assertTrue(publicKeyPem.endsWith("-----END PUBLIC KEY-----"));
    }

    @Test
    public void test_GetPrivateKeyPem() throws Exception {
        final String testStr = "test";

        String encryptStr = RsaCryptoUtil.encrypt(testStr);
        String decrypt = RsaCryptoUtil.decryptWith(encryptStr);

        assertEquals(testStr, decrypt);
    }
}
