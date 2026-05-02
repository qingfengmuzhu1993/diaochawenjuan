package com.smartsurvey.module.survey.controller;

import com.smartsurvey.common.annotation.CurrentUser;
import com.smartsurvey.common.dto.ApiResponse;
import com.smartsurvey.common.dto.PageResult;
import com.smartsurvey.module.survey.dto.*;
import com.smartsurvey.module.survey.service.SurveyService;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/surveys")
public class SurveyController {
    private final SurveyService surveyService;

    public SurveyController(SurveyService surveyService) {
        this.surveyService = surveyService;
    }

    @PostMapping
    public ApiResponse<SurveyDetailResponse> create(
            @CurrentUser Long userId, @Valid @RequestBody CreateSurveyRequest req) {
        return ApiResponse.ok(surveyService.create(userId, req));
    }

    @GetMapping("/{id}")
    public ApiResponse<SurveyDetailResponse> getDetail(@PathVariable Long id) {
        return ApiResponse.ok(surveyService.getDetail(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<SurveyDetailResponse> update(
            @CurrentUser Long userId, @PathVariable Long id,
            @Valid @RequestBody CreateSurveyRequest req) {
        return ApiResponse.ok(surveyService.update(userId, id, req));
    }

    @GetMapping("/my")
    public ApiResponse<PageResult<SurveyBriefResponse>> getMySurveys(
            @CurrentUser Long userId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(surveyService.getMySurveys(userId, status, page, size));
    }

    @PostMapping("/{id}/publish")
    public ApiResponse<Void> publish(@CurrentUser Long userId, @PathVariable Long id,
                                     @Valid @RequestBody PublishRequest req) {
        SurveyDetailResponse detail = surveyService.getDetail(id);
        if (detail.getTotalQuestions() == null || detail.getTotalQuestions() < 3) {
            return ApiResponse.fail(40000, "题目数量不足，至少需要3题");
        }
        surveyService.publish(userId, id, req);
        return ApiResponse.ok();
    }

    @PostMapping("/{id}/close")
    public ApiResponse<Void> close(@CurrentUser Long userId, @PathVariable Long id) {
        surveyService.changeStatus(userId, id, "closed");
        return ApiResponse.ok();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@CurrentUser Long userId, @PathVariable Long id) {
        surveyService.changeStatus(userId, id, "archived");
        return ApiResponse.ok();
    }

    @PostMapping("/{id}/duplicate")
    public ApiResponse<SurveyDetailResponse> duplicate(@CurrentUser Long userId, @PathVariable Long id) {
        return ApiResponse.ok(surveyService.duplicate(userId, id));
    }

    @PostMapping("/{id}/share")
    public ApiResponse<Map<String, String>> share(@CurrentUser Long userId, @PathVariable Long id) {
        String shareCode = toBase62(userId) + "_" + toBase62(id);
        Map<String, String> result = new HashMap<>();
        result.put("shareUrl", "/survey/" + id + "?ref=" + shareCode);
        result.put("shareCode", shareCode);
        return ApiResponse.ok(result);
    }

    private static final String BASE62 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    private String toBase62(long num) {
        if (num == 0) return "0";
        StringBuilder sb = new StringBuilder();
        while (num > 0) {
            sb.append(BASE62.charAt((int)(num % 62)));
            num /= 62;
        }
        return sb.reverse().toString();
    }

    private long fromBase62(String s) {
        long result = 0;
        for (int i = 0; i < s.length(); i++) {
            result = result * 62 + BASE62.indexOf(s.charAt(i));
        }
        return result;
    }
}
