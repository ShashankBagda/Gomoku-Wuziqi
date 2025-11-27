package com.goody.nus.se.gomoku.common.errorcode;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * common error code
 *
 * @author Goody
 * @version 1.0, 2020/3/11 16:27
 * @since 1.0.0
 */
@AllArgsConstructor
@Getter
public enum ErrorCodeEnum {
    /** 成功 */
    OK(0, "Success"),
    UNKNOWN_ERROR(1, "Unknown error occurred"),
    BAD_REQUEST(2, "Invalid request"),
    UNAUTHORIZED(3, "Unauthorized access"),
    FORBIDDEN(4, "Access forbidden"),
    METHOD_NOT_ALLOWED(5, "Method not allowed"),
    RETRY(6, "Please retry"),
    TIMEOUT(7, "Request timeout"),
    RATE_LIMITER_EXCEEDED(8, "Rate limit exceeded"),
    INVALID_VERSION(9, "Invalid version"),
    SERVICE_RATE_LIMITER_EXCEEDED(10, "Service rate limit exceeded"),
    VERSION_TOO_LOW(11, "Version too low"),
    FUNCTION_PAUSE_TEMPORARY(12, "Function temporarily paused"),

    // User related errors (10000-11000)
    USER_EMAIL_ALREADY_EXISTS(10000, "Email already exists"),
    USER_NICKNAME_ALREADY_EXISTS(10001, "Nickname already exists"),
    USER_USER_NOT_FOUND(10002, "User not found"),
    USER_ACCOUNT_INACTIVE(10003, "Account is inactive"),
    USER_ACCOUNT_DISABLED(10004, "Account is disabled"),
    USER_ACCOUNT_DELETED(10005, "Account has been deleted"),
    USER_INVALID_PASSWORD(10006, "Invalid password"),
    USER_FAILED_TO_SAVE_USER(10007, "Failed to save user"),
    USER_FAILED_TO_RETRIEVE_USER(10008, "Failed to retrieve user"),
    USER_INVALID_VERIFICATION_CODE(10009, "Invalid verification code"),

    // Game related errors (20000-21000)
    GAME_NOT_FOUND(20000, "Game not found"),
    PLAYER_NOT_IN_GAME(20001, "Player not in game"),
    INVALID_GAME_ACTION(20002, "Invalid game action"),
    ROOM_NOT_FOUND(20003, "Room not found"),
    PLAYER_IN_MATCH_QUEUE(20004, "Player is currently in match queue: {0}. Please cancel queue first."),
    ;

    private final int errorCode;
    private final String message;
}
