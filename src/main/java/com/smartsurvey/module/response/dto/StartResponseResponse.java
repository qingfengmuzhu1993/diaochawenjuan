package com.smartsurvey.module.response.dto;

import java.time.LocalDateTime;

public class StartResponseResponse {
    private Long responseId;
    private Object firstQuestion;
    private int totalQuestions;
    private LocalDateTime expireAt;

    public Long getResponseId() { return responseId; }
    public void setResponseId(Long responseId) { this.responseId = responseId; }
    public Object getFirstQuestion() { return firstQuestion; }
    public void setFirstQuestion(Object firstQuestion) { this.firstQuestion = firstQuestion; }
    public int getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }
    public LocalDateTime getExpireAt() { return expireAt; }
    public void setExpireAt(LocalDateTime expireAt) { this.expireAt = expireAt; }
}
