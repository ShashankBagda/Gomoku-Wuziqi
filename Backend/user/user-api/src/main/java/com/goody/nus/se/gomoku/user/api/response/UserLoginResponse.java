package com.goody.nus.se.gomoku.user.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * User login response
 *
 * @author HaoTian
 * @version 1.0, 2025/10/4
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserLoginResponse {

    /**
     * User ID
     */
    private Long userId;

    /**
     * Email address
     */
    private String email;

    /**
     * Nickname
     */
    private String nickname;

    /**
     * Avatar URL
     */
    private String avatarUrl;

    /**
     * Authentication token (JWT or session token)
     */
    private String token;
}
