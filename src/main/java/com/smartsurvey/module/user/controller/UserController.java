package com.smartsurvey.module.user.controller;

import com.smartsurvey.common.annotation.CurrentUser;
import com.smartsurvey.common.dto.ApiResponse;
import com.smartsurvey.module.user.dto.*;
import com.smartsurvey.module.user.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public ApiResponse<UserProfileResponse> getProfile(@CurrentUser Long userId) {
        return ApiResponse.ok(userService.getProfile(userId));
    }

    @GetMapping("/profile/{targetUserId}")
    public ApiResponse<UserProfileResponse> getUserProfile(@PathVariable Long targetUserId) {
        return ApiResponse.ok(userService.getProfileByUserId(targetUserId));
    }

    @PutMapping("/profile")
    public ApiResponse<UserProfileResponse> updateProfile(
            @CurrentUser Long userId, @Valid @RequestBody UpdateProfileRequest req) {
        return ApiResponse.ok(userService.updateProfile(userId, req));
    }
}
