package com.smartsurvey.module.response.controller;

import com.smartsurvey.common.annotation.CurrentUser;
import com.smartsurvey.common.dto.ApiResponse;
import com.smartsurvey.module.response.dto.*;
import com.smartsurvey.module.response.service.ResponseService;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/responses")
public class ResponseController {
    private final ResponseService responseService;

    public ResponseController(ResponseService responseService) {
        this.responseService = responseService;
    }

    @PostMapping("/start")
    public ApiResponse<StartResponseResponse> start(
            @CurrentUser Long userId, @Valid @RequestBody StartResponseRequest req) {
        return ApiResponse.ok(responseService.startResponse(userId, req));
    }

    @PostMapping("/{responseId}/answers")
    public ApiResponse<Void> submitAnswers(
            @CurrentUser Long userId, @PathVariable Long responseId,
            @Valid @RequestBody SubmitAnswerRequest req) {
        responseService.submitAnswer(userId, responseId, req);
        return ApiResponse.ok();
    }

    @PostMapping("/{responseId}/submit")
    public ApiResponse<Void> submit(
            @CurrentUser Long userId, @PathVariable Long responseId) {
        responseService.submitResponse(userId, responseId);
        return ApiResponse.ok();
    }

    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> getResponseDetail(
            @CurrentUser Long userId, @PathVariable Long id) {
        return ApiResponse.ok(responseService.getResponseDetail(userId, id));
    }

    @GetMapping("/my")
    public ApiResponse<List<Map<String, Object>>> getMyResponses(
            @CurrentUser Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(responseService.getMyResponses(userId, page, size));
    }
}
