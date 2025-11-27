package com.goody.nus.se.gomoku.user.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * User reset password response
 *
 * @author HaoTian
 * @version 1.0, 2025/10/15
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResetPasswordResponse {

    /**
     * Email address
     */
    private String email;

    /**
     * Success message
     */
    private String message;
}
