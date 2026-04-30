package com.smartsurvey.module.user.dto;

import java.math.BigDecimal;

public class UserDashboardResponse {
    private long surveyCount;
    private long responseCount;
    private BigDecimal totalEarnings;
    private long followerCount;

    public long getSurveyCount() { return surveyCount; }
    public void setSurveyCount(long surveyCount) { this.surveyCount = surveyCount; }
    public long getResponseCount() { return responseCount; }
    public void setResponseCount(long responseCount) { this.responseCount = responseCount; }
    public BigDecimal getTotalEarnings() { return totalEarnings; }
    public void setTotalEarnings(BigDecimal totalEarnings) { this.totalEarnings = totalEarnings; }
    public long getFollowerCount() { return followerCount; }
    public void setFollowerCount(long followerCount) { this.followerCount = followerCount; }
}
