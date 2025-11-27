package com.goody.nus.se.gomoku.gomoku.api.response;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LeaveRoomResponse {
    private String status; // "success", "notFound", "empty"
    private String message;
}
