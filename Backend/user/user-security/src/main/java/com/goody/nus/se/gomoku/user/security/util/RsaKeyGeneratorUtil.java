package com.goody.nus.se.gomoku.user.security.util;

import lombok.NoArgsConstructor;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

/**
 * RSA Key Pair Generator
 * This utility generates an RSA public/private key pair,
 * encodes them in Base64 and PEM formats, and saves them to local files.
 * <p>
 * Usage:
 * 1. Run the main method to generate keys.
 * 2. The keys will be printed to the console and saved as .pem files.
 * <p>
 * Note: Ensure you have appropriate permissions to write files in the execution directory.
 *
 * @author Haotian
 * @version 1.0, 2025/10/3
 * @since 1.0
 */
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class RsaKeyGeneratorUtil {

    public static void main(String[] args) throws Exception {
        // 1. Generate RSA Key Pair (2048 bits - secure by default)
        KeyPair keyPair = generateRsaKeyPair(2048);

        // 2. Extract Public and Private Keys
        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();

        // 3. Convert Keys to Base64 Strings (for easy printing/storage)
        String publicKeyBase64 = encodeKeyToBase64(publicKey);
        String privateKeyBase64 = encodeKeyToBase64(privateKey);

        // 4. Convert Keys to Standard PEM Format (compatible with OpenSSL/Java)
        String publicKeyPem = toPemFormat(publicKey, "PUBLIC KEY");
        String privateKeyPem = toPemFormat(privateKey, "PRIVATE KEY");

        // Print Results
        System.out.println("=== RSA Public Key (Base64) ===");
        System.out.println(publicKeyBase64);
        System.out.println("=== RSA Private Key (Base64) ===");
        System.out.println(privateKeyBase64);
        System.out.println("=== RSA Public Key (PEM Format) ===");
        System.out.println(publicKeyPem);
        System.out.println("=== RSA Private Key (PEM Format) ===");
        System.out.println(privateKeyPem);

        // Optional: Save Keys to Local Files (.pem format)
        saveToFile("user-service-public.pem", publicKeyPem);
        saveToFile("user-service-private.pem", privateKeyPem);
        System.out.println("Keys saved to current directory as .pem files.");
    }

    /**
     * Generate an RSA Key Pair
     *
     * @param keySize Key length (2048 or 4096 recommended for security)
     * @return KeyPair containing public and private keys
     */
    private static KeyPair generateRsaKeyPair(int keySize) throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        // Initialize with desired key length
        keyPairGenerator.initialize(keySize);
        return keyPairGenerator.generateKeyPair();
    }

    /**
     * Encode a Key to Base64 String
     *
     * @param key Public or Private Key
     * @return Base64-encoded string of the key
     */
    private static String encodeKeyToBase64(Key key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    /**
     * Convert a Key to Standard PEM Format
     *
     * @param key     Public or Private Key
     * @param keyType Type of key ("PUBLIC KEY" or "PRIVATE KEY")
     * @return PEM-formatted string (with BEGIN/END headers)
     */
    private static String toPemFormat(Key key, String keyType) {
        String base64 = encodeKeyToBase64(key);
        // Format to 64 characters per line (PEM standard)
        String formattedBase64 = base64.replaceAll("(.{64})", "$1").trim();
        return "-----BEGIN " + keyType + "-----" + formattedBase64 + "-----END " + keyType + "-----";
    }

    /**
     * Save Content to a Local File
     *
     * @param filePath Path of the file (e.g., "user-service-public.pem")
     * @param content  Content to write to the file
     */
    private static void saveToFile(String filePath, String content) throws Exception {
        Files.write(Paths.get(filePath), content.getBytes("UTF-8"));
    }
}
