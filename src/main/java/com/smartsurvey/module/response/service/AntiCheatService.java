package com.smartsurvey.module.response.service;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.smartsurvey.module.response.entity.Answer;
import com.smartsurvey.module.response.entity.Response;
import com.smartsurvey.module.response.mapper.AnswerMapper;
import com.smartsurvey.module.response.mapper.ResponseMapper;
import com.smartsurvey.module.survey.entity.Question;
import com.smartsurvey.module.survey.mapper.QuestionMapper;
import com.smartsurvey.module.survey.mapper.SurveyMapper;
import com.smartsurvey.module.survey.service.SurveyLogicService;
import com.smartsurvey.module.survey.service.SurveyService;
import com.smartsurvey.module.user.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AntiCheatService {
    private static final Logger log = LoggerFactory.getLogger(AntiCheatService.class);
    private final ResponseMapper responseMapper;
    private final AnswerMapper answerMapper;
    private final QuestionMapper questionMapper;
    private final SurveyMapper surveyMapper;
    private final SurveyLogicService surveyLogicService;
    private final SurveyService surveyService;
    private final ResponseService responseService;
    private final RewardService rewardService;
    private final UserMapper userMapper;

    public AntiCheatService(ResponseMapper responseMapper, AnswerMapper answerMapper,
                             QuestionMapper questionMapper, SurveyMapper surveyMapper,
                             SurveyLogicService surveyLogicService, SurveyService surveyService,
                             @Lazy ResponseService responseService, RewardService rewardService,
                             UserMapper userMapper) {
        this.responseMapper = responseMapper;
        this.answerMapper = answerMapper;
        this.questionMapper = questionMapper;
        this.surveyMapper = surveyMapper;
        this.surveyLogicService = surveyLogicService;
        this.surveyService = surveyService;
        this.responseService = responseService;
        this.rewardService = rewardService;
        this.userMapper = userMapper;
    }

    public void evaluateAsync(Response response) {
        new Thread(() -> {
            try {
                int score = evaluate(response);
                response.setQualityScore(BigDecimal.valueOf(score));
                if (score < 3) {
                    response.setStatus("rejected");
                    response.setReviewNote("AI检测到异常答题行为（评分过低）");
                    userMapper.updateReputation(response.getUserId(), -5);
                } else if (score < 6) {
                    response.setReviewType("manual");
                    response.setReviewNote("质量评分偏低，需人工审核");
                } else {
                    response.setStatus("approved");
                    response.setReviewNote("AI审核通过（评分：" + score + "）");
                    rewardService.grantReward(response);
                    if (response.getReferrerId() != null && response.getRewardAmount() != null
                            && response.getRewardAmount().compareTo(BigDecimal.ZERO) > 0) {
                        rewardService.grantViralBonus(response.getReferrerId(),
                            response.getRewardAmount(), response.getId());
                    }
                }
                responseMapper.updateById(response);
            } catch (Exception e) {
                log.error("Auto evaluation failed for response {}", response.getId(), e);
            }
        }).start();
    }

    private int evaluate(Response response) {
        String behaviorData = response.getBehaviorData();
        if (behaviorData == null || behaviorData.isEmpty()) return 5;

        int deductions = 0;
        try {
            JSONObject data = JSONUtil.parseObj(behaviorData);
            for (String key : data.keySet()) {
                JSONObject qData = data.getJSONObject(key);
                int duration = qData.getInt("durationSeconds", 999);
                if (duration < 2) deductions += 2;
                else if (duration < 5) deductions += 1;
                int focusLoss = qData.getInt("lostFocusCount", 0);
                if (focusLoss > 5) deductions += 1;
            }
            // Check total time too short
            int totalDuration = 0;
            for (String key : data.keySet()) {
                totalDuration += data.getJSONObject(key).getInt("durationSeconds", 0);
            }
            if (totalDuration < 10) deductions += 3;
            else if (totalDuration < 30) deductions += 1;
        } catch (Exception ignored) {}

        return Math.max(0, 10 - deductions);
    }

    private boolean isPatternAnswer(List<Answer> answers) {
        int count = 0;
        String prev = null;
        for (Answer a : answers) {
            if (a.getAnswerOptions() != null) {
                if (prev != null && prev.equals(a.getAnswerOptions())) {
                    count++;
                }
                prev = a.getAnswerOptions();
            }
        }
        return answers.size() >= 5 && count >= answers.size() - 2;
    }

    @Transactional
    public void approve(Long responseId) {
        Response r = responseMapper.selectById(responseId);
        r.setStatus("approved");
        r.setReviewType("auto");
        r.setQualityScore(new BigDecimal("8.0"));
        r.setReviewNote("自动审核通过");
        responseMapper.updateById(r);
        rewardService.grantReward(r);

        // Process viral reward if this response came from a share link
        if (r.getReferrerId() != null) {
            rewardService.grantViralBonus(r.getReferrerId(), r.getRewardAmount(), r.getId());
        }
    }

    @Transactional
    public void reject(Long responseId, String reason) {
        Response r = responseMapper.selectById(responseId);
        r.setStatus("rejected");
        r.setReviewType("auto");
        r.setQualityScore(BigDecimal.ZERO);
        r.setReviewNote(reason);
        responseMapper.updateById(r);
        surveyService.incrementRemainingQuota(r.getSurveyId());
    }

    @Transactional
    public void markForManualReview(Long responseId, String reason) {
        Response r = responseMapper.selectById(responseId);
        r.setReviewType("manual");
        r.setReviewNote("AI标记: " + reason);
        responseMapper.updateById(r);
    }
}
