package com.smartsurvey.module.user.controller;

import com.smartsurvey.common.annotation.CurrentUser;
import com.smartsurvey.common.dto.ApiResponse;
import com.smartsurvey.module.user.service.FollowService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/social")
public class SocialController {
    private final FollowService followService;

    public SocialController(FollowService followService) {
        this.followService = followService;
    }

    @PostMapping("/follow/{userId}")
    public ApiResponse<Void> follow(@CurrentUser Long currentUserId, @PathVariable Long userId) {
        followService.follow(currentUserId, userId);
        return ApiResponse.ok();
    }

    @DeleteMapping("/follow/{userId}")
    public ApiResponse<Void> unfollow(@CurrentUser Long currentUserId, @PathVariable Long userId) {
        followService.unfollow(currentUserId, userId);
        return ApiResponse.ok();
    }

    @GetMapping("/following/{userId}")
    public ApiResponse<List<Map<String, Object>>> getFollowing(@PathVariable Long userId) {
        return ApiResponse.ok(followService.getFollowing(userId));
    }

    @GetMapping("/followers/{userId}")
    public ApiResponse<List<Map<String, Object>>> getFollowers(@PathVariable Long userId) {
        return ApiResponse.ok(followService.getFollowers(userId));
    }

    @GetMapping("/is-following/{userId}")
    public ApiResponse<Boolean> isFollowing(@CurrentUser Long currentUserId, @PathVariable Long userId) {
        return ApiResponse.ok(followService.isFollowing(currentUserId, userId));
    }
}
