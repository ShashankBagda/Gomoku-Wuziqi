package com.goody.nus.se.gomoku.user.controller;

import com.goody.nus.se.gomoku.user.api.request.UserLoginRequest;
import com.goody.nus.se.gomoku.user.api.request.UserRegisterRequest;
import com.goody.nus.se.gomoku.user.api.request.UserResetPasswordRequest;
import com.goody.nus.se.gomoku.user.api.response.UserLoginResponse;
import com.goody.nus.se.gomoku.user.api.response.UserRegisterResponse;
import com.goody.nus.se.gomoku.user.api.response.UserVerifyResponse;
import com.goody.nus.se.gomoku.user.biz.service.IUserLoginBizService;
import com.goody.nus.se.gomoku.web.base.response.ApiResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;

/**
 * user login controller
 *
 * @author Goody
 * @version 1.0, 2023/5/12
 * @since 1.0.0
 */
@RestController
@RequestMapping
@RequiredArgsConstructor
public class UserLoginController {

    private final IUserLoginBizService userLoginBizService;

    @Autowired
    @Lazy
    private Executor bizThreadPool;

    /**
     * Get RSA public key for client to encrypt password
     *
     * @return public key in PEM format
     */
    @GetMapping("/public-key")
    public CompletionStage<ApiResult<String>> publicKey() {
        return CompletableFuture.supplyAsync(() -> {
            final String publicKey = userLoginBizService.publicKey();
            return ApiResult.success(publicKey);
        }, bizThreadPool);
    }

    /**
     * User registration
     *
     * @param request user registration request
     * @return user registration response
     */
    @PostMapping("/register")
    public CompletionStage<ApiResult<UserRegisterResponse>> register(@Valid @RequestBody UserRegisterRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            final UserRegisterResponse response = userLoginBizService.register(request);
            return ApiResult.success(response);
        }, bizThreadPool);
    }

    /**
     * User login
     *
     * @param request user login request
     * @return user login response with token
     */
    @PostMapping("/login")
    public CompletionStage<ApiResult<UserLoginResponse>> login(@Valid @RequestBody UserLoginRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            final UserLoginResponse response = userLoginBizService.login(request);
            return ApiResult.success(response);
        }, bizThreadPool);
    }

    /**
     * Verify JWT token
     *
     * @param authorization Authorization header with Bearer token
     * @return user verify response
     */
    @GetMapping("/verify")
    public CompletionStage<ApiResult<UserVerifyResponse>> verify(@RequestHeader("Authorization") String authorization) {
        return CompletableFuture.supplyAsync(() -> {
            final UserVerifyResponse response = userLoginBizService.verify(authorization);
            return ApiResult.success(response);
        }, bizThreadPool);
    }

    /**
     * Reset user password (PUBLIC API - no authentication required)
     * Supports two scenarios:
     * 1. Forgot password: user provides email + verification code + new password
     * 2. Change password: same flow but email can be auto-filled from frontend session
     *
     * @param request user reset password request (contains email, verification code, new password)
     * @return success response
     */
    @PostMapping("/reset-password")
    public CompletionStage<ApiResult<Void>> resetPassword(@Valid @RequestBody UserResetPasswordRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            userLoginBizService.resetPassword(request);
            return ApiResult.success();
        }, bizThreadPool);
    }

    /**
     * User logout
     * Invalidate user token to force re-login
     *
     * @param userId user ID from X-USER-ID header
     * @return success response
     */
    @PostMapping("/logout")
    public CompletionStage<ApiResult<Void>> logout(@RequestHeader("X-USER-ID") Long userId) {
        return CompletableFuture.supplyAsync(() -> {
            userLoginBizService.logout(userId);
            return ApiResult.success();
        }, bizThreadPool);
    }
}
