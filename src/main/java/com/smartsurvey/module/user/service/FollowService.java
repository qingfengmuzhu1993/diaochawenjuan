package com.smartsurvey.module.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartsurvey.common.exception.BusinessException;
import com.smartsurvey.common.exception.ErrorCode;
import com.smartsurvey.module.user.entity.Follow;
import com.smartsurvey.module.user.mapper.FollowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class FollowService {
    private final FollowMapper followMapper;
    private final NotificationService notificationService;

    public FollowService(FollowMapper followMapper, NotificationService notificationService) {
        this.followMapper = followMapper;
        this.notificationService = notificationService;
    }

    @Transactional
    public void follow(Long followerId, Long followeeId) {
        if (followerId.equals(followeeId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "不能关注自己");
        }
        Long count = followMapper.selectCount(new LambdaQueryWrapper<Follow>()
            .eq(Follow::getFollowerId, followerId)
            .eq(Follow::getFolloweeId, followeeId));
        if (count > 0) return;

        Long followingCount = followMapper.selectCount(new LambdaQueryWrapper<Follow>()
            .eq(Follow::getFollowerId, followerId));
        if (followingCount >= 2000) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "关注人数已达上限2000人");
        }

        Follow follow = new Follow();
        follow.setFollowerId(followerId);
        follow.setFolloweeId(followeeId);
        follow.setCreatedAt(LocalDateTime.now());
        followMapper.insert(follow);

        notificationService.create(followeeId, "interaction", "有人关注了你", null, null, "follow");
    }

    @Transactional
    public void unfollow(Long followerId, Long followeeId) {
        followMapper.delete(new LambdaQueryWrapper<Follow>()
            .eq(Follow::getFollowerId, followerId)
            .eq(Follow::getFolloweeId, followeeId));
    }

    public boolean isFollowing(Long followerId, Long followeeId) {
        return followMapper.selectCount(new LambdaQueryWrapper<Follow>()
            .eq(Follow::getFollowerId, followerId)
            .eq(Follow::getFolloweeId, followeeId)) > 0;
    }

    public List<Map<String, Object>> getFollowers(Long userId) {
        return followMapper.getFollowers(userId);
    }

    public List<Map<String, Object>> getFollowing(Long userId) {
        return followMapper.getFollowing(userId);
    }
}
