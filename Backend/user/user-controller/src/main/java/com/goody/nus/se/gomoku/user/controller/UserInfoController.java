package com.goody.nus.se.gomoku.user.controller;

import com.goody.nus.se.gomoku.user.api.response.UserInfoResponse;
import com.goody.nus.se.gomoku.user.biz.service.IUserInfoService;
import com.goody.nus.se.gomoku.web.base.response.ApiResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class UserInfoController {
    private final IUserInfoService userInfoService;

    @GetMapping("/{userId}")
    public ApiResult<UserInfoResponse> getUserInfoById(@PathVariable Long userId) {
        return ApiResult.success(userInfoService.getUserInfoById(userId));
    }
}
