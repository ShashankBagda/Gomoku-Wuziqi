package com.goody.nus.se.gomoku.ranking.model.dao;

import jakarta.annotation.Generated;
import java.sql.JDBCType;
import java.time.LocalDateTime;
import org.mybatis.dynamic.sql.AliasableSqlTable;
import org.mybatis.dynamic.sql.SqlColumn;

public final class RankingDynamicSqlSupport {
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: ranking")
    public static final Ranking ranking = new Ranking();

    /**
     * Database Column Remarks:
     *   Primary key
     */
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source field: ranking.id")
    public static final SqlColumn<Long> id = ranking.id;

    /**
     * Database Column Remarks:
     *   Player ID
     */
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source field: ranking.user_id")
    public static final SqlColumn<Long> userId = ranking.userId;

    /**
     * Database Column Remarks:
     *   Leaderboard ID
     */
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source field: ranking.leaderboard_rule_id")
    public static final SqlColumn<Long> leaderboardRuleId = ranking.leaderboardRuleId;

    /**
     * Database Column Remarks:
     *   Player total experience
     */
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source field: ranking.total_exp")
    public static final SqlColumn<Integer> totalExp = ranking.totalExp;

    /**
     * Database Column Remarks:
     *   Current level ID
     */
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source field: ranking.level_id")
    public static final SqlColumn<Long> levelId = ranking.levelId;

    /**
     * Database Column Remarks:
     *   Current total score (only changes in ranked mode)
     */
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source field: ranking.current_total_score")
    public static final SqlColumn<Integer> currentTotalScore = ranking.currentTotalScore;

    /**
     * Database Column Remarks:
     *   Current ranking position (refresh periodically)
     */
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source field: ranking.rank_position")
    public static final SqlColumn<Integer> rankPosition = ranking.rankPosition;

    /**
     * Database Column Remarks:
     *   Creation time
     */
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source field: ranking.created_time")
    public static final SqlColumn<LocalDateTime> createdTime = ranking.createdTime;

    /**
     * Database Column Remarks:
     *   Update time
     */
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source field: ranking.updated_time")
    public static final SqlColumn<LocalDateTime> updatedTime = ranking.updatedTime;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: ranking")
    public static final class Ranking extends AliasableSqlTable<Ranking> {
        public final SqlColumn<Long> id = column("id", JDBCType.BIGINT);

        public final SqlColumn<Long> userId = column("user_id", JDBCType.BIGINT);

        public final SqlColumn<Long> leaderboardRuleId = column("leaderboard_rule_id", JDBCType.BIGINT);

        public final SqlColumn<Integer> totalExp = column("total_exp", JDBCType.INTEGER);

        public final SqlColumn<Long> levelId = column("level_id", JDBCType.BIGINT);

        public final SqlColumn<Integer> currentTotalScore = column("current_total_score", JDBCType.INTEGER);

        public final SqlColumn<Integer> rankPosition = column("rank_position", JDBCType.INTEGER);

        public final SqlColumn<LocalDateTime> createdTime = column("created_time", JDBCType.TIMESTAMP);

        public final SqlColumn<LocalDateTime> updatedTime = column("updated_time", JDBCType.TIMESTAMP);

        public Ranking() {
            super("ranking", Ranking::new);
        }
    }
}