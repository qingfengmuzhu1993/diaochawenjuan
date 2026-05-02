package com.smartsurvey.module.survey.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartsurvey.common.dto.PageResult;
import com.smartsurvey.common.exception.BusinessException;
import com.smartsurvey.common.exception.ErrorCode;
import com.smartsurvey.module.survey.dto.*;
import com.smartsurvey.module.survey.entity.Question;
import com.smartsurvey.module.survey.entity.Survey;
import com.smartsurvey.module.survey.mapper.QuestionMapper;
import com.smartsurvey.module.survey.mapper.SurveyMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SurveyService {
    private final SurveyMapper surveyMapper;
    private final QuestionMapper questionMapper;

    public SurveyService(SurveyMapper surveyMapper, QuestionMapper questionMapper) {
        this.surveyMapper = surveyMapper;
        this.questionMapper = questionMapper;
    }

    @Transactional
    public SurveyDetailResponse create(Long userId, CreateSurveyRequest req) {
        Survey survey = new Survey();
        fillSurveyFromRequest(survey, req);
        survey.setUserId(userId);
        survey.setStatus("draft");
        survey.setTotalQuestions(req.getQuestions() != null ? req.getQuestions().size() : 0);
        survey.setTotalResponses(0);
        survey.setRemainingQuota(req.getTargetQuota());
        survey.setViewCount(0);
        survey.setShareCount(0);
        survey.setCreatedAt(LocalDateTime.now());
        survey.setUpdatedAt(LocalDateTime.now());
        surveyMapper.insert(survey);

        if (req.getQuestions() != null) {
            for (int i = 0; i < req.getQuestions().size(); i++) {
                CreateSurveyRequest.QuestionItem qi = req.getQuestions().get(i);
                Question q = new Question();
                q.setSurveyId(survey.getId());
                q.setType(qi.getType());
                q.setContent(qi.getContent());
                q.setRequired(qi.getRequired() != null ? qi.getRequired() : 1);
                q.setOrderIndex(i);
                q.setOptions(qi.getOptions());
                q.setSettings(qi.getSettings());
                q.setLogicJump(qi.getLogicJump());
                q.setLogicShow(qi.getLogicShow());
                q.setIsRandomOptions(qi.getIsRandomOptions() != null ? qi.getIsRandomOptions() : 0);
                q.setCreatedAt(LocalDateTime.now());
                questionMapper.insert(q);
            }
        }

        return getDetail(survey.getId());
    }

    @Transactional
    public SurveyDetailResponse update(Long userId, Long surveyId, CreateSurveyRequest req) {
        Survey survey = surveyMapper.selectById(surveyId);
        if (survey == null) throw new BusinessException(ErrorCode.SURVEY_NOT_FOUND);
        if (!survey.getUserId().equals(userId)) throw new BusinessException(ErrorCode.FORBIDDEN);
        if (!"draft".equals(survey.getStatus())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "只能修改草稿状态的问卷");
        }

        fillSurveyFromRequest(survey, req);
        survey.setTotalQuestions(req.getQuestions() != null ? req.getQuestions().size() : 0);
        survey.setRemainingQuota(req.getTargetQuota());
        survey.setUpdatedAt(LocalDateTime.now());
        surveyMapper.updateById(survey);

        // Delete old questions and recreate
        questionMapper.delete(new LambdaQueryWrapper<Question>().eq(Question::getSurveyId, surveyId));
        if (req.getQuestions() != null) {
            for (int i = 0; i < req.getQuestions().size(); i++) {
                CreateSurveyRequest.QuestionItem qi = req.getQuestions().get(i);
                Question q = new Question();
                q.setSurveyId(survey.getId());
                q.setType(qi.getType());
                q.setContent(qi.getContent());
                q.setRequired(qi.getRequired() != null ? qi.getRequired() : 1);
                q.setOrderIndex(i);
                q.setOptions(qi.getOptions());
                q.setSettings(qi.getSettings());
                q.setLogicJump(qi.getLogicJump());
                q.setLogicShow(qi.getLogicShow());
                q.setIsRandomOptions(qi.getIsRandomOptions() != null ? qi.getIsRandomOptions() : 0);
                q.setCreatedAt(LocalDateTime.now());
                questionMapper.insert(q);
            }
        }

        return getDetail(surveyId);
    }

    public SurveyDetailResponse getDetail(Long surveyId) {
        Survey survey = surveyMapper.selectById(surveyId);
        if (survey == null) throw new BusinessException(ErrorCode.SURVEY_NOT_FOUND);
        List<Question> questions = questionMapper.selectBySurveyId(surveyId);
        return buildDetail(survey, questions);
    }

    public PageResult<SurveyBriefResponse> getMySurveys(Long userId, String status, int page, int size) {
        Page<Survey> p = new Page<>(page, size);
        LambdaQueryWrapper<Survey> wrapper = new LambdaQueryWrapper<Survey>()
            .eq(Survey::getUserId, userId)
            .orderByDesc(Survey::getCreatedAt);
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Survey::getStatus, status);
        } else {
            wrapper.ne(Survey::getStatus, "archived");
        }
        Page<Survey> result = surveyMapper.selectPage(p, wrapper);

        List<SurveyBriefResponse> list = result.getRecords().stream().map(s -> {
            SurveyBriefResponse b = new SurveyBriefResponse();
            b.setId(s.getId()); b.setTitle(s.getTitle()); b.setStatus(s.getStatus());
            b.setTotalQuestions(s.getTotalQuestions()); b.setTotalResponses(s.getTotalResponses());
            b.setRewardPerResponse(s.getRewardPerResponse()); b.setCreatedAt(s.getCreatedAt());
            return b;
        }).collect(Collectors.toList());

        return new PageResult<>(page, size, result.getTotal(), list);
    }

    @Transactional
    public void changeStatus(Long userId, Long surveyId, String newStatus) {
        Survey survey = surveyMapper.selectById(surveyId);
        if (survey == null) throw new BusinessException(ErrorCode.SURVEY_NOT_FOUND);
        if (!survey.getUserId().equals(userId)) throw new BusinessException(ErrorCode.FORBIDDEN);
        survey.setStatus(newStatus);
        survey.setUpdatedAt(LocalDateTime.now());
        surveyMapper.updateById(survey);
    }

    @Transactional
    public SurveyDetailResponse duplicate(Long userId, Long surveyId) {
        Survey original = surveyMapper.selectById(surveyId);
        if (original == null) throw new BusinessException(ErrorCode.SURVEY_NOT_FOUND);

        Survey copy = new Survey();
        copy.setUserId(userId);
        copy.setTitle((original.getTitle() != null ? original.getTitle() : "问卷") + " - 副本");
        copy.setDescription(original.getDescription());
        copy.setCoverImage(original.getCoverImage());
        copy.setStatus("draft");
        copy.setTotalQuestions(original.getTotalQuestions());
        copy.setTotalResponses(0);
        copy.setTargetQuota(original.getTargetQuota());
        copy.setRemainingQuota(original.getTargetQuota());
        copy.setRewardType(original.getRewardType());
        copy.setRewardPerResponse(original.getRewardPerResponse());
        copy.setIsAnonymous(original.getIsAnonymous());
        copy.setAllowResume(original.getAllowResume());
        copy.setTimeLimitMinutes(original.getTimeLimitMinutes());
        copy.setMaxAttempts(original.getMaxAttempts());
        copy.setClosingMessage(original.getClosingMessage());
        copy.setAiGenerated(0);
        copy.setAuditStatus("pending");
        copy.setViewCount(0);
        copy.setShareCount(0);
        copy.setCreatedAt(LocalDateTime.now());
        copy.setUpdatedAt(LocalDateTime.now());
        surveyMapper.insert(copy);

        List<Question> questions = questionMapper.selectBySurveyId(surveyId);
        if (questions != null) {
            for (Question q : questions) {
                Question cq = new Question();
                cq.setSurveyId(copy.getId());
                cq.setType(q.getType());
                cq.setContent(q.getContent());
                cq.setRequired(q.getRequired());
                cq.setOrderIndex(q.getOrderIndex());
                cq.setOptions(q.getOptions());
                cq.setSettings(q.getSettings());
                cq.setLogicJump(q.getLogicJump());
                cq.setLogicShow(q.getLogicShow());
                cq.setIsRandomOptions(q.getIsRandomOptions());
                cq.setCreatedAt(LocalDateTime.now());
                questionMapper.insert(cq);
            }
        }
        return getDetail(copy.getId());
    }

    @Transactional
    public void publish(Long userId, Long surveyId, PublishRequest req) {
        Survey survey = surveyMapper.selectById(surveyId);
        if (survey == null) throw new BusinessException(ErrorCode.SURVEY_NOT_FOUND);
        if (!survey.getUserId().equals(userId)) throw new BusinessException(ErrorCode.FORBIDDEN);

        if (req.getTargetQuota() == null || req.getTargetQuota() <= 0) {
            if (survey.getTargetQuota() == null || survey.getTargetQuota() <= 0) {
                throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "请设置目标回收份数");
            }
            req.setTargetQuota(survey.getTargetQuota());
        }

        survey.setStatus("published");
        survey.setAuditStatus("approved");
        survey.setDispatchType(req.getDispatchType() != null ? req.getDispatchType() : "public");
        survey.setTargetQuota(req.getTargetQuota());
        survey.setRemainingQuota(req.getTargetQuota());
        survey.setRewardTotalBudget(req.getRewardTotalBudget());
        if (req.getEndTime() != null) {
            survey.setEndTime(req.getEndTime());
        }
        survey.setUpdatedAt(LocalDateTime.now());
        surveyMapper.updateById(survey);
    }

    @Transactional
    public void incrementRemainingQuota(Long surveyId) {
        surveyMapper.decrementQuota(surveyId);
        // For now, just update directly
        Survey s = surveyMapper.selectById(surveyId);
        if (s != null && s.getRemainingQuota() != null) {
            s.setRemainingQuota(s.getRemainingQuota() + 1);
            surveyMapper.updateById(s);
        }
    }

    private void fillSurveyFromRequest(Survey survey, CreateSurveyRequest req) {
        survey.setTitle(req.getTitle());
        survey.setDescription(req.getDescription());
        survey.setCoverImage(req.getCoverImage());
        survey.setClosingMessage(req.getClosingMessage());
        survey.setTimeLimitMinutes(req.getTimeLimitMinutes());
        survey.setMaxAttempts(req.getMaxAttempts() != null ? req.getMaxAttempts() : 1);
        survey.setIsAnonymous(req.getIsAnonymous() != null ? req.getIsAnonymous() : 0);
        survey.setAllowResume(req.getAllowResume() != null ? req.getAllowResume() : 1);
        survey.setTargetQuota(req.getTargetQuota());
        survey.setRewardPerResponse(req.getRewardPerResponse());
        survey.setRewardType(req.getRewardType() != null ? req.getRewardType() : "fixed");
        survey.setStartTime(req.getStartTime());
        survey.setEndTime(req.getEndTime());
    }

    private SurveyDetailResponse buildDetail(Survey survey, List<Question> questions) {
        SurveyDetailResponse resp = new SurveyDetailResponse();
        resp.setId(survey.getId()); resp.setUserId(survey.getUserId());
        resp.setTitle(survey.getTitle()); resp.setDescription(survey.getDescription());
        resp.setCoverImage(survey.getCoverImage()); resp.setStatus(survey.getStatus());
        resp.setTotalQuestions(survey.getTotalQuestions()); resp.setTotalResponses(survey.getTotalResponses());
        resp.setTargetQuota(survey.getTargetQuota()); resp.setRemainingQuota(survey.getRemainingQuota());
        resp.setRewardType(survey.getRewardType()); resp.setRewardPerResponse(survey.getRewardPerResponse());
        resp.setIsAnonymous(survey.getIsAnonymous()); resp.setAllowResume(survey.getAllowResume());
        resp.setTimeLimitMinutes(survey.getTimeLimitMinutes()); resp.setMaxAttempts(survey.getMaxAttempts());
        resp.setStartTime(survey.getStartTime()); resp.setEndTime(survey.getEndTime());
        resp.setClosingMessage(survey.getClosingMessage()); resp.setCreatedAt(survey.getCreatedAt());
        resp.setQuestions(questions);
        return resp;
    }
}
