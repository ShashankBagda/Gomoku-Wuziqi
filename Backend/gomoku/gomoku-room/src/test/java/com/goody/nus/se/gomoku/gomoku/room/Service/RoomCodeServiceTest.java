package com.goody.nus.se.gomoku.gomoku.room.Service;

import com.goody.nus.se.gomoku.gomoku.api.request.JoinRoomRequest;
import com.goody.nus.se.gomoku.gomoku.api.request.LeaveRoomRequest;
import com.goody.nus.se.gomoku.gomoku.api.response.JoinRoomResponse;
import com.goody.nus.se.gomoku.gomoku.room.RoomTestApplication;
import com.goody.nus.se.gomoku.redis.service.RedisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = RoomTestApplication.class)
public class RoomCodeServiceTest {

    @Autowired
    private RoomCodeService roomCodeService;

    @Autowired
    private RedisService redisService;

    @BeforeEach
    void clearRedis() {
        // Warring! Clear Redis before each test
        for (String key : redisService.keys("room:*")) {
            redisService.delete(key);
        }
    }

    /**
     * Test the createRoom method to ensure it generates a valid 6-digit code
     * and stores it in Redis with the correct key format.
     */
    @Test
    void testCreateRoomGeneratesValidCode() {
        String code = roomCodeService.createRoom();
        assertThat(code).hasSize(6);
        assertThat(code).matches("\\d{6}");
        assertThat(redisService.exists("room:" + code)).isTrue();
    }

    /**
     * Test the createRoom method to ensure it generates unique room codes
     * across multiple invocations.
     */
    @Test
    void testCreateRoomGeneratesUniqueCodes() {
        Set<String> generatedCodes = new HashSet<>();
        for (int i = 0; i < 20; i++) {
            String code = roomCodeService.createRoom();
            assertThat(generatedCodes).doesNotContain(code);
            generatedCodes.add(code);
        }
    }

    /**
     * Test the joinRoom method when the room does not exist.
     */
    @Test
    void testJoinRoom_NotFound() {
        JoinRoomRequest request = new JoinRoomRequest();
        request.setRoomCode("999999");
        String requestPlayerId = "userX";
        JoinRoomResponse response = roomCodeService.joinRoom(request, requestPlayerId);
        assertThat(response.getStatus()).isEqualTo("Not Found");
        assertThat(response.getPlayers()).isNull();
    }

    /**
     * Test the joinRoom method when the room exists but has no players.
     */
    @Test
    void testJoinRoom_Waiting() {
        String roomCode = roomCodeService.createRoom();
        JoinRoomRequest joinRequest = new JoinRoomRequest();
        joinRequest.setRoomCode(roomCode);
        String requestPlayerId = "userX";
        JoinRoomResponse response = roomCodeService.joinRoom(joinRequest, requestPlayerId);
        assertThat(response.getStatus()).isEqualTo("waiting");
        assertThat(response.getPlayers()).isNull();
    }

    /**
     * Test the joinRoom method when the room has one player and a second player joins.
     */
    @Test
    void testJoinRoom_Matched() {
        String roomCode = roomCodeService.createRoom();
        JoinRoomRequest joinRequest = new JoinRoomRequest();
        joinRequest.setRoomCode(roomCode);
        String requestPlayerId = "1000";
        roomCodeService.joinRoom(joinRequest, requestPlayerId);

        JoinRoomRequest request = new JoinRoomRequest();
        request.setRoomCode(roomCode);
        String requestPlayerIdB = "1001";
        JoinRoomResponse response = roomCodeService.joinRoom(request, requestPlayerIdB);
        assertThat(response.getStatus()).isEqualTo("matched");
        assertThat(response.getPlayers()).containsExactlyInAnyOrder("1000", "1001");
    }

    /**
     * Test the joinRoom method when the room is full (two players already present).
     */
    @Test
    void testJoinRoom_Full() {
        String roomCode = roomCodeService.createRoom();
        JoinRoomRequest joinRequestOne = new JoinRoomRequest();
        joinRequestOne.setRoomCode(roomCode);
        String requestPlayerIdA = "1000";
        JoinRoomRequest joinRequestTwo = new JoinRoomRequest();
        joinRequestTwo.setRoomCode(roomCode);
        String requestPlayerIdB = "1001";
        roomCodeService.joinRoom(joinRequestOne, requestPlayerIdA);
        roomCodeService.joinRoom(joinRequestTwo, requestPlayerIdB);

        JoinRoomRequest request = new JoinRoomRequest();
        request.setRoomCode(roomCode);
        String requestPlayerIdC = "userC";
        JoinRoomResponse response = roomCodeService.joinRoom(request, requestPlayerIdC);
        assertThat(response.getStatus()).isEqualTo("full");
        assertThat(response.getPlayers()).containsExactlyInAnyOrder("1000", "1001");
    }

    /**
     * Test the leaveRoom method to ensure it removes the player and deletes the room when empty.
     */
    @Test
    void testLeaveRoomAndDeleteWhenEmpty() {
        String roomCode = roomCodeService.createRoom();
        String requestPlayerId = "1000";
        roomCodeService.joinRoom(new JoinRoomRequest(roomCode), requestPlayerId);
        LeaveRoomRequest leaveRequest = new LeaveRoomRequest();
        leaveRequest.setRoomCode(roomCode);
        roomCodeService.leaveRoom(leaveRequest, "1000");

        assertThat(redisService.exists("room:" + roomCode)).isFalse();
    }

    /**
     * Test deduplication: first player joins room multiple times (idempotency test)
     * Verify that duplicate requests return "waiting" status
     */
    @Test
    void testJoinRoomDeduplication_FirstPlayerMultipleCalls() {
        String roomCode = roomCodeService.createRoom();
        JoinRoomRequest request = new JoinRoomRequest();
        request.setRoomCode(roomCode);
        String playerId = "1000";

        // First call - player joins room
        JoinRoomResponse response1 = roomCodeService.joinRoom(request, playerId);
        assertThat(response1.getStatus()).isEqualTo("waiting");
        assertThat(response1.getPlayers()).isNull();

        // Second call - same player tries to join again (duplicate)
        JoinRoomResponse response2 = roomCodeService.joinRoom(request, playerId);
        assertThat(response2.getStatus()).isEqualTo("waiting");
        assertThat(response2.getPlayers()).isNull();

        // Third call - same player tries again (duplicate)
        JoinRoomResponse response3 = roomCodeService.joinRoom(request, playerId);
        assertThat(response3.getStatus()).isEqualTo("waiting");
        assertThat(response3.getPlayers()).isNull();

        // Verify room only contains player once
        var players = redisService.lRange("room:" + roomCode + ":players", 0, -1);
        assertThat(players).hasSize(1);
        assertThat(players).containsExactly(playerId);
    }

    /**
     * Test deduplication: second player joins room multiple times after first player
     * Verify that first duplicate returns "matched" and subsequent calls also return "matched"
     */
    @Test
    void testJoinRoomDeduplication_SecondPlayerMultipleCalls() {
        String roomCode = roomCodeService.createRoom();
        JoinRoomRequest request = new JoinRoomRequest();
        request.setRoomCode(roomCode);

        // First player joins
        String player1Id = "1000";
        JoinRoomResponse response1 = roomCodeService.joinRoom(request, player1Id);
        assertThat(response1.getStatus()).isEqualTo("waiting");

        // Second player joins - should match
        String player2Id = "1001";
        JoinRoomResponse response2 = roomCodeService.joinRoom(request, player2Id);
        assertThat(response2.getStatus()).isEqualTo("matched");
        assertThat(response2.getPlayers()).containsExactlyInAnyOrder(player1Id, player2Id);

        // Second player tries to join again (duplicate) - should return matched
        JoinRoomResponse response3 = roomCodeService.joinRoom(request, player2Id);
        assertThat(response3.getStatus()).isEqualTo("matched");
        assertThat(response3.getPlayers()).containsExactlyInAnyOrder(player1Id, player2Id);

        // Verify room still only contains two players
        var players = redisService.lRange("room:" + roomCode + ":players", 0, -1);
        assertThat(players).hasSize(2);
        assertThat(players).containsExactlyInAnyOrder(player1Id, player2Id);
    }

    /**
     * Test deduplication: first player joins multiple times, then second player joins
     * Verify matching works correctly despite duplicates
     */
    @Test
    void testJoinRoomDeduplication_FirstPlayerDuplicateThenSecondJoins() {
        String roomCode = roomCodeService.createRoom();
        JoinRoomRequest request = new JoinRoomRequest();
        request.setRoomCode(roomCode);
        String player1Id = "1000";
        String player2Id = "1001";

        // First player joins twice
        JoinRoomResponse responseA1 = roomCodeService.joinRoom(request, player1Id);
        assertThat(responseA1.getStatus()).isEqualTo("waiting");

        JoinRoomResponse responseA2 = roomCodeService.joinRoom(request, player1Id);
        assertThat(responseA2.getStatus()).isEqualTo("waiting");

        // Second player joins - should match with first player
        JoinRoomResponse responseB = roomCodeService.joinRoom(request, player2Id);
        assertThat(responseB.getStatus()).isEqualTo("matched");
        assertThat(responseB.getPlayers()).containsExactlyInAnyOrder(player1Id, player2Id);

        // Verify room contains exactly two unique players
        var players = redisService.lRange("room:" + roomCode + ":players", 0, -1);
        assertThat(players).hasSize(2);
        assertThat(players).containsExactlyInAnyOrder(player1Id, player2Id);
    }

}
