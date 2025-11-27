package com.goody.nus.se.gomoku.gomoku.api.response;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class MatchResponse {
    private String status;
    private String message;
    private String roomCode;
    private List<String> players;
    private Long roomId;
}
