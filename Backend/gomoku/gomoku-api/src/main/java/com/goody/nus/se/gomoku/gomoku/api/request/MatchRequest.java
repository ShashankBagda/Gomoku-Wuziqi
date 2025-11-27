package com.goody.nus.se.gomoku.gomoku.api.request;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MatchRequest {
    private String mode;    // causal or ranked
}
