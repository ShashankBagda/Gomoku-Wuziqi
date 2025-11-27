package com.goody.nus.se.gomoku.ranking.model.dao;

import jakarta.annotation.Generated;
import java.sql.JDBCType;
import java.time.LocalDateTime;
import org.mybatis.dynamic.sql.AliasableSqlTable;
import org.mybatis.dynamic.sql.SqlColumn;

public final class LevelDynamicSqlSupport {
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: level")
    public static final Level level = new Level();

    /**
     * Database Column Remarks:
     *   Level ID
     */
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source field: level.id")
    public static final SqlColumn<Long> id = level.id;

    /**
     * Database Column Remarks:
     *   Experience required to reach the next level
     */
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source field: level.exp_required")
    public static final SqlColumn<Integer> expRequired = level.expRequired;

    /**
     * Database Column Remarks:
     *   Creation time
     */
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source field: level.created_time")
    public static final SqlColumn<LocalDateTime> createdTime = level.createdTime;

    /**
     * Database Column Remarks:
     *   Update time
     */
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source field: level.updated_time")
    public static final SqlColumn<LocalDateTime> updatedTime = level.updatedTime;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: level")
    public static final class Level extends AliasableSqlTable<Level> {
        public final SqlColumn<Long> id = column("id", JDBCType.BIGINT);

        public final SqlColumn<Integer> expRequired = column("exp_required", JDBCType.INTEGER);

        public final SqlColumn<LocalDateTime> createdTime = column("created_time", JDBCType.TIMESTAMP);

        public final SqlColumn<LocalDateTime> updatedTime = column("updated_time", JDBCType.TIMESTAMP);

        public Level() {
            super("level", Level::new);
        }
    }
}