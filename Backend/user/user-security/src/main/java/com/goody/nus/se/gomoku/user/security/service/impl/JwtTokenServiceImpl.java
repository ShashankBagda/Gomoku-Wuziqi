package com.goody.nus.se.gomoku.user.security.service.impl;

import com.goody.nus.se.gomoku.user.security.service.IJwtTokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT Token Service Implementation
 * Uses refresh token as signing key instead of fixed secret
 *
 * @author Goody
 * @version 1.0, 2025/10/8
 * @since 1.0.0
 */
@Service
@Slf4j
public class JwtTokenServiceImpl implements IJwtTokenService {

    @Value("${jwt.expiration:86400000}") // Default: 24 hours in milliseconds
    private Long expiration;

    @Override
    public String generateToken(Long userId, String email, String nickname, String refreshToken) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("email", email);
        claims.put("nickname", nickname);

        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + expiration);

        // Use refresh token as signing key
        SecretKey signingKey = createSigningKey(refreshToken);

        String token = Jwts.builder()
                .claims(claims)
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiration(expirationDate)
                .signWith(signingKey)
                .compact();

        log.debug("Generated JWT token for userId={}, expiresAt={}", userId, expirationDate);
        return token;
    }

    @Override
    public Long verifyToken(String token, String refreshToken) {
        try {
            // Use refresh token as signing key to verify
            SecretKey signingKey = createSigningKey(refreshToken);

            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            // Check expiration
            if (claims.getExpiration().before(new Date())) {
                log.warn("Token expired");
                return null;
            }

            Long userId = claims.get("userId", Long.class);
            log.debug("Token verified successfully for userId={}", userId);
            return userId;
        } catch (io.jsonwebtoken.security.SignatureException e) {
            // Signature verification failed - token was signed with different key
            log.warn("Token signature verification failed: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            // Handle other JWT parsing errors (malformed, expired, etc.)
            log.warn("Token verification failed: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public Long extractUserId(String token) {
        try {
            // Parse without verification to extract userId
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                log.warn("Invalid JWT format");
                return null;
            }

            // Decode payload (base64)
            String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);

            // Parse JSON to extract userId
            // Using simple string parsing to avoid Jackson dependency
            int userIdIndex = payload.indexOf("\"userId\":");
            if (userIdIndex == -1) {
                log.warn("userId not found in JWT payload");
                return null;
            }

            int startIndex = userIdIndex + "\"userId\":".length();
            int endIndex = payload.indexOf(",", startIndex);
            if (endIndex == -1) {
                endIndex = payload.indexOf("}", startIndex);
            }

            String userIdStr = payload.substring(startIndex, endIndex).trim();
            Long userId = Long.parseLong(userIdStr);

            log.debug("Extracted userId={} from JWT without verification", userId);
            return userId;
        } catch (IllegalArgumentException e) {
            // Handle Base64 decoding errors or NumberFormatException
            log.warn("Failed to extract userId from token: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            // Handle any other unexpected errors
            log.warn("Unexpected error extracting userId from token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Create signing key from refresh token
     * Ensures the key is at least 256 bits for HMAC-SHA256
     */
    private SecretKey createSigningKey(String refreshToken) {
        // UUID is 36 characters, we need at least 32 bytes (256 bits)
        // Pad the refresh token to ensure sufficient length
        String keyMaterial = refreshToken;
        while (keyMaterial.getBytes(StandardCharsets.UTF_8).length < 32) {
            keyMaterial = keyMaterial + refreshToken;
        }

        byte[] keyBytes = keyMaterial.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
