package com.goody.nus.se.gomoku.ranking.model.dao;

import jakarta.annotation.Generated;
import java.sql.JDBCType;
import java.time.LocalDateTime;
import org.mybatis.dynamic.sql.AliasableSqlTable;
import org.mybatis.dynamic.sql.SqlColumn;

public final class LeaderboardRuleDynamicSqlSupport {
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: leaderboard_rule")
    public static final LeaderboardRule leaderboardRule = new LeaderboardRule();

    /**
     * Database Column Remarks:
     *   Primary key
     */
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source field: leaderboard_rule.id")
    public static final SqlColumn<Long> id = leaderboardRule.id;

    /**
     * Database Column Remarks:
     *   Leaderboard start time (Unix timestamp)
     */
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source field: leaderboard_rule.start_time")
    public static final SqlColumn<Integer> startTime = leaderboardRule.startTime;

    /**
     * Database Column Remarks:
     *   Leaderboard end time (Unix timestamp)
     */
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source field: leaderboard_rule.end_time")
    public static final SqlColumn<Integer> endTime = leaderboardRule.endTime;

    /**
     * Database Column Remarks:
     *   Leaderboard type
     */
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source field: leaderboard_rule.type")
    public static final SqlColumn<String> type = leaderboardRule.type;

    /**
     * Database Column Remarks:
     *   Linked score rule ID
     */
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source field: leaderboard_rule.rule_id")
    public static final SqlColumn<Long> ruleId = leaderboardRule.ruleId;

    /**
     * Database Column Remarks:
     *   Leaderboard description
     */
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source field: leaderboard_rule.description")
    public static final SqlColumn<String> description = leaderboardRule.description;

    /**
     * Database Column Remarks:
     *   Creation time
     */
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source field: leaderboard_rule.created_time")
    public static final SqlColumn<LocalDateTime> createdTime = leaderboardRule.createdTime;

    /**
     * Database Column Remarks:
     *   Update time
     */
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source field: leaderboard_rule.updated_time")
    public static final SqlColumn<LocalDateTime> updatedTime = leaderboardRule.updatedTime;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: leaderboard_rule")
    public static final class LeaderboardRule extends AliasableSqlTable<LeaderboardRule> {
        public final SqlColumn<Long> id = column("id", JDBCType.BIGINT);

        public final SqlColumn<Integer> startTime = column("start_time", JDBCType.INTEGER);

        public final SqlColumn<Integer> endTime = column("end_time", JDBCType.INTEGER);

        public final SqlColumn<String> type = column("`type`", JDBCType.CHAR);

        public final SqlColumn<Long> ruleId = column("rule_id", JDBCType.BIGINT);

        public final SqlColumn<String> description = column("description", JDBCType.VARCHAR);

        public final SqlColumn<LocalDateTime> createdTime = column("created_time", JDBCType.TIMESTAMP);

        public final SqlColumn<LocalDateTime> updatedTime = column("updated_time", JDBCType.TIMESTAMP);

        public LeaderboardRule() {
            super("leaderboard_rule", LeaderboardRule::new);
        }
    }
}