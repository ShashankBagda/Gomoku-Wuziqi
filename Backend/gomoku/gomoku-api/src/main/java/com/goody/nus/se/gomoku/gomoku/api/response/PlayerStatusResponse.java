package com.goody.nus.se.gomoku.gomoku.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response for player status query
 * Used to check if player is currently in a queue or room
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerStatusResponse {
    private MatchingStatus matchingStatus;
    private RoomStatus roomStatus;

    /**
     * Player's matching queue status
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MatchingStatus {
        /**
         * Whether player is currently in a matching queue
         */
        private Boolean inQueue;

        /**
         * Match mode if in queue: "casual" or "ranked"
         */
        private String mode;
    }

    /**
     * Player's room status
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RoomStatus {
        /**
         * Whether player is currently in a room
         */
        private Boolean inRoom;

        /**
         * Room code if in room
         */
        private String roomCode;

        /**
         * Room ID if room is matched
         */
        private Long roomId;

        /**
         * Room status: "waiting" or "matched"
         */
        private String status;
    }
}
