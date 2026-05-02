package com.smartsurvey.module.marketplace.service;

import com.smartsurvey.common.exception.BusinessException;
import com.smartsurvey.common.exception.ErrorCode;
import com.smartsurvey.module.marketplace.dto.LeaderboardResponse;
import com.smartsurvey.module.user.entity.User;
import com.smartsurvey.module.user.mapper.UserMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
public class GamificationService {
    private final JdbcTemplate jdbcTemplate;
    private final UserMapper userMapper;

    public GamificationService(JdbcTemplate jdbcTemplate, UserMapper userMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.userMapper = userMapper;
    }

    public LeaderboardResponse getLeaderboard(String period) {
        String startDate;
        LocalDate today = LocalDate.now();
        if ("daily".equals(period)) {
            startDate = today.toString();
        } else if ("weekly".equals(period)) {
            startDate = today.minusDays(7).toString();
        } else {
            startDate = today.minusDays(30).toString();
        }

        String sql = "SELECT user_id, SUM(amount) as earnings FROM transactions " +
            "WHERE type = 'reward' AND status = 'success' AND created_at >= ? " +
            "GROUP BY user_id ORDER BY earnings DESC LIMIT 10";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, startDate + " 00:00:00");

        LeaderboardResponse resp = new LeaderboardResponse();
        resp.setPeriod(period);
        List<LeaderboardResponse.Entry> list = new ArrayList<>();
        int rank = 1;
        for (Map<String, Object> row : rows) {
            Long userId = ((Number) row.get("user_id")).longValue();
            BigDecimal earnings = new BigDecimal(row.get("earnings").toString());
            User user = userMapper.selectById(userId);
            LeaderboardResponse.Entry entry = new LeaderboardResponse.Entry();
            entry.setUserId(userId);
            entry.setRank(rank++);
            entry.setEarnings(earnings);
            entry.setUsername(user != null ? user.getUsername() : "未知用户");
            list.add(entry);
        }
        resp.setEntries(list);
        return resp;
    }

    public String checkIn(Long userId) {
        LocalDate today = LocalDate.now();

        // Check if already signed in today
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM sign_in_records WHERE user_id = ? AND sign_date = ?",
            Integer.class, userId, today.toString());
        if (count != null && count > 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "今日已签到");
        }

        // Calculate streak from previous day
        LocalDate yesterday = today.minusDays(1);
        List<Integer> prevResults = jdbcTemplate.queryForList(
            "SELECT streak_days FROM sign_in_records WHERE user_id = ? AND sign_date = ?",
            Integer.class, userId, yesterday.toString());
        int prevStreak = prevResults.isEmpty() ? 0 : prevResults.get(0);
        int streak = prevStreak + 1;

        int points = streak >= 7 ? 20 : 5;

        jdbcTemplate.update(
            "INSERT INTO sign_in_records (user_id, sign_date, streak_days, points_earned) VALUES (?, ?, ?, ?)",
            userId, today.toString(), streak, points);

        return "签到成功，连续" + streak + "天，获得" + points + "积分";
    }

    public boolean hasCheckedInToday(Long userId) {
        LocalDate today = LocalDate.now();
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM sign_in_records WHERE user_id = ? AND sign_date = ?",
            Integer.class, userId, today.toString());
        return count != null && count > 0;
    }
}
