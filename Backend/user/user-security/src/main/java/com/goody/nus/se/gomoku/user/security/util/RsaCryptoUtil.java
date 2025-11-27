package com.goody.nus.se.gomoku.user.security.util;

import lombok.NoArgsConstructor;

import javax.crypto.Cipher;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * Utility class for RSA encryption and decryption.
 *
 * @author Haotian
 * @version 1.0, 2025/10/3
 * @since 1.0
 */
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class RsaCryptoUtil {

    private static final String PUBLIC_KEY_RESOURCE = "/user-service-public.pem";
    private static final String PRIVATE_KEY_RESOURCE = "/user-service-private.pem";

    // Lazy-loaded key cache
    private static volatile PublicKey cachedPublicKey = null;
    private static volatile PrivateKey cachedPrivateKey = null;
    private static volatile String cachedPublicKeyPem = null;

    /**
     * Encrypts the given plaintext using the RSA public key from resources.
     *
     * @param plainText Text to encrypt.
     * @return Base64-encoded ciphertext.
     * @throws Exception if encryption fails.
     */
    public static String encrypt(String plainText) throws Exception {
        PublicKey publicKey = getPublicKey();
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes("UTF-8"));
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    /**
     * Decrypts the given Base64-encoded ciphertext using the RSA private key from resources.
     *
     * @param encryptedBase64 Base64-encoded ciphertext.
     * @return Decrypted plaintext.
     * @throws Exception if decryption fails.
     */
    public static String decryptWith(String encryptedBase64) throws Exception {
        PrivateKey privateKey = getPrivateKey();
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedBase64));
        return new String(decryptedBytes, "UTF-8");
    }

    /**
     * Returns the PEM-formatted public key string from resources.
     *
     * @return Public key PEM string.
     * @throws Exception if reading fails.
     */
    public static String getPublicKeyPem() throws Exception {
        if (cachedPublicKeyPem == null) {
            synchronized (RsaCryptoUtil.class) {
                if (cachedPublicKeyPem == null) {
                    cachedPublicKeyPem = readPemFromResource(PUBLIC_KEY_RESOURCE);
                }
            }
        }
        return cachedPublicKeyPem;
    }

    /**
     * Lazily loads and returns the RSA public key from resources.
     *
     * @return PublicKey instance.
     * @throws Exception if loading fails.
     */
    private static PublicKey getPublicKey() throws Exception {
        if (cachedPublicKey == null) {
            synchronized (RsaCryptoUtil.class) {
                if (cachedPublicKey == null) {
                    cachedPublicKey = readPublicKeyFromPem();
                }
            }
        }
        return cachedPublicKey;
    }

    /**
     * Lazily loads and returns the RSA private key from resources.
     *
     * @return PrivateKey instance.
     * @throws Exception if loading fails.
     */
    private static PrivateKey getPrivateKey() throws Exception {
        if (cachedPrivateKey == null) {
            synchronized (RsaCryptoUtil.class) {
                if (cachedPrivateKey == null) {
                    cachedPrivateKey = readPrivateKeyFromPem();
                }
            }
        }
        return cachedPrivateKey;
    }

    /**
     * Loads and parses the RSA public key from PEM in resources.
     *
     * @return PublicKey instance.
     * @throws Exception if parsing fails.
     */
    private static PublicKey readPublicKeyFromPem() throws Exception {
        String pem = readPemFromResource(PUBLIC_KEY_RESOURCE);
        String base64 = pem.replaceAll("-----BEGIN PUBLIC KEY-----", "")
                .replaceAll("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        byte[] keyBytes = Base64.getDecoder().decode(base64);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }

    /**
     * Loads and parses the RSA private key from PEM in resources.
     *
     * @return PrivateKey instance.
     * @throws Exception if parsing fails.
     */
    private static PrivateKey readPrivateKeyFromPem() throws Exception {
        String pem = readPemFromResource(PRIVATE_KEY_RESOURCE);
        String base64 = pem.replaceAll("-----BEGIN PRIVATE KEY-----", "")
                .replaceAll("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        byte[] keyBytes = Base64.getDecoder().decode(base64);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }

    /**
     * Reads PEM file content as string from resources.
     *
     * @param resourcePath Resource path (e.g. "/user-service-public.pem").
     * @return PEM file content as string.
     * @throws Exception if reading fails.
     */
    private static String readPemFromResource(String resourcePath) throws Exception {
        try (java.io.InputStream is = RsaCryptoUtil.class.getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new IllegalArgumentException("Resource not found: " + resourcePath);
            }
            return new String(is.readAllBytes(), "UTF-8");
        }
    }
}
