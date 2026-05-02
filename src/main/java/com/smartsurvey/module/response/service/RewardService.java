package com.smartsurvey.module.response.service;

import com.smartsurvey.module.incentive.entity.Transaction;
import com.smartsurvey.module.incentive.mapper.TransactionMapper;
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
import java.time.LocalDateTime;

@Service
public class RewardService {
    private final SurveyMapper surveyMapper;
    private final UserMapper userMapper;
    private final ResponseMapper responseMapper;
    private final TransactionService transactionService;
    private final TransactionMapper transactionMapper;

    public RewardService(SurveyMapper surveyMapper, UserMapper userMapper,
                          ResponseMapper responseMapper, TransactionService transactionService,
                          TransactionMapper transactionMapper) {
        this.surveyMapper = surveyMapper;
        this.userMapper = userMapper;
        this.responseMapper = responseMapper;
        this.transactionService = transactionService;
        this.transactionMapper = transactionMapper;
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

    @Transactional
    public void grantViralBonus(Long referrerId, BigDecimal answererReward, Long responseId) {
        if (answererReward == null || answererReward.compareTo(BigDecimal.ZERO) <= 0) return;
        BigDecimal bonus = answererReward.multiply(new BigDecimal("0.10")).setScale(2, java.math.RoundingMode.DOWN);
        if (bonus.compareTo(new BigDecimal("0.01")) < 0) return;

        // Create transaction record
        Transaction tx = new Transaction();
        tx.setUserId(referrerId);
        tx.setType("reward");
        tx.setAmount(bonus);
        tx.setRelatedId(responseId);
        tx.setRelatedType("viral_share");
        tx.setStatus("success");
        tx.setRemark("邀请好友答题奖励");
        tx.setCreatedAt(LocalDateTime.now());
        transactionMapper.insert(tx);

        // Update user balance
        User user = userMapper.selectById(referrerId);
        if (user != null) {
            user.setBalance(user.getBalance().add(bonus));
            userMapper.updateById(user);
        }
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
