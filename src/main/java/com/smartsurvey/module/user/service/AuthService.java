package com.smartsurvey.module.user.service;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartsurvey.common.exception.BusinessException;
import com.smartsurvey.common.exception.ErrorCode;
import com.smartsurvey.common.utils.JwtUtils;
import com.smartsurvey.module.user.dto.*;
import com.smartsurvey.module.user.entity.User;
import com.smartsurvey.module.user.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private static final Map<String, CodeEntry> smsStore = new ConcurrentHashMap<>();
    private static final long SMS_TTL_MS = 300_000;
    private static final long SMS_RATE_LIMIT_MS = 60_000;

    private static class CodeEntry {
        final String code;
        final long createdAt;
        CodeEntry(String code) { this.code = code; this.createdAt = System.currentTimeMillis(); }
        boolean isExpired() { return System.currentTimeMillis() - createdAt > SMS_TTL_MS; }
    }

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public AuthService(UserMapper userMapper, PasswordEncoder passwordEncoder, JwtUtils jwtUtils) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    @Transactional
    public LoginResponse register(RegisterRequest req) {
        CodeEntry entry = smsStore.get(req.getPhone());
        if (entry == null || entry.isExpired() || !entry.code.equals(req.getSmsCode())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR.getCode(), "验证码错误或已过期");
        }

        Long count = userMapper.selectCount(
            new LambdaQueryWrapper<User>().eq(User::getPhone, req.getPhone()));
        if (count > 0) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR.getCode(), "该手机号已注册");
        }

        User user = new User();
        user.setUsername(req.getUsername());
        user.setPhone(req.getPhone());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        user.setLevel(1);
        user.setReputation(100);
        user.setBalance(BigDecimal.ZERO);
        user.setFrozenBalance(BigDecimal.ZERO);
        user.setRole("user");
        user.setStatus("normal");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.insert(user);

        smsStore.remove(req.getPhone());

        String accessToken = jwtUtils.createToken(user.getId(), user.getRole());
        String refreshToken = jwtUtils.createRefreshToken(user.getId());
        return buildLoginResponse(user, accessToken, refreshToken);
    }

    public LoginResponse login(LoginRequest req) {
        User user = userMapper.selectOne(
            new LambdaQueryWrapper<User>()
                .eq(User::getPhone, req.getAccount())
                .or()
                .eq(User::getEmail, req.getAccount()));

        if (user == null) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }
        if ("banned".equals(user.getStatus())) {
            throw new BusinessException(ErrorCode.ACCOUNT_FROZEN);
        }
        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }

        user.setLoginAt(LocalDateTime.now());
        userMapper.updateById(user);

        String accessToken = jwtUtils.createToken(user.getId(), user.getRole());
        String refreshToken = jwtUtils.createRefreshToken(user.getId());
        return buildLoginResponse(user, accessToken, refreshToken);
    }

    public void sendSmsCode(String phone) {
        CodeEntry existing = smsStore.get(phone);
        if (existing != null && !existing.isExpired()) {
            long elapsed = System.currentTimeMillis() - existing.createdAt;
            if (elapsed < SMS_RATE_LIMIT_MS) {
                throw new BusinessException(ErrorCode.SMS_LIMIT);
            }
        }
        String code = RandomUtil.randomNumbers(6);
        smsStore.put(phone, new CodeEntry(code));
        log.info("SMS code for {}: {}", phone, code);
    }

    public LoginResponse refreshToken(String refreshToken) {
        if (!jwtUtils.validateToken(refreshToken)) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }
        Long userId = jwtUtils.getUserId(refreshToken);
        User user = userMapper.selectById(userId);
        if (user == null || "banned".equals(user.getStatus())) {
            throw new BusinessException(ErrorCode.ACCOUNT_FROZEN);
        }
        String newAccessToken = jwtUtils.createToken(user.getId(), user.getRole());
        return buildLoginResponse(user, newAccessToken, refreshToken);
    }

    private LoginResponse buildLoginResponse(User user, String accessToken, String refreshToken) {
        LoginResponse resp = new LoginResponse();
        resp.setAccessToken(accessToken);
        resp.setRefreshToken(refreshToken);
        resp.setExpiresIn(7200000L);

        LoginResponse.UserInfo info = new LoginResponse.UserInfo();
        info.setId(user.getId());
        info.setUsername(user.getUsername());
        info.setAvatarUrl(user.getAvatarUrl());
        info.setLevel(user.getLevel());
        info.setReputation(user.getReputation());
        info.setRole(user.getRole());
        resp.setUserInfo(info);
        return resp;
    }
}
