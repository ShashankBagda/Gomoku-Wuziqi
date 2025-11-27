package com.goody.nus.se.gomoku.user.api.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserEmailVerifyRequest {
    private String email;
}
