package com.goody.nus.se.gomoku.user.model.dao;

import static com.goody.nus.se.gomoku.user.model.dao.UserDynamicSqlSupport.*;
import static org.mybatis.dynamic.sql.SqlBuilder.isEqualTo;

import com.goody.nus.se.gomoku.user.model.entity.User;
import jakarta.annotation.Generated;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.type.JdbcType;
import org.mybatis.dynamic.sql.BasicColumn;
import org.mybatis.dynamic.sql.delete.DeleteDSLCompleter;
import org.mybatis.dynamic.sql.select.CountDSLCompleter;
import org.mybatis.dynamic.sql.select.SelectDSLCompleter;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.update.UpdateDSL;
import org.mybatis.dynamic.sql.update.UpdateDSLCompleter;
import org.mybatis.dynamic.sql.update.UpdateModel;
import org.mybatis.dynamic.sql.util.SqlProviderAdapter;
import org.mybatis.dynamic.sql.util.mybatis3.CommonCountMapper;
import org.mybatis.dynamic.sql.util.mybatis3.CommonDeleteMapper;
import org.mybatis.dynamic.sql.util.mybatis3.CommonInsertMapper;
import org.mybatis.dynamic.sql.util.mybatis3.CommonUpdateMapper;
import org.mybatis.dynamic.sql.util.mybatis3.MyBatis3Utils;

@Mapper
public interface UserMapper extends CommonCountMapper, CommonDeleteMapper, CommonInsertMapper<User>, CommonUpdateMapper {
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: user")
    BasicColumn[] selectList = BasicColumn.columnList(id, email, nickname, passwordHash, passwordSalt, avatarUrl, country, gender, status, createdAt, updatedAt, avatarBase64);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: user")
    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    @Results(id="UserResult", value = {
        @Result(column="id", property="id", jdbcType=JdbcType.BIGINT, id=true),
        @Result(column="email", property="email", jdbcType=JdbcType.VARCHAR),
        @Result(column="nickname", property="nickname", jdbcType=JdbcType.VARCHAR),
        @Result(column="password_hash", property="passwordHash", jdbcType=JdbcType.CHAR),
        @Result(column="password_salt", property="passwordSalt", jdbcType=JdbcType.CHAR),
        @Result(column="avatar_url", property="avatarUrl", jdbcType=JdbcType.VARCHAR),
        @Result(column="country", property="country", jdbcType=JdbcType.VARCHAR),
        @Result(column="gender", property="gender", jdbcType=JdbcType.TINYINT),
        @Result(column="status", property="status", jdbcType=JdbcType.TINYINT),
        @Result(column="created_at", property="createdAt", jdbcType=JdbcType.TIMESTAMP),
        @Result(column="updated_at", property="updatedAt", jdbcType=JdbcType.TIMESTAMP),
        @Result(column="avatar_base64", property="avatarBase64", jdbcType=JdbcType.LONGVARCHAR)
    })
    List<User> selectMany(SelectStatementProvider selectStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: user")
    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    @ResultMap("UserResult")
    Optional<User> selectOne(SelectStatementProvider selectStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: user")
    default long count(CountDSLCompleter completer) {
        return MyBatis3Utils.countFrom(this::count, user, completer);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: user")
    default int delete(DeleteDSLCompleter completer) {
        return MyBatis3Utils.deleteFrom(this::delete, user, completer);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: user")
    default int deleteByPrimaryKey(Long id_) {
        return delete(c -> 
            c.where(id, isEqualTo(id_))
        );
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: user")
    default int insert(User row) {
        return MyBatis3Utils.insert(this::insert, row, user, c ->
            c.map(id).toProperty("id")
            .map(email).toProperty("email")
            .map(nickname).toProperty("nickname")
            .map(passwordHash).toProperty("passwordHash")
            .map(passwordSalt).toProperty("passwordSalt")
            .map(avatarUrl).toProperty("avatarUrl")
            .map(country).toProperty("country")
            .map(gender).toProperty("gender")
            .map(status).toProperty("status")
            .map(createdAt).toProperty("createdAt")
            .map(updatedAt).toProperty("updatedAt")
            .map(avatarBase64).toProperty("avatarBase64")
        );
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: user")
    default int insertMultiple(Collection<User> records) {
        return MyBatis3Utils.insertMultiple(this::insertMultiple, records, user, c ->
            c.map(id).toProperty("id")
            .map(email).toProperty("email")
            .map(nickname).toProperty("nickname")
            .map(passwordHash).toProperty("passwordHash")
            .map(passwordSalt).toProperty("passwordSalt")
            .map(avatarUrl).toProperty("avatarUrl")
            .map(country).toProperty("country")
            .map(gender).toProperty("gender")
            .map(status).toProperty("status")
            .map(createdAt).toProperty("createdAt")
            .map(updatedAt).toProperty("updatedAt")
            .map(avatarBase64).toProperty("avatarBase64")
        );
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: user")
    default int insertSelective(User row) {
        return MyBatis3Utils.insert(this::insert, row, user, c ->
            c.map(id).toPropertyWhenPresent("id", row::getId)
            .map(email).toPropertyWhenPresent("email", row::getEmail)
            .map(nickname).toPropertyWhenPresent("nickname", row::getNickname)
            .map(passwordHash).toPropertyWhenPresent("passwordHash", row::getPasswordHash)
            .map(passwordSalt).toPropertyWhenPresent("passwordSalt", row::getPasswordSalt)
            .map(avatarUrl).toPropertyWhenPresent("avatarUrl", row::getAvatarUrl)
            .map(country).toPropertyWhenPresent("country", row::getCountry)
            .map(gender).toPropertyWhenPresent("gender", row::getGender)
            .map(status).toPropertyWhenPresent("status", row::getStatus)
            .map(createdAt).toPropertyWhenPresent("createdAt", row::getCreatedAt)
            .map(updatedAt).toPropertyWhenPresent("updatedAt", row::getUpdatedAt)
            .map(avatarBase64).toPropertyWhenPresent("avatarBase64", row::getAvatarBase64)
        );
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: user")
    default Optional<User> selectOne(SelectDSLCompleter completer) {
        return MyBatis3Utils.selectOne(this::selectOne, selectList, user, completer);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: user")
    default List<User> select(SelectDSLCompleter completer) {
        return MyBatis3Utils.selectList(this::selectMany, selectList, user, completer);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: user")
    default List<User> selectDistinct(SelectDSLCompleter completer) {
        return MyBatis3Utils.selectDistinct(this::selectMany, selectList, user, completer);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: user")
    default Optional<User> selectByPrimaryKey(Long id_) {
        return selectOne(c ->
            c.where(id, isEqualTo(id_))
        );
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: user")
    default int update(UpdateDSLCompleter completer) {
        return MyBatis3Utils.update(this::update, user, completer);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: user")
    static UpdateDSL<UpdateModel> updateAllColumns(User row, UpdateDSL<UpdateModel> dsl) {
        return dsl.set(id).equalTo(row::getId)
                .set(email).equalTo(row::getEmail)
                .set(nickname).equalTo(row::getNickname)
                .set(passwordHash).equalTo(row::getPasswordHash)
                .set(passwordSalt).equalTo(row::getPasswordSalt)
                .set(avatarUrl).equalTo(row::getAvatarUrl)
                .set(country).equalTo(row::getCountry)
                .set(gender).equalTo(row::getGender)
                .set(status).equalTo(row::getStatus)
                .set(createdAt).equalTo(row::getCreatedAt)
                .set(updatedAt).equalTo(row::getUpdatedAt)
                .set(avatarBase64).equalTo(row::getAvatarBase64);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: user")
    static UpdateDSL<UpdateModel> updateSelectiveColumns(User row, UpdateDSL<UpdateModel> dsl) {
        return dsl.set(id).equalToWhenPresent(row::getId)
                .set(email).equalToWhenPresent(row::getEmail)
                .set(nickname).equalToWhenPresent(row::getNickname)
                .set(passwordHash).equalToWhenPresent(row::getPasswordHash)
                .set(passwordSalt).equalToWhenPresent(row::getPasswordSalt)
                .set(avatarUrl).equalToWhenPresent(row::getAvatarUrl)
                .set(country).equalToWhenPresent(row::getCountry)
                .set(gender).equalToWhenPresent(row::getGender)
                .set(status).equalToWhenPresent(row::getStatus)
                .set(createdAt).equalToWhenPresent(row::getCreatedAt)
                .set(updatedAt).equalToWhenPresent(row::getUpdatedAt)
                .set(avatarBase64).equalToWhenPresent(row::getAvatarBase64);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: user")
    default int updateByPrimaryKey(User row) {
        return update(c ->
            c.set(email).equalTo(row::getEmail)
            .set(nickname).equalTo(row::getNickname)
            .set(passwordHash).equalTo(row::getPasswordHash)
            .set(passwordSalt).equalTo(row::getPasswordSalt)
            .set(avatarUrl).equalTo(row::getAvatarUrl)
            .set(country).equalTo(row::getCountry)
            .set(gender).equalTo(row::getGender)
            .set(status).equalTo(row::getStatus)
            .set(createdAt).equalTo(row::getCreatedAt)
            .set(updatedAt).equalTo(row::getUpdatedAt)
            .set(avatarBase64).equalTo(row::getAvatarBase64)
            .where(id, isEqualTo(row::getId))
        );
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", comments="Source Table: user")
    default int updateByPrimaryKeySelective(User row) {
        return update(c ->
            c.set(email).equalToWhenPresent(row::getEmail)
            .set(nickname).equalToWhenPresent(row::getNickname)
            .set(passwordHash).equalToWhenPresent(row::getPasswordHash)
            .set(passwordSalt).equalToWhenPresent(row::getPasswordSalt)
            .set(avatarUrl).equalToWhenPresent(row::getAvatarUrl)
            .set(country).equalToWhenPresent(row::getCountry)
            .set(gender).equalToWhenPresent(row::getGender)
            .set(status).equalToWhenPresent(row::getStatus)
            .set(createdAt).equalToWhenPresent(row::getCreatedAt)
            .set(updatedAt).equalToWhenPresent(row::getUpdatedAt)
            .set(avatarBase64).equalToWhenPresent(row::getAvatarBase64)
            .where(id, isEqualTo(row::getId))
        );
    }

    @Insert({"<script>" +
            " INSERT IGNORE INTO user" +
              " (`id`, `email`, `nickname`, `password_hash`, `password_salt`, `avatar_url`, `country`, `gender`, `status`, `created_at`, `updated_at`, `avatar_base64`)" +
            " VALUES" +
              "(#{item.id}, #{item.email}, #{item.nickname}, #{item.passwordHash}, #{item.passwordSalt}, #{item.avatarUrl}, #{item.country}, #{item.gender}, #{item.status}, #{item.createdAt}, #{item.updatedAt}, #{item.avatarBase64})" +
        "</script>"})
    void insertIgnoreCustom(@Param("item") User record);

    @Insert({"<script>" +
            " INSERT IGNORE INTO user" +
            " (`id`, `email`, `nickname`, `password_hash`, `password_salt`, `avatar_url`, `country`, `gender`, `status`, `created_at`, `updated_at`, `avatar_base64`)" +
            " VALUES" +
            "<foreach collection='items' item='item' separator=','>" +
               "(#{item.id}, #{item.email}, #{item.nickname}, #{item.passwordHash}, #{item.passwordSalt}, #{item.avatarUrl}, #{item.country}, #{item.gender}, #{item.status}, #{item.createdAt}, #{item.updatedAt}, #{item.avatarBase64})" +
            "</foreach> " +
        "</script>"})
    void insertIgnoreBatchCustom(@Param("items") Collection<User> records);

    @Insert({"<script>" +
            " INSERT INTO user" +
              "(`id`, `email`, `nickname`, `password_hash`, `password_salt`, `avatar_url`, `country`, `gender`, `status`, `created_at`, `updated_at`, `avatar_base64`)" +
            " VALUES" +
              "(#{item.id}, #{item.email}, #{item.nickname}, #{item.passwordHash}, #{item.passwordSalt}, #{item.avatarUrl}, #{item.country}, #{item.gender}, #{item.status}, #{item.createdAt}, #{item.updatedAt}, #{item.avatarBase64})" +
            " AS r" +
            " ON DUPLICATE KEY UPDATE" +
            "  email = r.email, nickname = r.nickname, password_hash = r.password_hash, password_salt = r.password_salt, avatar_url = r.avatar_url, country = r.country, gender = r.gender, status = r.status, created_at = r.created_at, updated_at = r.updated_at" +
        "</script>"})
    void insertOnDuplicateKeyCustom(@Param("item") User record);

    @Insert({"<script>" +
            " INSERT INTO user" +
            " (`id`, `email`, `nickname`, `password_hash`, `password_salt`, `avatar_url`, `country`, `gender`, `status`, `created_at`, `updated_at`, `avatar_base64`)" +
            " values" +
            "<foreach collection='items' item='item' separator=','>" +
               "(#{item.id}, #{item.email}, #{item.nickname}, #{item.passwordHash}, #{item.passwordSalt}, #{item.avatarUrl}, #{item.country}, #{item.gender}, #{item.status}, #{item.createdAt}, #{item.updatedAt}, #{item.avatarBase64})" +
            "</foreach> " +
            " AS r" +
            " ON DUPLICATE KEY UPDATE" +
            "  email = r.email, nickname = r.nickname, password_hash = r.password_hash, password_salt = r.password_salt, avatar_url = r.avatar_url, country = r.country, gender = r.gender, status = r.status, created_at = r.created_at, updated_at = r.updated_at" +
        "</script>"})
    void insertOnDuplicateKeyBatchCustom(@Param("items") Collection<User> records);

    @Insert({"<script>" +
            " REPLACE INTO user" +
              " (`id`, `email`, `nickname`, `password_hash`, `password_salt`, `avatar_url`, `country`, `gender`, `status`, `created_at`, `updated_at`, `avatar_base64`)" +
            " VALUES" +
              "(#{item.id}, #{item.email}, #{item.nickname}, #{item.passwordHash}, #{item.passwordSalt}, #{item.avatarUrl}, #{item.country}, #{item.gender}, #{item.status}, #{item.createdAt}, #{item.updatedAt}, #{item.avatarBase64})" +
        "</script>"})
    void replaceIntoCustom(@Param("item") User record);

    @Insert({"<script>" +
            " REPLACE INTO user" +
            " (`id`, `email`, `nickname`, `password_hash`, `password_salt`, `avatar_url`, `country`, `gender`, `status`, `created_at`, `updated_at`, `avatar_base64`)" +
            " VALUES" +
            "<foreach collection='items' item='item' separator=','>" +
               "(#{item.id}, #{item.email}, #{item.nickname}, #{item.passwordHash}, #{item.passwordSalt}, #{item.avatarUrl}, #{item.country}, #{item.gender}, #{item.status}, #{item.createdAt}, #{item.updatedAt}, #{item.avatarBase64})" +
            "</foreach> " +
        "</script>"})
    void replaceIntoBatchCustom(@Param("items") Collection<User> records);
}