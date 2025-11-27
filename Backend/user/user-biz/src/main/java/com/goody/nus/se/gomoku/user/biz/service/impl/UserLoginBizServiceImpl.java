package com.goody.nus.se.gomoku.user.biz.service.impl;

import com.goody.nus.se.gomoku.common.exception.BizException;
import com.goody.nus.se.gomoku.common.valuechecker.ValueCheckerReentrantThreadLocal;
import com.goody.nus.se.gomoku.common.valuechecker.ValueCheckers;
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
import com.goody.nus.se.gomoku.user.security.service.IEmailService;
import com.goody.nus.se.gomoku.user.security.service.IJwtTokenService;
import com.goody.nus.se.gomoku.user.security.service.IPasswordHashService;
import com.goody.nus.se.gomoku.user.security.service.IRsaSecurityService;
import com.goody.nus.se.gomoku.user.service.interfaces.IUserService;
import com.goody.nus.se.gomoku.user.service.interfaces.IUserTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static com.goody.nus.se.gomoku.common.errorcode.ErrorCodeEnum.*;

/**
 * {@link IUserLoginBizService} impl
 *
 * @author Haotian
 * @version 1.0, 2025/10/3
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserLoginBizServiceImpl implements IUserLoginBizService {

    private final IRsaSecurityService rsaSecurityService;
    private final IPasswordHashService passwordHashService;
    private final IUserService userService;
    private final IJwtTokenService jwtTokenService;
    private final IUserTokenService userTokenService;
    private final IEmailService emailService;

    @Override
    public String publicKey() {
        log.debug("Retrieving RSA public key");
        try {
            return this.rsaSecurityService.publicKey();
        } catch (Exception e) {
            log.error("Failed to retrieve RSA public key", e);
            throw new BizException(UNKNOWN_ERROR);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @ValueCheckers(checkers = {
            @ValueCheckers.ValueChecker(method = "validateEmailUniqueness", keys = "#request.email", handler = UserValidationBizServiceImpl.class)
    })
    public UserRegisterResponse register(UserRegisterRequest request) {
        log.info("User registration started: email={}, nickname={}", request.getEmail(), request.getNickname());
        // 1. Decrypt password from RSA encryption
        String plainPassword;
        try {
            plainPassword = passwordHashService.decryptPassword(request.getEncryptedPassword());
        } catch (Exception e) {
            log.warn("Failed to decrypt password during registration: email={}", request.getEmail());
            throw new BizException(USER_INVALID_PASSWORD);
        }

        // 1.1 Verify email code
        if (emailService.verifyCode(request.getEmail(), request.getVerificationCode())) {
            log.info("Email verification succeeded for email={}", request.getEmail());
        } else {
            log.warn("Email verification failed for email={}", request.getEmail());
            throw new BizException(USER_INVALID_VERIFICATION_CODE);
        }


        // 2. Generate UUID salt
        String salt = passwordHashService.generateSalt();

        // 3. Hash password with salt
        String passwordHash = passwordHashService.hashPassword(plainPassword, salt);

        // 4. Create user entity
        UserDTO userDTO = UserDTO.builder()
                .email(request.getEmail())
                .nickname(request.getNickname())
                .passwordHash(passwordHash)
                .passwordSalt(salt)
                .avatarUrl(request.getAvatarUrl() != null ? request.getAvatarUrl() : "")
                .avatarBase64(request.getAvatarBase64() != null ? request.getAvatarBase64() : "")
                .country(request.getCountry() != null ? request.getCountry() : "")
                .gender(request.getGender() != null ? request.getGender() : (byte) 0)
                .status((byte) 1)
                .build();

        // 5. Save to database
        log.debug("Saving user to database: email={}", request.getEmail());
        Long result = userService.save(userDTO);
        if (result == 0) {
            log.error("Failed to save user to database: email={}", request.getEmail());
            throw new BizException(USER_FAILED_TO_SAVE_USER);
        }

        // 6. Query the saved user to get the generated ID
        UserDTO savedUser = userService.findByEmail(request.getEmail());
        if (savedUser == null) {
            log.error("Failed to retrieve saved user: email={}", request.getEmail());
            throw new BizException(USER_FAILED_TO_RETRIEVE_USER);
        }

        log.info("User registration completed successfully: userId={}, email={}", savedUser.getId(), savedUser.getEmail());

        // 7. Generate refresh token and JWT token (auto-login after registration)
        String refreshToken = UUID.randomUUID().toString();
        UserTokenDTO userToken = UserTokenDTO.builder()
                .userId(savedUser.getId())
                .refreshToken(refreshToken)
                .expiresAt(java.time.LocalDateTime.now().plusDays(7)) // 7 days expiration
                .build();

        int tokenSaved = userTokenService.saveOrUpdate(userToken);
        if (tokenSaved == 0) {
            log.warn("Failed to save user token for userId={}", savedUser.getId());
            throw new BizException(USER_FAILED_TO_SAVE_USER);
        }

        // 8. Generate JWT token with refresh token
        String token = jwtTokenService.generateToken(savedUser.getId(), savedUser.getEmail(), savedUser.getNickname(), refreshToken);

        // 9. Build response with token
        return UserRegisterResponse.builder()
                .userId(savedUser.getId())
                .email(savedUser.getEmail())
                .nickname(savedUser.getNickname())
                .avatarUrl(savedUser.getAvatarUrl())
                .country(savedUser.getCountry())
                .gender(savedUser.getGender())
                .status(savedUser.getStatus())
                .createdAt(savedUser.getCreatedAt())
                .token(token)
                .build();
    }

    @Override
    @ValueCheckers(checkers = {
            @ValueCheckers.ValueChecker(method = "validateAccountStatus", keys = "#request.username", handler = UserValidationBizServiceImpl.class)
    })
    public UserLoginResponse login(UserLoginRequest request) {
        log.info("User login started: username={}", request.getUsername());

        // 1. Get user from ThreadLocal (already queried and validated by ValueChecker)
        UserDTO user = ValueCheckerReentrantThreadLocal.get(UserDTO.class, () -> {
            throw new BizException(USER_USER_NOT_FOUND);
        });

        // 2. Decrypt password from RSA encryption
        String plainPassword;
        try {
            plainPassword = passwordHashService.decryptPassword(request.getEncryptedPassword());
        } catch (Exception e) {
            log.warn("Failed to decrypt password during login: username={}", request.getUsername());
            throw new BizException(USER_INVALID_PASSWORD);
        }

        // 3. Verify password with stored salt
        log.debug("Verifying password for userId={}", user.getId());
        boolean passwordMatches = passwordHashService.verifyPassword(
                plainPassword,
                user.getPasswordHash(),
                user.getPasswordSalt()
        );

        if (!passwordMatches) {
            log.warn("Invalid password for userId={}", user.getId());
            throw new BizException(USER_INVALID_PASSWORD);
        }

        // 4. Generate or update refresh token
        String refreshToken = UUID.randomUUID().toString();
        UserTokenDTO userToken = UserTokenDTO.builder()
                .userId(user.getId())
                .refreshToken(refreshToken)
                .expiresAt(java.time.LocalDateTime.now().plusDays(7)) // 7 days expiration
                .build();

        int tokenSaved = userTokenService.saveOrUpdate(userToken);
        if (tokenSaved == 0) {
            log.warn("Failed to save user token for userId={}", user.getId());
            throw new BizException(USER_FAILED_TO_SAVE_USER);
        }

        // 5. Generate JWT token with refresh token
        String token = jwtTokenService.generateToken(user.getId(), user.getEmail(), user.getNickname(), refreshToken);

        log.info("User login completed successfully: userId={}, email={}", user.getId(), user.getEmail());

        // 6. Build response
        return UserLoginResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .avatarUrl(user.getAvatarUrl())
                .token(token)
                .build();
    }

    @Override
    public UserVerifyResponse verify(String token) {
        if (token == null || token.isEmpty()) {
            log.warn("Token is null or empty");
            return UserVerifyResponse.builder().valid(false).build();
        }

        // Remove "Bearer " prefix if present
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // 1. Extract userId from JWT WITHOUT verifying signature
        Long userId = jwtTokenService.extractUserId(token);
        if (userId == null) {
            log.warn("Failed to extract userId from JWT");
            return UserVerifyResponse.builder()
                    .valid(false)
                    .build();
        }

        // 2. Query database to get refresh token by userId
        UserTokenDTO userToken = userTokenService.findByUserId(userId);
        if (userToken == null) {
            log.debug("No refresh token found in database for userId={}", userId);
            return UserVerifyResponse.builder()
                    .valid(false)
                    .build();
        }

        // 3. Check if refresh token is expired
        if (userToken.getExpiresAt().isBefore(java.time.LocalDateTime.now())) {
            log.warn("Refresh token expired for userId={}", userId);
            return UserVerifyResponse.builder()
                    .valid(false)
                    .build();
        }

        // 4. Verify JWT signature using refresh token from database
        Long verifiedUserId = jwtTokenService.verifyToken(token, userToken.getRefreshToken());
        if (verifiedUserId == null) {
            log.warn("JWT signature verification failed for userId={}", userId);
            return UserVerifyResponse.builder()
                    .valid(false)
                    .build();
        }

        // 5. Verify userId matches
        if (!verifiedUserId.equals(userId)) {
            log.warn("User ID mismatch: extracted={}, verified={}", userId, verifiedUserId);
            return UserVerifyResponse.builder()
                    .valid(false)
                    .build();
        }

        // 6. Query user info from database
        UserDTO user = userService.findById(userId);
        if (user == null) {
            log.warn("User not found for userId={}", userId);
            return UserVerifyResponse.builder()
                    .valid(false)
                    .build();
        }

        log.info("Token verified successfully: userId={}", userId);
        return UserVerifyResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .valid(true)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserResetPasswordResponse resetPassword(UserResetPasswordRequest request) {
        log.info("Password reset started for email={}", request.getEmail());

        // 1. Query user by email
        UserDTO user = userService.findByEmail(request.getEmail());
        if (user == null) {
            log.error("User not found for email={}", request.getEmail());
            throw new BizException(USER_USER_NOT_FOUND);
        }

        // 2. Verify email verification code
        if (emailService.verifyCode(request.getEmail(), request.getVerificationCode())) {
            log.info("Email verification succeeded for email={}", request.getEmail());
        } else {
            log.warn("Email verification failed for email={}", request.getEmail());
            throw new BizException(USER_INVALID_VERIFICATION_CODE);
        }

        // 3. Decrypt new password from RSA encryption
        String plainNewPassword;
        try {
            plainNewPassword = passwordHashService.decryptPassword(request.getEncryptedNewPassword());
        } catch (Exception e) {
            log.warn("Failed to decrypt new password for email={}", request.getEmail());
            throw new BizException(USER_INVALID_PASSWORD);
        }

        // 4. Generate new salt
        String newSalt = passwordHashService.generateSalt();

        // 5. Hash new password with new salt
        String newPasswordHash = passwordHashService.hashPassword(plainNewPassword, newSalt);

        // 6. Update user password
        UserDTO updateDTO = UserDTO.builder()
                .id(user.getId())
                .passwordHash(newPasswordHash)
                .passwordSalt(newSalt)
                .build();

        int result = userService.update(updateDTO);
        if (result == 0) {
            log.error("Failed to update password for email={}", request.getEmail());
            throw new BizException(USER_FAILED_TO_SAVE_USER);
        }

        log.info("Password reset completed successfully for userId={}, email={}", user.getId(), user.getEmail());

        // 7. Delete user token to force re-login
        userTokenService.deleteByUserId(user.getId());
        log.debug("User token deleted for userId={}, user must re-login", user.getId());

        // 8. Build response
        return UserResetPasswordResponse.builder()
                .email(user.getEmail())
                .message("Password reset successfully")
                .build();
    }

    @Override
    public void logout(Long userId) {
        log.info("User logout started for userId={}", userId);

        // Delete user token from database
        int deleted = userTokenService.deleteByUserId(userId);

        if (deleted > 0) {
            log.info("User logout completed successfully for userId={}", userId);
        } else {
            log.warn("No token found to delete for userId={}", userId);
        }
    }
}
