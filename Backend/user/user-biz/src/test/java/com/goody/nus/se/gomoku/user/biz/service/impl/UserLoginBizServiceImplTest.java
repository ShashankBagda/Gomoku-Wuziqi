package com.goody.nus.se.gomoku.user.biz.service.impl;

import com.goody.nus.se.gomoku.user.TestDbApplication;
import com.goody.nus.se.gomoku.user.api.request.UserLoginRequest;
import com.goody.nus.se.gomoku.user.api.request.UserRegisterRequest;
import com.goody.nus.se.gomoku.user.api.request.UserResetPasswordRequest;
import com.goody.nus.se.gomoku.user.api.response.UserLoginResponse;
import com.goody.nus.se.gomoku.user.api.response.UserRegisterResponse;
import com.goody.nus.se.gomoku.user.api.response.UserResetPasswordResponse;
import com.goody.nus.se.gomoku.user.api.response.UserVerifyResponse;
import com.goody.nus.se.gomoku.user.biz.service.IUserLoginBizService;
import com.goody.nus.se.gomoku.user.model.dto.UserDTO;
import com.goody.nus.se.gomoku.user.model.dto.UserTokenDTO;
import com.goody.nus.se.gomoku.user.security.util.RsaCryptoUtil;
import com.goody.nus.se.gomoku.user.service.interfaces.IUserService;
import com.goody.nus.se.gomoku.user.service.interfaces.IUserTokenService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration test for {@link UserLoginBizServiceImpl}
 * Uses @SpringBootTest to load full Spring context including AOP
 *
 * @author Haotian
 * @version 1.0, 2025/10/15
 */
@SpringBootTest(classes = TestDbApplication.class)
class UserLoginBizServiceImplTest {

    @Autowired
    private IUserLoginBizService userLoginBizService;

    @Autowired
    private IUserService userService;

    @Autowired
    private IUserTokenService userTokenService;

    private String testEmail;
    private String testNickname;
    private String plainPassword;
    private String encryptedPassword;

    @BeforeEach
    void setUp() throws Exception {
        // Use unique email and nickname for each test to avoid conflicts
        long timestamp = System.currentTimeMillis();
        testEmail = "test" + timestamp + "@example.com";
        testNickname = "testUser" + timestamp;
        plainPassword = "TestPassword123!";

        // Use real RSA encryption for testing
        encryptedPassword = RsaCryptoUtil.encrypt(plainPassword);
    }

    @AfterEach
    void tearDown() {
        // Clean up test data
        try {
            UserDTO user = userService.findByEmail(testEmail);
            if (user != null) {
                userTokenService.deleteByUserId(user.getId());
                userService.deleteById(user.getId());
            }
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }

    // ==================== Public Key Tests ====================

    @Test
    void publicKey_shouldReturnPublicKey() {
        // When
        String publicKey = userLoginBizService.publicKey();

        // Then
        assertNotNull(publicKey);
        assertTrue(publicKey.length() > 0);
    }

    // ==================== Register Tests ====================

    @Test
    void register_withValidData_shouldCreateUserSuccessfully() {
        // Given
        UserRegisterRequest request = UserRegisterRequest.builder()
                .email(testEmail)
                .nickname(testNickname)
                .encryptedPassword(encryptedPassword)
                .country("US")
                .gender((byte) 1)
                .build();

        // When
        UserRegisterResponse response = userLoginBizService.register(request);

        // Then
        assertNotNull(response);
        assertNotNull(response.getUserId());
        assertEquals(testEmail, response.getEmail());
        assertEquals(testNickname, response.getNickname());
        assertEquals((byte) 1, response.getStatus());

        // Verify user exists in database
        UserDTO user = userService.findByEmail(testEmail);
        assertNotNull(user);
        assertEquals(testEmail, user.getEmail());
        assertEquals(testNickname, user.getNickname());
    }

    // ==================== Login Tests ====================

    @Test
    void login_withValidCredentials_shouldReturnTokenAndCreateUserToken() {
        // Given - first register a user
        UserRegisterRequest registerRequest = UserRegisterRequest.builder()
                .email(testEmail)
                .nickname(testNickname)
                .encryptedPassword(encryptedPassword)
                .build();
        userLoginBizService.register(registerRequest);

        UserLoginRequest loginRequest = UserLoginRequest.builder()
                .username(testEmail)
                .encryptedPassword(encryptedPassword)
                .build();

        // When - AOP will automatically handle ValueChecker
        UserLoginResponse response = userLoginBizService.login(loginRequest);

        // Then
        assertNotNull(response);
        assertNotNull(response.getUserId());
        assertNotNull(response.getToken());
        assertEquals(testEmail, response.getEmail());
        assertEquals(testNickname, response.getNickname());

        // Verify user_token exists in database
        UserTokenDTO userToken = userTokenService.findByUserId(response.getUserId());
        assertNotNull(userToken);
        assertNotNull(userToken.getRefreshToken());
        assertNotNull(userToken.getExpiresAt());
    }

    // ==================== Verify Tests ====================

    @Test
    void verify_withValidToken_shouldReturnUserInfo() {
        // Given - register and login to get a token
        UserRegisterRequest registerRequest = UserRegisterRequest.builder()
                .email(testEmail)
                .nickname(testNickname)
                .encryptedPassword(encryptedPassword)
                .build();
        userLoginBizService.register(registerRequest);

        UserLoginRequest loginRequest = UserLoginRequest.builder()
                .username(testEmail)
                .encryptedPassword(encryptedPassword)
                .build();
        UserLoginResponse loginResponse = userLoginBizService.login(loginRequest);

        // When
        UserVerifyResponse verifyResponse = userLoginBizService.verify(loginResponse.getToken());

        // Then
        assertNotNull(verifyResponse);
        assertTrue(verifyResponse.getValid());
        assertEquals(loginResponse.getUserId(), verifyResponse.getUserId());
        assertEquals(testEmail, verifyResponse.getEmail());
        assertEquals(testNickname, verifyResponse.getNickname());
    }

    @Test
    void verify_withInvalidToken_shouldReturnInvalid() {
        // Given - completely invalid token format
        String invalidToken = "invalid.token.string";

        // When - verify should gracefully handle and return invalid
        // Note: The verify method catches all exceptions internally
        UserVerifyResponse response = userLoginBizService.verify(invalidToken);

        // Then
        assertNotNull(response);
        assertFalse(response.getValid());
        assertNull(response.getUserId());
    }

    @Test
    void verify_withMalformedJwt_shouldReturnInvalid() {
        // Given - malformed JWT (not 3 parts)
        String malformedToken = "malformed.token";

        // When
        UserVerifyResponse response = userLoginBizService.verify(malformedToken);

        // Then
        assertNotNull(response);
        assertFalse(response.getValid());
    }

    @Test
    void verify_withNullToken_shouldReturnInvalid() {
        // When
        UserVerifyResponse response = userLoginBizService.verify(null);

        // Then
        assertNotNull(response);
        assertFalse(response.getValid());
    }

    @Test
    void verify_withEmptyToken_shouldReturnInvalid() {
        // When
        UserVerifyResponse response = userLoginBizService.verify("");

        // Then
        assertNotNull(response);
        assertFalse(response.getValid());
    }

    @Test
    void verify_afterLogout_shouldReturnInvalid() {
        // Given - register, login, then logout
        UserRegisterRequest registerRequest = UserRegisterRequest.builder()
                .email(testEmail)
                .nickname(testNickname)
                .encryptedPassword(encryptedPassword)
                .build();
        userLoginBizService.register(registerRequest);

        UserLoginRequest loginRequest = UserLoginRequest.builder()
                .username(testEmail)
                .encryptedPassword(encryptedPassword)
                .build();
        UserLoginResponse loginResponse = userLoginBizService.login(loginRequest);

        // Logout
        userLoginBizService.logout(loginResponse.getUserId());

        // When - verify with old token
        UserVerifyResponse verifyResponse = userLoginBizService.verify(loginResponse.getToken());

        // Then - should be invalid because user_token was deleted
        assertNotNull(verifyResponse);
        assertFalse(verifyResponse.getValid());
    }

    // ==================== Logout Tests ====================

    @Test
    void logout_shouldDeleteUserToken() {
        // Given - register and login
        UserRegisterRequest registerRequest = UserRegisterRequest.builder()
                .email(testEmail)
                .nickname(testNickname)
                .encryptedPassword(encryptedPassword)
                .build();
        userLoginBizService.register(registerRequest);

        UserLoginRequest loginRequest = UserLoginRequest.builder()
                .username(testEmail)
                .encryptedPassword(encryptedPassword)
                .build();
        UserLoginResponse loginResponse = userLoginBizService.login(loginRequest);

        // Verify token exists before logout
        UserTokenDTO tokenBefore = userTokenService.findByUserId(loginResponse.getUserId());
        assertNotNull(tokenBefore);

        // When
        userLoginBizService.logout(loginResponse.getUserId());

        // Then
        UserTokenDTO tokenAfter = userTokenService.findByUserId(loginResponse.getUserId());
        assertNull(tokenAfter);
    }

    // ==================== Reset Password Tests ====================

    @Test
    void resetPassword_shouldUpdatePasswordAndDeleteToken() throws Exception {
        // Given - register and login
        UserRegisterRequest registerRequest = UserRegisterRequest.builder()
                .email(testEmail)
                .nickname(testNickname)
                .encryptedPassword(encryptedPassword)
                .build();
        userLoginBizService.register(registerRequest);

        UserLoginRequest loginRequest = UserLoginRequest.builder()
                .username(testEmail)
                .encryptedPassword(encryptedPassword)
                .build();
        UserLoginResponse loginResponse = userLoginBizService.login(loginRequest);

        // Verify token exists before reset
        UserTokenDTO tokenBefore = userTokenService.findByUserId(loginResponse.getUserId());
        assertNotNull(tokenBefore);

        // When - reset password with real RSA encryption
        String newPlainPassword = "NewPassword456!";
        String newEncryptedPassword = RsaCryptoUtil.encrypt(newPlainPassword);
        UserResetPasswordRequest resetRequest = UserResetPasswordRequest.builder()
                .email(testEmail)  // Email is now required in the request
                .encryptedNewPassword(newEncryptedPassword)
                .verificationCode("123456")  // Verification code is required (will be skipped in test due to verify-switch-off=true)
                .build();
        UserResetPasswordResponse resetResponse = userLoginBizService.resetPassword(resetRequest);

        // Then
        assertNotNull(resetResponse);
        assertEquals(testEmail, resetResponse.getEmail());
        assertEquals("Password reset successfully", resetResponse.getMessage());

        // Verify token was deleted
        UserTokenDTO tokenAfter = userTokenService.findByUserId(loginResponse.getUserId());
        assertNull(tokenAfter);

        // Verify old token is now invalid
        UserVerifyResponse verifyResponse = userLoginBizService.verify(loginResponse.getToken());
        assertFalse(verifyResponse.getValid());
    }

    // ==================== Integration Flow Tests ====================

    @Test
    void completeUserFlow_registerLoginVerifyLogout() {
        // 1. Register
        UserRegisterRequest registerRequest = UserRegisterRequest.builder()
                .email(testEmail)
                .nickname(testNickname)
                .encryptedPassword(encryptedPassword)
                .country("SG")
                .gender((byte) 2)
                .build();
        UserRegisterResponse registerResponse = userLoginBizService.register(registerRequest);
        assertNotNull(registerResponse);
        assertNotNull(registerResponse.getUserId());

        // 2. Login
        UserLoginRequest loginRequest = UserLoginRequest.builder()
                .username(testEmail)
                .encryptedPassword(encryptedPassword)
                .build();
        UserLoginResponse loginResponse = userLoginBizService.login(loginRequest);
        assertNotNull(loginResponse);
        assertNotNull(loginResponse.getToken());

        // 3. Verify token
        UserVerifyResponse verifyResponse = userLoginBizService.verify(loginResponse.getToken());
        assertTrue(verifyResponse.getValid());
        assertEquals(testEmail, verifyResponse.getEmail());

        // 4. Logout
        userLoginBizService.logout(loginResponse.getUserId());

        // 5. Verify token is now invalid
        UserVerifyResponse verifyAfterLogout = userLoginBizService.verify(loginResponse.getToken());
        assertFalse(verifyAfterLogout.getValid());
    }

    @Test
    void multipleLogins_shouldReplaceToken() {
        // Given - register user
        UserRegisterRequest registerRequest = UserRegisterRequest.builder()
                .email(testEmail)
                .nickname(testNickname)
                .encryptedPassword(encryptedPassword)
                .build();
        userLoginBizService.register(registerRequest);

        UserLoginRequest loginRequest = UserLoginRequest.builder()
                .username(testEmail)
                .encryptedPassword(encryptedPassword)
                .build();

        // When - login twice
        UserLoginResponse firstLogin = userLoginBizService.login(loginRequest);
        UserTokenDTO firstToken = userTokenService.findByUserId(firstLogin.getUserId());

        // Sleep a bit to ensure different token
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            // Ignore
        }

        UserLoginResponse secondLogin = userLoginBizService.login(loginRequest);
        UserTokenDTO secondToken = userTokenService.findByUserId(secondLogin.getUserId());

        // Then - should have new token
        assertNotNull(firstToken);
        assertNotNull(secondToken);
        // Tokens should be different
        assertFalse(firstToken.getRefreshToken().equals(secondToken.getRefreshToken()));

        // First token should be invalid now
        UserVerifyResponse verifyFirst = userLoginBizService.verify(firstLogin.getToken());
        assertFalse(verifyFirst.getValid());

        // Second token should be valid
        UserVerifyResponse verifySecond = userLoginBizService.verify(secondLogin.getToken());
        assertTrue(verifySecond.getValid());
    }
}
