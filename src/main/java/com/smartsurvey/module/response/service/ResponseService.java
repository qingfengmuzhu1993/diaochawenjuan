package com.smartsurvey.module.response.service;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartsurvey.common.exception.BusinessException;
import com.smartsurvey.common.exception.ErrorCode;
import com.smartsurvey.module.response.dto.*;
import com.smartsurvey.module.response.entity.Answer;
import com.smartsurvey.module.response.entity.Response;
import com.smartsurvey.module.response.mapper.AnswerMapper;
import com.smartsurvey.module.response.mapper.ResponseMapper;
import com.smartsurvey.module.survey.entity.Question;
import com.smartsurvey.module.survey.entity.Survey;
import com.smartsurvey.module.survey.mapper.QuestionMapper;
import com.smartsurvey.module.survey.mapper.SurveyMapper;
import com.smartsurvey.module.survey.service.SurveyLogicService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ResponseService {
    private final ResponseMapper responseMapper;
    private final AnswerMapper answerMapper;
    private final SurveyMapper surveyMapper;
    private final QuestionMapper questionMapper;
    private final SurveyLogicService surveyLogicService;
    private final AntiCheatService antiCheatService;

    public ResponseService(ResponseMapper responseMapper, AnswerMapper answerMapper,
                            SurveyMapper surveyMapper, QuestionMapper questionMapper,
                            SurveyLogicService surveyLogicService, AntiCheatService antiCheatService) {
        this.responseMapper = responseMapper;
        this.answerMapper = answerMapper;
        this.surveyMapper = surveyMapper;
        this.questionMapper = questionMapper;
        this.surveyLogicService = surveyLogicService;
        this.antiCheatService = antiCheatService;
    }

    @Transactional
    public StartResponseResponse startResponse(Long userId, StartResponseRequest req) {
        Survey survey = surveyMapper.selectById(req.getSurveyId());
        if (survey == null) {
            throw new BusinessException(ErrorCode.SURVEY_NOT_FOUND);
        }
        if (!"published".equals(survey.getStatus())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "问卷未发布或已结束");
        }
        if (survey.getRemainingQuota() != null && survey.getRemainingQuota() <= 0) {
            throw new BusinessException(ErrorCode.QUOTA_FULL);
        }

        Long existingCount = responseMapper.countBySurveyAndUser(req.getSurveyId(), userId);
        if (existingCount >= survey.getMaxAttempts()) {
            throw new BusinessException(ErrorCode.DUPLICATE_SUBMIT);
        }

        Long activeCount = responseMapper.selectCount(new LambdaQueryWrapper<Response>()
                .eq(Response::getUserId, userId).eq(Response::getStatus, "in_progress"));
        if (activeCount >= 5) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "您有待完成的任务过多");
        }

        Response response = new Response();
        response.setSurveyId(req.getSurveyId());
        response.setUserId(userId);
        response.setStatus("in_progress");
        response.setChannel(req.getChannel() != null ? req.getChannel() : "direct");
        response.setStartTime(LocalDateTime.now());
        response.setCreatedAt(LocalDateTime.now());
        responseMapper.insert(response);

        List<Question> questions = surveyLogicService.getOrderedQuestions(
                questionMapper.selectBySurveyId(req.getSurveyId()));

        StartResponseResponse resp = new StartResponseResponse();
        resp.setResponseId(response.getId());
        resp.setFirstQuestion(questions.isEmpty() ? null : questions.get(0));
        resp.setTotalQuestions(questions.size());
        resp.setExpireAt(LocalDateTime.now().plusMinutes(
                survey.getTimeLimitMinutes() != null && survey.getTimeLimitMinutes() > 0
                        ? survey.getTimeLimitMinutes() : 15));
        return resp;
    }

    @Transactional
    public void submitAnswer(Long userId, Long responseId, SubmitAnswerRequest req) {
        Response response = responseMapper.selectById(responseId);
        if (response == null || !response.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        if (!"in_progress".equals(response.getStatus())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "该回答已提交或已过期");
        }

        for (SubmitAnswerRequest.AnswerItem item : req.getAnswers()) {
            Answer answer = answerMapper.selectOne(new LambdaQueryWrapper<Answer>()
                    .eq(Answer::getResponseId, responseId)
                    .eq(Answer::getQuestionId, item.getQuestionId()));
            boolean isNew = (answer == null);
            if (isNew) {
                answer = new Answer();
                answer.setResponseId(responseId);
                answer.setQuestionId(item.getQuestionId());
            }
            answer.setAnswerText(item.getAnswerText());
            answer.setAnswerOptions(item.getAnswerOptions() != null
                    ? JSONUtil.toJsonStr(item.getAnswerOptions()) : null);
            answer.setAnswerRating(item.getAnswerRating());
            answer.setCreatedAt(LocalDateTime.now());
            if (isNew) {
                answerMapper.insert(answer);
            } else {
                answerMapper.updateById(answer);
            }
        }
    }

    @Transactional
    public void submitResponse(Long userId, Long responseId) {
        Response response = responseMapper.selectById(responseId);
        verifySubmitPermission(response, userId);

        List<Question> questions = questionMapper.selectBySurveyId(response.getSurveyId());
        List<Answer> answers = answerMapper.selectByResponseId(responseId);
        for (Question q : questions) {
            if (q.getRequired() != null && q.getRequired() == 1) {
                boolean answered = answers.stream().anyMatch(a -> a.getQuestionId().equals(q.getId()));
                if (!answered) {
                    throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(),
                            "题目『" + truncate(q.getContent(), 20) + "』为必答题");
                }
            }
        }

        response.setEndTime(LocalDateTime.now());
        response.setDurationSeconds((int) Duration.between(response.getStartTime(), response.getEndTime()).getSeconds());
        response.setStatus("submitted");
        responseMapper.updateById(response);

        antiCheatService.evaluateAsync(response);
    }

    private void verifySubmitPermission(Response response, Long userId) {
        if (response == null || !response.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        Survey survey = surveyMapper.selectById(response.getSurveyId());
        if (survey.getTimeLimitMinutes() != null && survey.getTimeLimitMinutes() > 0) {
            long elapsed = Duration.between(response.getStartTime(), LocalDateTime.now()).getSeconds();
            if (elapsed > survey.getTimeLimitMinutes() * 60L) {
                response.setStatus("expired");
                responseMapper.updateById(response);
                throw new BusinessException(ErrorCode.ANSWER_EXPIRED);
            }
        }
    }

    private String truncate(String s, int len) {
        return s != null && s.length() > len ? s.substring(0, len) + "..." : s;
    }
}
