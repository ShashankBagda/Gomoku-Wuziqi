package com.goody.nus.se.gomoku.gomoku.matching.impl;

import com.goody.nus.se.gomoku.common.errorcode.ErrorCodeEnum;
import com.goody.nus.se.gomoku.common.exception.BizException;
import com.goody.nus.se.gomoku.gomoku.api.request.MatchRequest;
import com.goody.nus.se.gomoku.gomoku.api.response.CancelMatchResponse;
import com.goody.nus.se.gomoku.gomoku.api.response.MatchResponse;
import com.goody.nus.se.gomoku.gomoku.matching.service.IMatchService;
import com.goody.nus.se.gomoku.gomoku.room.RoomCodeDao;
import com.goody.nus.se.gomoku.gomoku.service.interfaces.IGameRoomService;
import com.goody.nus.se.gomoku.redis.service.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Match service implementation
 *
 * <p>Handles player matching for casual and ranked modes.
 * Supports idempotent match requests by checking player status before adding to queue.
 *
 * <p>Match flow:
 * <ol>
 *   <li>Check if player already in a room (return room status)</li>
 *   <li>Check if player already in queue (return waiting status)</li>
 *   <li>Add player to queue</li>
 *   <li>If queue has 2+ players, match them and create room</li>
 * </ol>
 *
 * @author HaoTian
 */
@Service
@Slf4j
public class MatchServiceImpl implements IMatchService {

    @Autowired
    private RedisService redisService;
    @Autowired
    private RoomCodeDao roomCodeDao;
    @Autowired
    private IGameRoomService gameRoomService;

    private static final String CASUAL_QUEUE = "match:casual";
    private static final String RANKED_QUEUE = "match:ranked";
    private static final int ROOM_TTL_MINUTES = 3;
    private static final int QUEUE_EXPIRE_MINUTES = 3;

    /**
     * Handle player match request
     *
     * <p>This method is idempotent - calling it multiple times with same playerId
     * will return consistent results without side effects.
     *
     * @param request  match request containing mode (casual/ranked)
     * @param playerId player ID
     * @return match response with status (waiting/matched), room code, and players
     */
    @Override
    public MatchResponse match(MatchRequest request, String playerId) {
        String mode = request.getMode();
        String queueKey = getQueueKey(mode);

        log.info("[Match] Processing match request for player {} in {} mode", playerId, mode);

        // Step 1: Check if player is already in a room (idempotency check)
        MatchResponse roomStatus = checkPlayerRoomStatus(playerId);
        if (roomStatus != null) {
            return roomStatus;
        }

        // Step 2: Check if player is in a different queue (mutual exclusion check)
        String existingQueue = findPlayerQueue(playerId);
        if (existingQueue != null && !existingQueue.equalsIgnoreCase(mode)) {
            log.warn("[Match] Player {} already in {} queue, cannot join {} queue",
                    playerId, existingQueue, mode);
            throw new BizException(ErrorCodeEnum.PLAYER_IN_MATCH_QUEUE, existingQueue);
        }

        // Step 3: Check if player is already in current queue (idempotency check)
        if (isPlayerInQueue(queueKey, playerId)) {
            log.info("[Match] Player {} already in {} queue, returning waiting status", playerId, mode);
            return createWaitingResponse(mode);
        }

        // Step 4: Add player to queue
        addPlayerToQueue(queueKey, playerId);

        // Step 5: Try to match players
        MatchResponse response = tryMatchPlayers(queueKey, mode);

        // Step 6: Update queue TTL to prevent stale data
        updateQueueExpiration(queueKey);

        log.info("[Match] Match request completed for player {} with status: {}", playerId, response.getStatus());
        return response;
    }

    /**
     * Get Redis queue key for given match mode
     *
     * @param mode match mode (casual/ranked)
     * @return Redis queue key
     */
    private String getQueueKey(String mode) {
        return "casual".equalsIgnoreCase(mode) ? CASUAL_QUEUE : RANKED_QUEUE;
    }

    /**
     * Check if player is already in a room
     *
     * <p>If player is in a room, returns appropriate response based on room status:
     * <ul>
     *   <li>2 players in room: return "matched" status with roomId</li>
     *   <li>1 player in room: return "waiting" status</li>
     * </ul>
     *
     * @param playerId player ID
     * @return MatchResponse if player in room, null otherwise
     */
    private MatchResponse checkPlayerRoomStatus(String playerId) {
        String roomCode = roomCodeDao.findRoomCodeByPlayerId(playerId);
        if (roomCode == null) {
            return null;
        }

        log.info("[Match] Player {} found in existing room {}", playerId, roomCode);
        List<String> players = roomCodeDao.getPlayersByRoom(roomCode);

        MatchResponse response = new MatchResponse();
        response.setRoomCode(roomCode);
        response.setPlayers(players);

        // Check if room is fully matched (2 players)
        if (players != null && players.size() == 2) {
            Long roomId = gameRoomService.findRoomIdByRoomCode(roomCode);
            response.setStatus("matched");
            response.setRoomId(roomId);
            response.setMessage("Already matched! Room: " + roomCode);
            log.info("[Match] Player {} in matched room {} with roomId {}", playerId, roomCode, roomId);
        } else {
            // Room exists but waiting for second player
            response.setStatus("waiting");
            response.setMessage("Waiting for another player in room: " + roomCode);
            log.info("[Match] Player {} in waiting room {} with {} player(s)",
                    playerId, roomCode, players != null ? players.size() : 0);
        }

        return response;
    }

    /**
     * Check if player is already in the queue
     *
     * @param queueKey Redis queue key
     * @param playerId player ID
     * @return true if player in queue, false otherwise
     */
    private boolean isPlayerInQueue(String queueKey, String playerId) {
        List<String> currentQueue = redisService.lRange(queueKey, 0, -1);
        return currentQueue != null && currentQueue.contains(playerId);
    }

    /**
     * Create waiting response
     *
     * @param mode match mode
     * @return MatchResponse with waiting status
     */
    private MatchResponse createWaitingResponse(String mode) {
        MatchResponse response = new MatchResponse();
        response.setStatus("waiting");
        response.setMessage("You are already in the " + mode + " queue, waiting for another player...");
        return response;
    }

    /**
     * Add player to matching queue
     *
     * @param queueKey Redis queue key
     * @param playerId player ID
     */
    private void addPlayerToQueue(String queueKey, String playerId) {
        redisService.lPush(queueKey, playerId);
        int queueSize = redisService.lSize(queueKey);
        log.info("[Match] Player {} joined queue {} (current size: {})", playerId, queueKey, queueSize);
    }

    /**
     * Try to match players from queue
     *
     * <p>If queue has at least 2 players, match the last two players and create a room.
     * Otherwise, return waiting response.
     *
     * @param queueKey Redis queue key
     * @param mode     match mode
     * @return MatchResponse with match result
     */
    private MatchResponse tryMatchPlayers(String queueKey, String mode) {
        int queueSize = redisService.lSize(queueKey);

        // Not enough players to match
        if (queueSize < 2) {
            log.debug("[Match] Queue {} has {} player(s), waiting for more", queueKey, queueSize);
            MatchResponse response = new MatchResponse();
            response.setStatus("waiting");
            response.setMessage("Waiting for another player to join " + mode + " queue...");
            return response;
        }

        // Get last two players from queue
        List<String> players = redisService.lRange(queueKey, queueSize - 2, queueSize - 1);
        String playerA = (String) players.get(0);
        String playerB = (String) players.get(1);

        log.info("[Match] Matching players {} and {} from queue {}", playerA, playerB, queueKey);

        // Create room for matched players
        String roomCode = generateRoomCode();
        createMatchedRoom(roomCode, playerA, playerB);

        // Remove matched players from queue
        removePlayersFromQueue(queueKey, playerA, playerB);

        // Build matched response
        MatchResponse response = new MatchResponse();
        response.setStatus("matched");
        response.setRoomCode(roomCode);
        response.setPlayers(Arrays.asList(playerA, playerB));
        response.setMessage("Matched with another player! Room created: " + roomCode);

        log.info("[Match] Successfully matched players {} & {} into room {}", playerA, playerB, roomCode);
        return response;
    }

    /**
     * Generate unique room code
     *
     * @return 6-character uppercase room code
     */
    private String generateRoomCode() {
        return UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    /**
     * Create room for matched players
     *
     * @param roomCode room code
     * @param playerA  first player ID
     * @param playerB  second player ID
     */
    private void createMatchedRoom(String roomCode, String playerA, String playerB) {
        roomCodeDao.createRoomCode(roomCode, ROOM_TTL_MINUTES);
        roomCodeDao.addPlayerToRoom(roomCode, playerA);
        roomCodeDao.addPlayerToRoom(roomCode, playerB);
        log.debug("[Match] Created room {} with players {} and {}", roomCode, playerA, playerB);
    }

    /**
     * Remove matched players from queue
     *
     * @param queueKey Redis queue key
     * @param playerA  first player ID
     * @param playerB  second player ID
     */
    private void removePlayersFromQueue(String queueKey, String playerA, String playerB) {
        redisService.getList(queueKey).remove(playerA);
        redisService.getList(queueKey).remove(playerB);
        log.debug("[Match] Removed players {} and {} from queue {}", playerA, playerB, queueKey);
    }

    /**
     * Update queue expiration time
     *
     * @param queueKey Redis queue key
     */
    private void updateQueueExpiration(String queueKey) {
        redisService.expire(queueKey, QUEUE_EXPIRE_MINUTES, TimeUnit.MINUTES);
    }

    /**
     * Find which queue the player is currently in
     *
     * <p>Used by player status service to check if player is waiting in a match queue.
     *
     * @param playerId player ID to search for
     * @return match mode ("casual" or "ranked") if player in queue, null if not in any queue
     */
    @Override
    public String findPlayerQueue(String playerId) {
        log.debug("[Match] Searching for player {} in match queues", playerId);

        // Check casual queue
        List<String> casualQueue = redisService.lRange(CASUAL_QUEUE, 0, -1);
        if (casualQueue != null && casualQueue.contains(playerId)) {
            log.debug("[Match] Player {} found in casual queue", playerId);
            return "casual";
        }

        // Check ranked queue
        List<String> rankedQueue = redisService.lRange(RANKED_QUEUE, 0, -1);
        if (rankedQueue != null && rankedQueue.contains(playerId)) {
            log.debug("[Match] Player {} found in ranked queue", playerId);
            return "ranked";
        }

        log.debug("[Match] Player {} not found in any match queue", playerId);
        return null;
    }

    /**
     * Cancel player's current match queue
     *
     * <p>Implementation follows high cohesion and low coupling principles:
     * <ul>
     *   <li>Reuses {@link #findPlayerQueue(String)} to locate player's queue (DRY principle)</li>
     *   <li>Encapsulates queue removal logic in private method for better modularity</li>
     *   <li>Idempotent operation - safe to call multiple times</li>
     * </ul>
     *
     * <p>Cancellation flow:
     * <ol>
     *   <li>Find which queue player is in (if any)</li>
     *   <li>Remove player from that queue</li>
     *   <li>Return appropriate response based on result</li>
     * </ol>
     *
     * @param playerId player ID
     * @return CancelMatchResponse with status ("success" or "not_in_queue") and queue mode
     */
    @Override
    public CancelMatchResponse cancelMatch(String playerId) {
        log.info("[Match] Processing cancel match request for player {}", playerId);

        // Step 1: Find which queue the player is in (reuse existing method)
        String queueMode = findPlayerQueue(playerId);

        // Step 2: If player not in any queue, return not_in_queue status
        if (queueMode == null) {
            log.info("[Match] Player {} not found in any queue, nothing to cancel", playerId);
            return buildCancelResponse("not_in_queue", "You are not currently in any match queue", null);
        }

        // Step 3: Remove player from the queue
        String queueKey = getQueueKey(queueMode);
        removePlayerFromQueue(queueKey, playerId);

        log.info("[Match] Successfully canceled match for player {} from {} queue", playerId, queueMode);
        return buildCancelResponse("success", "Successfully canceled match queue", queueMode);
    }

    /**
     * Remove player from specified queue
     *
     * <p>Helper method to encapsulate queue removal logic.
     * Maintains single responsibility principle.
     *
     * @param queueKey Redis queue key
     * @param playerId player ID to remove
     */
    private void removePlayerFromQueue(String queueKey, String playerId) {
        boolean removed = redisService.getList(queueKey).remove(playerId);
        int newQueueSize = redisService.lSize(queueKey);
        log.debug("[Match] Removed player {} from queue {} (removed: {}, new queue size: {})",
                playerId, queueKey, removed, newQueueSize);
    }

    /**
     * Build cancel match response
     *
     * <p>Factory method to construct CancelMatchResponse with consistent structure.
     * Improves code readability and maintainability.
     *
     * @param status    cancellation status ("success" or "not_in_queue")
     * @param message   descriptive message about the result
     * @param queueMode queue mode that was canceled (null if not in any queue)
     * @return constructed CancelMatchResponse
     */
    private CancelMatchResponse buildCancelResponse(String status, String message, String queueMode) {
        return CancelMatchResponse.builder()
                .status(status)
                .message(message)
                .queueMode(queueMode)
                .build();
    }
}
