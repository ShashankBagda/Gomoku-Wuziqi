package com.goody.nus.se.gomoku.user.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInfoResponse {
    private Long id;
    private String email;
    private String nickname;
    private String avatarUrl;
    private String country;
    private Byte gender;
    private String avatarBase64;
}
