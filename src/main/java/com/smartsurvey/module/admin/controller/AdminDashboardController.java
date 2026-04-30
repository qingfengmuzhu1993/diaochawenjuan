package com.smartsurvey.module.admin.controller;

import com.smartsurvey.common.dto.ApiResponse;
import com.smartsurvey.module.survey.mapper.SurveyMapper;
import com.smartsurvey.module.user.mapper.UserMapper;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminDashboardController {
    private final UserMapper userMapper;
    private final SurveyMapper surveyMapper;

    public AdminDashboardController(UserMapper userMapper, SurveyMapper surveyMapper) {
        this.userMapper = userMapper; this.surveyMapper = surveyMapper;
    }

    @GetMapping("/dashboard")
    public ApiResponse<Map<String, Object>> dashboard() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("totalUsers", userMapper.selectCount(null));
        data.put("totalSurveys", surveyMapper.selectCount(null));
        data.put("timestamp", System.currentTimeMillis());
        return ApiResponse.ok(data);
    }
}
