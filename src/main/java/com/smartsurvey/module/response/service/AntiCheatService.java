package com.smartsurvey.module.response.service;

import com.smartsurvey.module.response.entity.Answer;
import com.smartsurvey.module.response.entity.Response;
import com.smartsurvey.module.response.mapper.AnswerMapper;
import com.smartsurvey.module.response.mapper.ResponseMapper;
import com.smartsurvey.module.survey.entity.Question;
import com.smartsurvey.module.survey.mapper.QuestionMapper;
import com.smartsurvey.module.survey.mapper.SurveyMapper;
import com.smartsurvey.module.survey.service.SurveyLogicService;
import com.smartsurvey.module.survey.service.SurveyService;
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

    public AntiCheatService(ResponseMapper responseMapper, AnswerMapper answerMapper,
                             QuestionMapper questionMapper, SurveyMapper surveyMapper,
                             SurveyLogicService surveyLogicService, SurveyService surveyService,
                             @Lazy ResponseService responseService, RewardService rewardService) {
        this.responseMapper = responseMapper;
        this.answerMapper = answerMapper;
        this.questionMapper = questionMapper;
        this.surveyMapper = surveyMapper;
        this.surveyLogicService = surveyLogicService;
        this.surveyService = surveyService;
        this.responseService = responseService;
        this.rewardService = rewardService;
    }

    @Async
    @Transactional
    public void evaluateAsync(Response response) {
        int riskScore = 0;
        StringBuilder reason = new StringBuilder();
        List<Question> questions = questionMapper.selectBySurveyId(response.getSurveyId());
        int minExpectedSeconds = surveyLogicService.estimateDuration(questions) / 3;

        // Check 1: Duration anomaly
        if (response.getDurationSeconds() != null && minExpectedSeconds > 0
                && response.getDurationSeconds() < minExpectedSeconds) {
            riskScore += 30;
            reason.append("完成时间异常短(").append(response.getDurationSeconds()).append("s); ");
        }

        // Check 2: Pattern answers
        List<Answer> answers = answerMapper.selectByResponseId(response.getId());
        if (isPatternAnswer(answers)) {
            riskScore += 40;
            reason.append("检测到规律性选择; ");
        }

        // Check 3: Multi-account on same device
        if (response.getDeviceFingerprint() != null) {
            int accountsOnDevice = responseMapper.countAccountsByFingerprint(
                    response.getDeviceFingerprint(), response.getSurveyId());
            if (accountsOnDevice > 2) {
                riskScore += 25;
                reason.append("同设备多账号回答; ");
            }
        }

        // Decision
        if (riskScore >= 60) {
            reject(response.getId(), reason.toString());
        } else if (riskScore >= 30) {
            markForManualReview(response.getId(), reason.toString());
        } else {
            approve(response.getId());
        }
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
