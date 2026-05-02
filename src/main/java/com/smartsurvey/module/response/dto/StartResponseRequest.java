package com.smartsurvey.module.response.dto;

public class StartResponseRequest {
    private Long surveyId;
    private String channel;
    private String referrerCode;

    public Long getSurveyId() { return surveyId; }
    public void setSurveyId(Long surveyId) { this.surveyId = surveyId; }
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public String getReferrerCode() { return referrerCode; }
    public void setReferrerCode(String referrerCode) { this.referrerCode = referrerCode; }
}
