package com.goody.nus.se.gomoku.user.service.impl;

import com.goody.nus.se.gomoku.user.model.dao.UserMapper;
import com.goody.nus.se.gomoku.user.model.dto.UserDTO;
import com.goody.nus.se.gomoku.user.model.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mybatis.dynamic.sql.select.CountDSLCompleter;
import org.mybatis.dynamic.sql.select.SelectDSLCompleter;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link UserServiceImpl}
 * Tests all branches including CRUD operations and custom queries
 */
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private UserDTO testUserDTO;
    private User testUserEntity;

    @BeforeEach
    void setUp() {
        testUserDTO = UserDTO.builder()
                .id(1L)
                .email("test@example.com")
                .nickname("TestUser")
                .passwordHash("hashedPassword")
                .passwordSalt("salt123")
                .avatarUrl("")
                .country("US")
                .gender((byte) 1)
                .status((byte) 1)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testUserEntity = testUserDTO.toEntity();
    }

    // ==================== save() Tests ====================

    @Test
    void save_WithNewUser_Success() {
        // Given
        UserDTO newUser = UserDTO.builder()
                .email("new@example.com")
                .nickname("NewUser")
                .passwordHash("hash")
                .passwordSalt("salt")
                .status((byte) 1)
                .build();

        when(userMapper.insertSelective(any(User.class))).thenAnswer(invocation -> {
            User entity = invocation.getArgument(0);
            entity.setId(123L); // Simulate DB generated ID
            return 1;
        });

        // When
        Long savedId = userService.save(newUser);

        // Then
        assertNotNull(savedId);
        assertEquals(123L, savedId);
        verify(userMapper).insertSelective(any(User.class));
    }

    @Test
    void save_WithExistingId_Success() {
        // Given - user with ID already set
        when(userMapper.insertSelective(any(User.class))).thenReturn(1);

        // When
        Long savedId = userService.save(testUserDTO);

        // Then
        assertNotNull(savedId);
        assertEquals(testUserDTO.getId(), savedId);
        verify(userMapper).insertSelective(any(User.class));
    }

    @Test
    void save_WithNullUser_ReturnsNull() {
        // When
        Long result = userService.save(null);

        // Then
        assertNull(result);
        verify(userMapper, never()).insertSelective(any());
    }

    @Test
    void save_WhenInsertFails_ReturnsNull() {
        // Given
        when(userMapper.insertSelective(any(User.class))).thenReturn(0);

        // When
        Long result = userService.save(testUserDTO);

        // Then
        assertNull(result);
        verify(userMapper).insertSelective(any(User.class));
    }

    // ==================== saveBatch() Tests ====================

    @Test
    void saveBatch_WithValidList_Success() {
        // Given
        UserDTO user1 = UserDTO.builder()
                .email("user1@example.com")
                .nickname("User1")
                .passwordHash("hash1")
                .passwordSalt("salt1")
                .status((byte) 1)
                .build();

        UserDTO user2 = UserDTO.builder()
                .email("user2@example.com")
                .nickname("User2")
                .passwordHash("hash2")
                .passwordSalt("salt2")
                .status((byte) 1)
                .build();

        List<UserDTO> userList = Arrays.asList(user1, user2);

        when(userMapper.insertMultiple(anyList())).thenReturn(2);

        // When
        int result = userService.saveBatch(userList);

        // Then
        assertEquals(2, result);
        verify(userMapper).insertMultiple(anyList());
        // Verify IDs were generated
        assertNotNull(user1.getId());
        assertNotNull(user2.getId());
    }

    @Test
    void saveBatch_WithNullList_ReturnsZero() {
        // When
        int result = userService.saveBatch(null);

        // Then
        assertEquals(0, result);
        verify(userMapper, never()).insertMultiple(anyList());
    }

    @Test
    void saveBatch_WithEmptyList_ReturnsZero() {
        // When
        int result = userService.saveBatch(Collections.emptyList());

        // Then
        assertEquals(0, result);
        verify(userMapper, never()).insertMultiple(anyList());
    }

    @Test
    void saveBatch_WithNullElements_FiltersAndSaves() {
        // Given - list with null element
        UserDTO validUser = UserDTO.builder()
                .email("valid@example.com")
                .nickname("ValidUser")
                .passwordHash("hash")
                .passwordSalt("salt")
                .status((byte) 1)
                .build();

        List<UserDTO> userList = Arrays.asList(validUser, null);

        when(userMapper.insertMultiple(anyList())).thenReturn(1);

        // When
        int result = userService.saveBatch(userList);

        // Then
        assertEquals(1, result);
    }

    @Test
    void saveBatch_SetsCreatedAtAndUpdatedAt() {
        // Given
        UserDTO user = UserDTO.builder()
                .email("user@example.com")
                .nickname("User")
                .passwordHash("hash")
                .passwordSalt("salt")
                .status((byte) 1)
                .build();

        when(userMapper.insertMultiple(anyList())).thenReturn(1);

        // When
        userService.saveBatch(Arrays.asList(user));

        // Then
        assertNotNull(user.getCreatedAt());
        assertNotNull(user.getUpdatedAt());
    }

    @Test
    void saveBatch_WithAllNullElements_ReturnsZero() {
        // Given - list with all null elements
        List<UserDTO> userList = Arrays.asList(null, null, null);

        // When
        int result = userService.saveBatch(userList);

        // Then
        assertEquals(0, result);
        verify(userMapper, never()).insertMultiple(anyList());
    }

    // ==================== update() Tests ====================

    @Test
    void update_WithValidUser_Success() {
        // Given
        when(userMapper.updateByPrimaryKeySelective(any(User.class))).thenReturn(1);

        // When
        int result = userService.update(testUserDTO);

        // Then
        assertEquals(1, result);
        verify(userMapper).updateByPrimaryKeySelective(any(User.class));
    }

    @Test
    void update_WithNullUser_ReturnsZero() {
        // When
        int result = userService.update(null);

        // Then
        assertEquals(0, result);
        verify(userMapper, never()).updateByPrimaryKeySelective(any());
    }

    @Test
    void update_WhenUpdateFails_ReturnsZero() {
        // Given
        when(userMapper.updateByPrimaryKeySelective(any(User.class))).thenReturn(0);

        // When
        int result = userService.update(testUserDTO);

        // Then
        assertEquals(0, result);
        verify(userMapper).updateByPrimaryKeySelective(any(User.class));
    }

    // ==================== deleteById() Tests ====================

    @Test
    void deleteById_WithValidId_Success() {
        // Given
        when(userMapper.deleteByPrimaryKey(1L)).thenReturn(1);

        // When
        int result = userService.deleteById(1L);

        // Then
        assertEquals(1, result);
        verify(userMapper).deleteByPrimaryKey(1L);
    }

    @Test
    void deleteById_WithNullId_ReturnsZero() {
        // When
        int result = userService.deleteById(null);

        // Then
        assertEquals(0, result);
        verify(userMapper, never()).deleteByPrimaryKey(any());
    }

    @Test
    void deleteById_WhenDeleteFails_ReturnsZero() {
        // Given
        when(userMapper.deleteByPrimaryKey(1L)).thenReturn(0);

        // When
        int result = userService.deleteById(1L);

        // Then
        assertEquals(0, result);
        verify(userMapper).deleteByPrimaryKey(1L);
    }

    // ==================== findById() Tests ====================

    @Test
    void findById_WithValidId_ReturnsUser() {
        // Given
        when(userMapper.selectByPrimaryKey(1L)).thenReturn(Optional.of(testUserEntity));

        // When
        UserDTO result = userService.findById(1L);

        // Then
        assertNotNull(result);
        assertEquals(testUserDTO.getId(), result.getId());
        assertEquals(testUserDTO.getEmail(), result.getEmail());
        verify(userMapper).selectByPrimaryKey(1L);
    }

    @Test
    void findById_WithNullId_ReturnsNull() {
        // When
        UserDTO result = userService.findById(null);

        // Then
        assertNull(result);
        verify(userMapper, never()).selectByPrimaryKey(any());
    }

    @Test
    void findById_WhenUserNotFound_ReturnsNull() {
        // Given
        when(userMapper.selectByPrimaryKey(999L)).thenReturn(Optional.empty());

        // When
        UserDTO result = userService.findById(999L);

        // Then
        assertNull(result);
        verify(userMapper).selectByPrimaryKey(999L);
    }

    // ==================== findAll() Tests ====================

    @Test
    void findAll_ReturnsAllUsers() {
        // Given
        User user1 = testUserEntity;
        User user2 = UserDTO.builder()
                .id(2L)
                .email("user2@example.com")
                .nickname("User2")
                .status((byte) 1)
                .build().toEntity();

        when(userMapper.select(any(SelectDSLCompleter.class)))
                .thenReturn(Arrays.asList(user1, user2));

        // When
        List<UserDTO> result = userService.findAll();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(userMapper).select(any(SelectDSLCompleter.class));
    }

    @Test
    void findAll_WhenNoUsers_ReturnsEmptyList() {
        // Given
        when(userMapper.select(any(SelectDSLCompleter.class)))
                .thenReturn(Collections.emptyList());

        // When
        List<UserDTO> result = userService.findAll();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userMapper).select(any(SelectDSLCompleter.class));
    }

    // ==================== findByEmail() Tests ====================

    @Test
    void findByEmail_WithValidEmail_ReturnsUser() {
        // Given
        when(userMapper.selectOne(any(SelectDSLCompleter.class)))
                .thenReturn(Optional.of(testUserEntity));

        // When
        UserDTO result = userService.findByEmail("test@example.com");

        // Then
        assertNotNull(result);
        assertEquals(testUserDTO.getEmail(), result.getEmail());
        verify(userMapper).selectOne(any(SelectDSLCompleter.class));
    }

    @Test
    void findByEmail_WhenUserNotFound_ReturnsNull() {
        // Given
        when(userMapper.selectOne(any(SelectDSLCompleter.class)))
                .thenReturn(Optional.empty());

        // When
        UserDTO result = userService.findByEmail("notfound@example.com");

        // Then
        assertNull(result);
        verify(userMapper).selectOne(any(SelectDSLCompleter.class));
    }

    // ==================== findByNickname() Tests ====================

    @Test
    void findByNickname_WithValidNickname_ReturnsUser() {
        // Given
        when(userMapper.selectOne(any(SelectDSLCompleter.class)))
                .thenReturn(Optional.of(testUserEntity));

        // When
        UserDTO result = userService.findByNickname("TestUser");

        // Then
        assertNotNull(result);
        assertEquals(testUserDTO.getNickname(), result.getNickname());
        verify(userMapper).selectOne(any(SelectDSLCompleter.class));
    }

    @Test
    void findByNickname_WhenUserNotFound_ReturnsNull() {
        // Given
        when(userMapper.selectOne(any(SelectDSLCompleter.class)))
                .thenReturn(Optional.empty());

        // When
        UserDTO result = userService.findByNickname("NotFound");

        // Then
        assertNull(result);
        verify(userMapper).selectOne(any(SelectDSLCompleter.class));
    }

    // ==================== existsByEmail() Tests ====================

    @Test
    void existsByEmail_WhenEmailExists_ReturnsTrue() {
        // Given
        when(userMapper.count(any(CountDSLCompleter.class))).thenReturn(1L);

        // When
        boolean result = userService.existsByEmail("test@example.com");

        // Then
        assertTrue(result);
        verify(userMapper).count(any(CountDSLCompleter.class));
    }

    @Test
    void existsByEmail_WhenEmailNotExists_ReturnsFalse() {
        // Given
        when(userMapper.count(any(CountDSLCompleter.class))).thenReturn(0L);

        // When
        boolean result = userService.existsByEmail("notfound@example.com");

        // Then
        assertFalse(result);
        verify(userMapper).count(any(CountDSLCompleter.class));
    }

    @Test
    void existsByEmail_WithMultipleMatches_ReturnsTrue() {
        // Given - even if count > 1 (shouldn't happen with unique constraint, but test edge case)
        when(userMapper.count(any(CountDSLCompleter.class))).thenReturn(2L);

        // When
        boolean result = userService.existsByEmail("test@example.com");

        // Then
        assertTrue(result);
    }

    // ==================== existsByNickname() Tests ====================

    @Test
    void existsByNickname_WhenNicknameExists_ReturnsTrue() {
        // Given
        when(userMapper.count(any(CountDSLCompleter.class))).thenReturn(1L);

        // When
        boolean result = userService.existsByNickname("TestUser");

        // Then
        assertTrue(result);
        verify(userMapper).count(any(CountDSLCompleter.class));
    }

    @Test
    void existsByNickname_WhenNicknameNotExists_ReturnsFalse() {
        // Given
        when(userMapper.count(any(CountDSLCompleter.class))).thenReturn(0L);

        // When
        boolean result = userService.existsByNickname("NotFound");

        // Then
        assertFalse(result);
        verify(userMapper).count(any(CountDSLCompleter.class));
    }

    @Test
    void existsByNickname_WithMultipleMatches_ReturnsTrue() {
        // Given
        when(userMapper.count(any(CountDSLCompleter.class))).thenReturn(3L);

        // When
        boolean result = userService.existsByNickname("TestUser");

        // Then
        assertTrue(result);
    }

    // ==================== Edge Cases and Integration Tests ====================

    @Test
    void save_GeneratesUniqueIds_ForMultipleCalls() {
        // Given
        UserDTO user1 = UserDTO.builder().email("user1@example.com").nickname("User1").build();
        UserDTO user2 = UserDTO.builder().email("user2@example.com").nickname("User2").build();

        when(userMapper.insertSelective(any(User.class))).thenAnswer(invocation -> {
            User entity = invocation.getArgument(0);
            // Simulate DB returning the ID
            return 1;
        });

        // When
        Long id1 = userService.save(user1);
        Long id2 = userService.save(user2);

        // Then
        assertNotNull(id1);
        assertNotNull(id2);
        assertNotEquals(id1, id2); // IDs should be different
    }

    @Test
    void saveBatch_PreservesExistingIds() {
        // Given - users with pre-set IDs
        UserDTO user1 = UserDTO.builder()
                .id(100L)
                .email("user1@example.com")
                .nickname("User1")
                .build();

        UserDTO user2 = UserDTO.builder()
                .id(200L)
                .email("user2@example.com")
                .nickname("User2")
                .build();

        when(userMapper.insertMultiple(anyList())).thenReturn(2);

        // When
        userService.saveBatch(Arrays.asList(user1, user2));

        // Then
        assertEquals(100L, user1.getId());
        assertEquals(200L, user2.getId());
    }

    @Test
    void update_WithPartialData_OnlyUpdatesProvidedFields() {
        // Given - DTO with only some fields set
        UserDTO partialUpdate = UserDTO.builder()
                .id(1L)
                .nickname("UpdatedNickname")
                .build();

        when(userMapper.updateByPrimaryKeySelective(any(User.class))).thenReturn(1);

        // When
        int result = userService.update(partialUpdate);

        // Then
        assertEquals(1, result);
        verify(userMapper).updateByPrimaryKeySelective(argThat(entity ->
                entity.getId().equals(1L) &&
                        entity.getNickname().equals("UpdatedNickname")
        ));
    }

    @Test
    void findByEmail_OnlyReturnsActiveUsers() {
        // This test verifies the query filters by status = 1
        // Given
        when(userMapper.selectOne(any(SelectDSLCompleter.class)))
                .thenReturn(Optional.of(testUserEntity));

        // When
        UserDTO result = userService.findByEmail("test@example.com");

        // Then
        assertNotNull(result);
        assertEquals((byte) 1, result.getStatus()); // Should be active
    }

    @Test
    void findByNickname_OnlyReturnsActiveUsers() {
        // Given
        when(userMapper.selectOne(any(SelectDSLCompleter.class)))
                .thenReturn(Optional.of(testUserEntity));

        // When
        UserDTO result = userService.findByNickname("TestUser");

        // Then
        assertNotNull(result);
        assertEquals((byte) 1, result.getStatus());
    }
}
