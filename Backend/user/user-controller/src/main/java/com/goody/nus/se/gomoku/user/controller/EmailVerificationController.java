package com.goody.nus.se.gomoku.user.controller;

import com.goody.nus.se.gomoku.user.api.request.UserEmailVerifyRequest;
import com.goody.nus.se.gomoku.user.security.service.IEmailService;
import com.goody.nus.se.gomoku.web.base.response.ApiResult;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;

@RestController
@RequestMapping("/verify/send-email")
@RequiredArgsConstructor
public class EmailVerificationController {
    private final IEmailService emailService;

    @Autowired
    @Lazy
    private Executor bizThreadPool;

    @PostMapping("/send")
    public CompletionStage<ApiResult<String>> sendVerificationCode(@RequestBody UserEmailVerifyRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            emailService.sendVerificationCode(request.getEmail());
            final String response = "Verification code sent to " + request.getEmail();
            return ApiResult.success(response);
        }, bizThreadPool);
    }
}
