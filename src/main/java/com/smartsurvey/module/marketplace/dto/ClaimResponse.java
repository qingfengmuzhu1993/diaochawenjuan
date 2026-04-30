package com.smartsurvey.module.marketplace.dto;

import java.time.LocalDateTime;

public class ClaimResponse {
    private Long responseId;
    private LocalDateTime expireAt;

    public Long getResponseId() { return responseId; }
    public void setResponseId(Long responseId) { this.responseId = responseId; }
    public LocalDateTime getExpireAt() { return expireAt; }
    public void setExpireAt(LocalDateTime expireAt) { this.expireAt = expireAt; }
}
