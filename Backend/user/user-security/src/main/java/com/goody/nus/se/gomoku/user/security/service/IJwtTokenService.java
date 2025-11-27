package com.goody.nus.se.gomoku.user.security.service;

/**
 * JWT Token Service Interface
 *
 * @author Goody
 * @version 1.0, 2025/10/8
 * @since 1.0.0
 */
public interface IJwtTokenService {

    /**
     * Generate JWT token for user
     * Uses refresh token as signing key
     *
     * @param userId       user ID
     * @param email        user email
     * @param nickname     user nickname
     * @param refreshToken refresh token from user_token table (used as signing key)
     * @return JWT token
     */
    String generateToken(Long userId, String email, String nickname, String refreshToken);

    /**
     * Verify JWT token signature and extract user ID
     *
     * @param token        JWT token
     * @param refreshToken refresh token from database (used to verify signature)
     * @return user ID if valid, null if invalid
     */
    Long verifyToken(String token, String refreshToken);

    /**
     * Extract user ID from JWT without verifying signature
     * Used to get userId before querying database for refresh token
     *
     * @param token JWT token
     * @return user ID if parseable, null if invalid
     */
    Long extractUserId(String token);
}
