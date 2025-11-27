package com.goody.nus.se.gomoku.user.model.dao;

import jakarta.annotation.Generated;
import java.sql.JDBCType;
import java.time.LocalDateTime;
import org.mybatis.dynamic.sql.AliasableSqlTable;
import org.mybatis.dynamic.sql.SqlColumn;

public final class UserDynamicSqlSupport {
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: user")
    public static final User user = new User();

    /**
     * Database Column Remarks:
     *   User ID
     */
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source field: user.id")
    public static final SqlColumn<Long> id = user.id;

    /**
     * Database Column Remarks:
     *   Bound email address
     */
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source field: user.email")
    public static final SqlColumn<String> email = user.email;

    /**
     * Database Column Remarks:
     *   User nickname
     */
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source field: user.nickname")
    public static final SqlColumn<String> nickname = user.nickname;

    /**
     * Database Column Remarks:
     *   BCrypt password hash
     */
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source field: user.password_hash")
    public static final SqlColumn<String> passwordHash = user.passwordHash;

    /**
     * Database Column Remarks:
     *   UUID-based password salt
     */
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source field: user.password_salt")
    public static final SqlColumn<String> passwordSalt = user.passwordSalt;

    /**
     * Database Column Remarks:
     *   Avatar URL
     */
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source field: user.avatar_url")
    public static final SqlColumn<String> avatarUrl = user.avatarUrl;

    /**
     * Database Column Remarks:
     *   Country
     */
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source field: user.country")
    public static final SqlColumn<String> country = user.country;

    /**
     * Database Column Remarks:
     *   Gender (0=unknown, 1=male, 2=female)
     */
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source field: user.gender")
    public static final SqlColumn<Byte> gender = user.gender;

    /**
     * Database Column Remarks:
     *   Account status (0=inactive, 1=active, 2=disabled, 3=deleted)
     */
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source field: user.status")
    public static final SqlColumn<Byte> status = user.status;

    /**
     * Database Column Remarks:
     *   Creation time
     */
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source field: user.created_at")
    public static final SqlColumn<LocalDateTime> createdAt = user.createdAt;

    /**
     * Database Column Remarks:
     *   Update time
     */
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source field: user.updated_at")
    public static final SqlColumn<LocalDateTime> updatedAt = user.updatedAt;

    /**
     * Database Column Remarks:
     *   Avatar base64
     */
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source field: user.avatar_base64")
    public static final SqlColumn<String> avatarBase64 = user.avatarBase64;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: user")
    public static final class User extends AliasableSqlTable<User> {
        public final SqlColumn<Long> id = column("id", JDBCType.BIGINT);

        public final SqlColumn<String> email = column("email", JDBCType.VARCHAR);

        public final SqlColumn<String> nickname = column("nickname", JDBCType.VARCHAR);

        public final SqlColumn<String> passwordHash = column("password_hash", JDBCType.CHAR);

        public final SqlColumn<String> passwordSalt = column("password_salt", JDBCType.CHAR);

        public final SqlColumn<String> avatarUrl = column("avatar_url", JDBCType.VARCHAR);

        public final SqlColumn<String> country = column("country", JDBCType.VARCHAR);

        public final SqlColumn<Byte> gender = column("gender", JDBCType.TINYINT);

        public final SqlColumn<Byte> status = column("`status`", JDBCType.TINYINT);

        public final SqlColumn<LocalDateTime> createdAt = column("created_at", JDBCType.TIMESTAMP);

        public final SqlColumn<LocalDateTime> updatedAt = column("updated_at", JDBCType.TIMESTAMP);

        public final SqlColumn<String> avatarBase64 = column("avatar_base64", JDBCType.LONGVARCHAR);

        public User() {
            super("user", User::new);
        }
    }
}