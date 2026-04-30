package com.smartsurvey.module.survey.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

@TableName("questions")
public class Question {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long surveyId;
    private String type;
    private String content;
    private Integer required;
    private Integer orderIndex;
    private String options;
    private String settings;
    private String logicJump;
    private String logicShow;
    private Integer isRandomOptions;
    private String quotaLimit;
    private LocalDateTime createdAt;

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public Long getSurveyId() { return surveyId; } public void setSurveyId(Long surveyId) { this.surveyId = surveyId; }
    public String getType() { return type; } public void setType(String type) { this.type = type; }
    public String getContent() { return content; } public void setContent(String content) { this.content = content; }
    public Integer getRequired() { return required; } public void setRequired(Integer required) { this.required = required; }
    public Integer getOrderIndex() { return orderIndex; } public void setOrderIndex(Integer orderIndex) { this.orderIndex = orderIndex; }
    public String getOptions() { return options; } public void setOptions(String options) { this.options = options; }
    public String getSettings() { return settings; } public void setSettings(String settings) { this.settings = settings; }
    public String getLogicJump() { return logicJump; } public void setLogicJump(String logicJump) { this.logicJump = logicJump; }
    public String getLogicShow() { return logicShow; } public void setLogicShow(String logicShow) { this.logicShow = logicShow; }
    public Integer getIsRandomOptions() { return isRandomOptions; } public void setIsRandomOptions(Integer isRandomOptions) { this.isRandomOptions = isRandomOptions; }
    public String getQuotaLimit() { return quotaLimit; } public void setQuotaLimit(String quotaLimit) { this.quotaLimit = quotaLimit; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
