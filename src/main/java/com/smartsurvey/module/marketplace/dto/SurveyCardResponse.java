package com.smartsurvey.module.marketplace.dto;

import java.math.BigDecimal;

public class SurveyCardResponse {
    private Long id;
    private String title;
    private String description;
    private BigDecimal rewardPerResponse;
    private int questionCount;
    private int estimatedMinutes;
    private int remainingQuota;
    private int totalResponses;
    private String creatorName;
    private Long creatorId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getRewardPerResponse() { return rewardPerResponse; }
    public void setRewardPerResponse(BigDecimal rewardPerResponse) { this.rewardPerResponse = rewardPerResponse; }
    public int getQuestionCount() { return questionCount; }
    public void setQuestionCount(int questionCount) { this.questionCount = questionCount; }
    public int getEstimatedMinutes() { return estimatedMinutes; }
    public void setEstimatedMinutes(int estimatedMinutes) { this.estimatedMinutes = estimatedMinutes; }
    public int getRemainingQuota() { return remainingQuota; }
    public void setRemainingQuota(int remainingQuota) { this.remainingQuota = remainingQuota; }
    public int getTotalResponses() { return totalResponses; }
    public void setTotalResponses(int totalResponses) { this.totalResponses = totalResponses; }
    public String getCreatorName() { return creatorName; }
    public void setCreatorName(String creatorName) { this.creatorName = creatorName; }
    public Long getCreatorId() { return creatorId; }
    public void setCreatorId(Long creatorId) { this.creatorId = creatorId; }
}
