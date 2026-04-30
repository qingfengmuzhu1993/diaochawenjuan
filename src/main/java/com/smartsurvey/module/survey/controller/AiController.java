package com.smartsurvey.module.survey.controller;

import com.smartsurvey.common.dto.ApiResponse;
import com.smartsurvey.module.survey.dto.*;
import com.smartsurvey.module.survey.service.AiGenerationService;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/ai")
public class AiController {
    private final AiGenerationService aiService;

    public AiController(AiGenerationService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/generate-survey")
    public ApiResponse<AiGenerateResponse> generate(@Valid @RequestBody AiGenerateRequest req) {
        return ApiResponse.ok(aiService.generateSurvey(req));
    }

    @PostMapping("/diagnose/{surveyId}")
    public ApiResponse<AiDiagnoseResponse> diagnose(@PathVariable Long surveyId) {
        return ApiResponse.ok(aiService.diagnoseSurvey(surveyId));
    }

    @GetMapping("/suggest-improvement/{questionId}")
    public ApiResponse<String> suggestImprovement(@PathVariable Long questionId) {
        return ApiResponse.ok(aiService.suggestQuestionImprovement(questionId));
    }
}
