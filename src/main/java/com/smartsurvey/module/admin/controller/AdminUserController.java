package com.smartsurvey.module.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartsurvey.common.dto.ApiResponse;
import com.smartsurvey.common.dto.PageResult;
import com.smartsurvey.common.exception.BusinessException;
import com.smartsurvey.common.exception.ErrorCode;
import com.smartsurvey.module.user.entity.User;
import com.smartsurvey.module.user.mapper.UserMapper;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/users")
public class AdminUserController {
    private final UserMapper userMapper;
    public AdminUserController(UserMapper userMapper) { this.userMapper = userMapper; }

    @GetMapping
    public ApiResponse<PageResult<User>> list(@RequestParam(defaultValue = "1") int page,
                                               @RequestParam(defaultValue = "20") int size,
                                               @RequestParam(required = false) String status) {
        Page<User> p = new Page<>(page, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (status != null) wrapper.eq(User::getStatus, status);
        wrapper.orderByDesc(User::getCreatedAt);
        Page<User> result = userMapper.selectPage(p, wrapper);
        return ApiResponse.ok(new PageResult<>(page, size, result.getTotal(), result.getRecords()));
    }

    @PutMapping("/{id}/ban")
    public ApiResponse<Void> ban(@PathVariable Long id, @RequestParam String reason) {
        User user = userMapper.selectById(id);
        if (user == null) throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        user.setStatus("banned");
        userMapper.updateById(user);
        return ApiResponse.ok();
    }

    @PutMapping("/{id}/unban")
    public ApiResponse<Void> unban(@PathVariable Long id) {
        User user = userMapper.selectById(id);
        if (user == null) throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        user.setStatus("normal");
        userMapper.updateById(user);
        return ApiResponse.ok();
    }
}
