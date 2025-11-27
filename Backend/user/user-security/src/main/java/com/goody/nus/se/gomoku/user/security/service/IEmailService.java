package com.goody.nus.se.gomoku.user.security.service;

public interface IEmailService {
    void sendVerificationCode(String email);

    boolean verifyCode(String email, String code);
}
