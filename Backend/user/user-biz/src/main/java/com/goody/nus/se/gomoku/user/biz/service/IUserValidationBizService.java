package com.goody.nus.se.gomoku.user.biz.service;

/**
 * User validation business service
 *
 * @author Haotian
 * @version 1.0, 2025/10/4
 */
public interface IUserValidationBizService {

    /**
     * Validate email uniqueness
     *
     * @param email user email
     */
    void validateEmailUniqueness(String email);

    /**
     * Validate nickname uniqueness
     *
     * @param nickname user nickname
     */
    void validateNicknameUniqueness(String nickname);

    /**
     * Validate user account status
     *
     * @param username user email or nickname
     */
    void validateAccountStatus(String username);
}
