package com.goody.nus.se.gomoku.user.api.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * User login request
 *
 * @author HaoTian
 * @version 1.0, 2025/10/4
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserLoginRequest {

    /**
     * Email or nickname
     */
    @NotBlank(message = "Username cannot be blank")
    private String username;

    /**
     * Password (RSA encrypted from client)
     */
    @NotBlank(message = "Password cannot be blank")
    private String encryptedPassword;
}
