package com.smartsurvey.module.survey.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("surveys")
public class Survey {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String title;
    private String description;
    private String coverImage;
    private String status;
    private Integer totalQuestions;
    private Integer totalResponses;
    private Integer targetQuota;
    private Integer remainingQuota;
    private String rewardType;
    private BigDecimal rewardPerResponse;
    private BigDecimal rewardTotalBudget;
    private String dispatchType;
    private String targetAudience;
    private Integer isAnonymous;
    private Integer allowResume;
    private Integer timeLimitMinutes;
    private Integer maxAttempts;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String closingMessage;
    private Integer viewCount;
    private Integer shareCount;
    private Integer aiGenerated;
    private String auditStatus;
    private String auditNote;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; } public void setUserId(Long userId) { this.userId = userId; }
    public String getTitle() { return title; } public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; } public void setDescription(String description) { this.description = description; }
    public String getCoverImage() { return coverImage; } public void setCoverImage(String coverImage) { this.coverImage = coverImage; }
    public String getStatus() { return status; } public void setStatus(String status) { this.status = status; }
    public Integer getTotalQuestions() { return totalQuestions; } public void setTotalQuestions(Integer totalQuestions) { this.totalQuestions = totalQuestions; }
    public Integer getTotalResponses() { return totalResponses; } public void setTotalResponses(Integer totalResponses) { this.totalResponses = totalResponses; }
    public Integer getTargetQuota() { return targetQuota; } public void setTargetQuota(Integer targetQuota) { this.targetQuota = targetQuota; }
    public Integer getRemainingQuota() { return remainingQuota; } public void setRemainingQuota(Integer remainingQuota) { this.remainingQuota = remainingQuota; }
    public String getRewardType() { return rewardType; } public void setRewardType(String rewardType) { this.rewardType = rewardType; }
    public BigDecimal getRewardPerResponse() { return rewardPerResponse; } public void setRewardPerResponse(BigDecimal rewardPerResponse) { this.rewardPerResponse = rewardPerResponse; }
    public BigDecimal getRewardTotalBudget() { return rewardTotalBudget; } public void setRewardTotalBudget(BigDecimal rewardTotalBudget) { this.rewardTotalBudget = rewardTotalBudget; }
    public String getDispatchType() { return dispatchType; } public void setDispatchType(String dispatchType) { this.dispatchType = dispatchType; }
    public String getTargetAudience() { return targetAudience; } public void setTargetAudience(String targetAudience) { this.targetAudience = targetAudience; }
    public Integer getIsAnonymous() { return isAnonymous; } public void setIsAnonymous(Integer isAnonymous) { this.isAnonymous = isAnonymous; }
    public Integer getAllowResume() { return allowResume; } public void setAllowResume(Integer allowResume) { this.allowResume = allowResume; }
    public Integer getTimeLimitMinutes() { return timeLimitMinutes; } public void setTimeLimitMinutes(Integer timeLimitMinutes) { this.timeLimitMinutes = timeLimitMinutes; }
    public Integer getMaxAttempts() { return maxAttempts; } public void setMaxAttempts(Integer maxAttempts) { this.maxAttempts = maxAttempts; }
    public LocalDateTime getStartTime() { return startTime; } public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; } public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public String getClosingMessage() { return closingMessage; } public void setClosingMessage(String closingMessage) { this.closingMessage = closingMessage; }
    public Integer getViewCount() { return viewCount; } public void setViewCount(Integer viewCount) { this.viewCount = viewCount; }
    public Integer getShareCount() { return shareCount; } public void setShareCount(Integer shareCount) { this.shareCount = shareCount; }
    public Integer getAiGenerated() { return aiGenerated; } public void setAiGenerated(Integer aiGenerated) { this.aiGenerated = aiGenerated; }
    public String getAuditStatus() { return auditStatus; } public void setAuditStatus(String auditStatus) { this.auditStatus = auditStatus; }
    public String getAuditNote() { return auditNote; } public void setAuditNote(String auditNote) { this.auditNote = auditNote; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; } public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
