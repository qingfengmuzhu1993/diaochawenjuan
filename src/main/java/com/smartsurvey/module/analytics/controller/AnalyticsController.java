package com.smartsurvey.module.analytics.controller;

import com.smartsurvey.common.dto.ApiResponse;
import com.smartsurvey.module.analytics.service.AiAnalysisService;
import com.smartsurvey.module.analytics.service.StatisticsService;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/analytics")
public class AnalyticsController {
    private final StatisticsService statisticsService;
    private final AiAnalysisService aiAnalysisService;

    public AnalyticsController(StatisticsService statisticsService, AiAnalysisService aiAnalysisService) {
        this.statisticsService = statisticsService; this.aiAnalysisService = aiAnalysisService;
    }

    @GetMapping("/surveys/{surveyId}")
    public ApiResponse<Map<String, Object>> getStatistics(@PathVariable Long surveyId) {
        return ApiResponse.ok(statisticsService.getStatistics(surveyId));
    }

    @GetMapping("/surveys/{surveyId}/findings")
    public ApiResponse<Map<String, Object>> getFindings(@PathVariable Long surveyId) {
        return ApiResponse.ok(aiAnalysisService.getKeyFindings(surveyId));
    }

    @GetMapping("/surveys/{surveyId}/sentiment/{questionId}")
    public ApiResponse<Map<String, Object>> getSentiment(@PathVariable Long surveyId, @PathVariable Long questionId) {
        return ApiResponse.ok(aiAnalysisService.analyzeSentiment(surveyId, questionId));
    }

    @GetMapping("/surveys/{surveyId}/cross")
    public ApiResponse<Map<String, Object>> crossTabulation(
            @PathVariable Long surveyId,
            @RequestParam Long rowQuestionId,
            @RequestParam Long colQuestionId) {
        return ApiResponse.ok(statisticsService.crossTabulation(surveyId, rowQuestionId, colQuestionId));
    }
}
