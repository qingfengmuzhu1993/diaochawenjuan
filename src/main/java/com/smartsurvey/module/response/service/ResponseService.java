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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ResponseService {
    private final ResponseMapper responseMapper;
    private final AnswerMapper answerMapper;
    private final SurveyMapper surveyMapper;
    private final QuestionMapper questionMapper;
    private final SurveyLogicService surveyLogicService;
    private final AntiCheatService antiCheatService;
    private final JdbcTemplate jdbcTemplate;

    public ResponseService(ResponseMapper responseMapper, AnswerMapper answerMapper,
                            SurveyMapper surveyMapper, QuestionMapper questionMapper,
                            SurveyLogicService surveyLogicService, AntiCheatService antiCheatService,
                            JdbcTemplate jdbcTemplate) {
        this.responseMapper = responseMapper;
        this.answerMapper = answerMapper;
        this.surveyMapper = surveyMapper;
        this.questionMapper = questionMapper;
        this.surveyLogicService = surveyLogicService;
        this.antiCheatService = antiCheatService;
        this.jdbcTemplate = jdbcTemplate;
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

        // track referrer from share link
        if (req.getReferrerCode() != null && !req.getReferrerCode().isEmpty()) {
            try {
                String[] parts = req.getReferrerCode().split("_");
                if (parts.length == 2) {
                    long referrerId = fromBase62Static(parts[0]);
                    if (referrerId != userId) {
                        response.setReferrerId(referrerId);
                    }
                }
            } catch (Exception ignored) {}
        }

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

        if (req.getBehaviorData() != null) {
            response.setBehaviorData(req.getBehaviorData());
            responseMapper.updateById(response);
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

    public Map<String, Object> getResponseDetail(Long userId, Long responseId) {
        Response response = responseMapper.selectById(responseId);
        if (response == null || !response.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        Survey survey = surveyMapper.selectById(response.getSurveyId());
        List<Question> questions = questionMapper.selectBySurveyId(response.getSurveyId());
        if (questions != null) {
            questions.sort(Comparator.comparingInt(Question::getOrderIndex));
        }
        List<Answer> answers = answerMapper.selectByResponseId(responseId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", response.getId());
        result.put("status", response.getStatus());
        result.put("rewardAmount", response.getRewardAmount());
        result.put("qualityScore", response.getQualityScore());
        result.put("createdAt", response.getCreatedAt());
        result.put("surveyTitle", survey != null ? survey.getTitle() : "已删除的问卷");

        List<Map<String, Object>> qaList = new ArrayList<>();
        if (questions != null) {
            for (Question q : questions) {
                Map<String, Object> qa = new LinkedHashMap<>();
                qa.put("questionId", q.getId());
                qa.put("content", q.getContent());
                qa.put("type", q.getType());
                qa.put("required", q.getRequired());
                qa.put("options", q.getOptions());

                Answer match = answers.stream()
                    .filter(a -> a.getQuestionId().equals(q.getId())).findFirst().orElse(null);
                if (match != null) {
                    qa.put("answerText", match.getAnswerText());
                    qa.put("answerOptions", match.getAnswerOptions());
                    qa.put("answerRating", match.getAnswerRating());
                }
                qaList.add(qa);
            }
        }
        result.put("questions", qaList);
        return result;
    }

    public List<Map<String, Object>> getMyResponses(Long userId, int page, int size) {
        int offset = (page - 1) * size;
        String sql = "SELECT r.id, r.survey_id, s.title AS survey_title, r.status, " +
            "r.reward_amount, r.quality_score, r.duration_seconds, r.created_at " +
            "FROM responses r LEFT JOIN surveys s ON r.survey_id = s.id " +
            "WHERE r.user_id = ? ORDER BY r.created_at DESC LIMIT ?, ?";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, userId, offset, size);
        for (Map<String, Object> m : list) {
            if (m.get("survey_title") == null) m.put("survey_title", "已删除的问卷");
        }
        return list;
    }

    private String truncate(String s, int len) {
        return s != null && s.length() > len ? s.substring(0, len) + "..." : s;
    }

    private static final String BASE62_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    private long fromBase62Static(String s) {
        long result = 0;
        for (int i = 0; i < s.length(); i++) {
            result = result * 62 + BASE62_CHARS.indexOf(s.charAt(i));
        }
        return result;
    }
}
