package com.goody.nus.se.gomoku.user.model.dao;

import com.goody.nus.se.gomoku.user.model.entity.UserToken;
import jakarta.annotation.Generated;
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

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static com.goody.nus.se.gomoku.user.model.dao.UserTokenDynamicSqlSupport.createdAt;
import static com.goody.nus.se.gomoku.user.model.dao.UserTokenDynamicSqlSupport.expiresAt;
import static com.goody.nus.se.gomoku.user.model.dao.UserTokenDynamicSqlSupport.id;
import static com.goody.nus.se.gomoku.user.model.dao.UserTokenDynamicSqlSupport.refreshToken;
import static com.goody.nus.se.gomoku.user.model.dao.UserTokenDynamicSqlSupport.updatedAt;
import static com.goody.nus.se.gomoku.user.model.dao.UserTokenDynamicSqlSupport.userId;
import static com.goody.nus.se.gomoku.user.model.dao.UserTokenDynamicSqlSupport.userToken;
import static org.mybatis.dynamic.sql.SqlBuilder.isEqualTo;

@Mapper
public interface UserTokenMapper extends CommonCountMapper, CommonDeleteMapper, CommonInsertMapper<UserToken>, CommonUpdateMapper {
    @Generated(value = "org.mybatis.generator.api.MyBatisGenerator", comments = "Source Table: user_token")
    BasicColumn[] selectList = BasicColumn.columnList(id, userId, refreshToken, expiresAt, createdAt, updatedAt);

    @Generated(value = "org.mybatis.generator.api.MyBatisGenerator", comments = "Source Table: user_token")
    static UpdateDSL<UpdateModel> updateAllColumns(UserToken row, UpdateDSL<UpdateModel> dsl) {
        return dsl.set(id).equalTo(row::getId)
                .set(userId).equalTo(row::getUserId)
                .set(refreshToken).equalTo(row::getRefreshToken)
                .set(expiresAt).equalTo(row::getExpiresAt)
                .set(createdAt).equalTo(row::getCreatedAt)
                .set(updatedAt).equalTo(row::getUpdatedAt);
    }

    @Generated(value = "org.mybatis.generator.api.MyBatisGenerator", comments = "Source Table: user_token")
    static UpdateDSL<UpdateModel> updateSelectiveColumns(UserToken row, UpdateDSL<UpdateModel> dsl) {
        return dsl.set(id).equalToWhenPresent(row::getId)
                .set(userId).equalToWhenPresent(row::getUserId)
                .set(refreshToken).equalToWhenPresent(row::getRefreshToken)
                .set(expiresAt).equalToWhenPresent(row::getExpiresAt)
                .set(createdAt).equalToWhenPresent(row::getCreatedAt)
                .set(updatedAt).equalToWhenPresent(row::getUpdatedAt);
    }

    @Generated(value = "org.mybatis.generator.api.MyBatisGenerator", comments = "Source Table: user_token")
    @SelectProvider(type = SqlProviderAdapter.class, method = "select")
    @Results(id = "UserTokenResult", value = {
            @Result(column = "id", property = "id", jdbcType = JdbcType.BIGINT, id = true),
            @Result(column = "user_id", property = "userId", jdbcType = JdbcType.BIGINT),
            @Result(column = "refresh_token", property = "refreshToken", jdbcType = JdbcType.VARCHAR),
            @Result(column = "expires_at", property = "expiresAt", jdbcType = JdbcType.TIMESTAMP),
            @Result(column = "created_at", property = "createdAt", jdbcType = JdbcType.TIMESTAMP),
            @Result(column = "updated_at", property = "updatedAt", jdbcType = JdbcType.TIMESTAMP)
    })
    List<UserToken> selectMany(SelectStatementProvider selectStatement);

    @Generated(value = "org.mybatis.generator.api.MyBatisGenerator", comments = "Source Table: user_token")
    @SelectProvider(type = SqlProviderAdapter.class, method = "select")
    @ResultMap("UserTokenResult")
    Optional<UserToken> selectOne(SelectStatementProvider selectStatement);

    @Generated(value = "org.mybatis.generator.api.MyBatisGenerator", comments = "Source Table: user_token")
    default long count(CountDSLCompleter completer) {
        return MyBatis3Utils.countFrom(this::count, userToken, completer);
    }

    @Generated(value = "org.mybatis.generator.api.MyBatisGenerator", comments = "Source Table: user_token")
    default int delete(DeleteDSLCompleter completer) {
        return MyBatis3Utils.deleteFrom(this::delete, userToken, completer);
    }

    @Generated(value = "org.mybatis.generator.api.MyBatisGenerator", comments = "Source Table: user_token")
    default int deleteByPrimaryKey(Long id_) {
        return delete(c ->
                c.where(id, isEqualTo(id_))
        );
    }

    @Generated(value = "org.mybatis.generator.api.MyBatisGenerator", comments = "Source Table: user_token")
    default int insert(UserToken row) {
        return MyBatis3Utils.insert(this::insert, row, userToken, c ->
                c.map(id).toProperty("id")
                        .map(userId).toProperty("userId")
                        .map(refreshToken).toProperty("refreshToken")
                        .map(expiresAt).toProperty("expiresAt")
                        .map(createdAt).toProperty("createdAt")
                        .map(updatedAt).toProperty("updatedAt")
        );
    }

    @Generated(value = "org.mybatis.generator.api.MyBatisGenerator", comments = "Source Table: user_token")
    default int insertMultiple(Collection<UserToken> records) {
        return MyBatis3Utils.insertMultiple(this::insertMultiple, records, userToken, c ->
                c.map(id).toProperty("id")
                        .map(userId).toProperty("userId")
                        .map(refreshToken).toProperty("refreshToken")
                        .map(expiresAt).toProperty("expiresAt")
                        .map(createdAt).toProperty("createdAt")
                        .map(updatedAt).toProperty("updatedAt")
        );
    }

    @Generated(value = "org.mybatis.generator.api.MyBatisGenerator", comments = "Source Table: user_token")
    default int insertSelective(UserToken row) {
        return MyBatis3Utils.insert(this::insert, row, userToken, c ->
                c.map(id).toPropertyWhenPresent("id", row::getId)
                        .map(userId).toPropertyWhenPresent("userId", row::getUserId)
                        .map(refreshToken).toPropertyWhenPresent("refreshToken", row::getRefreshToken)
                        .map(expiresAt).toPropertyWhenPresent("expiresAt", row::getExpiresAt)
                        .map(createdAt).toPropertyWhenPresent("createdAt", row::getCreatedAt)
                        .map(updatedAt).toPropertyWhenPresent("updatedAt", row::getUpdatedAt)
        );
    }

    @Generated(value = "org.mybatis.generator.api.MyBatisGenerator", comments = "Source Table: user_token")
    default Optional<UserToken> selectOne(SelectDSLCompleter completer) {
        return MyBatis3Utils.selectOne(this::selectOne, selectList, userToken, completer);
    }

    @Generated(value = "org.mybatis.generator.api.MyBatisGenerator", comments = "Source Table: user_token")
    default List<UserToken> select(SelectDSLCompleter completer) {
        return MyBatis3Utils.selectList(this::selectMany, selectList, userToken, completer);
    }

    @Generated(value = "org.mybatis.generator.api.MyBatisGenerator", comments = "Source Table: user_token")
    default List<UserToken> selectDistinct(SelectDSLCompleter completer) {
        return MyBatis3Utils.selectDistinct(this::selectMany, selectList, userToken, completer);
    }

    @Generated(value = "org.mybatis.generator.api.MyBatisGenerator", comments = "Source Table: user_token")
    default Optional<UserToken> selectByPrimaryKey(Long id_) {
        return selectOne(c ->
                c.where(id, isEqualTo(id_))
        );
    }

    @Generated(value = "org.mybatis.generator.api.MyBatisGenerator", comments = "Source Table: user_token")
    default int update(UpdateDSLCompleter completer) {
        return MyBatis3Utils.update(this::update, userToken, completer);
    }

    @Generated(value = "org.mybatis.generator.api.MyBatisGenerator", comments = "Source Table: user_token")
    default int updateByPrimaryKey(UserToken row) {
        return update(c ->
                c.set(userId).equalTo(row::getUserId)
                        .set(refreshToken).equalTo(row::getRefreshToken)
                        .set(expiresAt).equalTo(row::getExpiresAt)
                        .set(createdAt).equalTo(row::getCreatedAt)
                        .set(updatedAt).equalTo(row::getUpdatedAt)
                        .where(id, isEqualTo(row::getId))
        );
    }

    @Generated(value = "org.mybatis.generator.api.MyBatisGenerator", comments = "Source Table: user_token")
    default int updateByPrimaryKeySelective(UserToken row) {
        return update(c ->
                c.set(userId).equalToWhenPresent(row::getUserId)
                        .set(refreshToken).equalToWhenPresent(row::getRefreshToken)
                        .set(expiresAt).equalToWhenPresent(row::getExpiresAt)
                        .set(createdAt).equalToWhenPresent(row::getCreatedAt)
                        .set(updatedAt).equalToWhenPresent(row::getUpdatedAt)
                        .where(id, isEqualTo(row::getId))
        );
    }

    @Insert({"<script>" +
            " INSERT IGNORE INTO user_token" +
            " (`id`, `user_id`, `refresh_token`, `expires_at`, `created_at`, `updated_at`)" +
            " VALUES" +
            "(#{item.id}, #{item.userId}, #{item.refreshToken}, #{item.expiresAt}, #{item.createdAt}, #{item.updatedAt})" +
            "</script>"})
    void insertIgnoreCustom(@Param("item") UserToken record);

    @Insert({"<script>" +
            " INSERT IGNORE INTO user_token" +
            " (`id`, `user_id`, `refresh_token`, `expires_at`, `created_at`, `updated_at`)" +
            " VALUES" +
            "<foreach collection='items' item='item' separator=','>" +
            "(#{item.id}, #{item.userId}, #{item.refreshToken}, #{item.expiresAt}, #{item.createdAt}, #{item.updatedAt})" +
            "</foreach> " +
            "</script>"})
    void insertIgnoreBatchCustom(@Param("items") Collection<UserToken> records);

    @Insert({"<script>" +
            " INSERT INTO user_token" +
            "(`id`, `user_id`, `refresh_token`, `expires_at`, `created_at`, `updated_at`)" +
            " VALUES" +
            "(#{item.id}, #{item.userId}, #{item.refreshToken}, #{item.expiresAt}, #{item.createdAt}, #{item.updatedAt})" +
            " AS r" +
            " ON DUPLICATE KEY UPDATE" +
            "  user_id = r.user_id, refresh_token = r.refresh_token, expires_at = r.expires_at, created_at = r.created_at, updated_at = r.updated_at" +
            "</script>"})
    void insertOnDuplicateKeyCustom(@Param("item") UserToken record);

    @Insert({"<script>" +
            " INSERT INTO user_token" +
            " (`id`, `user_id`, `refresh_token`, `expires_at`, `created_at`, `updated_at`)" +
            " values" +
            "<foreach collection='items' item='item' separator=','>" +
            "(#{item.id}, #{item.userId}, #{item.refreshToken}, #{item.expiresAt}, #{item.createdAt}, #{item.updatedAt})" +
            "</foreach> " +
            " AS r" +
            " ON DUPLICATE KEY UPDATE" +
            "  user_id = r.user_id, refresh_token = r.refresh_token, expires_at = r.expires_at, created_at = r.created_at, updated_at = r.updated_at" +
            "</script>"})
    void insertOnDuplicateKeyBatchCustom(@Param("items") Collection<UserToken> records);

    @Insert({"<script>" +
            " REPLACE INTO user_token" +
            " (`id`, `user_id`, `refresh_token`, `expires_at`, `created_at`, `updated_at`)" +
            " VALUES" +
            "(#{item.id}, #{item.userId}, #{item.refreshToken}, #{item.expiresAt}, #{item.createdAt}, #{item.updatedAt})" +
            "</script>"})
    void replaceIntoCustom(@Param("item") UserToken record);

    @Insert({"<script>" +
            " REPLACE INTO user_token" +
            " (`id`, `user_id`, `refresh_token`, `expires_at`, `created_at`, `updated_at`)" +
            " VALUES" +
            "<foreach collection='items' item='item' separator=','>" +
            "(#{item.id}, #{item.userId}, #{item.refreshToken}, #{item.expiresAt}, #{item.createdAt}, #{item.updatedAt})" +
            "</foreach> " +
            "</script>"})
    void replaceIntoBatchCustom(@Param("items") Collection<UserToken> records);
}
