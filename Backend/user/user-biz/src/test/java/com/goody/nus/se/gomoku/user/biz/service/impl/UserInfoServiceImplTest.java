package com.goody.nus.se.gomoku.user.biz.service.impl;

import com.goody.nus.se.gomoku.common.exception.BizException;
import com.goody.nus.se.gomoku.user.api.response.UserInfoResponse;
import com.goody.nus.se.gomoku.user.model.dto.UserDTO;
import com.goody.nus.se.gomoku.user.service.interfaces.IUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static com.goody.nus.se.gomoku.common.errorcode.ErrorCodeEnum.USER_USER_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit test for {@link UserInfoServiceImpl}
 *
 * @author Claude
 * @version 1.0
 */
class UserInfoServiceImplTest {

    @Mock
    private IUserService userMapper;

    @InjectMocks
    private UserInfoServiceImpl userInfoService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getUserInfoById_withExistingUser_shouldReturnUserInfo() {
        // Given
        Long userId = 123L;
        UserDTO mockUser = UserDTO.builder()
                .id(userId)
                .email("test@example.com")
                .nickname("TestUser")
                .avatarUrl("https://example.com/avatar.jpg")
                .country("US")
                .gender((byte) 1)
                .avatarBase64("base64encodedstring")
                .build();

        when(userMapper.findById(userId)).thenReturn(mockUser);

        // When
        UserInfoResponse response = userInfoService.getUserInfoById(userId);

        // Then
        assertNotNull(response);
        assertEquals(userId, response.getId());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("TestUser", response.getNickname());
        assertEquals("https://example.com/avatar.jpg", response.getAvatarUrl());
        assertEquals("US", response.getCountry());
        assertEquals((byte) 1, response.getGender());
        assertEquals("base64encodedstring", response.getAvatarBase64());

        verify(userMapper, times(1)).findById(userId);
    }

    @Test
    void getUserInfoById_withNonExistingUser_shouldThrowBizException() {
        // Given
        Long userId = 999L;
        when(userMapper.findById(userId)).thenReturn(null);

        // When & Then
        BizException exception = assertThrows(BizException.class, () -> {
            userInfoService.getUserInfoById(userId);
        });

        assertEquals(USER_USER_NOT_FOUND, exception.getErrorCode());
        assertNotNull(exception.getArgs());
        assertEquals(1, exception.getArgs().length);
        assertEquals("User not found with id: " + userId, exception.getArgs()[0]);

        verify(userMapper, times(1)).findById(userId);
    }

    @Test
    void getUserInfoById_withNullFields_shouldHandleGracefully() {
        // Given
        Long userId = 456L;
        UserDTO mockUser = UserDTO.builder()
                .id(userId)
                .email("test2@example.com")
                .nickname("User2")
                .avatarUrl(null)
                .country(null)
                .gender(null)
                .avatarBase64(null)
                .build();

        when(userMapper.findById(userId)).thenReturn(mockUser);

        // When
        UserInfoResponse response = userInfoService.getUserInfoById(userId);

        // Then
        assertNotNull(response);
        assertEquals(userId, response.getId());
        assertEquals("test2@example.com", response.getEmail());
        assertEquals("User2", response.getNickname());
        assertNull(response.getAvatarUrl());
        assertNull(response.getCountry());
        assertNull(response.getGender());
        assertNull(response.getAvatarBase64());

        verify(userMapper, times(1)).findById(userId);
    }

    @Test
    void getUserInfoById_withMinimalUserData_shouldReturnValidResponse() {
        // Given
        Long userId = 789L;
        UserDTO mockUser = UserDTO.builder()
                .id(userId)
                .email("minimal@example.com")
                .nickname("MinimalUser")
                .build();

        when(userMapper.findById(userId)).thenReturn(mockUser);

        // When
        UserInfoResponse response = userInfoService.getUserInfoById(userId);

        // Then
        assertNotNull(response);
        assertEquals(userId, response.getId());
        assertEquals("minimal@example.com", response.getEmail());
        assertEquals("MinimalUser", response.getNickname());

        verify(userMapper, times(1)).findById(userId);
    }
}
