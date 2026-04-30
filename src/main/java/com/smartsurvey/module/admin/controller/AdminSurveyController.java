package com.smartsurvey.module.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartsurvey.common.dto.ApiResponse;
import com.smartsurvey.common.dto.PageResult;
import com.smartsurvey.common.exception.BusinessException;
import com.smartsurvey.common.exception.ErrorCode;
import com.smartsurvey.module.survey.entity.Survey;
import com.smartsurvey.module.survey.mapper.SurveyMapper;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/surveys")
public class AdminSurveyController {
    private final SurveyMapper surveyMapper;
    public AdminSurveyController(SurveyMapper surveyMapper) { this.surveyMapper = surveyMapper; }

    @GetMapping
    public ApiResponse<PageResult<Survey>> list(@RequestParam(defaultValue = "1") int page,
                                                 @RequestParam(defaultValue = "20") int size,
                                                 @RequestParam(required = false) String auditStatus) {
        Page<Survey> p = new Page<>(page, size);
        LambdaQueryWrapper<Survey> wrapper = new LambdaQueryWrapper<>();
        if (auditStatus != null) wrapper.eq(Survey::getAuditStatus, auditStatus);
        wrapper.orderByDesc(Survey::getCreatedAt);
        Page<Survey> result = surveyMapper.selectPage(p, wrapper);
        return ApiResponse.ok(new PageResult<>(page, size, result.getTotal(), result.getRecords()));
    }

    @PutMapping("/{id}/approve")
    public ApiResponse<Void> approve(@PathVariable Long id) {
        Survey survey = surveyMapper.selectById(id);
        if (survey == null) throw new BusinessException(ErrorCode.SURVEY_NOT_FOUND);
        survey.setAuditStatus("approved");
        surveyMapper.updateById(survey);
        return ApiResponse.ok();
    }

    @PutMapping("/{id}/reject")
    public ApiResponse<Void> reject(@PathVariable Long id, @RequestParam String reason) {
        Survey survey = surveyMapper.selectById(id);
        if (survey == null) throw new BusinessException(ErrorCode.SURVEY_NOT_FOUND);
        survey.setAuditStatus("rejected");
        survey.setAuditNote(reason);
        surveyMapper.updateById(survey);
        return ApiResponse.ok();
    }

    @PutMapping("/{id}/remove")
    public ApiResponse<Void> remove(@PathVariable Long id) {
        Survey survey = surveyMapper.selectById(id);
        if (survey == null) throw new BusinessException(ErrorCode.SURVEY_NOT_FOUND);
        survey.setStatus("archived");
        surveyMapper.updateById(survey);
        return ApiResponse.ok();
    }
}
