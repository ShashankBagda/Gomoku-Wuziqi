package com.goody.nus.se.gomoku.user.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * User verify response
 *
 * @author Goody
 * @version 1.0, 2025/10/8
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserVerifyResponse {

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
     * Token is valid
     */
    private Boolean valid;
}
