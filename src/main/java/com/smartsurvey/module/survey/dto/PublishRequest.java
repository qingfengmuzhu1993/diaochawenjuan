package com.smartsurvey.module.survey.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PublishRequest {
    private String dispatchType;
    private BigDecimal rewardTotalBudget;
    private Integer targetQuota;
    private LocalDateTime endTime;

    public String getDispatchType() { return dispatchType; }
    public void setDispatchType(String dispatchType) { this.dispatchType = dispatchType; }
    public BigDecimal getRewardTotalBudget() { return rewardTotalBudget; }
    public void setRewardTotalBudget(BigDecimal rewardTotalBudget) { this.rewardTotalBudget = rewardTotalBudget; }
    public Integer getTargetQuota() { return targetQuota; }
    public void setTargetQuota(Integer targetQuota) { this.targetQuota = targetQuota; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
}
