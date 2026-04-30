package com.smartsurvey.common.utils;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
public class RedisUtils {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisUtils(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    public void setex(String key, long timeout, Object value) {
        redisTemplate.opsForValue().set(key, value, timeout, TimeUnit.SECONDS);
    }

    public String get(String key) {
        Object value = redisTemplate.opsForValue().get(key);
        return value != null ? value.toString() : null;
    }

    public void del(String key) {
        redisTemplate.delete(key);
    }

    public boolean exists(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public void expire(String key, long timeout, TimeUnit unit) {
        redisTemplate.expire(key, timeout, unit);
    }

    public void incrementScore(String key, String member, double score) {
        redisTemplate.opsForZSet().incrementScore(key, member, score);
    }

    public Set<Map.Entry<String, Double>> getZSetTop(String key, int count) {
        Set<org.springframework.data.redis.core.ZSetOperations.TypedTuple<Object>> tuples =
            redisTemplate.opsForZSet().reverseRangeWithScores(key, 0, count - 1);
        if (tuples == null) {
            return new java.util.LinkedHashSet<>();
        }
        java.util.LinkedHashMap<String, Double> result = new java.util.LinkedHashMap<>();
        for (org.springframework.data.redis.core.ZSetOperations.TypedTuple<Object> t : tuples) {
            result.put(t.getValue().toString(), t.getScore());
        }
        return result.entrySet();
    }

    public Boolean getBit(String key, long offset) {
        return redisTemplate.opsForValue().getBit(key, offset);
    }

    public void setBit(String key, long offset, boolean value) {
        redisTemplate.opsForValue().setBit(key, offset, value);
    }
}
