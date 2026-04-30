package com.smartsurvey.module.survey.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SurveyBriefResponse {
    private Long id;
    private String title;
    private String status;
    private Integer totalQuestions;
    private Integer totalResponses;
    private BigDecimal rewardPerResponse;
    private LocalDateTime createdAt;

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; } public void setTitle(String title) { this.title = title; }
    public String getStatus() { return status; } public void setStatus(String status) { this.status = status; }
    public Integer getTotalQuestions() { return totalQuestions; } public void setTotalQuestions(Integer totalQuestions) { this.totalQuestions = totalQuestions; }
    public Integer getTotalResponses() { return totalResponses; } public void setTotalResponses(Integer totalResponses) { this.totalResponses = totalResponses; }
    public BigDecimal getRewardPerResponse() { return rewardPerResponse; } public void setRewardPerResponse(BigDecimal rewardPerResponse) { this.rewardPerResponse = rewardPerResponse; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
