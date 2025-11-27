package com.goody.nus.se.gomoku.user.api.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * User reset password request
 * This is a PUBLIC API - no authentication required.
 * Supports two scenarios:
 * 1. Forgot password (user not logged in): manually input email
 * 2. Change password (user logged in): email auto-filled from current session
 *
 * @author HaoTian
 * @version 1.0, 2025/10/19
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResetPasswordRequest {

    /**
     * Email address (required for identifying the user)
     */
    @NotBlank(message = "Email cannot be blank")
    private String email;

    /**
     * New password (RSA encrypted from client)
     */
    @NotBlank(message = "New password cannot be blank")
    private String encryptedNewPassword;

    /**
     * Verification code sent to user's email (required for security)
     */
    @NotBlank(message = "Verification code cannot be blank")
    private String verificationCode;
}
