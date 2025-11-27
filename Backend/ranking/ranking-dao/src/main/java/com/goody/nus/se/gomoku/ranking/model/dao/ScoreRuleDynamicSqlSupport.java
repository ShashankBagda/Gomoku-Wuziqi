package com.goody.nus.se.gomoku.ranking.model.dao;

import jakarta.annotation.Generated;
import java.sql.JDBCType;
import java.time.LocalDateTime;
import org.mybatis.dynamic.sql.AliasableSqlTable;
import org.mybatis.dynamic.sql.SqlColumn;

public final class ScoreRuleDynamicSqlSupport {
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: score_rule")
    public static final ScoreRule scoreRule = new ScoreRule();

    /**
     * Database Column Remarks:
     *   Primary key
     */
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source field: score_rule.id")
    public static final SqlColumn<Long> id = scoreRule.id;

    /**
     * Database Column Remarks:
     *   Rule name (e.g., Standard, Season1, etc.)
     */
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source field: score_rule.rule_name")
    public static final SqlColumn<String> ruleName = scoreRule.ruleName;

    /**
     * Database Column Remarks:
     *   RANKED mode WIN score change
     */
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source field: score_rule.ranked_win_score")
    public static final SqlColumn<Integer> rankedWinScore = scoreRule.rankedWinScore;

    /**
     * Database Column Remarks:
     *   RANKED mode LOSE score change
     */
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source field: score_rule.ranked_lose_score")
    public static final SqlColumn<Integer> rankedLoseScore = scoreRule.rankedLoseScore;

    /**
     * Database Column Remarks:
     *   RANKED mode DRAW score change
     */
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source field: score_rule.ranked_draw_score")
    public static final SqlColumn<Integer> rankedDrawScore = scoreRule.rankedDrawScore;

    /**
     * Database Column Remarks:
     *   CASUAL mode WIN score change
     */
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source field: score_rule.casual_win_score")
    public static final SqlColumn<Integer> casualWinScore = scoreRule.casualWinScore;

    /**
     * Database Column Remarks:
     *   CASUAL mode LOSE score change
     */
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source field: score_rule.casual_lose_score")
    public static final SqlColumn<Integer> casualLoseScore = scoreRule.casualLoseScore;

    /**
     * Database Column Remarks:
     *   CASUAL mode DRAW score change
     */
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source field: score_rule.casual_draw_score")
    public static final SqlColumn<Integer> casualDrawScore = scoreRule.casualDrawScore;

    /**
     * Database Column Remarks:
     *   PRIVATE mode WIN score change
     */
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source field: score_rule.private_win_score")
    public static final SqlColumn<Integer> privateWinScore = scoreRule.privateWinScore;

    /**
     * Database Column Remarks:
     *   PRIVATE mode LOSE score change
     */
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source field: score_rule.private_lose_score")
    public static final SqlColumn<Integer> privateLoseScore = scoreRule.privateLoseScore;

    /**
     * Database Column Remarks:
     *   PRIVATE mode DRAW score change
     */
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source field: score_rule.private_draw_score")
    public static final SqlColumn<Integer> privateDrawScore = scoreRule.privateDrawScore;

    /**
     * Database Column Remarks:
     *   Rule description
     */
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source field: score_rule.description")
    public static final SqlColumn<String> description = scoreRule.description;

    /**
     * Database Column Remarks:
     *   Creation time
     */
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source field: score_rule.created_time")
    public static final SqlColumn<LocalDateTime> createdTime = scoreRule.createdTime;

    /**
     * Database Column Remarks:
     *   Update time
     */
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source field: score_rule.updated_time")
    public static final SqlColumn<LocalDateTime> updatedTime = scoreRule.updatedTime;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: score_rule")
    public static final class ScoreRule extends AliasableSqlTable<ScoreRule> {
        public final SqlColumn<Long> id = column("id", JDBCType.BIGINT);

        public final SqlColumn<String> ruleName = column("rule_name", JDBCType.VARCHAR);

        public final SqlColumn<Integer> rankedWinScore = column("ranked_win_score", JDBCType.INTEGER);

        public final SqlColumn<Integer> rankedLoseScore = column("ranked_lose_score", JDBCType.INTEGER);

        public final SqlColumn<Integer> rankedDrawScore = column("ranked_draw_score", JDBCType.INTEGER);

        public final SqlColumn<Integer> casualWinScore = column("casual_win_score", JDBCType.INTEGER);

        public final SqlColumn<Integer> casualLoseScore = column("casual_lose_score", JDBCType.INTEGER);

        public final SqlColumn<Integer> casualDrawScore = column("casual_draw_score", JDBCType.INTEGER);

        public final SqlColumn<Integer> privateWinScore = column("private_win_score", JDBCType.INTEGER);

        public final SqlColumn<Integer> privateLoseScore = column("private_lose_score", JDBCType.INTEGER);

        public final SqlColumn<Integer> privateDrawScore = column("private_draw_score", JDBCType.INTEGER);

        public final SqlColumn<String> description = column("description", JDBCType.VARCHAR);

        public final SqlColumn<LocalDateTime> createdTime = column("created_time", JDBCType.TIMESTAMP);

        public final SqlColumn<LocalDateTime> updatedTime = column("updated_time", JDBCType.TIMESTAMP);

        public ScoreRule() {
            super("score_rule", ScoreRule::new);
        }
    }
}