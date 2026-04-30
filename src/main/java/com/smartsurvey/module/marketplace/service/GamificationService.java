package com.smartsurvey.module.marketplace.service;

import com.smartsurvey.common.exception.BusinessException;
import com.smartsurvey.common.exception.ErrorCode;
import com.smartsurvey.module.marketplace.dto.LeaderboardResponse;
import com.smartsurvey.module.user.entity.User;
import com.smartsurvey.module.user.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GamificationService {
    private final Map<String, Map<Long, Double>> leaderboards = new ConcurrentHashMap<>();
    private final Map<String, BitSet> checkinData = new ConcurrentHashMap<>();
    private final UserMapper userMapper;

    public GamificationService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public void updateLeaderboard(Long userId, BigDecimal reward) {
        String dailyKey = "daily:" + LocalDate.now();
        leaderboards.computeIfAbsent(dailyKey, k -> new ConcurrentHashMap<>())
            .merge(userId, reward.doubleValue(), Double::sum);
    }

    public LeaderboardResponse getLeaderboard(String period) {
        String key = period + ":" + LocalDate.now();
        Map<Long, Double> scores = leaderboards.getOrDefault(key, Collections.emptyMap());

        LeaderboardResponse resp = new LeaderboardResponse();
        resp.setPeriod(period);
        List<LeaderboardResponse.Entry> list = new ArrayList<>();
        List<Map.Entry<Long, Double>> sorted = new ArrayList<>(scores.entrySet());
        sorted.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        int rank = 1;
        for (Map.Entry<Long, Double> e : sorted) {
            if (rank > 10) break;
            Long uid = e.getKey();
            User user = userMapper.selectById(uid);
            LeaderboardResponse.Entry entry = new LeaderboardResponse.Entry();
            entry.setUserId(uid);
            entry.setRank(rank++);
            entry.setEarnings(BigDecimal.valueOf(e.getValue()));
            entry.setUsername(user != null ? user.getUsername() : "未知用户");
            list.add(entry);
        }
        resp.setEntries(list);
        return resp;
    }

    public String checkIn(Long userId) {
        String key = YearMonth.now().toString();
        BitSet bits = checkinData.computeIfAbsent(key, k -> new BitSet(31));
        int dayOfMonth = (int) LocalDate.now().getDayOfMonth() - 1;

        if (bits.get(dayOfMonth)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "今日已签到");
        }
        bits.set(dayOfMonth);

        int streak = 0;
        for (int i = dayOfMonth; i >= 0; i--) {
            if (bits.get(i)) streak++; else break;
        }
        int points = streak >= 7 ? 20 : 5;
        return "签到成功，连续" + streak + "天，获得" + points + "积分";
    }
}
