package com.goody.nus.se.gomoku.gomoku.biz.service.impl;

import com.goody.nus.se.gomoku.gomoku.api.response.PlayerStatusResponse;
import com.goody.nus.se.gomoku.gomoku.biz.service.IPlayerStatusService;
import com.goody.nus.se.gomoku.gomoku.matching.service.IMatchService;
import com.goody.nus.se.gomoku.gomoku.room.RoomCodeDao;
import com.goody.nus.se.gomoku.gomoku.service.interfaces.IGameRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Player status business service implementation
 * Aggregates player status from matching queue and room modules
 *
 * @author HaoTian
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlayerStatusServiceImpl implements IPlayerStatusService {

    private final IMatchService matchService;
    private final RoomCodeDao roomCodeDao;
    private final IGameRoomService gameRoomService;

    @Override
    public PlayerStatusResponse getPlayerStatus(String playerId) {
        log.debug("Getting player status for playerId={}", playerId);

        // Check matching status
        PlayerStatusResponse.MatchingStatus matchingStatus = checkMatchingStatus(playerId);

        // Check room status
        PlayerStatusResponse.RoomStatus roomStatus = checkRoomStatus(playerId);

        return PlayerStatusResponse.builder()
                .matchingStatus(matchingStatus)
                .roomStatus(roomStatus)
                .build();
    }

    /**
     * Check if player is in a matching queue
     */
    private PlayerStatusResponse.MatchingStatus checkMatchingStatus(String playerId) {
        String mode = matchService.findPlayerQueue(playerId);

        return PlayerStatusResponse.MatchingStatus.builder()
                .inQueue(mode != null)
                .mode(mode)
                .build();
    }

    /**
     * Check if player is in a room
     */
    private PlayerStatusResponse.RoomStatus checkRoomStatus(String playerId) {
        String roomCode = roomCodeDao.findRoomCodeByPlayerId(playerId);

        if (roomCode == null) {
            return PlayerStatusResponse.RoomStatus.builder()
                    .inRoom(false)
                    .roomCode(null)
                    .roomId(null)
                    .status(null)
                    .build();
        }

        // Player is in a room, check the room status
        List<String> players = roomCodeDao.getPlayersByRoom(roomCode);
        String status = (players != null && players.size() == 2) ? "matched" : "waiting";

        // If matched, get roomId from database
        Long roomId = null;
        if ("matched".equals(status)) {
            roomId = gameRoomService.findRoomIdByRoomCode(roomCode);
        }

        return PlayerStatusResponse.RoomStatus.builder()
                .inRoom(true)
                .roomCode(roomCode)
                .roomId(roomId)
                .status(status)
                .build();
    }
}
