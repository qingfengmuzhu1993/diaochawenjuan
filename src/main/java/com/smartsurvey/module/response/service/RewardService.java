package com.smartsurvey.module.response.service;

import com.smartsurvey.module.incentive.service.TransactionService;
import com.smartsurvey.module.response.entity.Response;
import com.smartsurvey.module.response.mapper.ResponseMapper;
import com.smartsurvey.module.survey.entity.Survey;
import com.smartsurvey.module.survey.mapper.SurveyMapper;
import com.smartsurvey.module.user.entity.User;
import com.smartsurvey.module.user.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class RewardService {
    private final SurveyMapper surveyMapper;
    private final UserMapper userMapper;
    private final ResponseMapper responseMapper;
    private final TransactionService transactionService;

    public RewardService(SurveyMapper surveyMapper, UserMapper userMapper,
                          ResponseMapper responseMapper, TransactionService transactionService) {
        this.surveyMapper = surveyMapper;
        this.userMapper = userMapper;
        this.responseMapper = responseMapper;
        this.transactionService = transactionService;
    }

    @Transactional
    public void grantReward(Response response) {
        Survey survey = surveyMapper.selectById(response.getSurveyId());
        if (survey.getRewardPerResponse() == null
                || survey.getRewardPerResponse().compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        BigDecimal reward = calculateReward(survey, response);

        // Deduct from creator
        User creator = userMapper.selectById(survey.getUserId());
        creator.setBalance(creator.getBalance().subtract(reward));
        userMapper.updateById(creator);

        // Add to responder
        User responder = userMapper.selectById(response.getUserId());
        responder.setBalance(responder.getBalance().add(reward));
        userMapper.updateById(responder);

        // Create transactions
        transactionService.createRewardTransaction(creator.getId(), reward.negate(),
                response.getId(), "问卷奖励支出 - 问卷#" + survey.getId());
        transactionService.createRewardTransaction(responder.getId(), reward,
                response.getId(), "回答奖励收入 - 问卷#" + survey.getId());

        // Update response
        response.setRewardAmount(reward);
        responseMapper.updateById(response);
    }

    private BigDecimal calculateReward(Survey survey, Response response) {
        BigDecimal base = survey.getRewardPerResponse();
        if ("tiered".equals(survey.getRewardType())) {
            BigDecimal qualityMult = BigDecimal.ONE;
            if (response.getQualityScore() != null
                    && response.getQualityScore().compareTo(new BigDecimal("8")) >= 0) {
                qualityMult = new BigDecimal("1.5");
            }
            return base.multiply(qualityMult).min(base.multiply(new BigDecimal("2")));
        }
        return base;
    }
}
