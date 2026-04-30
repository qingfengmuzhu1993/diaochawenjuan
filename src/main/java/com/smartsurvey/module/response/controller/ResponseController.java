package com.smartsurvey.module.response.controller;

import com.smartsurvey.common.annotation.CurrentUser;
import com.smartsurvey.common.dto.ApiResponse;
import com.smartsurvey.module.response.dto.*;
import com.smartsurvey.module.response.service.ResponseService;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

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
}
