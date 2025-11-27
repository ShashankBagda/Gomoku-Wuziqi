package com.goody.nus.se.gomoku.user.service.impl;

import com.goody.nus.se.gomoku.user.model.dao.UserTokenMapper;
import com.goody.nus.se.gomoku.user.model.dao.customer.CustomerUserTokenMapper;
import com.goody.nus.se.gomoku.user.model.dto.UserTokenDTO;
import com.goody.nus.se.gomoku.user.model.entity.UserToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit test for {@link UserTokenServiceImpl}
 *
 * @author Claude
 * @version 1.0
 */
class UserTokenServiceImplTest {

    @Mock
    private UserTokenMapper userTokenMapper;

    @Mock
    private CustomerUserTokenMapper customerUserTokenMapper;

    @InjectMocks
    private UserTokenServiceImpl userTokenService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ==================== Save Tests ====================

    @Test
    void save_withValidDTO_shouldReturnGeneratedId() {
        // Given
        UserTokenDTO dto = UserTokenDTO.builder()
                .userId(123L)
                .refreshToken("refresh_token_123")
                .build();

        when(userTokenMapper.insertSelective(any(UserToken.class))).thenAnswer(invocation -> {
            UserToken entity = invocation.getArgument(0);
            entity.setId(999L);
            return 1;
        });

        // When
        Long result = userTokenService.save(dto);

        // Then
        assertNotNull(result);
        verify(userTokenMapper, times(1)).insertSelective(any(UserToken.class));
    }

    @Test
    void save_withNullDTO_shouldReturnNull() {
        // When
        Long result = userTokenService.save(null);

        // Then
        assertNull(result);
        verify(userTokenMapper, never()).insertSelective(any());
    }

    @Test
    void save_withExistingId_shouldUseProvidedId() {
        // Given
        UserTokenDTO dto = UserTokenDTO.builder()
                .id(555L)
                .userId(123L)
                .refreshToken("refresh_token_123")
                .build();

        when(userTokenMapper.insertSelective(any(UserToken.class))).thenReturn(1);

        // When
        Long result = userTokenService.save(dto);

        // Then
        assertNotNull(result);
        assertEquals(555L, dto.getId());
        verify(userTokenMapper, times(1)).insertSelective(any(UserToken.class));
    }

    // ==================== SaveBatch Tests ====================

    @Test
    void saveBatch_withValidList_shouldInsertAll() {
        // Given
        List<UserTokenDTO> dtoList = Arrays.asList(
                UserTokenDTO.builder().userId(1L).refreshToken("token1").build(),
                UserTokenDTO.builder().userId(2L).refreshToken("token2").build()
        );

        when(userTokenMapper.insertMultiple(anyList())).thenReturn(2);

        // When
        int result = userTokenService.saveBatch(dtoList);

        // Then
        assertEquals(2, result);
        verify(userTokenMapper, times(1)).insertMultiple(anyList());
    }

    @Test
    void saveBatch_withNullList_shouldReturnZero() {
        // When
        int result = userTokenService.saveBatch(null);

        // Then
        assertEquals(0, result);
        verify(userTokenMapper, never()).insertMultiple(anyList());
    }

    @Test
    void saveBatch_withEmptyList_shouldReturnZero() {
        // When
        int result = userTokenService.saveBatch(Collections.emptyList());

        // Then
        assertEquals(0, result);
        verify(userTokenMapper, never()).insertMultiple(anyList());
    }

    // ==================== Update Tests ====================

    @Test
    void update_withValidDTO_shouldReturnOne() {
        // Given
        UserTokenDTO dto = UserTokenDTO.builder()
                .id(123L)
                .userId(456L)
                .refreshToken("new_token")
                .build();

        when(userTokenMapper.updateByPrimaryKeySelective(any(UserToken.class))).thenReturn(1);

        // When
        int result = userTokenService.update(dto);

        // Then
        assertEquals(1, result);
        verify(userTokenMapper, times(1)).updateByPrimaryKeySelective(any(UserToken.class));
    }

    @Test
    void update_withNullDTO_shouldReturnZero() {
        // When
        int result = userTokenService.update(null);

        // Then
        assertEquals(0, result);
        verify(userTokenMapper, never()).updateByPrimaryKeySelective(any());
    }

    // ==================== Delete Tests ====================

    @Test
    void deleteById_withValidId_shouldReturnOne() {
        // Given
        Long id = 123L;
        when(userTokenMapper.deleteByPrimaryKey(id)).thenReturn(1);

        // When
        int result = userTokenService.deleteById(id);

        // Then
        assertEquals(1, result);
        verify(userTokenMapper, times(1)).deleteByPrimaryKey(id);
    }

    @Test
    void deleteById_withNullId_shouldReturnZero() {
        // When
        int result = userTokenService.deleteById(null);

        // Then
        assertEquals(0, result);
        verify(userTokenMapper, never()).deleteByPrimaryKey(any());
    }

    @Test
    void deleteByUserId_withValidUserId_shouldReturnOne() {
        // Given
        Long userId = 456L;
        when(customerUserTokenMapper.deleteByUserId(userId)).thenReturn(1);

        // When
        int result = userTokenService.deleteByUserId(userId);

        // Then
        assertEquals(1, result);
        verify(customerUserTokenMapper, times(1)).deleteByUserId(userId);
    }

    // ==================== FindById Tests ====================

    @Test
    void findById_withExistingId_shouldReturnDTO() {
        // Given
        Long id = 123L;
        UserToken entity = new UserToken();
        entity.setId(id);
        entity.setUserId(456L);
        entity.setRefreshToken("token");

        when(userTokenMapper.selectByPrimaryKey(id)).thenReturn(Optional.of(entity));

        // When
        UserTokenDTO result = userTokenService.findById(id);

        // Then
        assertNotNull(result);
        assertEquals(id, result.getId());
        verify(userTokenMapper, times(1)).selectByPrimaryKey(id);
    }

    @Test
    void findById_withNonExistingId_shouldReturnNull() {
        // Given
        Long id = 999L;
        when(userTokenMapper.selectByPrimaryKey(id)).thenReturn(Optional.empty());

        // When
        UserTokenDTO result = userTokenService.findById(id);

        // Then
        assertNull(result);
        verify(userTokenMapper, times(1)).selectByPrimaryKey(id);
    }

    @Test
    void findById_withNullId_shouldReturnNull() {
        // When
        UserTokenDTO result = userTokenService.findById(null);

        // Then
        assertNull(result);
        verify(userTokenMapper, never()).selectByPrimaryKey(any());
    }

    // ==================== FindByUserId Tests ====================

    @Test
    void findByUserId_withExistingUserId_shouldReturnDTO() {
        // Given
        Long userId = 456L;
        UserToken entity = new UserToken();
        entity.setId(123L);
        entity.setUserId(userId);
        entity.setRefreshToken("token");

        when(customerUserTokenMapper.selectByUserId(userId)).thenReturn(entity);

        // When
        UserTokenDTO result = userTokenService.findByUserId(userId);

        // Then
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        verify(customerUserTokenMapper, times(1)).selectByUserId(userId);
    }

    @Test
    void findByUserId_withNullUserId_shouldReturnNull() {
        // When
        UserTokenDTO result = userTokenService.findByUserId(null);

        // Then
        assertNull(result);
        verify(customerUserTokenMapper, never()).selectByUserId(any());
    }

    // ==================== FindByRefreshToken Tests ====================

    @Test
    void findByRefreshToken_withValidToken_shouldReturnDTO() {
        // Given
        String token = "valid_refresh_token";
        UserToken entity = new UserToken();
        entity.setId(123L);
        entity.setRefreshToken(token);

        when(customerUserTokenMapper.selectByRefreshToken(token)).thenReturn(entity);

        // When
        UserTokenDTO result = userTokenService.findByRefreshToken(token);

        // Then
        assertNotNull(result);
        assertEquals(token, result.getRefreshToken());
        verify(customerUserTokenMapper, times(1)).selectByRefreshToken(token);
    }

    @Test
    void findByRefreshToken_withNullToken_shouldReturnNull() {
        // When
        UserTokenDTO result = userTokenService.findByRefreshToken(null);

        // Then
        assertNull(result);
        verify(customerUserTokenMapper, never()).selectByRefreshToken(any());
    }

    @Test
    void findByRefreshToken_withEmptyToken_shouldReturnNull() {
        // When
        UserTokenDTO result = userTokenService.findByRefreshToken("");

        // Then
        assertNull(result);
        verify(customerUserTokenMapper, never()).selectByRefreshToken(any());
    }

    // ==================== SaveOrUpdate Tests ====================

    @Test
    void saveOrUpdate_withNewUser_shouldInsert() {
        // Given
        UserTokenDTO dto = UserTokenDTO.builder()
                .userId(123L)
                .refreshToken("new_token")
                .build();

        when(customerUserTokenMapper.selectByUserId(123L)).thenReturn(null);
        when(userTokenMapper.insertSelective(any(UserToken.class))).thenAnswer(invocation -> {
            UserToken entity = invocation.getArgument(0);
            entity.setId(999L);
            return 1;
        });

        // When
        int result = userTokenService.saveOrUpdate(dto);

        // Then
        assertEquals(1, result);
        verify(customerUserTokenMapper, times(1)).selectByUserId(123L);
        verify(userTokenMapper, times(1)).insertSelective(any(UserToken.class));
        verify(userTokenMapper, never()).updateByPrimaryKeySelective(any());
    }

    @Test
    void saveOrUpdate_withExistingUser_shouldUpdate() {
        // Given
        UserTokenDTO dto = UserTokenDTO.builder()
                .userId(123L)
                .refreshToken("updated_token")
                .build();

        UserToken existingEntity = new UserToken();
        existingEntity.setId(555L);
        existingEntity.setUserId(123L);

        when(customerUserTokenMapper.selectByUserId(123L)).thenReturn(existingEntity);
        when(userTokenMapper.updateByPrimaryKeySelective(any(UserToken.class))).thenReturn(1);

        // When
        int result = userTokenService.saveOrUpdate(dto);

        // Then
        assertEquals(1, result);
        assertEquals(555L, dto.getId());
        verify(customerUserTokenMapper, times(1)).selectByUserId(123L);
        verify(userTokenMapper, times(1)).updateByPrimaryKeySelective(any(UserToken.class));
        verify(userTokenMapper, never()).insertSelective(any());
    }

    @Test
    void saveOrUpdate_withNullDTO_shouldReturnZero() {
        // When
        int result = userTokenService.saveOrUpdate(null);

        // Then
        assertEquals(0, result);
        verify(customerUserTokenMapper, never()).selectByUserId(any());
    }

    @Test
    void saveOrUpdate_withNullUserId_shouldReturnZero() {
        // Given
        UserTokenDTO dto = UserTokenDTO.builder()
                .refreshToken("token")
                .build();

        // When
        int result = userTokenService.saveOrUpdate(dto);

        // Then
        assertEquals(0, result);
        verify(customerUserTokenMapper, never()).selectByUserId(any());
    }
}
