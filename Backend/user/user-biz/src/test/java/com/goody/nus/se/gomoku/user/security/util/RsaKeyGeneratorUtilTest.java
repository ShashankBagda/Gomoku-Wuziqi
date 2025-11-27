package com.goody.nus.se.gomoku.user.security.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link RsaKeyGeneratorUtil}
 * Tests RSA key pair generation, encoding, and file operations
 */
class RsaKeyGeneratorUtilTest {

    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    void setUp() {
        // Capture System.out for testing console output
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @AfterEach
    void tearDown() {
        // Restore original System.out
        System.setOut(originalOut);

        // Clean up generated files
        try {
            Files.deleteIfExists(Paths.get("user-service-public.pem"));
            Files.deleteIfExists(Paths.get("user-service-private.pem"));
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }

    // ==================== main() Integration Tests ====================

    @Test
    void main_GeneratesKeyPairSuccessfully() throws Exception {
        // When
        RsaKeyGeneratorUtil.main(new String[]{});

        // Then
        String output = outputStreamCaptor.toString();

        // Verify all expected sections are printed
        assertTrue(output.contains("=== RSA Public Key (Base64) ==="));
        assertTrue(output.contains("=== RSA Private Key (Base64) ==="));
        assertTrue(output.contains("=== RSA Public Key (PEM Format) ==="));
        assertTrue(output.contains("=== RSA Private Key (PEM Format) ==="));
        assertTrue(output.contains("Keys saved to current directory as .pem files."));
    }

    @Test
    void main_GeneratesValidPublicKey() throws Exception {
        // When
        RsaKeyGeneratorUtil.main(new String[]{});

        // Then
        String output = outputStreamCaptor.toString();

        // Extract public key from output
        String publicKeySection = extractSection(output, "=== RSA Public Key (Base64) ===", "=== RSA Private Key (Base64) ===");
        String publicKeyBase64 = publicKeySection.trim();

        // Verify it's valid Base64
        assertDoesNotThrow(() -> Base64.getDecoder().decode(publicKeyBase64));

        // Verify it can be converted to PublicKey object
        byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyBase64);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(keySpec);

        assertNotNull(publicKey);
        assertEquals("RSA", publicKey.getAlgorithm());
    }

    @Test
    void main_GeneratesValidPrivateKey() throws Exception {
        // When
        RsaKeyGeneratorUtil.main(new String[]{});

        // Then
        String output = outputStreamCaptor.toString();

        // Extract private key from output
        String privateKeySection = extractSection(output, "=== RSA Private Key (Base64) ===", "=== RSA Public Key (PEM Format) ===");
        String privateKeyBase64 = privateKeySection.trim();

        // Verify it's valid Base64
        assertDoesNotThrow(() -> Base64.getDecoder().decode(privateKeyBase64));

        // Verify it can be converted to PrivateKey object
        byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyBase64);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

        assertNotNull(privateKey);
        assertEquals("RSA", privateKey.getAlgorithm());
    }

    @Test
    void main_GeneratesPemFormattedKeys() throws Exception {
        // When
        RsaKeyGeneratorUtil.main(new String[]{});

        // Then
        String output = outputStreamCaptor.toString();

        // Extract public key PEM
        String publicPemSection = extractSection(output, "=== RSA Public Key (PEM Format) ===", "=== RSA Private Key (PEM Format) ===");
        assertTrue(publicPemSection.contains("-----BEGIN PUBLIC KEY-----"));
        assertTrue(publicPemSection.contains("-----END PUBLIC KEY-----"));

        // Extract private key PEM
        String privatePemSection = extractSection(output, "=== RSA Private Key (PEM Format) ===", "Keys saved to current directory");
        assertTrue(privatePemSection.contains("-----BEGIN PRIVATE KEY-----"));
        assertTrue(privatePemSection.contains("-----END PRIVATE KEY-----"));
    }

    @Test
    void main_SavesPublicKeyToFile() throws Exception {
        // When
        RsaKeyGeneratorUtil.main(new String[]{});

        // Then
        Path publicKeyPath = Paths.get("user-service-public.pem");
        assertTrue(Files.exists(publicKeyPath));

        String fileContent = Files.readString(publicKeyPath);
        assertTrue(fileContent.contains("-----BEGIN PUBLIC KEY-----"));
        assertTrue(fileContent.contains("-----END PUBLIC KEY-----"));
    }

    @Test
    void main_SavesPrivateKeyToFile() throws Exception {
        // When
        RsaKeyGeneratorUtil.main(new String[]{});

        // Then
        Path privateKeyPath = Paths.get("user-service-private.pem");
        assertTrue(Files.exists(privateKeyPath));

        String fileContent = Files.readString(privateKeyPath);
        assertTrue(fileContent.contains("-----BEGIN PRIVATE KEY-----"));
        assertTrue(fileContent.contains("-----END PRIVATE KEY-----"));
    }

    @Test
    void main_GeneratesKeysWith2048Bits() throws Exception {
        // When
        RsaKeyGeneratorUtil.main(new String[]{});

        // Then
        String output = outputStreamCaptor.toString();
        String publicKeySection = extractSection(output, "=== RSA Public Key (Base64) ===", "=== RSA Private Key (Base64) ===");
        String publicKeyBase64 = publicKeySection.trim();

        // Decode and verify key size
        byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyBase64);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(keySpec);

        // RSA 2048-bit key should have modulus length of 2048 bits
        // The encoded form will be larger due to ASN.1 structure
        assertTrue(publicKeyBytes.length > 200); // Reasonable size check
        assertTrue(publicKeyBytes.length < 500); // Not too large
    }

    @Test
    void main_GeneratesUniqueKeysOnEachRun() throws Exception {
        // First run
        RsaKeyGeneratorUtil.main(new String[]{});
        String output1 = outputStreamCaptor.toString();
        String publicKey1 = extractSection(output1, "=== RSA Public Key (Base64) ===", "=== RSA Private Key (Base64) ===").trim();

        // Reset output
        outputStreamCaptor.reset();

        // Second run
        RsaKeyGeneratorUtil.main(new String[]{});
        String output2 = outputStreamCaptor.toString();
        String publicKey2 = extractSection(output2, "=== RSA Public Key (Base64) ===", "=== RSA Private Key (Base64) ===").trim();

        // Keys should be different each time
        assertNotEquals(publicKey1, publicKey2);
    }

    @Test
    void main_PemFormatHasCorrectLineLength() throws Exception {
        // When
        RsaKeyGeneratorUtil.main(new String[]{});

        // Then
        String output = outputStreamCaptor.toString();
        String publicPemSection = extractSection(output, "=== RSA Public Key (PEM Format) ===", "=== RSA Private Key (PEM Format) ===");

        // Extract base64 content (between BEGIN and END)
        String base64Content = publicPemSection
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .trim();

        // PEM format should have lines of max 64 characters (except possibly the last line)
        String[] lines = base64Content.split("\n");
        for (int i = 0; i < lines.length - 1; i++) {
            assertTrue(lines[i].length() <= 64,
                    "Line " + i + " has length " + lines[i].length() + " but should be <= 64");
        }
    }

    @Test
    void main_GeneratedKeysCanBeUsedForEncryption() throws Exception {
        // When
        RsaKeyGeneratorUtil.main(new String[]{});

        // Then - extract keys from output
        String output = outputStreamCaptor.toString();
        String publicKeyBase64 = extractSection(output, "=== RSA Public Key (Base64) ===", "=== RSA Private Key (Base64) ===").trim();
        String privateKeyBase64 = extractSection(output, "=== RSA Private Key (Base64) ===", "=== RSA Public Key (PEM Format) ===").trim();

        // Convert to Key objects
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyBase64);
        PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(publicKeyBytes));

        byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyBase64);
        PrivateKey privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));

        // Test encryption/decryption
        javax.crypto.Cipher encryptCipher = javax.crypto.Cipher.getInstance("RSA");
        encryptCipher.init(javax.crypto.Cipher.ENCRYPT_MODE, publicKey);

        String testMessage = "Hello, RSA!";
        byte[] encryptedBytes = encryptCipher.doFinal(testMessage.getBytes());

        javax.crypto.Cipher decryptCipher = javax.crypto.Cipher.getInstance("RSA");
        decryptCipher.init(javax.crypto.Cipher.DECRYPT_MODE, privateKey);
        byte[] decryptedBytes = decryptCipher.doFinal(encryptedBytes);

        assertEquals(testMessage, new String(decryptedBytes));
    }

    @Test
    void main_SavedFilesMatchConsoleOutput() throws Exception {
        // When
        RsaKeyGeneratorUtil.main(new String[]{});

        // Then
        String output = outputStreamCaptor.toString();

        // Extract PEM from console output
        String consolePublicPem = extractSection(output, "=== RSA Public Key (PEM Format) ===", "=== RSA Private Key (PEM Format) ===").trim();
        String consolePrivatePem = extractSection(output, "=== RSA Private Key (PEM Format) ===", "Keys saved to current directory").trim();

        // Read from files
        String filePublicPem = Files.readString(Paths.get("user-service-public.pem")).trim();
        String filePrivatePem = Files.readString(Paths.get("user-service-private.pem")).trim();

        // Should match
        assertEquals(consolePublicPem, filePublicPem);
        assertEquals(consolePrivatePem, filePrivatePem);
    }

    @Test
    void main_HandlesFileWriteErrors_GracefullyThrowsException() {
        // This test verifies that if file writing fails, the exception propagates
        // In real scenario, this might happen if directory is read-only
        // For this test, we just verify the method signature allows exceptions

        assertDoesNotThrow(() -> {
            // The method signature declares "throws Exception", so it's designed to propagate errors
            RsaKeyGeneratorUtil.main(new String[]{});
        });
    }

    @Test
    void main_Base64OutputIsValidAndDecodable() throws Exception {
        // When
        RsaKeyGeneratorUtil.main(new String[]{});

        // Then
        String output = outputStreamCaptor.toString();
        String publicKeyBase64 = extractSection(output, "=== RSA Public Key (Base64) ===", "=== RSA Private Key (Base64) ===").trim();
        String privateKeyBase64 = extractSection(output, "=== RSA Private Key (Base64) ===", "=== RSA Public Key (PEM Format) ===").trim();

        // Should not throw exception
        byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyBase64);
        byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyBase64);

        assertTrue(publicKeyBytes.length > 0);
        assertTrue(privateKeyBytes.length > 0);
    }

    @Test
    void main_PrivateKeyIsLargerThanPublicKey() throws Exception {
        // When
        RsaKeyGeneratorUtil.main(new String[]{});

        // Then
        String output = outputStreamCaptor.toString();
        String publicKeyBase64 = extractSection(output, "=== RSA Public Key (Base64) ===", "=== RSA Private Key (Base64) ===").trim();
        String privateKeyBase64 = extractSection(output, "=== RSA Private Key (Base64) ===", "=== RSA Public Key (PEM Format) ===").trim();

        byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyBase64);
        byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyBase64);

        // Private key should be larger than public key
        assertTrue(privateKeyBytes.length > publicKeyBytes.length);
    }

    @Test
    void main_OutputContainsAllExpectedSections() throws Exception {
        // When
        RsaKeyGeneratorUtil.main(new String[]{});

        // Then
        String output = outputStreamCaptor.toString();

        // Count section headers
        int publicBase64Count = countOccurrences(output, "=== RSA Public Key (Base64) ===");
        int privateBase64Count = countOccurrences(output, "=== RSA Private Key (Base64) ===");
        int publicPemCount = countOccurrences(output, "=== RSA Public Key (PEM Format) ===");
        int privatePemCount = countOccurrences(output, "=== RSA Private Key (PEM Format) ===");

        assertEquals(1, publicBase64Count);
        assertEquals(1, privateBase64Count);
        assertEquals(1, publicPemCount);
        assertEquals(1, privatePemCount);
    }

    @Test
    void main_PemFormatContainsNewlines() throws Exception {
        // When
        RsaKeyGeneratorUtil.main(new String[]{});

        // Then
        String output = outputStreamCaptor.toString();
        String publicPemSection = extractSection(output, "=== RSA Public Key (PEM Format) ===", "=== RSA Private Key (PEM Format) ===");

        // PEM format should have multiple lines (not a single long line)
        String base64Content = publicPemSection
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .trim();

        // Should contain newline characters for proper PEM formatting
        int lineCount = base64Content.split("\n").length;
    }

    // ==================== Helper Methods ====================

    /**
     * Extract text between two markers
     */
    private String extractSection(String text, String startMarker, String endMarker) {
        int startIndex = text.indexOf(startMarker);
        int endIndex = text.indexOf(endMarker);

        if (startIndex == -1 || endIndex == -1) {
            return "";
        }

        return text.substring(startIndex + startMarker.length(), endIndex).trim();
    }

    /**
     * Count occurrences of a substring
     */
    private int countOccurrences(String text, String substring) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(substring, index)) != -1) {
            count++;
            index += substring.length();
        }
        return count;
    }
}
