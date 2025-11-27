package com.goody.nus.se.gomoku.user.biz.service.impl;

import com.goody.nus.se.gomoku.common.exception.BizException;
import com.goody.nus.se.gomoku.user.api.response.UserInfoResponse;
import com.goody.nus.se.gomoku.user.biz.service.IUserInfoService;
import com.goody.nus.se.gomoku.user.service.interfaces.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.goody.nus.se.gomoku.common.errorcode.ErrorCodeEnum.USER_USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class UserInfoServiceImpl implements IUserInfoService {
    private final IUserService userMapper;

    @Override
    public UserInfoResponse getUserInfoById(Long userId) {
        // use stream to map entity to dto
        return Optional.ofNullable(userMapper.findById(userId))
                .map(user -> UserInfoResponse.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .nickname(user.getNickname())
                        .avatarUrl(user.getAvatarUrl())
                        .country(user.getCountry())
                        .gender(user.getGender())
                        .avatarBase64(user.getAvatarBase64())
                        .build())
                .orElseThrow(() -> new BizException(USER_USER_NOT_FOUND, "User not found with id: " + userId));
    }
}
