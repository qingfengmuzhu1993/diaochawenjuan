package com.smartsurvey.module.marketplace.controller;

import com.smartsurvey.common.annotation.CurrentUser;
import com.smartsurvey.common.dto.ApiResponse;
import com.smartsurvey.common.dto.PageResult;
import com.smartsurvey.module.marketplace.dto.*;
import com.smartsurvey.module.marketplace.service.MarketplaceService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/marketplace")
public class MarketplaceController {
    private final MarketplaceService marketplaceService;

    public MarketplaceController(MarketplaceService marketplaceService) {
        this.marketplaceService = marketplaceService;
    }

    @GetMapping("/surveys")
    public ApiResponse<PageResult<SurveyCardResponse>> listSurveys(MarketplaceQuery query) {
        return ApiResponse.ok(marketplaceService.listSurveys(query));
    }

    @PostMapping("/surveys/{id}/claim")
    public ApiResponse<ClaimResponse> claim(@CurrentUser Long userId, @PathVariable Long id) {
        return ApiResponse.ok(marketplaceService.claimSurvey(id, userId));
    }

    @GetMapping("/recommended")
    public ApiResponse<List<SurveyCardResponse>> getDailyRecommendations(@CurrentUser Long userId) {
        return ApiResponse.ok(marketplaceService.getDailyRecommendations(userId));
    }
}
