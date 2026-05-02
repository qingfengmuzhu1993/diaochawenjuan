package com.smartsurvey.module.user.service;

import com.smartsurvey.common.exception.BusinessException;
import com.smartsurvey.common.exception.ErrorCode;
import com.smartsurvey.module.user.dto.*;
import com.smartsurvey.module.user.entity.User;
import com.smartsurvey.module.user.mapper.UserMapper;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserMapper userMapper;

    public UserService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public UserProfileResponse getProfile(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return buildProfile(user);
    }

    public UserProfileResponse getProfileByUserId(Long targetUserId) {
        return getProfile(targetUserId);
    }

    public UserProfileResponse updateProfile(Long userId, UpdateProfileRequest req) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        if (req.getBio() != null) user.setBio(req.getBio());
        if (req.getTags() != null) user.setTags(req.getTags());
        if (req.getAvatarUrl() != null) user.setAvatarUrl(req.getAvatarUrl());
        userMapper.updateById(user);
        return buildProfile(user);
    }

    public void updateAvatar(Long userId, String avatarUrl) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        user.setAvatarUrl(avatarUrl);
        userMapper.updateById(user);
    }

    private UserProfileResponse buildProfile(User user) {
        UserProfileResponse resp = new UserProfileResponse();
        resp.setId(user.getId());
        resp.setUsername(user.getUsername());
        resp.setAvatarUrl(user.getAvatarUrl());
        resp.setBio(user.getBio());
        resp.setTags(user.getTags());
        resp.setLevel(user.getLevel());
        resp.setReputation(user.getReputation());
        resp.setRole(user.getRole());
        resp.setVerified(user.getIsVerified() != null && user.getIsVerified() == 1);
        return resp;
    }
}
