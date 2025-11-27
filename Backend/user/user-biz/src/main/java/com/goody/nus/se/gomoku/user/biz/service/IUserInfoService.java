package com.goody.nus.se.gomoku.user.biz.service;

import com.goody.nus.se.gomoku.user.api.response.UserInfoResponse;

public interface IUserInfoService {
    UserInfoResponse getUserInfoById(Long userId);
}
