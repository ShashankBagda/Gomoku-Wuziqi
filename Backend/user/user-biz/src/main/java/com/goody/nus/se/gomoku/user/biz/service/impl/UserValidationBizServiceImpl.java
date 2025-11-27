package com.goody.nus.se.gomoku.user.biz.service.impl;

import com.goody.nus.se.gomoku.common.exception.BizException;
import com.goody.nus.se.gomoku.common.valuechecker.IValueCheckerHandler;
import com.goody.nus.se.gomoku.common.valuechecker.ValueCheckerReentrantThreadLocal;
import com.goody.nus.se.gomoku.user.biz.service.IUserValidationBizService;
import com.goody.nus.se.gomoku.user.model.dto.UserDTO;
import com.goody.nus.se.gomoku.user.service.interfaces.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.goody.nus.se.gomoku.common.errorcode.ErrorCodeEnum.*;

/**
 * {@link IUserValidationBizService} implementation
 *
 * @author Haotian
 * @version 1.0, 2025/10/4
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserValidationBizServiceImpl implements IUserValidationBizService, IValueCheckerHandler {

    private final IUserService userService;

    @Override
    public void validateEmailUniqueness(String email) {
        log.debug("Validating email uniqueness: {}", email);
        if (userService.existsByEmail(email)) {
            log.warn("Email already exists: {}", email);
            throw new BizException(USER_EMAIL_ALREADY_EXISTS);
        }
    }

    @Override
    public void validateNicknameUniqueness(String nickname) {
        log.debug("Validating nickname uniqueness: {}", nickname);
        if (userService.existsByNickname(nickname)) {
            log.warn("Nickname already exists: {}", nickname);
            throw new BizException(USER_NICKNAME_ALREADY_EXISTS);
        }
    }

    @Override
    public void validateAccountStatus(String username) {
        log.debug("Validating account status for username: {}", username);
        // Query user and store in ThreadLocal for later reuse
        UserDTO user = ValueCheckerReentrantThreadLocal.get(UserDTO.class, () -> {
            UserDTO queriedUser = userService.findByEmail(username);
            if (queriedUser == null) {
                log.warn("User not found: {}", username);
                throw new BizException(USER_USER_NOT_FOUND);
            }
            log.debug("User found: userId={}, status={}", queriedUser.getId(), queriedUser.getStatus());
            return queriedUser;
        });

        if (user.getStatus() == 0) {
            log.warn("Account is inactive: userId={}", user.getId());
            throw new BizException(USER_ACCOUNT_INACTIVE);
        } else if (user.getStatus() == 2) {
            log.warn("Account is disabled: userId={}", user.getId());
            throw new BizException(USER_ACCOUNT_DISABLED);
        } else if (user.getStatus() == 3) {
            log.warn("Account is deleted: userId={}", user.getId());
            throw new BizException(USER_ACCOUNT_DELETED);
        }
        log.debug("Account status validation passed for userId={}", user.getId());
    }
}
