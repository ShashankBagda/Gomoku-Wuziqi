package com.goody.nus.se.gomoku.ranking.biz.service.impl;

import com.goody.nus.se.gomoku.ranking.TestDbApplication;
import com.goody.nus.se.gomoku.ranking.api.request.MatchSettlementRequest;
import com.goody.nus.se.gomoku.ranking.api.response.LeaderboardEntryResponse;
import com.goody.nus.se.gomoku.ranking.api.response.LeaderboardPageResponse;
import com.goody.nus.se.gomoku.ranking.api.response.LeaderboardsResponse;
import com.goody.nus.se.gomoku.ranking.api.response.MatchSettlementResponse;
import com.goody.nus.se.gomoku.ranking.api.response.PlayerProfileResponse;
import com.goody.nus.se.gomoku.ranking.api.response.RankingRulesResponse;
import com.goody.nus.se.gomoku.ranking.biz.RankingService;
import com.goody.nus.se.gomoku.ranking.model.dto.LeaderboardRuleDTO;
import com.goody.nus.se.gomoku.ranking.model.dto.LevelDTO;
import com.goody.nus.se.gomoku.ranking.model.dto.LevelRuleDTO;
import com.goody.nus.se.gomoku.ranking.model.dto.RankingDTO;
import com.goody.nus.se.gomoku.ranking.model.dto.ScoreDTO;
import com.goody.nus.se.gomoku.ranking.model.dto.ScoreRuleDTO;
import com.goody.nus.se.gomoku.ranking.service.interfaces.ILeaderboardRuleService;
import com.goody.nus.se.gomoku.ranking.service.interfaces.ILevelRuleService;
import com.goody.nus.se.gomoku.ranking.service.interfaces.ILevelService;
import com.goody.nus.se.gomoku.ranking.service.interfaces.IRankingService;
import com.goody.nus.se.gomoku.ranking.service.interfaces.IScoreRuleService;
import com.goody.nus.se.gomoku.ranking.service.interfaces.IScoreService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration test for RankingBizServiceImpl
 * Tests all core functionalities including match settlement, player profiles, leaderboards, and rules
 *
 * @author chengmuqin
 * @version 2.0, 2025/10/21
 */
@SpringBootTest(classes = TestDbApplication.class)
class RankingBizServiceImplTest {

    private static final Long TEST_USER_ID_1 = 10001L;
    private static final Long TEST_USER_ID_2 = 10002L;
    private static final Long TEST_USER_ID_3 = 10003L;

    @Autowired
    private ILevelRuleService levelRuleService;

    @Autowired
    private IScoreRuleService scoreRuleService;
    private static final Long TEST_MATCH_ID_1 = 20001L;
    private static final Long TEST_MATCH_ID_2 = 20002L;
    private static final Long TEST_MATCH_ID_3 = 20003L;
    @Autowired
    private RankingService rankingService;
    @Autowired
    private ILeaderboardRuleService leaderboardRuleService;
    @Autowired
    private ILevelService levelService;
    @Autowired
    private IRankingService rankingServiceDao;
    @Autowired
    private IScoreService scoreService;

    @BeforeEach
    void setUp() {
        // Clean up existing test data
        cleanupTestData();

        // Initialize configuration data
        initializeConfigData();
    }

    @AfterEach
    void tearDown() {
        // Clean up test data after each test
        cleanupTestData();
    }

    /**
     * Initialize configuration data: levels, leaderboard rules, level rules, score rules
     * Checks if data already exists before inserting to avoid duplicates
     */
    private void initializeConfigData() {
        // Check if configuration data already exists
        List<LevelDTO> existingLevels = levelService.findAll();
        if (!existingLevels.isEmpty()) {
            // Configuration data already initialized (likely from SQL script)
            return;
        }

        // 1. Create Level definitions
        levelService.save(LevelDTO.builder()
                .id(1L)
                .expRequired(0)
                .createdTime(LocalDateTime.now())
                .updatedTime(LocalDateTime.now())
                .build());

        levelService.save(LevelDTO.builder()
                .id(2L)
                .expRequired(100)
                .createdTime(LocalDateTime.now())
                .updatedTime(LocalDateTime.now())
                .build());

        levelService.save(LevelDTO.builder()
                .id(3L)
                .expRequired(300)
                .createdTime(LocalDateTime.now())
                .updatedTime(LocalDateTime.now())
                .build());

        // 2. Create LeaderboardRule (TOTAL, DAILY, WEEKLY, MONTHLY, SEASONAL)
        int now = (int) (System.currentTimeMillis() / 1000);
        int farFuture = now + 365 * 24 * 3600; // 1 year from now

        leaderboardRuleService.save(LeaderboardRuleDTO.builder()
                .type("TOTAL")
                .ruleId(1L)
                .startTime(0)
                .endTime(farFuture)
                .description("Total leaderboard")
                .createdTime(LocalDateTime.now())
                .updatedTime(LocalDateTime.now())
                .build());

        leaderboardRuleService.save(LeaderboardRuleDTO.builder()
                .type("DAILY")
                .ruleId(1L)
                .startTime(now)
                .endTime(farFuture)
                .description("Daily leaderboard")
                .createdTime(LocalDateTime.now())
                .updatedTime(LocalDateTime.now())
                .build());

        leaderboardRuleService.save(LeaderboardRuleDTO.builder()
                .type("WEEKLY")
                .ruleId(1L)
                .startTime(now)
                .endTime(farFuture)
                .description("Weekly leaderboard")
                .createdTime(LocalDateTime.now())
                .updatedTime(LocalDateTime.now())
                .build());

        leaderboardRuleService.save(LeaderboardRuleDTO.builder()
                .type("MONTHLY")
                .ruleId(1L)
                .startTime(now)
                .endTime(farFuture)
                .description("Monthly leaderboard")
                .createdTime(LocalDateTime.now())
                .updatedTime(LocalDateTime.now())
                .build());

        leaderboardRuleService.save(LeaderboardRuleDTO.builder()
                .type("SEASONAL")
                .ruleId(1L)
                .startTime(now)
                .endTime(farFuture)
                .description("Seasonal leaderboard")
                .createdTime(LocalDateTime.now())
                .updatedTime(LocalDateTime.now())
                .build());

        // 3. Create LevelRule (experience rewards)
        levelRuleService.save(LevelRuleDTO.builder()
                .modeType("RANKED")
                .matchResult("WIN")
                .expValue(50)
                .description("Ranked win exp")
                .createdTime(LocalDateTime.now())
                .updatedTime(LocalDateTime.now())
                .build());

        levelRuleService.save(LevelRuleDTO.builder()
                .modeType("RANKED")
                .matchResult("LOSE")
                .expValue(10)
                .description("Ranked lose exp")
                .createdTime(LocalDateTime.now())
                .updatedTime(LocalDateTime.now())
                .build());

        levelRuleService.save(LevelRuleDTO.builder()
                .modeType("RANKED")
                .matchResult("DRAW")
                .expValue(20)
                .description("Ranked draw exp")
                .createdTime(LocalDateTime.now())
                .updatedTime(LocalDateTime.now())
                .build());

        levelRuleService.save(LevelRuleDTO.builder()
                .modeType("CASUAL")
                .matchResult("WIN")
                .expValue(20)
                .description("Casual win exp")
                .createdTime(LocalDateTime.now())
                .updatedTime(LocalDateTime.now())
                .build());

        levelRuleService.save(LevelRuleDTO.builder()
                .modeType("CASUAL")
                .matchResult("LOSE")
                .expValue(5)
                .description("Casual lose exp")
                .createdTime(LocalDateTime.now())
                .updatedTime(LocalDateTime.now())
                .build());

        levelRuleService.save(LevelRuleDTO.builder()
                .modeType("CASUAL")
                .matchResult("DRAW")
                .expValue(10)
                .description("Casual draw exp")
                .createdTime(LocalDateTime.now())
                .updatedTime(LocalDateTime.now())
                .build());

        // 4. Create ScoreRule (score changes for RANKED mode only)
        scoreRuleService.save(ScoreRuleDTO.builder()
                .description("Ranked win score")
                .createdTime(LocalDateTime.now())
                .updatedTime(LocalDateTime.now())
                .build());

        scoreRuleService.save(ScoreRuleDTO.builder()
                .description("Ranked lose score")
                .createdTime(LocalDateTime.now())
                .updatedTime(LocalDateTime.now())
                .build());

        scoreRuleService.save(ScoreRuleDTO.builder()
                .description("Ranked draw score")
                .createdTime(LocalDateTime.now())
                .updatedTime(LocalDateTime.now())
                .build());
    }

    /**
     * Clean up test data
     * Only cleans up test-generated data (scores, rankings), preserves configuration data
     */
    private void cleanupTestData() {
        // Clean up scores (test data)
        List<ScoreDTO> allScores = scoreService.findAll();
        for (ScoreDTO score : allScores) {
            scoreService.deleteById(score.getId());
        }

        // Clean up rankings (test data)
        List<RankingDTO> allRankings = rankingServiceDao.findAll();
        for (RankingDTO ranking : allRankings) {
            rankingServiceDao.deleteById(ranking.getId());
        }

        // DO NOT clean up configuration data:
        // - score_rule (system configuration)
        // - level_rule (system configuration)
        // - leaderboard_rule (system configuration)
        // - level (system configuration)
        // These should be initialized once via SQL script: init_ranking_config_data.sql
    }

    // ==================== Test Cases ====================

    @Test
    void testSettleMatch_RankedWin_ShouldUpdateExpAndScore() {
        // Given
        MatchSettlementRequest request = MatchSettlementRequest.builder()
                .matchId(TEST_MATCH_ID_1)
                .winnerId(TEST_USER_ID_1)
                .loserId(TEST_USER_ID_2)
                .modeType("RANKED")
                .build();

        // When
        MatchSettlementResponse response = rankingService.settleMatch(request);

        // Then
        assertNotNull(response);
        assertEquals(TEST_MATCH_ID_1, response.getMatchId());
        assertEquals("RANKED", response.getModeType());

        // Verify winner rewards
        assertNotNull(response.getWinnerReward());
        assertEquals(TEST_USER_ID_1, response.getWinnerReward().getUserId());
        assertEquals(50, response.getWinnerReward().getExpChange(), "Winner should get 50 exp");
        assertEquals(25, response.getWinnerReward().getScoreChange(), "Winner should get 25 score");
        assertTrue(response.getWinnerReward().getTotalExp() > 0, "Total exp should be greater than 0");

        // Verify loser rewards
        assertNotNull(response.getLoserReward());
        assertEquals(TEST_USER_ID_2, response.getLoserReward().getUserId());
        assertEquals(10, response.getLoserReward().getExpChange(), "Loser should get 10 exp");
        assertEquals(-15, response.getLoserReward().getScoreChange(), "Loser should lose 15 score");
    }

    @Test
    void testSettleMatch_CasualWin_ShouldUpdateExpOnly() {
        // Given
        MatchSettlementRequest request = MatchSettlementRequest.builder()
                .matchId(TEST_MATCH_ID_1)
                .winnerId(TEST_USER_ID_1)
                .loserId(TEST_USER_ID_2)
                .modeType("CASUAL")
                .build();

        // When
        MatchSettlementResponse response = rankingService.settleMatch(request);

        // Then
        assertNotNull(response);

        // Verify winner rewards (CASUAL mode: no score change)
        assertEquals(20, response.getWinnerReward().getExpChange(), "Winner should get 20 exp");
        assertEquals(0, response.getWinnerReward().getScoreChange(), "Casual mode should not change score");

        // Verify loser rewards
        assertEquals(5, response.getLoserReward().getExpChange(), "Loser should get 5 exp");
        assertEquals(0, response.getLoserReward().getScoreChange(), "Casual mode should not change score");
    }

    @Test
    void testSettleMatch_Idempotency_ShouldNotUpdateTwice() {
        // Given
        MatchSettlementRequest request = MatchSettlementRequest.builder()
                .matchId(TEST_MATCH_ID_1)
                .winnerId(TEST_USER_ID_1)
                .loserId(TEST_USER_ID_2)
                .modeType("RANKED")
                .build();

        // When: First settlement
        MatchSettlementResponse firstResponse = rankingService.settleMatch(request);

        // When: Second settlement with same matchId
        MatchSettlementResponse secondResponse = rankingService.settleMatch(request);

        // Then: Second response should be empty (idempotent)
        assertNotNull(secondResponse);
        assertNull(secondResponse.getWinnerReward(), "Second settlement should not have winner reward");
        assertNull(secondResponse.getLoserReward(), "Second settlement should not have loser reward");

        // Verify first response has rewards
        assertNotNull(firstResponse.getWinnerReward());
        assertNotNull(firstResponse.getLoserReward());
    }

    @Test
    void testSettleMatch_LevelUp_ShouldUpdateLevel() {
        // Given: Settle multiple matches to accumulate exp
        for (int i = 0; i < 3; i++) {
            MatchSettlementRequest request = MatchSettlementRequest.builder()
                    .matchId(TEST_MATCH_ID_1 + i)
                    .winnerId(TEST_USER_ID_1)
                    .loserId(TEST_USER_ID_2)
                    .modeType("RANKED")
                    .build();
            rankingService.settleMatch(request);
        }

        // When: Get player profile
        PlayerProfileResponse profile = rankingService.getPlayerProfile(TEST_USER_ID_1);

        // Then: Player should have leveled up (3 wins * 50 exp = 150 exp > 100 exp required for level 2)
        assertNotNull(profile);
        assertTrue(profile.getLevel() >= 2, "Player should be at least level 2 after 150 exp");
        assertTrue(profile.getExp() >= 100, "Player should have at least 100 exp");
    }

    @Test
    void testGetPlayerProfile_ShouldReturnComprehensiveProfile() {
        // Given: Settle some matches first
        MatchSettlementRequest request1 = MatchSettlementRequest.builder()
                .matchId(TEST_MATCH_ID_1)
                .winnerId(TEST_USER_ID_1)
                .loserId(TEST_USER_ID_2)
                .modeType("RANKED")
                .build();
        rankingService.settleMatch(request1);

        MatchSettlementRequest request2 = MatchSettlementRequest.builder()
                .matchId(TEST_MATCH_ID_2)
                .winnerId(TEST_USER_ID_2)
                .loserId(TEST_USER_ID_1)
                .modeType("RANKED")
                .build();
        rankingService.settleMatch(request2);

        // When
        PlayerProfileResponse profile = rankingService.getPlayerProfile(TEST_USER_ID_1);

        // Then
        assertNotNull(profile);
        assertEquals(TEST_USER_ID_1, profile.getUserId());
        assertNotNull(profile.getLevel(), "Level should not be null");
        assertNotNull(profile.getExp(), "Exp should not be null");
        assertTrue(profile.getExp() > 0, "Exp should be greater than 0");
        assertEquals(2, profile.getTotalGames(), "Should have played 2 games");
        assertNotNull(profile.getWinRate(), "Win rate should not be null");
        assertEquals("50.0%", profile.getWinRate(), "Win rate should be 50%");

        // Verify scores map
        assertNotNull(profile.getScores());
        assertTrue(profile.getScores().containsKey("total"), "Should have total score");
        assertTrue(profile.getScores().containsKey("daily"), "Should have daily score");

        // Verify ranks map
        assertNotNull(profile.getRanks());
        assertTrue(profile.getRanks().containsKey("TOTAL"), "Should have TOTAL rank");
        assertNotNull(profile.getRanks().get("TOTAL").getRank(), "Should have rank position");
    }

    @Test
    void testGetLeaderboard_ShouldReturnTopPlayers() {
        // Given: Create multiple players with different scores
        settleMultipleMatches();

        // When
        LeaderboardPageResponse leaderboard = rankingService.getLeaderboard("TOTAL", 1, 50, TEST_USER_ID_1);

        // Then
        assertNotNull(leaderboard);
        assertEquals("TOTAL", leaderboard.getScope());
        assertNotNull(leaderboard.getTopList());
        assertTrue(leaderboard.getTopList().size() > 0, "Should have at least one player in leaderboard");

        // Verify top list is sorted by score (descending)
        List<LeaderboardEntryResponse> topList = leaderboard.getTopList();
        for (int i = 0; i < topList.size() - 1; i++) {
            assertTrue(topList.get(i).getScore() >= topList.get(i + 1).getScore(),
                    "Leaderboard should be sorted by score descending");
        }

        // Verify ranks are assigned correctly (1-based)
        for (int i = 0; i < topList.size(); i++) {
            assertEquals(i + 1, topList.get(i).getRank(), "Rank should be 1-based and sequential");
        }

        // Verify current user's entry
        assertNotNull(leaderboard.getMe(), "Should have current user's entry");
        assertEquals(TEST_USER_ID_1, leaderboard.getMe().getUserId());
    }

    @Test
    void testGetLeaderboard_InvalidScope_ShouldReturnEmpty() {
        // When
        LeaderboardPageResponse leaderboard = rankingService.getLeaderboard("INVALID", 1, 50, null);

        // Then
        assertNotNull(leaderboard);
        assertEquals("INVALID", leaderboard.getScope());
        assertTrue(leaderboard.getTopList().isEmpty(), "Invalid scope should return empty list");
        assertEquals(0, leaderboard.getTotalPlayers());
    }

    @Test
    void testGetRankingRules_ShouldReturnAllRules() {
        // When
        RankingRulesResponse rules = rankingService.getRankingRules();

        // Then
        assertNotNull(rules);

        // Verify exp rules
        assertNotNull(rules.getExpRules());
        assertTrue(rules.getExpRules().size() >= 6, "Should have at least 6 exp rules");
        assertTrue(rules.getExpRules().stream().anyMatch(r ->
                "RANKED".equals(r.getModeType()) && "WIN".equals(r.getMatchResult()) && r.getExpValue() == 50
        ), "Should have RANKED WIN exp rule");

        // Verify score rules
        assertNotNull(rules.getScoreRules());
        assertTrue(rules.getScoreRules().size() >= 3, "Should have at least 3 score rules");
        assertTrue(rules.getScoreRules().stream().anyMatch(r ->
                "RANKED".equals(r.getModeType()) && "WIN".equals(r.getMatchResult()) && r.getScoreValue() == 25
        ), "Should have RANKED WIN score rule");

        // Verify level thresholds
        assertNotNull(rules.getLevelThresholds());
        assertTrue(rules.getLevelThresholds().size() >= 3, "Should have at least 3 level thresholds");
        assertTrue(rules.getLevelThresholds().stream().anyMatch(t ->
                t.getLevel() == 1 && t.getExpRequired() == 0
        ), "Should have level 1 threshold");

        // Verify leaderboard configs
        assertNotNull(rules.getLeaderboards());
        assertTrue(rules.getLeaderboards().size() >= 4, "Should have at least 4 leaderboards");
        assertTrue(rules.getLeaderboards().stream().anyMatch(l ->
                "TOTAL".equals(l.getType())
        ), "Should have TOTAL leaderboard");
    }

    @Test
    void testMultipleLeaderboards_ShouldUpdateAll() {
        // Given
        MatchSettlementRequest request = MatchSettlementRequest.builder()
                .matchId(TEST_MATCH_ID_1)
                .winnerId(TEST_USER_ID_1)
                .loserId(TEST_USER_ID_2)
                .modeType("RANKED")
                .build();

        // When
        rankingService.settleMatch(request);

        // Then: Verify all leaderboards are updated
        PlayerProfileResponse profile = rankingService.getPlayerProfile(TEST_USER_ID_1);
        Map<String, Integer> scores = profile.getScores();

        assertNotNull(scores);
        assertTrue(scores.containsKey("total"), "Should have TOTAL score");
        assertTrue(scores.containsKey("daily"), "Should have DAILY score");
        assertTrue(scores.containsKey("weekly"), "Should have WEEKLY score");
        assertTrue(scores.containsKey("monthly"), "Should have MONTHLY score");
        assertTrue(scores.containsKey("seasonal"), "Should have SEASONAL score");

        // All scores should be equal for the first match
        assertEquals(scores.get("total"), scores.get("daily"), "TOTAL and DAILY should be equal");
        assertEquals(scores.get("total"), scores.get("weekly"), "TOTAL and WEEKLY should be equal");
        assertEquals(scores.get("total"), scores.get("monthly"), "TOTAL and MONTHLY should be equal");
        assertEquals(scores.get("total"), scores.get("seasonal"), "TOTAL and SEASONAL should be equal");
    }

    @Test
    void testWinRate_ShouldCalculateCorrectly() {
        // Given: Win 2, Lose 1
        MatchSettlementRequest win1 = MatchSettlementRequest.builder()
                .matchId(TEST_MATCH_ID_1)
                .winnerId(TEST_USER_ID_1)
                .loserId(TEST_USER_ID_2)
                .modeType("RANKED")
                .build();
        rankingService.settleMatch(win1);

        MatchSettlementRequest win2 = MatchSettlementRequest.builder()
                .matchId(TEST_MATCH_ID_2)
                .winnerId(TEST_USER_ID_1)
                .loserId(TEST_USER_ID_2)
                .modeType("RANKED")
                .build();
        rankingService.settleMatch(win2);

        MatchSettlementRequest loss = MatchSettlementRequest.builder()
                .matchId(TEST_MATCH_ID_3)
                .winnerId(TEST_USER_ID_2)
                .loserId(TEST_USER_ID_1)
                .modeType("RANKED")
                .build();
        rankingService.settleMatch(loss);

        // When
        PlayerProfileResponse profile = rankingService.getPlayerProfile(TEST_USER_ID_1);

        // Then
        assertNotNull(profile);
        assertEquals(3, profile.getTotalGames());
        assertEquals("66.7%", profile.getWinRate(), "Win rate should be 66.7% (2 wins out of 3)");
    }

    @Test
    void testRankCalculation_ShouldBeCorrect() {
        // Given: Create a clear ranking scenario
        // User 1: 2 wins (50 score)
        // User 2: 1 win (25 score)
        // User 3: 0 wins (0 score)
        rankingService.settleMatch(MatchSettlementRequest.builder()
                .matchId(TEST_MATCH_ID_1)
                .winnerId(TEST_USER_ID_1)
                .loserId(TEST_USER_ID_3)
                .modeType("RANKED")
                .build());

        rankingService.settleMatch(MatchSettlementRequest.builder()
                .matchId(TEST_MATCH_ID_2)
                .winnerId(TEST_USER_ID_1)
                .loserId(TEST_USER_ID_3)
                .modeType("RANKED")
                .build());

        rankingService.settleMatch(MatchSettlementRequest.builder()
                .matchId(TEST_MATCH_ID_3)
                .winnerId(TEST_USER_ID_2)
                .loserId(TEST_USER_ID_3)
                .modeType("RANKED")
                .build());

        // When
        PlayerProfileResponse profile1 = rankingService.getPlayerProfile(TEST_USER_ID_1);
        PlayerProfileResponse profile2 = rankingService.getPlayerProfile(TEST_USER_ID_2);
        PlayerProfileResponse profile3 = rankingService.getPlayerProfile(TEST_USER_ID_3);

        // Then: Verify ranks
        assertEquals(1, profile1.getRanks().get("TOTAL").getRank(), "User 1 should be rank 1");
        assertEquals(2, profile2.getRanks().get("TOTAL").getRank(), "User 2 should be rank 2");
        assertEquals(3, profile3.getRanks().get("TOTAL").getRank(), "User 3 should be rank 3");

        // Verify scores match ranks
        assertTrue(profile1.getScores().get("total") > profile2.getScores().get("total"));
        assertTrue(profile2.getScores().get("total") > profile3.getScores().get("total"));
    }

    @Test
    void testGetTopLeaderboards_ShouldReturnThreeLeaderboards() {
        // Given: Settle multiple matches to create leaderboard data
        settleMultipleMatches();

        // When: Get all three leaderboards without userId
        LeaderboardsResponse response = rankingService.getTopLeaderboards(null);

        // Then: Verify all three leaderboards are returned
        assertNotNull(response, "Response should not be null");
        assertNotNull(response.getDaily(), "Daily leaderboard should not be null");
        assertNotNull(response.getWeekly(), "Weekly leaderboard should not be null");
        assertNotNull(response.getMonthly(), "Monthly leaderboard should not be null");

        // Verify each leaderboard has correct scope
        assertEquals("DAILY", response.getDaily().getScope(), "Daily scope should be DAILY");
        assertEquals("WEEKLY", response.getWeekly().getScope(), "Weekly scope should be WEEKLY");
        assertEquals("MONTHLY", response.getMonthly().getScope(), "Monthly scope should be MONTHLY");

        // Verify all leaderboards have data
        assertTrue(response.getDaily().getTopList().size() > 0, "Daily leaderboard should have entries");
        assertTrue(response.getWeekly().getTopList().size() > 0, "Weekly leaderboard should have entries");
        assertTrue(response.getMonthly().getTopList().size() > 0, "Monthly leaderboard should have entries");

        // Verify top list does not exceed 50 entries
        assertTrue(response.getDaily().getTopList().size() <= 50, "Daily leaderboard should have max 50 entries");
        assertTrue(response.getWeekly().getTopList().size() <= 50, "Weekly leaderboard should have max 50 entries");
        assertTrue(response.getMonthly().getTopList().size() <= 50, "Monthly leaderboard should have max 50 entries");

        // Verify user entry is null when no userId provided
        assertNull(response.getDaily().getMe(), "Daily 'me' should be null when userId not provided");
        assertNull(response.getWeekly().getMe(), "Weekly 'me' should be null when userId not provided");
        assertNull(response.getMonthly().getMe(), "Monthly 'me' should be null when userId not provided");
    }

    @Test
    void testGetTopLeaderboards_WithUserId_ShouldIncludeUserRank() {
        // Given: Settle multiple matches to create leaderboard data
        settleMultipleMatches();

        // When: Get all three leaderboards with userId
        LeaderboardsResponse response = rankingService.getTopLeaderboards(TEST_USER_ID_1);

        // Then: Verify user's rank is included in each leaderboard
        assertNotNull(response, "Response should not be null");

        // Verify daily leaderboard includes user
        assertNotNull(response.getDaily().getMe(), "Daily 'me' should not be null");
        assertEquals(TEST_USER_ID_1, response.getDaily().getMe().getUserId(), "Daily 'me' should be correct user");
        assertNotNull(response.getDaily().getMe().getRank(), "Daily user rank should not be null");

        // Verify weekly leaderboard includes user
        assertNotNull(response.getWeekly().getMe(), "Weekly 'me' should not be null");
        assertEquals(TEST_USER_ID_1, response.getWeekly().getMe().getUserId(), "Weekly 'me' should be correct user");
        assertNotNull(response.getWeekly().getMe().getRank(), "Weekly user rank should not be null");

        // Verify monthly leaderboard includes user
        assertNotNull(response.getMonthly().getMe(), "Monthly 'me' should not be null");
        assertEquals(TEST_USER_ID_1, response.getMonthly().getMe().getUserId(), "Monthly 'me' should be correct user");
        assertNotNull(response.getMonthly().getMe().getRank(), "Monthly user rank should not be null");
    }

    @Test
    void testGetTopLeaderboards_LeaderboardsShouldBeSorted() {
        // Given: Settle multiple matches to create leaderboard data
        settleMultipleMatches();

        // When
        LeaderboardsResponse response = rankingService.getTopLeaderboards(null);

        // Then: Verify all leaderboards are sorted by score descending
        List<LeaderboardEntryResponse> dailyList = response.getDaily().getTopList();
        List<LeaderboardEntryResponse> weeklyList = response.getWeekly().getTopList();
        List<LeaderboardEntryResponse> monthlyList = response.getMonthly().getTopList();

        // Check daily leaderboard sorting
        for (int i = 0; i < dailyList.size() - 1; i++) {
            assertTrue(dailyList.get(i).getScore() >= dailyList.get(i + 1).getScore(),
                    "Daily leaderboard should be sorted by score descending");
            assertEquals(i + 1, dailyList.get(i).getRank(), "Daily ranks should be sequential");
        }

        // Check weekly leaderboard sorting
        for (int i = 0; i < weeklyList.size() - 1; i++) {
            assertTrue(weeklyList.get(i).getScore() >= weeklyList.get(i + 1).getScore(),
                    "Weekly leaderboard should be sorted by score descending");
            assertEquals(i + 1, weeklyList.get(i).getRank(), "Weekly ranks should be sequential");
        }

        // Check monthly leaderboard sorting
        for (int i = 0; i < monthlyList.size() - 1; i++) {
            assertTrue(monthlyList.get(i).getScore() >= monthlyList.get(i + 1).getScore(),
                    "Monthly leaderboard should be sorted by score descending");
            assertEquals(i + 1, monthlyList.get(i).getRank(), "Monthly ranks should be sequential");
        }
    }

    // ==================== Helper Methods ====================

    /**
     * Settle multiple matches to create test data
     */
    private void settleMultipleMatches() {
        // User 1 wins 3 matches
        for (int i = 0; i < 3; i++) {
            rankingService.settleMatch(MatchSettlementRequest.builder()
                    .matchId(TEST_MATCH_ID_1 + i * 10L)
                    .winnerId(TEST_USER_ID_1)
                    .loserId(TEST_USER_ID_2)
                    .modeType("RANKED")
                    .build());
        }

        // User 2 wins 2 matches
        for (int i = 0; i < 2; i++) {
            rankingService.settleMatch(MatchSettlementRequest.builder()
                    .matchId(TEST_MATCH_ID_1 + 100L + i * 10L)
                    .winnerId(TEST_USER_ID_2)
                    .loserId(TEST_USER_ID_3)
                    .modeType("RANKED")
                    .build());
        }

        // User 3 wins 1 match
        rankingService.settleMatch(MatchSettlementRequest.builder()
                .matchId(TEST_MATCH_ID_1 + 200L)
                .winnerId(TEST_USER_ID_3)
                .loserId(TEST_USER_ID_1)
                .modeType("RANKED")
                .build());
    }
}


