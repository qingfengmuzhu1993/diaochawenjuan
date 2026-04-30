package com.smartsurvey.module.marketplace.dto;

import java.math.BigDecimal;
import java.util.List;

public class LeaderboardResponse {
    private List<Entry> entries;
    private String period;

    public List<Entry> getEntries() { return entries; }
    public void setEntries(List<Entry> entries) { this.entries = entries; }
    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }

    public static class Entry {
        private Long userId;
        private String username;
        private String avatarUrl;
        private BigDecimal earnings;
        private int rank;

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getAvatarUrl() { return avatarUrl; }
        public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
        public BigDecimal getEarnings() { return earnings; }
        public void setEarnings(BigDecimal earnings) { this.earnings = earnings; }
        public int getRank() { return rank; }
        public void setRank(int rank) { this.rank = rank; }
    }
}
