package com.smartsurvey.module.marketplace.controller;

import com.smartsurvey.common.annotation.CurrentUser;
import com.smartsurvey.common.dto.ApiResponse;
import com.smartsurvey.common.exception.BusinessException;
import com.smartsurvey.common.exception.ErrorCode;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/points")
public class PointsController {

    private final JdbcTemplate jdbcTemplate;

    public PointsController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping
    public ApiResponse<Map<String, Object>> getPoints(@CurrentUser Long userId) {
        // Sum points from sign_in_records
        Integer totalEarned = jdbcTemplate.queryForObject(
            "SELECT COALESCE(SUM(points_earned), 0) FROM sign_in_records WHERE user_id = ?",
            Integer.class, userId);
        Integer totalSpent = jdbcTemplate.queryForObject(
            "SELECT COALESCE(SUM(points_spent), 0) FROM points_usage WHERE user_id = ?",
            Integer.class, userId);
        int balance = (totalEarned != null ? totalEarned : 0) - (totalSpent != null ? totalSpent : 0);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("balance", balance);
        result.put("totalEarned", totalEarned != null ? totalEarned : 0);
        return ApiResponse.ok(result);
    }

    @PostMapping("/exchange")
    public ApiResponse<Map<String, String>> exchange(@CurrentUser Long userId, @RequestBody Map<String, String> body) {
        String exchangeType = body.get("type"); // "priority" or "cash_coupon"

        // Get current balance
        Integer totalEarned = jdbcTemplate.queryForObject(
            "SELECT COALESCE(SUM(points_earned), 0) FROM sign_in_records WHERE user_id = ?",
            Integer.class, userId);
        Integer totalSpent = jdbcTemplate.queryForObject(
            "SELECT COALESCE(SUM(points_spent), 0) FROM points_usage WHERE user_id = ?",
            Integer.class, userId);
        int balance = (totalEarned != null ? totalEarned : 0) - (totalSpent != null ? totalSpent : 0);

        int cost;
        String reward;
        if ("priority".equals(exchangeType)) {
            cost = 100;
            reward = "优先抢单权1次";
        } else if ("cash_coupon".equals(exchangeType)) {
            // Check monthly limit (5 times)
            Integer monthlyCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM points_usage WHERE user_id = ? AND exchange_type = 'cash_coupon' " +
                "AND DATE_FORMAT(created_at, '%Y-%m') = DATE_FORMAT(NOW(), '%Y-%m')",
                Integer.class, userId);
            if (monthlyCount != null && monthlyCount >= 5) {
                throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "本月现金券兑换已达上限（5次）");
            }
            cost = 500;
            reward = "1元现金券";
        } else {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "无效的兑换类型");
        }

        if (balance < cost) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(),
                "积分不足，需要" + cost + "积分，当前" + balance + "积分");
        }

        // Record the usage
        jdbcTemplate.update(
            "INSERT INTO points_usage (user_id, exchange_type, points_spent, reward, created_at) VALUES (?, ?, ?, ?, NOW())",
            userId, exchangeType, cost, reward);

        Map<String, String> result = new LinkedHashMap<>();
        result.put("reward", reward);
        result.put("cost", String.valueOf(cost));
        result.put("remaining", String.valueOf(balance - cost));
        return ApiResponse.ok(result);
    }

    @GetMapping("/history")
    public ApiResponse<List<Map<String, Object>>> history(@CurrentUser Long userId) {
        String sql = "SELECT sign_date AS created_at, " +
            "CONCAT('连续签到', streak_days, '天') AS reason, " +
            "points_earned AS amount, 'earn' AS type " +
            "FROM sign_in_records WHERE user_id = ? " +
            "UNION ALL " +
            "SELECT created_at, reward AS reason, -points_spent AS amount, 'spend' AS type " +
            "FROM points_usage WHERE user_id = ? " +
            "ORDER BY created_at DESC LIMIT 50";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, userId, userId);
        return ApiResponse.ok(list);
    }
}
