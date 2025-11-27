package com.goody.nus.se.gomoku.user.biz.service;

import com.goody.nus.se.gomoku.user.api.request.UserLoginRequest;
import com.goody.nus.se.gomoku.user.api.request.UserRegisterRequest;
import com.goody.nus.se.gomoku.user.api.request.UserResetPasswordRequest;
import com.goody.nus.se.gomoku.user.api.response.UserLoginResponse;
import com.goody.nus.se.gomoku.user.api.response.UserRegisterResponse;
import com.goody.nus.se.gomoku.user.api.response.UserResetPasswordResponse;
import com.goody.nus.se.gomoku.user.api.response.UserVerifyResponse;

/**
 * user login biz service
 *
 * @author Haotian
 * @version 1.0, 2025/10/3
 */
public interface IUserLoginBizService {

    /**
     * get user public key for client to encrypt password
     *
     * @return public key
     */
    String publicKey();

    /**
     * Register a new user
     *
     * @param request user registration request
     * @return user registration response
     */
    UserRegisterResponse register(UserRegisterRequest request);

    /**
     * User login
     *
     * @param request user login request
     * @return user login response
     */
    UserLoginResponse login(UserLoginRequest request);

    /**
     * Verify JWT token
     *
     * @param token JWT token from Authorization header
     * @return user verify response with user info
     */
    UserVerifyResponse verify(String token);

    /**
     * Reset user password (PUBLIC API - no authentication required)
     * Supports two scenarios:
     * 1. Forgot password: user provides email + verification code + new password
     * 2. Change password: same flow but email can be auto-filled from session
     *
     * @param request user reset password request (contains email, verification code, new password)
     * @return user reset password response
     */
    UserResetPasswordResponse resetPassword(UserResetPasswordRequest request);

    /**
     * User logout
     * Invalidate user token to force re-login
     *
     * @param userId user ID
     */
    void logout(Long userId);
}
