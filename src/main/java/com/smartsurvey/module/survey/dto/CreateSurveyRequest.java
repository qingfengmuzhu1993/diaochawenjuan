package com.smartsurvey.module.survey.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class CreateSurveyRequest {
    @NotBlank(message = "问卷标题不能为空")
    @Size(max = 200)
    private String title;
    private String description;
    private String coverImage;
    private String closingMessage;
    private Integer timeLimitMinutes;
    private Integer maxAttempts;
    private Integer isAnonymous;
    private Integer allowResume;
    private Integer targetQuota;
    private BigDecimal rewardPerResponse;
    private String rewardType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<QuestionItem> questions;

    public String getTitle() { return title; } public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; } public void setDescription(String description) { this.description = description; }
    public String getCoverImage() { return coverImage; } public void setCoverImage(String coverImage) { this.coverImage = coverImage; }
    public String getClosingMessage() { return closingMessage; } public void setClosingMessage(String closingMessage) { this.closingMessage = closingMessage; }
    public Integer getTimeLimitMinutes() { return timeLimitMinutes; } public void setTimeLimitMinutes(Integer timeLimitMinutes) { this.timeLimitMinutes = timeLimitMinutes; }
    public Integer getMaxAttempts() { return maxAttempts; } public void setMaxAttempts(Integer maxAttempts) { this.maxAttempts = maxAttempts; }
    public Integer getIsAnonymous() { return isAnonymous; } public void setIsAnonymous(Integer isAnonymous) { this.isAnonymous = isAnonymous; }
    public Integer getAllowResume() { return allowResume; } public void setAllowResume(Integer allowResume) { this.allowResume = allowResume; }
    public Integer getTargetQuota() { return targetQuota; } public void setTargetQuota(Integer targetQuota) { this.targetQuota = targetQuota; }
    public BigDecimal getRewardPerResponse() { return rewardPerResponse; } public void setRewardPerResponse(BigDecimal rewardPerResponse) { this.rewardPerResponse = rewardPerResponse; }
    public String getRewardType() { return rewardType; } public void setRewardType(String rewardType) { this.rewardType = rewardType; }
    public LocalDateTime getStartTime() { return startTime; } public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; } public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public List<QuestionItem> getQuestions() { return questions; } public void setQuestions(List<QuestionItem> questions) { this.questions = questions; }

    public static class QuestionItem {
        private String type;
        private String content;
        private Integer required;
        private Integer orderIndex;
        private String options;
        private String settings;
        private String logicJump;
        private String logicShow;
        private Integer isRandomOptions;

        public String getType() { return type; } public void setType(String type) { this.type = type; }
        public String getContent() { return content; } public void setContent(String content) { this.content = content; }
        public Integer getRequired() { return required; } public void setRequired(Integer required) { this.required = required; }
        public Integer getOrderIndex() { return orderIndex; } public void setOrderIndex(Integer orderIndex) { this.orderIndex = orderIndex; }
        public String getOptions() { return options; } public void setOptions(String options) { this.options = options; }
        public String getSettings() { return settings; } public void setSettings(String settings) { this.settings = settings; }
        public String getLogicJump() { return logicJump; } public void setLogicJump(String logicJump) { this.logicJump = logicJump; }
        public String getLogicShow() { return logicShow; } public void setLogicShow(String logicShow) { this.logicShow = logicShow; }
        public Integer getIsRandomOptions() { return isRandomOptions; } public void setIsRandomOptions(Integer isRandomOptions) { this.isRandomOptions = isRandomOptions; }
    }
}
