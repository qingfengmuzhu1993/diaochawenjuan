package com.smartsurvey.module.marketplace.service;

import com.smartsurvey.common.exception.BusinessException;
import com.smartsurvey.common.exception.ErrorCode;
import com.smartsurvey.common.utils.RedisUtils;
import com.smartsurvey.module.marketplace.dto.LeaderboardResponse;
import com.smartsurvey.module.user.entity.User;
import com.smartsurvey.module.user.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.WeekFields;
import java.util.*;

@Service
public class GamificationService {
    private final RedisUtils redisUtils;
    private final UserMapper userMapper;

    public GamificationService(RedisUtils redisUtils, UserMapper userMapper) {
        this.redisUtils = redisUtils;
        this.userMapper = userMapper;
    }

    public void updateLeaderboard(Long userId, BigDecimal reward) {
        String dailyKey = "leaderboard:daily:" + LocalDate.now();
        String weeklyKey = "leaderboard:weekly:" + YearMonth.now() + ":W" +
            LocalDate.now().get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear());
        redisUtils.incrementScore(dailyKey, userId.toString(), reward.doubleValue());
        redisUtils.incrementScore(weeklyKey, userId.toString(), reward.doubleValue());
    }

    public LeaderboardResponse getLeaderboard(String period) {
        String key = "leaderboard:" + period + ":";
        if ("daily".equals(period)) {
            key += LocalDate.now();
        } else if ("weekly".equals(period)) {
            key += YearMonth.now() + ":W" +
                LocalDate.now().get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear());
        } else {
            key += LocalDate.now();
        }

        Set<Map.Entry<String, Double>> entries = redisUtils.getZSetTop(key, 10);
        LeaderboardResponse resp = new LeaderboardResponse();
        resp.setPeriod(period);
        List<LeaderboardResponse.Entry> list = new ArrayList<>();
        int rank = 1;
        for (Map.Entry<String, Double> e : entries) {
            Long uid = Long.valueOf(e.getKey());
            User user = userMapper.selectById(uid);
            LeaderboardResponse.Entry entry = new LeaderboardResponse.Entry();
            entry.setUserId(uid);
            entry.setRank(rank++);
            entry.setEarnings(BigDecimal.valueOf(e.getValue()));
            entry.setUsername(user != null ? user.getUsername() : "未知用户");
            entry.setAvatarUrl(user != null ? user.getAvatarUrl() : null);
            list.add(entry);
        }
        resp.setEntries(list);
        return resp;
    }

    public String checkIn(Long userId) {
        String key = "checkin:" + YearMonth.now();
        long dayOfMonth = LocalDate.now().getDayOfMonth();
        Boolean alreadyChecked = redisUtils.getBit(key, dayOfMonth - 1);
        if (Boolean.TRUE.equals(alreadyChecked)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "今日已签到");
        }
        redisUtils.setBit(key, dayOfMonth - 1, true);
        int streak = calculateStreak(key, (int) dayOfMonth);
        int points = streak >= 7 ? 20 : 5;
        return "签到成功，连续" + streak + "天，获得" + points + "积分";
    }

    private int calculateStreak(String key, int dayOfMonth) {
        int streak = 0;
        for (int i = dayOfMonth - 1; i >= 0; i--) {
            Boolean checked = redisUtils.getBit(key, i);
            if (Boolean.TRUE.equals(checked)) {
                streak++;
            } else {
                break;
            }
        }
        return streak;
    }
}
