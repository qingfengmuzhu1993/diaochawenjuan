package com.smartsurvey.module.response.dto;

public class StartResponseRequest {
    private Long surveyId;
    private String channel;

    public Long getSurveyId() { return surveyId; }
    public void setSurveyId(Long surveyId) { this.surveyId = surveyId; }
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
}
