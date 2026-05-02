package com.smartsurvey.module.marketplace.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartsurvey.module.marketplace.entity.Badge;
import com.smartsurvey.module.marketplace.entity.UserBadge;
import com.smartsurvey.module.marketplace.mapper.BadgeMapper;
import com.smartsurvey.module.marketplace.mapper.UserBadgeMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class BadgeService {
    private final BadgeMapper badgeMapper;
    private final UserBadgeMapper userBadgeMapper;

    public BadgeService(BadgeMapper badgeMapper, UserBadgeMapper userBadgeMapper) {
        this.badgeMapper = badgeMapper;
        this.userBadgeMapper = userBadgeMapper;
    }

    public void checkAndAward(Long userId, String conditionType, int currentValue) {
        List<Badge> badges = badgeMapper.selectList(
            new LambdaQueryWrapper<Badge>().eq(Badge::getConditionType, conditionType));
        for (Badge badge : badges) {
            if (currentValue >= badge.getConditionValue()) {
                Long count = userBadgeMapper.selectCount(
                    new LambdaQueryWrapper<UserBadge>()
                        .eq(UserBadge::getUserId, userId)
                        .eq(UserBadge::getBadgeId, badge.getId()));
                if (count == 0) {
                    UserBadge ub = new UserBadge();
                    ub.setUserId(userId);
                    ub.setBadgeId(badge.getId());
                    ub.setEarnedAt(LocalDateTime.now());
                    userBadgeMapper.insert(ub);
                }
            }
        }
    }

    public List<Map<String, Object>> getUserBadges(Long userId) {
        List<UserBadge> userBadges = userBadgeMapper.selectList(
            new LambdaQueryWrapper<UserBadge>().eq(UserBadge::getUserId, userId));
        List<Map<String, Object>> result = new ArrayList<>();
        for (UserBadge ub : userBadges) {
            Badge badge = badgeMapper.selectById(ub.getBadgeId());
            if (badge == null) continue;
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("name", badge.getName());
            m.put("description", badge.getDescription());
            m.put("icon", badge.getIcon());
            m.put("earnedAt", ub.getEarnedAt());
            result.add(m);
        }
        return result;
    }
}
