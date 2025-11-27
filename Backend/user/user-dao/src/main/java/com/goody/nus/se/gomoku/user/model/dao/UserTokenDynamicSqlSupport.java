package com.goody.nus.se.gomoku.user.model.dao;

import jakarta.annotation.Generated;
import org.mybatis.dynamic.sql.AliasableSqlTable;
import org.mybatis.dynamic.sql.SqlColumn;

import java.sql.JDBCType;
import java.time.LocalDateTime;

public final class UserTokenDynamicSqlSupport {
    @Generated(value = "org.mybatis.generator.api.MyBatisGenerator", comments = "Source Table: user_token")
    public static final UserToken userToken = new UserToken();

    /**
     * Database Column Remarks:
     * Primary Key
     */
    @Generated(value = "org.mybatis.generator.api.MyBatisGenerator", comments = "Source field: user_token.id")
    public static final SqlColumn<Long> id = userToken.id;

    /**
     * Database Column Remarks:
     * User ID
     */
    @Generated(value = "org.mybatis.generator.api.MyBatisGenerator", comments = "Source field: user_token.user_id")
    public static final SqlColumn<Long> userId = userToken.userId;

    /**
     * Database Column Remarks:
     * Refresh Token (UUID)
     */
    @Generated(value = "org.mybatis.generator.api.MyBatisGenerator", comments = "Source field: user_token.refresh_token")
    public static final SqlColumn<String> refreshToken = userToken.refreshToken;

    /**
     * Database Column Remarks:
     * Expiration time
     */
    @Generated(value = "org.mybatis.generator.api.MyBatisGenerator", comments = "Source field: user_token.expires_at")
    public static final SqlColumn<LocalDateTime> expiresAt = userToken.expiresAt;

    /**
     * Database Column Remarks:
     * Creation time
     */
    @Generated(value = "org.mybatis.generator.api.MyBatisGenerator", comments = "Source field: user_token.created_at")
    public static final SqlColumn<LocalDateTime> createdAt = userToken.createdAt;

    /**
     * Database Column Remarks:
     * Update time
     */
    @Generated(value = "org.mybatis.generator.api.MyBatisGenerator", comments = "Source field: user_token.updated_at")
    public static final SqlColumn<LocalDateTime> updatedAt = userToken.updatedAt;

    @Generated(value = "org.mybatis.generator.api.MyBatisGenerator", comments = "Source Table: user_token")
    public static final class UserToken extends AliasableSqlTable<UserToken> {
        public final SqlColumn<Long> id = column("id", JDBCType.BIGINT);

        public final SqlColumn<Long> userId = column("user_id", JDBCType.BIGINT);

        public final SqlColumn<String> refreshToken = column("refresh_token", JDBCType.VARCHAR);

        public final SqlColumn<LocalDateTime> expiresAt = column("expires_at", JDBCType.TIMESTAMP);

        public final SqlColumn<LocalDateTime> createdAt = column("created_at", JDBCType.TIMESTAMP);

        public final SqlColumn<LocalDateTime> updatedAt = column("updated_at", JDBCType.TIMESTAMP);

        public UserToken() {
            super("user_token", UserToken::new);
        }
    }
}
