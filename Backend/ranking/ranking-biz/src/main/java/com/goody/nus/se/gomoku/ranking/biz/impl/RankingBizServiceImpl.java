package com.goody.nus.se.gomoku.ranking.biz.impl;

import com.goody.nus.se.gomoku.ranking.api.request.MatchSettlementRequest;
import com.goody.nus.se.gomoku.ranking.api.response.LeaderboardEntryResponse;
import com.goody.nus.se.gomoku.ranking.api.response.LeaderboardPageResponse;
import com.goody.nus.se.gomoku.ranking.api.response.LeaderboardsResponse;
import com.goody.nus.se.gomoku.ranking.api.response.MatchRecordResponse;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Ranking Business Service Implementation
 * Provides comprehensive ranking management with high cohesion and low coupling design
 *
 * <p>Core Responsibilities:
 * - Match settlement with idempotency guarantee
 * - Player profile aggregation across multiple dimensions
 * - Leaderboard management for different time scopes
 * - Rule configuration exposure for frontend calculation
 *
 * <p>Design Principles:
 * - High Cohesion: Each method focuses on a single business concern
 * - Low Coupling: Uses DAO Service interfaces instead of direct Mapper access
 * - Code Reuse: Common logic extracted to private utility methods
 * - Data Consistency: Transactional boundaries clearly defined
 *
 * @author chengmuqin
 * @version 2.0, 2025/10/21
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RankingBizServiceImpl implements RankingService {

    private final ILeaderboardRuleService leaderboardRuleService;
    private final IRankingService rankingService;
    private final IScoreService scoreService;
    private final IScoreRuleService scoreRuleService;
    private final ILevelRuleService levelRuleService;
    private final ILevelService levelService;

    // ==================== Public API Methods ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MatchSettlementResponse settleMatch(MatchSettlementRequest request) {
        log.info("Starting match settlement: matchId={}, winnerId={}, loserId={}, modeType={}",
                request.getMatchId(), request.getWinnerId(), request.getLoserId(), request.getModeType());

        // 1. Idempotency check: Prevent duplicate settlement
        if (isMatchAlreadySettled(request.getMatchId())) {
            log.warn("Match already settled, skipping: matchId={}", request.getMatchId());
            return buildAlreadySettledResponse(request);
        }

        // 2. Determine match result type
        boolean isDraw = request.getWinnerId() == null && request.getLoserId() == null;
        boolean isRanked = "RANKED".equalsIgnoreCase(request.getModeType());

        // 3. Get active leaderboard rules
        Map<String, LeaderboardRuleDTO> activeRules = getActiveLeaderboardRules();
        if (activeRules.isEmpty()) {
            throw new IllegalStateException("No active leaderboard rules found");
        }

        // 4. Settle for both players
        MatchSettlementResponse.PlayerReward winnerReward = null;
        MatchSettlementResponse.PlayerReward loserReward = null;
        MatchSettlementResponse.PlayerReward player1Reward = null;
        MatchSettlementResponse.PlayerReward player2Reward = null;

        if (isDraw) {
            // Handle draw: settle for both players with DRAW result
            player1Reward = settleForPlayer(request.getMatchId(), request.getWinnerId(),
                    "DRAW", request.getModeType(), isRanked, activeRules);
            player2Reward = settleForPlayer(request.getMatchId(), request.getLoserId(),
                    "DRAW", request.getModeType(), isRanked, activeRules);
        } else {
            // Handle win/loss: settle for winner and loser separately
            winnerReward = settleForPlayer(request.getMatchId(), request.getWinnerId(),
                    "WIN", request.getModeType(), isRanked, activeRules);
            loserReward = settleForPlayer(request.getMatchId(), request.getLoserId(),
                    "LOSE", request.getModeType(), isRanked, activeRules);
        }

        // 5. Build and return settlement response
        MatchSettlementResponse response = MatchSettlementResponse.builder()
                .matchId(request.getMatchId())
                .modeType(request.getModeType())
                .winnerId(request.getWinnerId())
                .loserId(request.getLoserId())
                .winnerReward(winnerReward)
                .loserReward(loserReward)
                .player1Reward(player1Reward)
                .player2Reward(player2Reward)
                .timestamp(System.currentTimeMillis())
                .build();

        log.info("Match settlement completed: matchId={}", request.getMatchId());
        return response;
    }

    @Override
    public PlayerProfileResponse getPlayerProfile(Long userId) {
        log.info("Getting player profile: userId={}", userId);

        // 1. Get TOTAL leaderboard rule (for permanent stats)
        LeaderboardRuleDTO totalRule = getTotalLeaderboardRule();

        // 2. Get or create ranking record for TOTAL leaderboard
        RankingDTO totalRanking = getOrCreateRanking(userId, totalRule.getId());

        // 3. Calculate level and progress information
        int totalExp = Optional.ofNullable(totalRanking.getTotalExp()).orElse(0);
        List<LevelDTO> allLevels = levelService.findAll();
        LevelDTO currentLevel = findLevelByExp(allLevels, totalExp);
        LevelDTO nextLevel = findNextLevel(allLevels, currentLevel);

        // 4. Calculate win rate and total games from score records
        List<ScoreDTO> totalScores = scoreService.findByUserIdAndLeaderboardRuleId(userId, totalRule.getId());

        int totalGames = totalScores.size();
        long wins = totalScores.stream().filter(s -> "WIN".equalsIgnoreCase(s.getMatchResult())).count();
        String winRate = totalGames == 0 ? "0%" : String.format("%.1f%%", wins * 100.0 / totalGames);

        // 5. Get scores across all leaderboards
        Map<String, Integer> scoresMap = getAllLeaderboardScores(userId);

        // 6. Get ranks across all leaderboards
        Map<String, PlayerProfileResponse.LeaderboardRank> ranksMap = getAllLeaderboardRanks(userId);

        // 7. Build and return comprehensive profile
        return PlayerProfileResponse.builder()
                .userId(userId)
                .level(currentLevel.getId().intValue())
                .exp(totalExp)
                .winRate(winRate)
                .totalGames(totalGames)
                .nextLevelExpRequired(nextLevel != null ? nextLevel.getExpRequired() : null)
                .expToNext(nextLevel != null ? nextLevel.getExpRequired() - totalExp : 0)
                .progressPercent(calculateLevelProgress(totalExp, currentLevel, nextLevel))
                .scores(scoresMap)
                .ranks(ranksMap)
                .build();
    }

    @Override
    public LeaderboardPageResponse getLeaderboard(String scope, int page, int size, Long userId) {
        log.info("Getting leaderboard: scope={}, page={}, size={}, userId={}", scope, page, size, userId);

        // 1. Validate and normalize scope
        String normalizedScope = validateAndNormalizeScope(scope);
        if (normalizedScope == null) {
            return buildEmptyLeaderboardResponse(scope);
        }

        // 2. Get active leaderboard rule for the scope
        int currentTime = (int) (System.currentTimeMillis() / 1000);
        LeaderboardRuleDTO rule = leaderboardRuleService.findActiveRuleByType(normalizedScope, currentTime);
        if (rule == null) {
            throw new IllegalStateException("No active leaderboard rule for scope: " + normalizedScope);
        }

        Long ruleId = rule.getId();

        // 3. Get top 50 rankings
        List<RankingDTO> topRankings = rankingService.findTopByLeaderboardRuleId(ruleId, 50);

        // 4. Build top list
        List<LeaderboardEntryResponse> topList = new ArrayList<>(topRankings.size());
        int rank = 1;
        for (RankingDTO r : topRankings) {
            topList.add(buildLeaderboardEntry(r, rank++));
        }

        // 5. Get current user's rank (if userId provided)
        LeaderboardEntryResponse meEntry = null;
        if (userId != null) {
            meEntry = getCurrentUserRank(userId, ruleId);
        }

        // 6. Get total players count
        long totalPlayers = rankingService.countByLeaderboardRuleId(ruleId);

        // 7. Build and return response
        return LeaderboardPageResponse.builder()
                .scope(normalizedScope)
                .topList(topList)
                .me(meEntry)
                .totalPlayers((int) totalPlayers)
                .build();
    }

    @Override
    public RankingRulesResponse getRankingRules() {
        log.info("Getting ranking rules configuration");

        // 1. Get all experience rules
        List<LevelRuleDTO> levelRules = levelRuleService.findAll();
        List<RankingRulesResponse.ExpRule> expRules = levelRules.stream()
                .map(this::convertToExpRule)
                .collect(Collectors.toList());

        // 2. Get all score rules and expand flattened structure
        List<ScoreRuleDTO> scoreRules = scoreRuleService.findAll();
        List<RankingRulesResponse.ScoreRule> scoreRuleResponses = scoreRules.stream()
                .flatMap(this::expandScoreRuleToResponses)
                .collect(Collectors.toList());

        // 3. Get all level thresholds
        List<LevelDTO> levels = levelService.findAll();
        // Sort by exp required
        levels.sort(Comparator.comparing(l -> Optional.ofNullable(l.getExpRequired()).orElse(0)));
        List<RankingRulesResponse.LevelThreshold> levelThresholds = levels.stream()
                .map(this::convertToLevelThreshold)
                .collect(Collectors.toList());

        // 4. Get active leaderboard configurations
        int now = (int) (System.currentTimeMillis() / 1000);
        List<LeaderboardRuleDTO> activeLeaderboards = leaderboardRuleService.findActiveRules(now);
        List<RankingRulesResponse.LeaderboardConfig> leaderboardConfigs = activeLeaderboards.stream()
                .map(this::convertToLeaderboardConfig)
                .collect(Collectors.toList());

        return RankingRulesResponse.builder()
                .expRules(expRules)
                .scoreRules(scoreRuleResponses)
                .levelThresholds(levelThresholds)
                .leaderboards(leaderboardConfigs)
                .build();
    }

    @Override
    public List<MatchRecordResponse> getMatchHistory(Long userId, String modeType, int page, int size) {
        log.info("Getting match history: userId={}, modeType={}, page={}, size={}", userId, modeType, page, size);
        // TODO: Implement match history query
        return Collections.emptyList();
    }

    @Override
    public LeaderboardsResponse getTopLeaderboards(Long userId) {
        log.info("Getting top leaderboards for userId={}", userId);

        // Get top 50 from each primary leaderboard
        LeaderboardPageResponse daily = getLeaderboard("DAILY", 1, 50, userId);
        LeaderboardPageResponse weekly = getLeaderboard("WEEKLY", 1, 50, userId);
        LeaderboardPageResponse monthly = getLeaderboard("MONTHLY", 1, 50, userId);

        return LeaderboardsResponse.builder()
                .daily(daily)
                .weekly(weekly)
                .monthly(monthly)
                .build();
    }

    // ==================== Private Utility Methods (High Reusability) ====================

    /**
     * Check if a match has already been settled (idempotency)
     */
    private boolean isMatchAlreadySettled(Long matchId) {
        long count = scoreService.countByMatchId(matchId);
        return count > 0;
    }

    /**
     * Get all active leaderboard rules
     */
    private Map<String, LeaderboardRuleDTO> getActiveLeaderboardRules() {
        int now = (int) (System.currentTimeMillis() / 1000);
        List<LeaderboardRuleDTO> activeRules = leaderboardRuleService.findActiveRules(now);
        return activeRules.stream()
                .collect(Collectors.toMap(LeaderboardRuleDTO::getType, rule -> rule, (a, b) -> a));
    }

    /**
     * Get TOTAL leaderboard rule
     */
    private LeaderboardRuleDTO getTotalLeaderboardRule() {
        LeaderboardRuleDTO rule = leaderboardRuleService.findByType("TOTAL");
        if (rule == null) {
            throw new IllegalStateException("TOTAL leaderboard rule not found");
        }
        return rule;
    }

    /**
     * Get or create ranking record for user and leaderboard
     */
    private RankingDTO getOrCreateRanking(Long userId, Long leaderboardRuleId) {
        RankingDTO existing = rankingService.findByUserIdAndLeaderboardRuleId(userId, leaderboardRuleId);
        if (existing != null) {
            return existing;
        }
        return createNewRanking(userId, leaderboardRuleId);
    }

    /**
     * Create a new ranking record
     */
    private RankingDTO createNewRanking(Long userId, Long leaderboardRuleId) {
        RankingDTO newRanking = RankingDTO.builder()
                .userId(userId)
                .leaderboardRuleId(leaderboardRuleId)
                .totalExp(0)
                .levelId(1L)
                .currentTotalScore(0)
                .rankPosition(0)
                .createdTime(LocalDateTime.now())
                .updatedTime(LocalDateTime.now())
                .build();
        rankingService.save(newRanking);
        return newRanking;
    }

    /**
     * Settle match for a single player
     */
    private MatchSettlementResponse.PlayerReward settleForPlayer(
            Long matchId, Long userId, String matchResult, String modeType,
            boolean isRanked, Map<String, LeaderboardRuleDTO> activeRules) {

        // 1. Get TOTAL leaderboard rule to get score rule ID
        LeaderboardRuleDTO totalRule = activeRules.get("TOTAL");

        // 2. Calculate rewards
        Integer expChange = queryExpChange(modeType, matchResult);
        Integer scoreChange = isRanked ? queryScoreChange(totalRule.getRuleId(), modeType, matchResult) : 0;

        // 3. Get initial state from TOTAL leaderboard
        RankingDTO beforeRanking = getOrCreateRanking(userId, totalRule.getId());
        Integer oldLevel = beforeRanking.getLevelId().intValue();
        Integer oldExp = beforeRanking.getTotalExp();
        Integer oldScore = beforeRanking.getCurrentTotalScore();

        // 4. Update all active leaderboards
        for (LeaderboardRuleDTO rule : activeRules.values()) {
            upsertRankingAndScore(userId, rule, matchId, matchResult, modeType, expChange, scoreChange);
        }

        // 4. Get final state
        RankingDTO afterRanking = rankingService.findByUserIdAndLeaderboardRuleId(userId, totalRule.getId());
        if (afterRanking == null) {
            throw new IllegalStateException("Failed to get ranking after settlement");
        }

        Integer newLevel = afterRanking.getLevelId().intValue();
        Integer newExp = afterRanking.getTotalExp();
        Integer newScore = afterRanking.getCurrentTotalScore();

        // 5. Build reward response
        return MatchSettlementResponse.PlayerReward.builder()
                .userId(userId)
                .expChange(expChange)
                .totalExp(newExp)
                .scoreChange(scoreChange)
                .totalScore(newScore)
                .oldLevel(oldLevel)
                .newLevel(newLevel)
                .leveledUp(!oldLevel.equals(newLevel))
                .build();
    }

    /**
     * Update ranking and insert score record for a leaderboard
     */
    private void upsertRankingAndScore(Long userId, LeaderboardRuleDTO lbRule,
                                       Long matchId, String matchResult, String modeType,
                                       int expChange, int scoreChange) {
        // 1. Get or create ranking
        RankingDTO ranking = getOrCreateRanking(userId, lbRule.getId());

        // 2. Calculate new values
        int newExp = ranking.getTotalExp() + expChange;
        Long newLevel = calculateLevelByExp(newExp);
        int newScore = Math.max(0, ranking.getCurrentTotalScore() + scoreChange); // Ensure score doesn't go below 0

        // 3. Update ranking
        ranking.setTotalExp(newExp);
        ranking.setLevelId(newLevel);
        ranking.setCurrentTotalScore(newScore);
        ranking.setUpdatedTime(LocalDateTime.now());
        rankingService.update(ranking);

        // 4. Get score rule ID from leaderboard rule
        Long scoreRuleId = lbRule.getRuleId(); // Use score rule ID from leaderboard rule

        // 5. Insert score record
        ScoreDTO scoreRecord = ScoreDTO.builder()
                .leaderboardRuleId(lbRule.getId())
                .userId(userId)
                .matchId(matchId)
                .ruleId(scoreRuleId)
                .scoreChange(scoreChange)
                .finalScore(newScore)
                .matchResult(matchResult)
                .createdTime(LocalDateTime.now())
                .updatedTime(LocalDateTime.now())
                .build();
        scoreService.save(scoreRecord);
    }

    /**
     * Query experience change from level_rule table
     */
    private Integer queryExpChange(String modeType, String matchResult) {
        LevelRuleDTO rule = levelRuleService.findByModeTypeAndMatchResult(modeType, matchResult);
        return rule != null ? rule.getExpValue() : 0;
    }

    /**
     * Query score change from score_rule table
     */
    private Integer queryScoreChange(Long ruleId, String modeType, String matchResult) {
        if (ruleId == null) {
            return 0;
        }
        ScoreRuleDTO rule = scoreRuleService.findById(ruleId);
        return rule != null ? rule.getScoreValue(modeType, matchResult) : 0;
    }

    /**
     * Find level by total experience
     */
    private LevelDTO findLevelByExp(List<LevelDTO> levels, int exp) {
        LevelDTO currentLevel = LevelDTO.builder()
                .id(1L)
                .expRequired(0)
                .build();

        for (LevelDTO lv : levels) {
            if (lv.getExpRequired() != null && lv.getExpRequired() <= exp
                    && lv.getExpRequired() > currentLevel.getExpRequired()) {
                currentLevel = lv;
            }
        }
        return currentLevel;
    }

    /**
     * Find next level after current level
     */
    private LevelDTO findNextLevel(List<LevelDTO> levels, LevelDTO current) {
        return levels.stream()
                .filter(lv -> lv.getExpRequired() != null && lv.getExpRequired() > current.getExpRequired())
                .min(Comparator.comparing(LevelDTO::getExpRequired))
                .orElse(null);
    }

    /**
     * Calculate level by total experience
     */
    private Long calculateLevelByExp(int exp) {
        List<LevelDTO> levels = levelService.findAll();
        long bestId = 1L;
        int bestReq = Integer.MIN_VALUE;
        for (LevelDTO lv : levels) {
            if (lv.getExpRequired() != null && lv.getExpRequired() <= exp
                    && lv.getExpRequired() > bestReq) {
                bestReq = lv.getExpRequired();
                bestId = lv.getId();
            }
        }
        return bestId;
    }

    /**
     * Calculate level progress percentage
     */
    private Integer calculateLevelProgress(int exp, LevelDTO current, LevelDTO next) {
        if (next == null) {
            return 100;
        }
        int currBase = current.getExpRequired();
        int range = next.getExpRequired() - currBase;
        return (int) (((exp - currBase) * 100.0) / range);
    }

    /**
     * Get scores across all leaderboards for a user
     */
    private Map<String, Integer> getAllLeaderboardScores(Long userId) {
        Map<String, Integer> scoresMap = new HashMap<>();
        Map<String, LeaderboardRuleDTO> activeRules = getActiveLeaderboardRules();

        for (Map.Entry<String, LeaderboardRuleDTO> entry : activeRules.entrySet()) {
            String type = entry.getKey();
            Long ruleId = entry.getValue().getId();

            RankingDTO ranking = rankingService.findByUserIdAndLeaderboardRuleId(userId, ruleId);
            int score = ranking != null ? Optional.ofNullable(ranking.getCurrentTotalScore()).orElse(0) : 0;
            scoresMap.put(type.toLowerCase(), score);
        }

        return scoresMap;
    }

    /**
     * Get ranks across all leaderboards for a user
     */
    private Map<String, PlayerProfileResponse.LeaderboardRank> getAllLeaderboardRanks(Long userId) {
        Map<String, PlayerProfileResponse.LeaderboardRank> ranksMap = new HashMap<>();
        Map<String, LeaderboardRuleDTO> activeRules = getActiveLeaderboardRules();

        for (Map.Entry<String, LeaderboardRuleDTO> entry : activeRules.entrySet()) {
            String type = entry.getKey();
            Long ruleId = entry.getValue().getId();

            PlayerProfileResponse.LeaderboardRank rank = calculateUserRank(userId, ruleId, type);
            ranksMap.put(type, rank);
        }

        return ranksMap;
    }

    /**
     * Calculate user's rank in a specific leaderboard
     */
    private PlayerProfileResponse.LeaderboardRank calculateUserRank(Long userId, Long ruleId, String type) {
        RankingDTO userRanking = rankingService.findByUserIdAndLeaderboardRuleId(userId, ruleId);

        if (userRanking == null) {
            return PlayerProfileResponse.LeaderboardRank.builder()
                    .type(type)
                    .rank(null)
                    .totalPlayers(0)
                    .score(0)
                    .build();
        }

        int userScore = Optional.ofNullable(userRanking.getCurrentTotalScore()).orElse(0);

        // Count players with higher scores
        long higherCount = rankingService.countByLeaderboardRuleIdAndScoreGreaterThan(ruleId, userScore);

        // Count total players
        long totalPlayers = rankingService.countByLeaderboardRuleId(ruleId);

        return PlayerProfileResponse.LeaderboardRank.builder()
                .type(type)
                .rank((int) higherCount + 1)
                .totalPlayers((int) totalPlayers)
                .score(userScore)
                .build();
    }

    /**
     * Get current user's rank entry for leaderboard
     */
    private LeaderboardEntryResponse getCurrentUserRank(Long userId, Long ruleId) {
        RankingDTO userRanking = rankingService.findByUserIdAndLeaderboardRuleId(userId, ruleId);

        if (userRanking == null) {
            return LeaderboardEntryResponse.builder()
                    .userId(userId)
                    .score(0)
                    .rank(null)
                    .build();
        }

        int userScore = Optional.ofNullable(userRanking.getCurrentTotalScore()).orElse(0);
        long higherCount = rankingService.countByLeaderboardRuleIdAndScoreGreaterThan(ruleId, userScore);

        return LeaderboardEntryResponse.builder()
                .userId(userId)
                .score(userScore)
                .rank((int) higherCount + 1)
                .build();
    }

    /**
     * Build leaderboard entry from ranking record
     */
    private LeaderboardEntryResponse buildLeaderboardEntry(RankingDTO ranking, int rank) {
        return LeaderboardEntryResponse.builder()
                .userId(ranking.getUserId())
                .score(Optional.ofNullable(ranking.getCurrentTotalScore()).orElse(0))
                .rank(rank)
                .build();
    }

    /**
     * Validate and normalize leaderboard scope
     */
    private String validateAndNormalizeScope(String scope) {
        if (scope == null) {
            return null;
        }
        String normalized = scope.trim().toUpperCase();
        if (!normalized.matches("DAILY|WEEKLY|MONTHLY|SEASONAL|TOTAL")) {
            return null;
        }
        return normalized;
    }

    /**
     * Build empty leaderboard response
     */
    private LeaderboardPageResponse buildEmptyLeaderboardResponse(String scope) {
        return LeaderboardPageResponse.builder()
                .scope(scope)
                .topList(Collections.emptyList())
                .me(null)
                .totalPlayers(0)
                .build();
    }

    /**
     * Build already settled response
     */
    private MatchSettlementResponse buildAlreadySettledResponse(MatchSettlementRequest request) {
        return MatchSettlementResponse.builder()
                .matchId(request.getMatchId())
                .modeType(request.getModeType())
                .winnerId(request.getWinnerId())
                .loserId(request.getLoserId())
                .timestamp(System.currentTimeMillis())
                .build();
    }

    // ==================== Converter Methods ====================

    private RankingRulesResponse.ExpRule convertToExpRule(LevelRuleDTO levelRule) {
        return RankingRulesResponse.ExpRule.builder()
                .modeType(levelRule.getModeType())
                .matchResult(levelRule.getMatchResult())
                .expValue(levelRule.getExpValue())
                .description(levelRule.getDescription())
                .build();
    }

    /**
     * Expand flattened ScoreRuleDTO into 9 separate ScoreRule responses
     * (3 modes × 3 results = 9 combinations)
     */
    private java.util.stream.Stream<RankingRulesResponse.ScoreRule> expandScoreRuleToResponses(ScoreRuleDTO scoreRule) {
        List<RankingRulesResponse.ScoreRule> rules = new ArrayList<>();

        // RANKED mode
        rules.add(RankingRulesResponse.ScoreRule.builder()
                .modeType("RANKED")
                .matchResult("WIN")
                .scoreValue(scoreRule.getScoreValue("RANKED", "WIN"))
                .description(scoreRule.getDescription())
                .build());
        rules.add(RankingRulesResponse.ScoreRule.builder()
                .modeType("RANKED")
                .matchResult("LOSE")
                .scoreValue(scoreRule.getScoreValue("RANKED", "LOSE"))
                .description(scoreRule.getDescription())
                .build());
        rules.add(RankingRulesResponse.ScoreRule.builder()
                .modeType("RANKED")
                .matchResult("DRAW")
                .scoreValue(scoreRule.getScoreValue("RANKED", "DRAW"))
                .description(scoreRule.getDescription())
                .build());

        // CASUAL mode
        rules.add(RankingRulesResponse.ScoreRule.builder()
                .modeType("CASUAL")
                .matchResult("WIN")
                .scoreValue(scoreRule.getScoreValue("CASUAL", "WIN"))
                .description(scoreRule.getDescription())
                .build());
        rules.add(RankingRulesResponse.ScoreRule.builder()
                .modeType("CASUAL")
                .matchResult("LOSE")
                .scoreValue(scoreRule.getScoreValue("CASUAL", "LOSE"))
                .description(scoreRule.getDescription())
                .build());
        rules.add(RankingRulesResponse.ScoreRule.builder()
                .modeType("CASUAL")
                .matchResult("DRAW")
                .scoreValue(scoreRule.getScoreValue("CASUAL", "DRAW"))
                .description(scoreRule.getDescription())
                .build());

        // PRIVATE mode
        rules.add(RankingRulesResponse.ScoreRule.builder()
                .modeType("PRIVATE")
                .matchResult("WIN")
                .scoreValue(scoreRule.getScoreValue("PRIVATE", "WIN"))
                .description(scoreRule.getDescription())
                .build());
        rules.add(RankingRulesResponse.ScoreRule.builder()
                .modeType("PRIVATE")
                .matchResult("LOSE")
                .scoreValue(scoreRule.getScoreValue("PRIVATE", "LOSE"))
                .description(scoreRule.getDescription())
                .build());
        rules.add(RankingRulesResponse.ScoreRule.builder()
                .modeType("PRIVATE")
                .matchResult("DRAW")
                .scoreValue(scoreRule.getScoreValue("PRIVATE", "DRAW"))
                .description(scoreRule.getDescription())
                .build());

        return rules.stream();
    }

    private RankingRulesResponse.LevelThreshold convertToLevelThreshold(LevelDTO level) {
        return RankingRulesResponse.LevelThreshold.builder()
                .level(level.getId().intValue())
                .expRequired(level.getExpRequired())
                .build();
    }

    private RankingRulesResponse.LeaderboardConfig convertToLeaderboardConfig(LeaderboardRuleDTO rule) {
        return RankingRulesResponse.LeaderboardConfig.builder()
                .type(rule.getType())
                .startTime(rule.getStartTime().longValue())
                .endTime(rule.getEndTime().longValue())
                .description(rule.getDescription())
                .build();
    }
}
