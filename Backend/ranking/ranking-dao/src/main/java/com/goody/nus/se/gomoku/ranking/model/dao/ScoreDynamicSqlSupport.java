package com.goody.nus.se.gomoku.ranking.model.dao;

import jakarta.annotation.Generated;
import java.sql.JDBCType;
import java.time.LocalDateTime;
import org.mybatis.dynamic.sql.AliasableSqlTable;
import org.mybatis.dynamic.sql.SqlColumn;

public final class ScoreDynamicSqlSupport {
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: score")
    public static final Score score = new Score();

    /**
     * Database Column Remarks:
     *   Primary key
     */
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source field: score.id")
    public static final SqlColumn<Long> id = score.id;

    /**
     * Database Column Remarks:
     *   Leaderboard ID
     */
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source field: score.leaderboard_rule_id")
    public static final SqlColumn<Long> leaderboardRuleId = score.leaderboardRuleId;

    /**
     * Database Column Remarks:
     *   Player ID
     */
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source field: score.user_id")
    public static final SqlColumn<Long> userId = score.userId;

    /**
     * Database Column Remarks:
     *   Match ID (from match module)
     */
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source field: score.match_id")
    public static final SqlColumn<Long> matchId = score.matchId;

    /**
     * Database Column Remarks:
     *   Score rule ID (from score_rule)
     */
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source field: score.rule_id")
    public static final SqlColumn<Long> ruleId = score.ruleId;

    /**
     * Database Column Remarks:
     *   Score change in this match
     */
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source field: score.score_change")
    public static final SqlColumn<Integer> scoreChange = score.scoreChange;

    /**
     * Database Column Remarks:
     *   Player total score after match
     */
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source field: score.final_score")
    public static final SqlColumn<Integer> finalScore = score.finalScore;

    /**
     * Database Column Remarks:
     *   Match result
     */
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source field: score.match_result")
    public static final SqlColumn<String> matchResult = score.matchResult;

    /**
     * Database Column Remarks:
     *   Creation time
     */
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source field: score.created_time")
    public static final SqlColumn<LocalDateTime> createdTime = score.createdTime;

    /**
     * Database Column Remarks:
     *   Update time
     */
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source field: score.updated_time")
    public static final SqlColumn<LocalDateTime> updatedTime = score.updatedTime;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: score")
    public static final class Score extends AliasableSqlTable<Score> {
        public final SqlColumn<Long> id = column("id", JDBCType.BIGINT);

        public final SqlColumn<Long> leaderboardRuleId = column("leaderboard_rule_id", JDBCType.BIGINT);

        public final SqlColumn<Long> userId = column("user_id", JDBCType.BIGINT);

        public final SqlColumn<Long> matchId = column("match_id", JDBCType.BIGINT);

        public final SqlColumn<Long> ruleId = column("rule_id", JDBCType.BIGINT);

        public final SqlColumn<Integer> scoreChange = column("score_change", JDBCType.INTEGER);

        public final SqlColumn<Integer> finalScore = column("final_score", JDBCType.INTEGER);

        public final SqlColumn<String> matchResult = column("match_result", JDBCType.CHAR);

        public final SqlColumn<LocalDateTime> createdTime = column("created_time", JDBCType.TIMESTAMP);

        public final SqlColumn<LocalDateTime> updatedTime = column("updated_time", JDBCType.TIMESTAMP);

        public Score() {
            super("score", Score::new);
        }
    }
}