package com.goody.nus.se.gomoku.user.service.impl;

import com.goody.nus.se.gomoku.user.model.dao.UserTokenMapper;
import com.goody.nus.se.gomoku.user.model.dao.customer.CustomerUserTokenMapper;
import com.goody.nus.se.gomoku.user.model.dto.UserTokenDTO;
import com.goody.nus.se.gomoku.user.model.entity.UserToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mybatis.dynamic.sql.select.SelectDSLCompleter;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link UserTokenServiceImpl}
 * Tests all branches including CRUD operations and custom queries
 */
@ExtendWith(MockitoExtension.class)
class UserTokenServiceImplTest {

    @Mock
    private UserTokenMapper userTokenMapper;

    @Mock
    private CustomerUserTokenMapper customerUserTokenMapper;

    @InjectMocks
    private UserTokenServiceImpl userTokenService;

    private UserTokenDTO testTokenDTO;
    private UserToken testTokenEntity;

    @BeforeEach
    void setUp() {
        testTokenDTO = UserTokenDTO.builder()
                .id(1L)
                .userId(100L)
                .refreshToken("test-refresh-token-uuid")
                .expiresAt(LocalDateTime.now().plusDays(7))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testTokenEntity = testTokenDTO.toEntity();
    }

    // ==================== save() Tests ====================

    @Test
    void save_WithNewToken_Success() {
        // Given
        UserTokenDTO newToken = UserTokenDTO.builder()
                .userId(200L)
                .refreshToken("new-refresh-token")
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        when(userTokenMapper.insertSelective(any(UserToken.class))).thenAnswer(invocation -> {
            UserToken entity = invocation.getArgument(0);
            entity.setId(123L);
            return 1;
        });

        // When
        Long savedId = userTokenService.save(newToken);

        // Then
        assertNotNull(savedId);
        assertEquals(123L, savedId);
        verify(userTokenMapper).insertSelective(any(UserToken.class));
    }

    @Test
    void save_WithExistingId_Success() {
        // Given - token with ID already set
        when(userTokenMapper.insertSelective(any(UserToken.class))).thenReturn(1);

        // When
        Long savedId = userTokenService.save(testTokenDTO);

        // Then
        assertNotNull(savedId);
        assertEquals(testTokenDTO.getId(), savedId);
        verify(userTokenMapper).insertSelective(any(UserToken.class));
    }

    @Test
    void save_WithNullToken_ReturnsNull() {
        // When
        Long result = userTokenService.save(null);

        // Then
        assertNull(result);
        verify(userTokenMapper, never()).insertSelective(any());
    }

    @Test
    void save_WhenInsertFails_ReturnsNull() {
        // Given
        when(userTokenMapper.insertSelective(any(UserToken.class))).thenReturn(0);

        // When
        Long result = userTokenService.save(testTokenDTO);

        // Then
        assertNull(result);
        verify(userTokenMapper).insertSelective(any(UserToken.class));
    }

    @Test
    void save_GeneratesIdWhenNull() {
        // Given - token without ID
        UserTokenDTO tokenWithoutId = UserTokenDTO.builder()
                .userId(300L)
                .refreshToken("token-without-id")
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        when(userTokenMapper.insertSelective(any(UserToken.class))).thenAnswer(invocation -> {
            UserToken entity = invocation.getArgument(0);
            assertNotNull(entity.getId()); // ID should be generated
            return 1;
        });

        // When
        Long savedId = userTokenService.save(tokenWithoutId);

        // Then
        assertNotNull(savedId);
        assertNotNull(tokenWithoutId.getId()); // ID should be set on DTO
    }

    // ==================== saveBatch() Tests ====================

    @Test
    void saveBatch_WithValidList_Success() {
        // Given
        UserTokenDTO token1 = UserTokenDTO.builder()
                .userId(100L)
                .refreshToken("token1")
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        UserTokenDTO token2 = UserTokenDTO.builder()
                .userId(200L)
                .refreshToken("token2")
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        List<UserTokenDTO> tokenList = Arrays.asList(token1, token2);

        when(userTokenMapper.insertMultiple(anyList())).thenReturn(2);

        // When
        int result = userTokenService.saveBatch(tokenList);

        // Then
        assertEquals(2, result);
        verify(userTokenMapper).insertMultiple(anyList());
        assertNotNull(token1.getId());
        assertNotNull(token2.getId());
        assertNotNull(token1.getCreatedAt());
        assertNotNull(token1.getUpdatedAt());
    }

    @Test
    void saveBatch_WithNullList_ReturnsZero() {
        // When
        int result = userTokenService.saveBatch(null);

        // Then
        assertEquals(0, result);
        verify(userTokenMapper, never()).insertMultiple(anyList());
    }

    @Test
    void saveBatch_WithEmptyList_ReturnsZero() {
        // When
        int result = userTokenService.saveBatch(Collections.emptyList());

        // Then
        assertEquals(0, result);
        verify(userTokenMapper, never()).insertMultiple(anyList());
    }

    @Test
    void saveBatch_WithNullElements_FiltersAndSaves() {
        // Given - list with null element
        UserTokenDTO validToken = UserTokenDTO.builder()
                .userId(100L)
                .refreshToken("valid-token")
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        List<UserTokenDTO> tokenList = Arrays.asList(validToken, null);

        when(userTokenMapper.insertMultiple(anyList())).thenReturn(1);

        // When
        int result = userTokenService.saveBatch(tokenList);

        // Then
        assertEquals(1, result);
    }

    @Test
    void saveBatch_WithAllNullElements_ReturnsZero() {
        // Given
        List<UserTokenDTO> tokenList = Arrays.asList(null, null);

        // When
        int result = userTokenService.saveBatch(tokenList);

        // Then
        assertEquals(0, result);
        verify(userTokenMapper, never()).insertMultiple(anyList());
    }

    @Test
    void saveBatch_SetsTimestampsWhenNull() {
        // Given - token without timestamps
        UserTokenDTO token = UserTokenDTO.builder()
                .userId(100L)
                .refreshToken("token")
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        when(userTokenMapper.insertMultiple(anyList())).thenReturn(1);

        // When
        userTokenService.saveBatch(Arrays.asList(token));

        // Then
        assertNotNull(token.getCreatedAt());
        assertNotNull(token.getUpdatedAt());
    }

    @Test
    void saveBatch_PreservesExistingTimestamps() {
        // Given - token with existing timestamps
        LocalDateTime existingCreatedAt = LocalDateTime.now().minusDays(1);
        LocalDateTime existingUpdatedAt = LocalDateTime.now().minusHours(1);

        UserTokenDTO token = UserTokenDTO.builder()
                .userId(100L)
                .refreshToken("token")
                .expiresAt(LocalDateTime.now().plusDays(7))
                .createdAt(existingCreatedAt)
                .updatedAt(existingUpdatedAt)
                .build();

        when(userTokenMapper.insertMultiple(anyList())).thenReturn(1);

        // When
        userTokenService.saveBatch(Arrays.asList(token));

        // Then
        assertEquals(existingCreatedAt, token.getCreatedAt());
        assertEquals(existingUpdatedAt, token.getUpdatedAt());
    }

    // ==================== update() Tests ====================

    @Test
    void update_WithValidToken_Success() {
        // Given
        when(userTokenMapper.updateByPrimaryKeySelective(any(UserToken.class))).thenReturn(1);

        // When
        int result = userTokenService.update(testTokenDTO);

        // Then
        assertEquals(1, result);
        verify(userTokenMapper).updateByPrimaryKeySelective(any(UserToken.class));
    }

    @Test
    void update_WithNullToken_ReturnsZero() {
        // When
        int result = userTokenService.update(null);

        // Then
        assertEquals(0, result);
        verify(userTokenMapper, never()).updateByPrimaryKeySelective(any());
    }

    @Test
    void update_WhenUpdateFails_ReturnsZero() {
        // Given
        when(userTokenMapper.updateByPrimaryKeySelective(any(UserToken.class))).thenReturn(0);

        // When
        int result = userTokenService.update(testTokenDTO);

        // Then
        assertEquals(0, result);
        verify(userTokenMapper).updateByPrimaryKeySelective(any(UserToken.class));
    }

    // ==================== deleteById() Tests ====================

    @Test
    void deleteById_WithValidId_Success() {
        // Given
        when(userTokenMapper.deleteByPrimaryKey(1L)).thenReturn(1);

        // When
        int result = userTokenService.deleteById(1L);

        // Then
        assertEquals(1, result);
        verify(userTokenMapper).deleteByPrimaryKey(1L);
    }

    @Test
    void deleteById_WithNullId_ReturnsZero() {
        // When
        int result = userTokenService.deleteById(null);

        // Then
        assertEquals(0, result);
        verify(userTokenMapper, never()).deleteByPrimaryKey(any());
    }

    @Test
    void deleteById_WhenDeleteFails_ReturnsZero() {
        // Given
        when(userTokenMapper.deleteByPrimaryKey(1L)).thenReturn(0);

        // When
        int result = userTokenService.deleteById(1L);

        // Then
        assertEquals(0, result);
        verify(userTokenMapper).deleteByPrimaryKey(1L);
    }

    // ==================== findById() Tests ====================

    @Test
    void findById_WithValidId_ReturnsToken() {
        // Given
        when(userTokenMapper.selectByPrimaryKey(1L)).thenReturn(Optional.of(testTokenEntity));

        // When
        UserTokenDTO result = userTokenService.findById(1L);

        // Then
        assertNotNull(result);
        assertEquals(testTokenDTO.getId(), result.getId());
        assertEquals(testTokenDTO.getUserId(), result.getUserId());
        assertEquals(testTokenDTO.getRefreshToken(), result.getRefreshToken());
        verify(userTokenMapper).selectByPrimaryKey(1L);
    }

    @Test
    void findById_WithNullId_ReturnsNull() {
        // When
        UserTokenDTO result = userTokenService.findById(null);

        // Then
        assertNull(result);
        verify(userTokenMapper, never()).selectByPrimaryKey(any());
    }

    @Test
    void findById_WhenTokenNotFound_ReturnsNull() {
        // Given
        when(userTokenMapper.selectByPrimaryKey(999L)).thenReturn(Optional.empty());

        // When
        UserTokenDTO result = userTokenService.findById(999L);

        // Then
        assertNull(result);
        verify(userTokenMapper).selectByPrimaryKey(999L);
    }

    // ==================== findAll() Tests ====================

    @Test
    void findAll_ReturnsAllTokens() {
        // Given
        UserToken token1 = testTokenEntity;
        UserToken token2 = UserTokenDTO.builder()
                .id(2L)
                .userId(200L)
                .refreshToken("token2")
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build().toEntity();

        when(userTokenMapper.select(any(SelectDSLCompleter.class)))
                .thenReturn(Arrays.asList(token1, token2));

        // When
        List<UserTokenDTO> result = userTokenService.findAll();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(userTokenMapper).select(any(SelectDSLCompleter.class));
    }

    @Test
    void findAll_WhenNoTokens_ReturnsEmptyList() {
        // Given
        when(userTokenMapper.select(any(SelectDSLCompleter.class)))
                .thenReturn(Collections.emptyList());

        // When
        List<UserTokenDTO> result = userTokenService.findAll();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userTokenMapper).select(any(SelectDSLCompleter.class));
    }

    // ==================== findByUserId() Tests ====================

    @Test
    void findByUserId_WithValidUserId_ReturnsToken() {
        // Given
        when(customerUserTokenMapper.selectByUserId(100L)).thenReturn(testTokenEntity);

        // When
        UserTokenDTO result = userTokenService.findByUserId(100L);

        // Then
        assertNotNull(result);
        assertEquals(testTokenDTO.getUserId(), result.getUserId());
        assertEquals(testTokenDTO.getRefreshToken(), result.getRefreshToken());
        verify(customerUserTokenMapper).selectByUserId(100L);
    }

    @Test
    void findByUserId_WithNullUserId_ReturnsNull() {
        // When
        UserTokenDTO result = userTokenService.findByUserId(null);

        // Then
        assertNull(result);
        verify(customerUserTokenMapper, never()).selectByUserId(any());
    }

    @Test
    void findByUserId_WhenTokenNotFound_ReturnsNull() {
        // Given
        when(customerUserTokenMapper.selectByUserId(999L)).thenReturn(null);

        // When
        UserTokenDTO result = userTokenService.findByUserId(999L);

        // Then
        assertNull(result);
        verify(customerUserTokenMapper).selectByUserId(999L);
    }

    // ==================== findByRefreshToken() Tests ====================

    @Test
    void findByRefreshToken_WithValidToken_ReturnsToken() {
        // Given
        String refreshToken = "valid-refresh-token";
        when(customerUserTokenMapper.selectByRefreshToken(refreshToken)).thenReturn(testTokenEntity);

        // When
        UserTokenDTO result = userTokenService.findByRefreshToken(refreshToken);

        // Then
        assertNotNull(result);
        assertEquals(testTokenDTO.getRefreshToken(), result.getRefreshToken());
        verify(customerUserTokenMapper).selectByRefreshToken(refreshToken);
    }

    @Test
    void findByRefreshToken_WithNullToken_ReturnsNull() {
        // When
        UserTokenDTO result = userTokenService.findByRefreshToken(null);

        // Then
        assertNull(result);
        verify(customerUserTokenMapper, never()).selectByRefreshToken(any());
    }

    @Test
    void findByRefreshToken_WithEmptyToken_ReturnsNull() {
        // When
        UserTokenDTO result = userTokenService.findByRefreshToken("");

        // Then
        assertNull(result);
        verify(customerUserTokenMapper, never()).selectByRefreshToken(any());
    }

    @Test
    void findByRefreshToken_WhenTokenNotFound_ReturnsNull() {
        // Given
        when(customerUserTokenMapper.selectByRefreshToken("not-found")).thenReturn(null);

        // When
        UserTokenDTO result = userTokenService.findByRefreshToken("not-found");

        // Then
        assertNull(result);
        verify(customerUserTokenMapper).selectByRefreshToken("not-found");
    }

    // ==================== deleteByUserId() Tests ====================

    @Test
    void deleteByUserId_WithValidUserId_Success() {
        // Given
        when(customerUserTokenMapper.deleteByUserId(100L)).thenReturn(1);

        // When
        int result = userTokenService.deleteByUserId(100L);

        // Then
        assertEquals(1, result);
        verify(customerUserTokenMapper).deleteByUserId(100L);
    }

    @Test
    void deleteByUserId_WithNullUserId_ReturnsZero() {
        // When
        int result = userTokenService.deleteByUserId(null);

        // Then
        assertEquals(0, result);
        verify(customerUserTokenMapper, never()).deleteByUserId(any());
    }

    @Test
    void deleteByUserId_WhenTokenNotFound_ReturnsZero() {
        // Given
        when(customerUserTokenMapper.deleteByUserId(999L)).thenReturn(0);

        // When
        int result = userTokenService.deleteByUserId(999L);

        // Then
        assertEquals(0, result);
        verify(customerUserTokenMapper).deleteByUserId(999L);
    }

    // ==================== saveOrUpdate() Tests ====================

    @Test
    void saveOrUpdate_WhenTokenNotExists_InsertsNew() {
        // Given - no existing token
        UserTokenDTO newToken = UserTokenDTO.builder()
                .userId(300L)
                .refreshToken("new-token")
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        when(customerUserTokenMapper.selectByUserId(300L)).thenReturn(null);
        when(userTokenMapper.insertSelective(any(UserToken.class))).thenAnswer(invocation -> {
            UserToken entity = invocation.getArgument(0);
            entity.setId(456L);
            return 1;
        });

        // When
        int result = userTokenService.saveOrUpdate(newToken);

        // Then
        assertEquals(1, result);
        verify(customerUserTokenMapper).selectByUserId(300L);
        verify(userTokenMapper).insertSelective(any(UserToken.class));
        verify(userTokenMapper, never()).updateByPrimaryKeySelective(any());
    }

    @Test
    void saveOrUpdate_WhenTokenExists_UpdatesExisting() {
        // Given - existing token
        UserToken existingToken = new UserToken()
                .withId(100L)
                .withUserId(300L)
                .withRefreshToken("old-token")
                .withExpiresAt(LocalDateTime.now().plusDays(1));

        UserTokenDTO updateToken = UserTokenDTO.builder()
                .userId(300L)
                .refreshToken("updated-token")
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        when(customerUserTokenMapper.selectByUserId(300L)).thenReturn(existingToken);
        when(userTokenMapper.updateByPrimaryKeySelective(any(UserToken.class))).thenReturn(1);

        // When
        int result = userTokenService.saveOrUpdate(updateToken);

        // Then
        assertEquals(1, result);
        assertEquals(100L, updateToken.getId()); // Should use existing ID
        verify(customerUserTokenMapper).selectByUserId(300L);
        verify(userTokenMapper).updateByPrimaryKeySelective(any(UserToken.class));
        verify(userTokenMapper, never()).insertSelective(any());
    }

    @Test
    void saveOrUpdate_WithNullToken_ReturnsZero() {
        // When
        int result = userTokenService.saveOrUpdate(null);

        // Then
        assertEquals(0, result);
        verify(customerUserTokenMapper, never()).selectByUserId(any());
    }

    @Test
    void saveOrUpdate_WithNullUserId_ReturnsZero() {
        // Given - token without userId
        UserTokenDTO tokenWithoutUserId = UserTokenDTO.builder()
                .refreshToken("token")
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        // When
        int result = userTokenService.saveOrUpdate(tokenWithoutUserId);

        // Then
        assertEquals(0, result);
        verify(customerUserTokenMapper, never()).selectByUserId(any());
    }

    @Test
    void saveOrUpdate_WhenInsertFails_ReturnsZero() {
        // Given
        UserTokenDTO newToken = UserTokenDTO.builder()
                .userId(300L)
                .refreshToken("new-token")
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        when(customerUserTokenMapper.selectByUserId(300L)).thenReturn(null);
        when(userTokenMapper.insertSelective(any(UserToken.class))).thenReturn(0);

        // When
        int result = userTokenService.saveOrUpdate(newToken);

        // Then
        assertEquals(0, result);
    }

    @Test
    void saveOrUpdate_WhenUpdateFails_ReturnsZero() {
        // Given
        UserToken existingToken = new UserToken()
                .withId(100L)
                .withUserId(300L)
                .withRefreshToken("old-token");

        UserTokenDTO updateToken = UserTokenDTO.builder()
                .userId(300L)
                .refreshToken("updated-token")
                .build();

        when(customerUserTokenMapper.selectByUserId(300L)).thenReturn(existingToken);
        when(userTokenMapper.updateByPrimaryKeySelective(any(UserToken.class))).thenReturn(0);

        // When
        int result = userTokenService.saveOrUpdate(updateToken);

        // Then
        assertEquals(0, result);
    }

    // ==================== Integration Scenarios ====================

    @Test
    void scenario_UserLoginFlow_SaveOrUpdateMultipleTimes() {
        // Scenario: User logs in multiple times, token should be updated

        // First login - insert new token
        UserTokenDTO firstLogin = UserTokenDTO.builder()
                .userId(400L)
                .refreshToken("first-token")
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        when(customerUserTokenMapper.selectByUserId(400L))
                .thenReturn(null) // First time, no token exists
                .thenReturn(new UserToken().withId(1000L).withUserId(400L)); // Second time, token exists

        when(userTokenMapper.insertSelective(any(UserToken.class))).thenAnswer(invocation -> {
            UserToken entity = invocation.getArgument(0);
            entity.setId(1000L);
            return 1;
        });

        when(userTokenMapper.updateByPrimaryKeySelective(any(UserToken.class))).thenReturn(1);

        // First login
        int result1 = userTokenService.saveOrUpdate(firstLogin);
        assertEquals(1, result1);

        // Second login - should update
        UserTokenDTO secondLogin = UserTokenDTO.builder()
                .userId(400L)
                .refreshToken("second-token")
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        int result2 = userTokenService.saveOrUpdate(secondLogin);
        assertEquals(1, result2);
        assertEquals(1000L, secondLogin.getId()); // Should reuse same ID
    }

    @Test
    void scenario_TokenExpiration_CanBeUpdated() {
        // Given - expired token
        UserTokenDTO expiredToken = UserTokenDTO.builder()
                .id(1L)
                .userId(100L)
                .refreshToken("expired-token")
                .expiresAt(LocalDateTime.now().minusDays(1)) // Already expired
                .build();

        when(userTokenMapper.updateByPrimaryKeySelective(any(UserToken.class))).thenReturn(1);

        // When - update with new expiration
        expiredToken.setExpiresAt(LocalDateTime.now().plusDays(7));
        int result = userTokenService.update(expiredToken);

        // Then
        assertEquals(1, result);
        assertTrue(expiredToken.getExpiresAt().isAfter(LocalDateTime.now()));
    }

    @Test
    void scenario_MultipleUsersTokens_CanBeDistinguished() {
        // Given - tokens for different users
        UserToken user1Token = new UserToken()
                .withId(1L)
                .withUserId(100L)
                .withRefreshToken("user1-token");

        UserToken user2Token = new UserToken()
                .withId(2L)
                .withUserId(200L)
                .withRefreshToken("user2-token");

        when(customerUserTokenMapper.selectByUserId(100L)).thenReturn(user1Token);
        when(customerUserTokenMapper.selectByUserId(200L)).thenReturn(user2Token);

        // When
        UserTokenDTO result1 = userTokenService.findByUserId(100L);
        UserTokenDTO result2 = userTokenService.findByUserId(200L);

        // Then
        assertNotNull(result1);
        assertNotNull(result2);
        assertEquals(100L, result1.getUserId());
        assertEquals(200L, result2.getUserId());
        assertNotEquals(result1.getRefreshToken(), result2.getRefreshToken());
    }
}
