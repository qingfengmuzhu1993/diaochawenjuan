package com.smartsurvey.module.marketplace.controller;

import com.smartsurvey.common.annotation.CurrentUser;
import com.smartsurvey.common.dto.ApiResponse;
import com.smartsurvey.module.marketplace.dto.LeaderboardResponse;
import com.smartsurvey.module.marketplace.service.GamificationService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class GamificationController {
    private final GamificationService gamificationService;

    public GamificationController(GamificationService gamificationService) {
        this.gamificationService = gamificationService;
    }

    @GetMapping("/leaderboard")
    public ApiResponse<LeaderboardResponse> getLeaderboard(@RequestParam(defaultValue = "daily") String period) {
        return ApiResponse.ok(gamificationService.getLeaderboard(period));
    }

    @PostMapping("/checkin")
    public ApiResponse<String> checkIn(@CurrentUser Long userId) {
        return ApiResponse.ok(gamificationService.checkIn(userId));
    }
}
