package com.goody.nus.se.gomoku.gomoku.matching.service.impl;

import com.goody.nus.se.gomoku.gomoku.api.request.MatchRequest;
import com.goody.nus.se.gomoku.gomoku.api.response.CancelMatchResponse;
import com.goody.nus.se.gomoku.gomoku.api.response.MatchResponse;
import com.goody.nus.se.gomoku.gomoku.enums.RoomTypeEnum;
import com.goody.nus.se.gomoku.gomoku.matching.service.IMatchBizService;
import com.goody.nus.se.gomoku.gomoku.matching.service.IMatchService;
import com.goody.nus.se.gomoku.gomoku.room.Service.RoomCodeService;
import com.goody.nus.se.gomoku.gomoku.service.interfaces.IGameRoomService;
import com.goody.nus.se.gomoku.gomoku.service.interfaces.IRoomStateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Business layer implementation for matching operations
 *
 * <p>Follows high cohesion and low coupling principles:
 * <ul>
 *   <li>High cohesion: All methods focus on match-related business logic</li>
 *   <li>Low coupling: Delegates core logic to specialized services (IMatchService, RoomCodeService, etc.)</li>
 * </ul>
 *
 * @author Haotian
 * @version 1.0, 2025/10/21
 */
@Service
@Slf4j
public class MatchBizServiceImpl implements IMatchBizService {

    @Autowired
    private IMatchService matchService;

    @Autowired
    private RoomCodeService roomCodeService;

    @Autowired
    private IRoomStateService roomStateService;

    @Autowired
    private IGameRoomService gameRoomService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MatchResponse matchAndSave(MatchRequest request, String playerId) {
        // Call matching service to perform matching logic
        MatchResponse response = matchService.match(request, playerId);

        // If matched successfully, save room record to database
        if (!"matched".equals(response.getStatus()) || response.getRoomCode() == null) {
            return response;
        }

        // if players list is invalid, skip saving
        List<String> players = response.getPlayers();
        if (players == null || players.size() != 2) {
            return response;
        }

        String mode = request.getMode();
        RoomTypeEnum roomType = "casual".equalsIgnoreCase(mode) ? RoomTypeEnum.CASUAL : RoomTypeEnum.RANKED;

        // Map mode to modeType for ranking system (RANKED, CASUAL, PRIVATE)
        String modeType = "casual".equalsIgnoreCase(mode) ? "CASUAL" : "RANKED";

        Long player1Id = Long.parseLong(players.get(0));
        Long player2Id = Long.parseLong(players.get(1));

        // Step 1: Check if room record already exists (prevent duplicate creation in concurrent scenario)
        Long existingRoomId = gameRoomService.findRoomIdByRoomCode(response.getRoomCode());
        Long roomId;

        if (existingRoomId != null) {
            // Room already created by the other player, use existing roomId
            roomId = existingRoomId;
            log.info("[MatchBiz] Room already exists: roomId={}, roomCode={} (concurrent match detected)",
                    roomId, response.getRoomCode());
        } else {
            // Room not exists, create new record in MySQL (game_room table)
            roomId = roomCodeService.saveRoomRecord(
                    response.getRoomCode(),
                    player1Id,
                    player2Id,
                    roomType.getValue()
            );
            log.info("[MatchBiz] Created new room record: roomId={}, roomCode={}, player1={}, player2={}, type={}",
                    roomId, response.getRoomCode(), player1Id, player2Id, roomType.name());
        }

        response.setRoomId(roomId);

        // Step 2: Initialize game state in MongoDB (games collection)
        // This ensures that when clients query game state, the document already exists
        // Note: RoomStateService.initializeGameState() is idempotent, safe to call multiple times
        roomStateService.initializeGameState(roomId, player1Id, player2Id, modeType);
        log.info("[MatchBiz] Initialized game state in MongoDB: roomId={}, modeType={}", roomId, modeType);

        return response;
    }

    /**
     * {@inheritDoc}
     *
     * <p>This is a simple delegation to the match service layer.
     * No business orchestration needed for cancellation.
     */
    @Override
    public CancelMatchResponse cancelMatch(String playerId) {
        log.info("[MatchBiz] Canceling match for player: {}", playerId);
        CancelMatchResponse response = matchService.cancelMatch(playerId);
        log.info("[MatchBiz] Cancel match completed for player {} with status: {}", playerId, response.getStatus());
        return response;
    }
}
