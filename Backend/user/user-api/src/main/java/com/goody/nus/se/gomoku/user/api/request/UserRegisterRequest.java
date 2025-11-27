package com.goody.nus.se.gomoku.user.api.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * User registration request
 *
 * @author HaoTian
 * @version 1.0, 2025/10/4
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserRegisterRequest {

    /**
     * Email address (must be unique)
     */
    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Invalid email format")
    @Size(max = 128, message = "Email length cannot exceed 128 characters")
    private String email;

    /**
     * Nickname (must be unique)
     */
    @NotBlank(message = "Nickname cannot be blank")
    @Size(min = 2, max = 64, message = "Nickname length must be between 2 and 64 characters")
    private String nickname;

    /**
     * Password (RSA encrypted from client)
     */
    @NotBlank(message = "Password cannot be blank")
    private String encryptedPassword;

    /**
     * Avatar URL (optional)
     */
    @Size(max = 255, message = "Avatar URL length cannot exceed 255 characters")
    private String avatarUrl;

    /**
     * Avatar Base64 (optional)
     */
    private String avatarBase64;

    /**
     * Country (optional)
     */
    @Size(max = 64, message = "Country length cannot exceed 64 characters")
    private String country;

    /**
     * Gender (0=unknown, 1=male, 2=female)
     */
    private Byte gender;

    /**
     * Verification code (6-digit code sent to email)
     */
    @Size(min = 6, max = 6, message = "Verification code length cannot exceed 16 characters")
    private String verificationCode;
}
