package com.goody.nus.se.gomoku.gomoku.matching.service;

import com.goody.nus.se.gomoku.gomoku.api.request.MatchRequest;
import com.goody.nus.se.gomoku.gomoku.api.response.MatchResponse;
import com.goody.nus.se.gomoku.gomoku.matching.MatchTestApplication;
import com.goody.nus.se.gomoku.redis.service.RedisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = MatchTestApplication.class)
public class MatchServiceTest {
    @Autowired
    private IMatchService matchService;

    @Autowired
    private RedisService redisService;
    @BeforeEach
    void clearRedis() {
        // warring! Clear Redis before each test
        for (String key : redisService.keys("match:*")) {
            redisService.delete(key);
        }
        for (String key : redisService.keys("room:*")) {
            redisService.delete(key);
        }
    }

    /**
     * first player waits for the second player
     */
    @Test
    void testFirstPlayerWaits() {
        MatchRequest request = new MatchRequest();
        request.setMode("casual");

        MatchResponse response = matchService.match(request, "playerA");

        assertThat(response.getStatus()).isEqualTo("waiting");
        assertThat(response.getRoomCode()).isNull();
        assertThat(redisService.exists("match:casual")).isTrue();
    }

    /**
     * second player matches with the first player successfully
     */
    @Test
    void testTwoPlayersMatchSuccessfully() {
        // Player A joins first
        MatchRequest requestA = new MatchRequest();
        requestA.setMode("ranked");
        matchService.match(requestA, "playerA");

        // Player B joins later
        MatchRequest requestB = new MatchRequest();
        requestB.setMode("ranked");

        MatchResponse response = matchService.match(requestB, "playerB");

        assertThat(response.getStatus()).isEqualTo("matched");
        assertThat(response.getRoomCode()).isNotNull();
        assertThat(response.getPlayers()).containsExactlyInAnyOrder("playerA", "playerB");

        // confirm the match queue is cleared
        assertThat(redisService.exists("room:" + response.getRoomCode())).isTrue();
    }

    /**
     * test multiple players match in pairs
     */
    @Test
    void testMultiplePlayersMatchInPairs() {
        for (int i = 1; i <= 4; i++) {
            MatchRequest req = new MatchRequest();
            req.setMode("casual");
            MatchResponse response = matchService.match(req, "p" + i);

            // odd players wait, even players match
            if (i % 2 == 0) {
                assertThat(response.getStatus()).isEqualTo("matched");
                assertThat(response.getRoomCode()).isNotNull();
            } else {
                assertThat(response.getStatus()).isEqualTo("waiting");
            }
        }
    }

    /**
     * Test deduplication: player calls match multiple times (idempotency test)
     * Verify that duplicate requests are properly handled
     */
    @Test
    void testMatchDeduplication_SinglePlayerMultipleCalls() {
        MatchRequest request = new MatchRequest();
        request.setMode("casual");

        // First call - player joins queue
        MatchResponse response1 = matchService.match(request, "playerA");
        assertThat(response1.getStatus()).isEqualTo("waiting");
        assertThat(response1.getRoomCode()).isNull();

        // Second call - same player tries to join again (duplicate)
        MatchResponse response2 = matchService.match(request, "playerA");
        assertThat(response2.getStatus()).isEqualTo("waiting");
        assertThat(response2.getRoomCode()).isNull();

        // Third call - same player tries again (duplicate)
        MatchResponse response3 = matchService.match(request, "playerA");
        assertThat(response3.getStatus()).isEqualTo("waiting");
        assertThat(response3.getRoomCode()).isNull();

        // Verify queue only contains player once
        var queue = redisService.lRange("match:casual", 0, -1);
        assertThat(queue).hasSize(1);
        assertThat(queue).containsExactly("playerA");
    }

    /**
     * Test deduplication: player calls match twice before matching
     * Then a second player joins and they should match
     */
    @Test
    void testMatchDeduplication_DuplicateBeforeMatch() {
        MatchRequest request = new MatchRequest();
        request.setMode("ranked");

        // Player A joins twice
        MatchResponse responseA1 = matchService.match(request, "playerA");
        assertThat(responseA1.getStatus()).isEqualTo("waiting");

        MatchResponse responseA2 = matchService.match(request, "playerA");
        assertThat(responseA2.getStatus()).isEqualTo("waiting");

        // Player B joins once - should match with Player A
        MatchResponse responseB = matchService.match(request, "playerB");
        assertThat(responseB.getStatus()).isEqualTo("matched");
        assertThat(responseB.getRoomCode()).isNotNull();
        assertThat(responseB.getPlayers()).containsExactlyInAnyOrder("playerA", "playerB");

        // Verify room was created
        assertThat(redisService.exists("room:" + responseB.getRoomCode())).isTrue();
    }
}
