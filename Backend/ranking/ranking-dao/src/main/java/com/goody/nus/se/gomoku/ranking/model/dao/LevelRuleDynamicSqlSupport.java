package com.goody.nus.se.gomoku.ranking.model.dao;

import jakarta.annotation.Generated;
import java.sql.JDBCType;
import java.time.LocalDateTime;
import org.mybatis.dynamic.sql.AliasableSqlTable;
import org.mybatis.dynamic.sql.SqlColumn;

public final class LevelRuleDynamicSqlSupport {
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: level_rule")
    public static final LevelRule levelRule = new LevelRule();

    /**
     * Database Column Remarks:
     *   Primary key
     */
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source field: level_rule.id")
    public static final SqlColumn<Long> id = levelRule.id;

    /**
     * Database Column Remarks:
     *   Game mode type
     */
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source field: level_rule.mode_type")
    public static final SqlColumn<String> modeType = levelRule.modeType;

    /**
     * Database Column Remarks:
     *   Match result
     */
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source field: level_rule.match_result")
    public static final SqlColumn<String> matchResult = levelRule.matchResult;

    /**
     * Database Column Remarks:
     *   Experience value change
     */
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source field: level_rule.exp_value")
    public static final SqlColumn<Integer> expValue = levelRule.expValue;

    /**
     * Database Column Remarks:
     *   Rule description
     */
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source field: level_rule.description")
    public static final SqlColumn<String> description = levelRule.description;

    /**
     * Database Column Remarks:
     *   Creation time
     */
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source field: level_rule.created_time")
    public static final SqlColumn<LocalDateTime> createdTime = levelRule.createdTime;

    /**
     * Database Column Remarks:
     *   Update time
     */
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source field: level_rule.updated_time")
    public static final SqlColumn<LocalDateTime> updatedTime = levelRule.updatedTime;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: level_rule")
    public static final class LevelRule extends AliasableSqlTable<LevelRule> {
        public final SqlColumn<Long> id = column("id", JDBCType.BIGINT);

        public final SqlColumn<String> modeType = column("mode_type", JDBCType.CHAR);

        public final SqlColumn<String> matchResult = column("match_result", JDBCType.CHAR);

        public final SqlColumn<Integer> expValue = column("exp_value", JDBCType.INTEGER);

        public final SqlColumn<String> description = column("description", JDBCType.VARCHAR);

        public final SqlColumn<LocalDateTime> createdTime = column("created_time", JDBCType.TIMESTAMP);

        public final SqlColumn<LocalDateTime> updatedTime = column("updated_time", JDBCType.TIMESTAMP);

        public LevelRule() {
            super("level_rule", LevelRule::new);
        }
    }
}