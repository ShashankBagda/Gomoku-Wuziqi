package com.goody.nus.se.gomoku.user.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * User registration response
 *
 * @author HaoTian
 * @version 1.0, 2025/10/4
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserRegisterResponse {

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
     * Country
     */
    private String country;

    /**
     * Gender (0=unknown, 1=male, 2=female)
     */
    private Byte gender;

    /**
     * Account status (0=inactive, 1=active, 2=disabled, 3=deleted)
     */
    private Byte status;

    /**
     * Creation time
     */
    private LocalDateTime createdAt;

    /**
     * JWT authentication token (for auto-login after registration)
     */
    private String token;
}
