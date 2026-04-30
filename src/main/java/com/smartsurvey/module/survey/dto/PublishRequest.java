package com.smartsurvey.module.survey.dto;

import java.math.BigDecimal;

public class PublishRequest {
    private String dispatchType;
    private BigDecimal rewardTotalBudget;

    public String getDispatchType() { return dispatchType; }
    public void setDispatchType(String dispatchType) { this.dispatchType = dispatchType; }
    public BigDecimal getRewardTotalBudget() { return rewardTotalBudget; }
    public void setRewardTotalBudget(BigDecimal rewardTotalBudget) { this.rewardTotalBudget = rewardTotalBudget; }
}
