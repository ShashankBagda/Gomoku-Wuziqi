package com.goody.nus.se.gomoku.user.biz.service.impl;

import com.goody.nus.se.gomoku.common.exception.BizException;
import com.goody.nus.se.gomoku.common.valuechecker.ValueCheckerReentrantThreadLocal;
import com.goody.nus.se.gomoku.user.api.request.UserLoginRequest;
import com.goody.nus.se.gomoku.user.api.request.UserRegisterRequest;
import com.goody.nus.se.gomoku.user.api.request.UserResetPasswordRequest;
import com.goody.nus.se.gomoku.user.api.response.UserLoginResponse;
import com.goody.nus.se.gomoku.user.api.response.UserRegisterResponse;
import com.goody.nus.se.gomoku.user.api.response.UserResetPasswordResponse;
import com.goody.nus.se.gomoku.user.api.response.UserVerifyResponse;
import com.goody.nus.se.gomoku.user.model.dto.UserDTO;
import com.goody.nus.se.gomoku.user.model.dto.UserTokenDTO;
import com.goody.nus.se.gomoku.user.security.service.IEmailService;
import com.goody.nus.se.gomoku.user.security.service.IJwtTokenService;
import com.goody.nus.se.gomoku.user.security.service.IPasswordHashService;
import com.goody.nus.se.gomoku.user.security.service.IRsaSecurityService;
import com.goody.nus.se.gomoku.user.service.interfaces.IUserService;
import com.goody.nus.se.gomoku.user.service.interfaces.IUserTokenService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static com.goody.nus.se.gomoku.common.errorcode.ErrorCodeEnum.UNKNOWN_ERROR;
import static com.goody.nus.se.gomoku.common.errorcode.ErrorCodeEnum.USER_FAILED_TO_RETRIEVE_USER;
import static com.goody.nus.se.gomoku.common.errorcode.ErrorCodeEnum.USER_FAILED_TO_SAVE_USER;
import static com.goody.nus.se.gomoku.common.errorcode.ErrorCodeEnum.USER_INVALID_PASSWORD;
import static com.goody.nus.se.gomoku.common.errorcode.ErrorCodeEnum.USER_INVALID_VERIFICATION_CODE;
import static com.goody.nus.se.gomoku.common.errorcode.ErrorCodeEnum.USER_USER_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link UserLoginBizServiceImpl}
 * Uses Mockito to mock all dependencies for isolated testing
 * Focuses on exception branches to achieve 100% branch coverage
 */
@ExtendWith(MockitoExtension.class)
class UserLoginBizServiceImplUnitTest {

    @Mock
    private IRsaSecurityService rsaSecurityService;

    @Mock
    private IPasswordHashService passwordHashService;

    @Mock
    private IUserService userService;

    @Mock
    private IJwtTokenService jwtTokenService;

    @Mock
    private IUserTokenService userTokenService;

    @Mock
    private IEmailService emailService;

    @InjectMocks
    private UserLoginBizServiceImpl userLoginBizService;

    private String publicKeyString;
    private String encryptedPassword;
    private String plainPassword;
    private String passwordHash;
    private String salt;
    private UserDTO testUser;
    private MockedStatic<ValueCheckerReentrantThreadLocal> threadLocalMock;

    @BeforeEach
    void setUp() {
        publicKeyString = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA...";
        encryptedPassword = "encryptedPassword123";
        plainPassword = "Password123!";
        passwordHash = "hashedPassword123";
        salt = "randomSalt123";

        testUser = UserDTO.builder()
                .id(1L)
                .email("test@example.com")
                .nickname("TestUser")
                .passwordHash(passwordHash)
                .passwordSalt(salt)
                .avatarUrl("")
                .country("")
                .gender((byte) 0)
                .status((byte) 1)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @AfterEach
    void tearDown() {
        if (threadLocalMock != null) {
            threadLocalMock.close();
        }
    }

    // ==================== publicKey() Tests ====================

    @Test
    void publicKey_Success() throws Exception {
        when(rsaSecurityService.publicKey()).thenReturn(publicKeyString);

        String result = userLoginBizService.publicKey();

        assertEquals(publicKeyString, result);
        verify(rsaSecurityService).publicKey();
    }

    @Test
    void publicKey_ThrowsException_WhenRsaServiceFails() throws Exception {
        when(rsaSecurityService.publicKey()).thenThrow(new RuntimeException("RSA error"));

        BizException exception = assertThrows(BizException.class, () -> {
            userLoginBizService.publicKey();
        });

        assertEquals(UNKNOWN_ERROR, exception.getErrorCode());
        verify(rsaSecurityService).publicKey();
    }

    // ==================== register() Tests ====================

    @Test
    void register_Success() throws Exception {
        UserRegisterRequest request = UserRegisterRequest.builder()
                .email("newuser@example.com")
                .encryptedPassword(encryptedPassword)
                .nickname("NewUser")
                .verificationCode("123456")
                .build();

        UserDTO newUser = UserDTO.builder()
                .id(2L)
                .email(request.getEmail())
                .nickname(request.getNickname())
                .passwordHash(passwordHash)
                .passwordSalt(salt)
                .status((byte) 1)
                .createdAt(LocalDateTime.now())
                .build();
        String token = "jwt.token.here";

        when(passwordHashService.decryptPassword(encryptedPassword)).thenReturn(plainPassword);
        when(emailService.verifyCode(request.getEmail(), request.getVerificationCode())).thenReturn(true);
        when(passwordHashService.generateSalt()).thenReturn(salt);
        when(passwordHashService.hashPassword(plainPassword, salt)).thenReturn(passwordHash);
        when(userService.save(any(UserDTO.class))).thenReturn(2L);
        when(userService.findByEmail(request.getEmail())).thenReturn(newUser);
        when(jwtTokenService.generateToken(eq(newUser.getId()), anyString(), anyString(), anyString())).thenReturn(token);
        when(userTokenService.saveOrUpdate(any(UserTokenDTO.class))).thenReturn(1);

        UserRegisterResponse response = userLoginBizService.register(request);

        assertNotNull(response);
        assertEquals(token, response.getToken());
        assertEquals(newUser.getId(), response.getUserId());
        verify(passwordHashService).decryptPassword(encryptedPassword);
        verify(emailService).verifyCode(request.getEmail(), request.getVerificationCode());
        verify(userService).save(any(UserDTO.class));
    }

    @Test
    void register_ThrowsException_WhenPasswordDecryptionFails() throws Exception {
        UserRegisterRequest request = UserRegisterRequest.builder()
                .email("newuser@example.com")
                .encryptedPassword(encryptedPassword)
                .nickname("NewUser")
                .verificationCode("123456")
                .build();

        when(passwordHashService.decryptPassword(encryptedPassword)).thenThrow(new RuntimeException("Decryption failed"));

        BizException exception = assertThrows(BizException.class, () -> {
            userLoginBizService.register(request);
        });

        assertEquals(USER_INVALID_PASSWORD, exception.getErrorCode());
        verify(passwordHashService).decryptPassword(encryptedPassword);
        verify(emailService, never()).verifyCode(anyString(), anyString());
    }

    @Test
    void register_ThrowsException_WhenEmailVerificationFails() throws Exception {
        UserRegisterRequest request = UserRegisterRequest.builder()
                .email("newuser@example.com")
                .encryptedPassword(encryptedPassword)
                .nickname("NewUser")
                .verificationCode("wrong_code")
                .build();

        when(passwordHashService.decryptPassword(encryptedPassword)).thenReturn(plainPassword);
        when(emailService.verifyCode(request.getEmail(), request.getVerificationCode())).thenReturn(false);

        BizException exception = assertThrows(BizException.class, () -> {
            userLoginBizService.register(request);
        });

        assertEquals(USER_INVALID_VERIFICATION_CODE, exception.getErrorCode());
        verify(emailService).verifyCode(request.getEmail(), request.getVerificationCode());
        verify(userService, never()).save(any());
    }

    @Test
    void register_ThrowsException_WhenSaveFails() throws Exception {
        UserRegisterRequest request = UserRegisterRequest.builder()
                .email("newuser@example.com")
                .encryptedPassword(encryptedPassword)
                .nickname("NewUser")
                .verificationCode("123456")
                .build();

        when(passwordHashService.decryptPassword(encryptedPassword)).thenReturn(plainPassword);
        when(emailService.verifyCode(request.getEmail(), request.getVerificationCode())).thenReturn(true);
        when(passwordHashService.generateSalt()).thenReturn(salt);
        when(passwordHashService.hashPassword(plainPassword, salt)).thenReturn(passwordHash);
        when(userService.save(any(UserDTO.class))).thenReturn(0L);

        BizException exception = assertThrows(BizException.class, () -> {
            userLoginBizService.register(request);
        });

        assertEquals(USER_FAILED_TO_SAVE_USER, exception.getErrorCode());
        verify(userService).save(any(UserDTO.class));
    }

    @Test
    void register_ThrowsException_WhenRetrieveUserFails() throws Exception {
        UserRegisterRequest request = UserRegisterRequest.builder()
                .email("newuser@example.com")
                .encryptedPassword(encryptedPassword)
                .nickname("NewUser")
                .verificationCode("123456")
                .build();

        when(passwordHashService.decryptPassword(encryptedPassword)).thenReturn(plainPassword);
        when(emailService.verifyCode(request.getEmail(), request.getVerificationCode())).thenReturn(true);
        when(passwordHashService.generateSalt()).thenReturn(salt);
        when(passwordHashService.hashPassword(plainPassword, salt)).thenReturn(passwordHash);
        when(userService.save(any(UserDTO.class))).thenReturn(2L);
        when(userService.findByEmail(request.getEmail())).thenReturn(null);

        BizException exception = assertThrows(BizException.class, () -> {
            userLoginBizService.register(request);
        });

        assertEquals(USER_FAILED_TO_RETRIEVE_USER, exception.getErrorCode());
        verify(userService).findByEmail(request.getEmail());
    }

    // ==================== login() Tests ====================

    @Test
    void login_Success() throws Exception {
        UserLoginRequest request = UserLoginRequest.builder()
                .username(testUser.getEmail())
                .encryptedPassword(encryptedPassword)
                .build();

        String token = "jwt.token.here";

        // Mock ThreadLocal behavior
        threadLocalMock = mockStatic(ValueCheckerReentrantThreadLocal.class);
        threadLocalMock.when(() -> ValueCheckerReentrantThreadLocal.get(eq(UserDTO.class), any())).thenReturn(testUser);

        when(passwordHashService.decryptPassword(encryptedPassword)).thenReturn(plainPassword);
        when(passwordHashService.verifyPassword(plainPassword, testUser.getPasswordHash(), testUser.getPasswordSalt())).thenReturn(true);
        when(jwtTokenService.generateToken(eq(testUser.getId()), anyString(), anyString(), anyString())).thenReturn(token);
        when(userTokenService.saveOrUpdate(any(UserTokenDTO.class))).thenReturn(1);

        UserLoginResponse response = userLoginBizService.login(request);

        assertNotNull(response);
        assertEquals(token, response.getToken());
        assertEquals(testUser.getId(), response.getUserId());
        verify(passwordHashService).verifyPassword(plainPassword, testUser.getPasswordHash(), testUser.getPasswordSalt());
    }

    @Test
    void login_ThrowsException_WhenPasswordDecryptionFails() throws Exception {
        UserLoginRequest request = UserLoginRequest.builder()
                .username(testUser.getEmail())
                .encryptedPassword(encryptedPassword)
                .build();

        // Mock ThreadLocal behavior
        threadLocalMock = mockStatic(ValueCheckerReentrantThreadLocal.class);
        threadLocalMock.when(() -> ValueCheckerReentrantThreadLocal.get(eq(UserDTO.class), any())).thenReturn(testUser);

        when(passwordHashService.decryptPassword(encryptedPassword)).thenThrow(new RuntimeException("Decryption failed"));

        BizException exception = assertThrows(BizException.class, () -> {
            userLoginBizService.login(request);
        });

        assertEquals(USER_INVALID_PASSWORD, exception.getErrorCode());
        verify(passwordHashService).decryptPassword(encryptedPassword);
        verify(passwordHashService, never()).verifyPassword(anyString(), anyString(), anyString());
    }

    @Test
    void login_ThrowsException_WhenPasswordMismatch() throws Exception {
        UserLoginRequest request = UserLoginRequest.builder()
                .username(testUser.getEmail())
                .encryptedPassword(encryptedPassword)
                .build();

        // Mock ThreadLocal behavior
        threadLocalMock = mockStatic(ValueCheckerReentrantThreadLocal.class);
        threadLocalMock.when(() -> ValueCheckerReentrantThreadLocal.get(eq(UserDTO.class), any())).thenReturn(testUser);

        when(passwordHashService.decryptPassword(encryptedPassword)).thenReturn(plainPassword);
        when(passwordHashService.verifyPassword(plainPassword, testUser.getPasswordHash(), testUser.getPasswordSalt())).thenReturn(false);

        BizException exception = assertThrows(BizException.class, () -> {
            userLoginBizService.login(request);
        });

        assertEquals(USER_INVALID_PASSWORD, exception.getErrorCode());
        verify(passwordHashService).verifyPassword(plainPassword, testUser.getPasswordHash(), testUser.getPasswordSalt());
    }

    // ==================== verify() Tests ====================

    @Test
    void verify_Success() {
        String token = "jwt.token.here";
        UserTokenDTO userToken = UserTokenDTO.builder()
                .userId(testUser.getId())
                .refreshToken("refresh-token")
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        when(jwtTokenService.extractUserId(token)).thenReturn(testUser.getId());
        when(userTokenService.findByUserId(testUser.getId())).thenReturn(userToken);
        when(jwtTokenService.verifyToken(token, userToken.getRefreshToken())).thenReturn(testUser.getId());
        when(userService.findById(testUser.getId())).thenReturn(testUser);

        UserVerifyResponse result = userLoginBizService.verify(token);

        assertNotNull(result);
        assertTrue(result.getValid());
        assertEquals(testUser.getId(), result.getUserId());
        assertEquals(testUser.getEmail(), result.getEmail());
        verify(jwtTokenService).extractUserId(token);
        verify(userTokenService).findByUserId(testUser.getId());
    }

    @Test
    void verify_Success_WithBearerPrefix() {
        String token = "jwt.token.here";
        String bearerToken = "Bearer " + token;
        UserTokenDTO userToken = UserTokenDTO.builder()
                .userId(testUser.getId())
                .refreshToken("refresh-token")
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        when(jwtTokenService.extractUserId(token)).thenReturn(testUser.getId());
        when(userTokenService.findByUserId(testUser.getId())).thenReturn(userToken);
        when(jwtTokenService.verifyToken(token, userToken.getRefreshToken())).thenReturn(testUser.getId());
        when(userService.findById(testUser.getId())).thenReturn(testUser);

        UserVerifyResponse result = userLoginBizService.verify(bearerToken);

        assertNotNull(result);
        assertTrue(result.getValid());
        assertEquals(testUser.getId(), result.getUserId());
        verify(jwtTokenService).extractUserId(token);
    }

    @Test
    void verify_ReturnsFalse_WhenTokenIsNull() {
        UserVerifyResponse result = userLoginBizService.verify(null);

        assertNotNull(result);
        assertFalse(result.getValid());
        verify(jwtTokenService, never()).extractUserId(anyString());
    }

    @Test
    void verify_ReturnsFalse_WhenTokenIsEmpty() {
        UserVerifyResponse result = userLoginBizService.verify("");

        assertNotNull(result);
        assertFalse(result.getValid());
        verify(jwtTokenService, never()).extractUserId(anyString());
    }

    @Test
    void verify_ReturnsFalse_WhenExtractUserIdReturnsNull() {
        String token = "invalid.token";
        when(jwtTokenService.extractUserId(token)).thenReturn(null);

        UserVerifyResponse result = userLoginBizService.verify(token);

        assertNotNull(result);
        assertFalse(result.getValid());
        verify(jwtTokenService).extractUserId(token);
    }

    @Test
    void verify_ReturnsFalse_WhenUserTokenNotFound() {
        String token = "jwt.token.here";
        when(jwtTokenService.extractUserId(token)).thenReturn(testUser.getId());
        when(userTokenService.findByUserId(testUser.getId())).thenReturn(null);

        UserVerifyResponse result = userLoginBizService.verify(token);

        assertNotNull(result);
        assertFalse(result.getValid());
        verify(userTokenService).findByUserId(testUser.getId());
    }

    @Test
    void verify_ReturnsFalse_WhenRefreshTokenExpired() {
        String token = "jwt.token.here";
        UserTokenDTO expiredToken = UserTokenDTO.builder()
                .userId(testUser.getId())
                .refreshToken("refresh-token")
                .expiresAt(LocalDateTime.now().minusDays(1))
                .build();

        when(jwtTokenService.extractUserId(token)).thenReturn(testUser.getId());
        when(userTokenService.findByUserId(testUser.getId())).thenReturn(expiredToken);

        UserVerifyResponse result = userLoginBizService.verify(token);

        assertNotNull(result);
        assertFalse(result.getValid());
    }

    @Test
    void verify_ReturnsFalse_WhenJwtVerificationFails() {
        String token = "jwt.token.here";
        UserTokenDTO userToken = UserTokenDTO.builder()
                .userId(testUser.getId())
                .refreshToken("refresh-token")
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        when(jwtTokenService.extractUserId(token)).thenReturn(testUser.getId());
        when(userTokenService.findByUserId(testUser.getId())).thenReturn(userToken);
        when(jwtTokenService.verifyToken(token, userToken.getRefreshToken())).thenReturn(null);

        UserVerifyResponse result = userLoginBizService.verify(token);

        assertNotNull(result);
        assertFalse(result.getValid());
        verify(jwtTokenService).verifyToken(token, userToken.getRefreshToken());
    }

    @Test
    void verify_ReturnsFalse_WhenUserIdMismatch() {
        String token = "jwt.token.here";
        UserTokenDTO userToken = UserTokenDTO.builder()
                .userId(testUser.getId())
                .refreshToken("refresh-token")
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        when(jwtTokenService.extractUserId(token)).thenReturn(testUser.getId());
        when(userTokenService.findByUserId(testUser.getId())).thenReturn(userToken);
        when(jwtTokenService.verifyToken(token, userToken.getRefreshToken())).thenReturn(999L); // Different ID

        UserVerifyResponse result = userLoginBizService.verify(token);

        assertNotNull(result);
        assertFalse(result.getValid());
    }

    @Test
    void verify_ReturnsFalse_WhenUserNotFound() {
        String token = "jwt.token.here";
        UserTokenDTO userToken = UserTokenDTO.builder()
                .userId(testUser.getId())
                .refreshToken("refresh-token")
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        when(jwtTokenService.extractUserId(token)).thenReturn(testUser.getId());
        when(userTokenService.findByUserId(testUser.getId())).thenReturn(userToken);
        when(jwtTokenService.verifyToken(token, userToken.getRefreshToken())).thenReturn(testUser.getId());
        when(userService.findById(testUser.getId())).thenReturn(null);

        UserVerifyResponse result = userLoginBizService.verify(token);

        assertNotNull(result);
        assertFalse(result.getValid());
        verify(userService).findById(testUser.getId());
    }

    // ==================== resetPassword() Tests ====================

    @Test
    void resetPassword_Success() throws Exception {
        UserResetPasswordRequest request = UserResetPasswordRequest.builder()
                .email(testUser.getEmail())
                .verificationCode("123456")
                .encryptedNewPassword(encryptedPassword)
                .build();

        String newPasswordHash = "newHashedPassword123";
        String newSalt = "newSalt123";

        when(userService.findByEmail(request.getEmail())).thenReturn(testUser);
        when(emailService.verifyCode(request.getEmail(), request.getVerificationCode())).thenReturn(true);
        when(passwordHashService.decryptPassword(request.getEncryptedNewPassword())).thenReturn(plainPassword);
        when(passwordHashService.generateSalt()).thenReturn(newSalt);
        when(passwordHashService.hashPassword(plainPassword, newSalt)).thenReturn(newPasswordHash);
        when(userService.update(any(UserDTO.class))).thenReturn(1);
        when(userTokenService.deleteByUserId(testUser.getId())).thenReturn(1);

        UserResetPasswordResponse response = userLoginBizService.resetPassword(request);

        assertNotNull(response);
        assertEquals(testUser.getEmail(), response.getEmail());
        verify(userService).findByEmail(request.getEmail());
        verify(emailService).verifyCode(request.getEmail(), request.getVerificationCode());
        verify(userService).update(any(UserDTO.class));
        verify(userTokenService).deleteByUserId(testUser.getId());
    }

    @Test
    void resetPassword_ThrowsException_WhenUserNotFound() {
        UserResetPasswordRequest request = UserResetPasswordRequest.builder()
                .email("nonexistent@example.com")
                .verificationCode("123456")
                .encryptedNewPassword(encryptedPassword)
                .build();

        when(userService.findByEmail(request.getEmail())).thenReturn(null);

        BizException exception = assertThrows(BizException.class, () -> {
            userLoginBizService.resetPassword(request);
        });

        assertEquals(USER_USER_NOT_FOUND, exception.getErrorCode());
        verify(userService).findByEmail(request.getEmail());
        verify(emailService, never()).verifyCode(anyString(), anyString());
    }

    @Test
    void resetPassword_ThrowsException_WhenVerificationFails() throws Exception {
        UserResetPasswordRequest request = UserResetPasswordRequest.builder()
                .email(testUser.getEmail())
                .verificationCode("wrong_code")
                .encryptedNewPassword(encryptedPassword)
                .build();

        when(userService.findByEmail(request.getEmail())).thenReturn(testUser);
        when(emailService.verifyCode(request.getEmail(), request.getVerificationCode())).thenReturn(false);

        BizException exception = assertThrows(BizException.class, () -> {
            userLoginBizService.resetPassword(request);
        });

        assertEquals(USER_INVALID_VERIFICATION_CODE, exception.getErrorCode());
        verify(emailService).verifyCode(request.getEmail(), request.getVerificationCode());
        verify(passwordHashService, never()).decryptPassword(anyString());
    }

    @Test
    void resetPassword_ThrowsException_WhenPasswordDecryptionFails() throws Exception {
        UserResetPasswordRequest request = UserResetPasswordRequest.builder()
                .email(testUser.getEmail())
                .verificationCode("123456")
                .encryptedNewPassword(encryptedPassword)
                .build();

        when(userService.findByEmail(request.getEmail())).thenReturn(testUser);
        when(emailService.verifyCode(request.getEmail(), request.getVerificationCode())).thenReturn(true);
        when(passwordHashService.decryptPassword(request.getEncryptedNewPassword())).thenThrow(new RuntimeException("Decryption failed"));

        BizException exception = assertThrows(BizException.class, () -> {
            userLoginBizService.resetPassword(request);
        });

        assertEquals(USER_INVALID_PASSWORD, exception.getErrorCode());
        verify(passwordHashService).decryptPassword(request.getEncryptedNewPassword());
        verify(userService, never()).update(any());
    }

    @Test
    void resetPassword_ThrowsException_WhenUpdateFails() throws Exception {
        UserResetPasswordRequest request = UserResetPasswordRequest.builder()
                .email(testUser.getEmail())
                .verificationCode("123456")
                .encryptedNewPassword(encryptedPassword)
                .build();

        String newPasswordHash = "newHashedPassword123";
        String newSalt = "newSalt123";

        when(userService.findByEmail(request.getEmail())).thenReturn(testUser);
        when(emailService.verifyCode(request.getEmail(), request.getVerificationCode())).thenReturn(true);
        when(passwordHashService.decryptPassword(request.getEncryptedNewPassword())).thenReturn(plainPassword);
        when(passwordHashService.generateSalt()).thenReturn(newSalt);
        when(passwordHashService.hashPassword(plainPassword, newSalt)).thenReturn(newPasswordHash);
        when(userService.update(any(UserDTO.class))).thenReturn(0);

        BizException exception = assertThrows(BizException.class, () -> {
            userLoginBizService.resetPassword(request);
        });

        assertEquals(USER_FAILED_TO_SAVE_USER, exception.getErrorCode());
        verify(userService).update(any(UserDTO.class));
    }

    // ==================== logout() Tests ====================

    @Test
    void logout_Success() {
        Long userId = testUser.getId();
        when(userTokenService.deleteByUserId(userId)).thenReturn(1);

        // Should not throw exception
        assertDoesNotThrow(() -> userLoginBizService.logout(userId));

        verify(userTokenService).deleteByUserId(userId);
    }

    @Test
    void logout_WhenNoTokenFound() {
        Long userId = testUser.getId();
        when(userTokenService.deleteByUserId(userId)).thenReturn(0);

        // Should not throw exception, just logs a warning
        assertDoesNotThrow(() -> userLoginBizService.logout(userId));

        verify(userTokenService).deleteByUserId(userId);
    }
}
