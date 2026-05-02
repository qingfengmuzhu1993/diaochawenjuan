package com.smartsurvey.module.marketplace.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartsurvey.common.dto.PageResult;
import com.smartsurvey.common.exception.BusinessException;
import com.smartsurvey.common.exception.ErrorCode;
import com.smartsurvey.module.marketplace.dto.*;
import com.smartsurvey.module.response.entity.Response;
import com.smartsurvey.module.response.mapper.ResponseMapper;
import com.smartsurvey.module.survey.entity.Survey;
import com.smartsurvey.module.survey.mapper.SurveyMapper;
import com.smartsurvey.module.survey.service.SurveyLogicService;
import com.smartsurvey.module.user.entity.User;
import com.smartsurvey.module.user.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class MarketplaceService {
    private final SurveyMapper surveyMapper;
    private final ResponseMapper responseMapper;
    private final SurveyLogicService surveyLogicService;
    private final UserMapper userMapper;

    public MarketplaceService(SurveyMapper surveyMapper, ResponseMapper responseMapper,
                               SurveyLogicService surveyLogicService, UserMapper userMapper) {
        this.surveyMapper = surveyMapper;
        this.responseMapper = responseMapper;
        this.surveyLogicService = surveyLogicService;
        this.userMapper = userMapper;
    }

    public PageResult<SurveyCardResponse> listSurveys(MarketplaceQuery query) {
        Page<Survey> page = new Page<>(query.getPage(), query.getSize());
        LambdaQueryWrapper<Survey> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Survey::getStatus, "published")
               .eq(Survey::getAuditStatus, "approved")
               .gt(Survey::getRemainingQuota, 0);

        // keyword search
        if (query.getKeyword() != null && !query.getKeyword().trim().isEmpty()) {
            wrapper.like(Survey::getTitle, query.getKeyword());
        }

        // reward range filter
        if (query.getMinReward() != null) {
            wrapper.ge(Survey::getRewardPerResponse, BigDecimal.valueOf(query.getMinReward()));
        }
        if (query.getMaxReward() != null) {
            wrapper.le(Survey::getRewardPerResponse, BigDecimal.valueOf(query.getMaxReward()));
        }

        // duration filter: time_limit_minutes=0 means unlimited
        if (query.getMaxDuration() != null) {
            wrapper.and(w -> w.eq(Survey::getTimeLimitMinutes, 0)
                .or().le(Survey::getTimeLimitMinutes, query.getMaxDuration()));
        }

        // sort
        if ("newest".equals(query.getSort())) {
            wrapper.orderByDesc(Survey::getCreatedAt);
        } else if ("highest_reward".equals(query.getSort())) {
            wrapper.orderByDesc(Survey::getRewardPerResponse);
        } else if ("ending_soon".equals(query.getSort())) {
            wrapper.isNotNull(Survey::getEndTime).orderByAsc(Survey::getEndTime);
        } else {
            // default = recommended = highest_reward
            wrapper.orderByDesc(Survey::getRewardPerResponse);
        }

        Page<Survey> result = surveyMapper.selectPage(page, wrapper);
        List<SurveyCardResponse> list = new ArrayList<>();
        for (Survey s : result.getRecords()) {
            SurveyCardResponse card = new SurveyCardResponse();
            card.setId(s.getId());
            card.setTitle(s.getTitle());
            card.setDescription(s.getDescription());
            card.setRewardPerResponse(s.getRewardPerResponse());
            card.setQuestionCount(s.getTotalQuestions() != null ? s.getTotalQuestions() : 0);
            card.setRemainingQuota(s.getRemainingQuota() != null ? s.getRemainingQuota() : 0);
            card.setTotalResponses(s.getTotalResponses() != null ? s.getTotalResponses() : 0);
            User creator = userMapper.selectById(s.getUserId());
            card.setCreatorName(creator != null ? creator.getUsername() : "匿名用户");
            card.setCreatorId(s.getUserId());
            list.add(card);
        }
        return new PageResult<>(query.getPage(), query.getSize(), result.getTotal(), list);
    }

    @Transactional
    public ClaimResponse claimSurvey(Long surveyId, Long userId) {
        Survey survey = surveyMapper.selectById(surveyId);
        if (survey == null) {
            throw new BusinessException(ErrorCode.SURVEY_NOT_FOUND);
        }
        if (survey.getRemainingQuota() != null && survey.getRemainingQuota() <= 0) {
            throw new BusinessException(ErrorCode.QUOTA_FULL);
        }

        Long activeCount = responseMapper.selectCount(new LambdaQueryWrapper<Response>()
            .eq(Response::getUserId, userId).eq(Response::getStatus, "in_progress"));
        if (activeCount >= 5) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "您有5个待完成的任务");
        }

        int updated = surveyMapper.decrementQuota(surveyId);
        if (updated == 0) {
            throw new BusinessException(ErrorCode.QUOTA_FULL);
        }

        Response existing = responseMapper.selectOne(new LambdaQueryWrapper<Response>()
            .eq(Response::getSurveyId, surveyId).eq(Response::getUserId, userId));
        if (existing != null) {
            if ("in_progress".equals(existing.getStatus())) {
                ClaimResponse resp = new ClaimResponse();
                resp.setResponseId(existing.getId());
                resp.setExpireAt(LocalDateTime.now().plusMinutes(15));
                return resp;
            }
            throw new BusinessException(ErrorCode.DUPLICATE_SUBMIT);
        }

        Response response = new Response();
        response.setSurveyId(surveyId);
        response.setUserId(userId);
        response.setStatus("in_progress");
        response.setStartTime(LocalDateTime.now());
        response.setChannel("marketplace");
        response.setCreatedAt(LocalDateTime.now());
        responseMapper.insert(response);

        ClaimResponse resp = new ClaimResponse();
        resp.setResponseId(response.getId());
        resp.setExpireAt(LocalDateTime.now().plusMinutes(15));
        return resp;
    }

    public List<SurveyCardResponse> getDailyRecommendations(Long userId) {
        MarketplaceQuery query = new MarketplaceQuery();
        query.setPage(1);
        query.setSize(10);
        query.setSort("highest_reward");
        return listSurveys(query).getList();
    }
}
