package com.smartsurvey.module.response.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

@TableName("answers")
public class Answer {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long responseId;
    private Long questionId;
    private String answerText;
    private String answerOptions;
    private Integer answerRating;
    private String answerOrder;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getResponseId() { return responseId; }
    public void setResponseId(Long responseId) { this.responseId = responseId; }
    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }
    public String getAnswerText() { return answerText; }
    public void setAnswerText(String answerText) { this.answerText = answerText; }
    public String getAnswerOptions() { return answerOptions; }
    public void setAnswerOptions(String answerOptions) { this.answerOptions = answerOptions; }
    public Integer getAnswerRating() { return answerRating; }
    public void setAnswerRating(Integer answerRating) { this.answerRating = answerRating; }
    public String getAnswerOrder() { return answerOrder; }
    public void setAnswerOrder(String answerOrder) { this.answerOrder = answerOrder; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
