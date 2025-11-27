package com.goody.nus.se.gomoku.user.biz.service.impl;

import com.goody.nus.se.gomoku.common.exception.BizException;
import com.goody.nus.se.gomoku.common.valuechecker.ValueCheckerReentrantThreadLocal;
import com.goody.nus.se.gomoku.user.model.dto.UserDTO;
import com.goody.nus.se.gomoku.user.service.interfaces.IUserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.goody.nus.se.gomoku.common.errorcode.ErrorCodeEnum.USER_ACCOUNT_DELETED;
import static com.goody.nus.se.gomoku.common.errorcode.ErrorCodeEnum.USER_ACCOUNT_DISABLED;
import static com.goody.nus.se.gomoku.common.errorcode.ErrorCodeEnum.USER_ACCOUNT_INACTIVE;
import static com.goody.nus.se.gomoku.common.errorcode.ErrorCodeEnum.USER_EMAIL_ALREADY_EXISTS;
import static com.goody.nus.se.gomoku.common.errorcode.ErrorCodeEnum.USER_NICKNAME_ALREADY_EXISTS;
import static com.goody.nus.se.gomoku.common.errorcode.ErrorCodeEnum.USER_USER_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test for {@link UserValidationBizServiceImpl}
 *
 * @author Haotian
 * @version 1.0, 2025/10/4
 */
@ExtendWith(MockitoExtension.class)
class UserValidationBizServiceImplTest {

    @Mock
    private IUserService userService;

    @InjectMocks
    private UserValidationBizServiceImpl validationService;

    private UserDTO testUser;

    @BeforeEach
    void setUp() {
        ValueCheckerReentrantThreadLocal.init();

        testUser = UserDTO.builder()
                .id(1L)
                .email("test@example.com")
                .nickname("testUser")
                .status((byte) 1)
                .build();
    }

    @AfterEach
    void tearDown() {
        ValueCheckerReentrantThreadLocal.clear();
    }

    @Test
    void validateEmailUniqueness_emailNotExists_shouldNotThrowException() {
        when(userService.existsByEmail("new@example.com")).thenReturn(false);

        assertDoesNotThrow(() -> validationService.validateEmailUniqueness("new@example.com"));

        verify(userService).existsByEmail("new@example.com");
    }

    @Test
    void validateEmailUniqueness_emailExists_shouldThrowBizException() {
        when(userService.existsByEmail("existing@example.com")).thenReturn(true);

        BizException exception = assertThrows(BizException.class,
                () -> validationService.validateEmailUniqueness("existing@example.com"));

        assertEquals(USER_EMAIL_ALREADY_EXISTS, exception.getErrorCode());
        verify(userService).existsByEmail("existing@example.com");
    }

    @Test
    void validateNicknameUniqueness_nicknameNotExists_shouldNotThrowException() {
        when(userService.existsByNickname("newNickname")).thenReturn(false);

        assertDoesNotThrow(() -> validationService.validateNicknameUniqueness("newNickname"));

        verify(userService).existsByNickname("newNickname");
    }

    @Test
    void validateNicknameUniqueness_nicknameExists_shouldThrowBizException() {
        when(userService.existsByNickname("existingNickname")).thenReturn(true);

        BizException exception = assertThrows(BizException.class,
                () -> validationService.validateNicknameUniqueness("existingNickname"));

        assertEquals(USER_NICKNAME_ALREADY_EXISTS, exception.getErrorCode());
        verify(userService).existsByNickname("existingNickname");
    }

    @Test
    void validateAccountStatus_activeUser_shouldNotThrowException() {
        testUser.setStatus((byte) 1);
        ValueCheckerReentrantThreadLocal.put(testUser);

        assertDoesNotThrow(() -> validationService.validateAccountStatus("test@example.com"));

        verify(userService, never()).findByEmail(anyString());
    }

    @Test
    void validateAccountStatus_userNotFound_shouldThrowBizException() {
        when(userService.findByEmail("nonexistent@example.com")).thenReturn(null);

        BizException exception = assertThrows(BizException.class,
                () -> validationService.validateAccountStatus("nonexistent@example.com"));

        assertEquals(USER_USER_NOT_FOUND, exception.getErrorCode());
        verify(userService).findByEmail("nonexistent@example.com");
    }

    @Test
    void validateAccountStatus_inactiveUser_shouldThrowBizException() {
        testUser.setStatus((byte) 0);
        ValueCheckerReentrantThreadLocal.put(testUser);

        BizException exception = assertThrows(BizException.class,
                () -> validationService.validateAccountStatus("test@example.com"));

        assertEquals(USER_ACCOUNT_INACTIVE, exception.getErrorCode());
        verify(userService, never()).findByEmail(anyString());
    }

    @Test
    void validateAccountStatus_disabledUser_shouldThrowBizException() {
        testUser.setStatus((byte) 2);
        ValueCheckerReentrantThreadLocal.put(testUser);

        BizException exception = assertThrows(BizException.class,
                () -> validationService.validateAccountStatus("test@example.com"));

        assertEquals(USER_ACCOUNT_DISABLED, exception.getErrorCode());
        verify(userService, never()).findByEmail(anyString());
    }

    @Test
    void validateAccountStatus_deletedUser_shouldThrowBizException() {
        testUser.setStatus((byte) 3);
        ValueCheckerReentrantThreadLocal.put(testUser);

        BizException exception = assertThrows(BizException.class,
                () -> validationService.validateAccountStatus("test@example.com"));

        assertEquals(USER_ACCOUNT_DELETED, exception.getErrorCode());
        verify(userService, never()).findByEmail(anyString());
    }
}
