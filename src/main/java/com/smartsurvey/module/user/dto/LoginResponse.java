package com.smartsurvey.module.user.dto;

public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private Long expiresIn;
    private UserInfo userInfo;

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    public Long getExpiresIn() { return expiresIn; }
    public void setExpiresIn(Long expiresIn) { this.expiresIn = expiresIn; }
    public UserInfo getUserInfo() { return userInfo; }
    public void setUserInfo(UserInfo userInfo) { this.userInfo = userInfo; }

    public static class UserInfo {
        private Long id;
        private String username;
        private String avatarUrl;
        private Integer level;
        private Integer reputation;
        private String role;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getAvatarUrl() { return avatarUrl; }
        public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
        public Integer getLevel() { return level; }
        public void setLevel(Integer level) { this.level = level; }
        public Integer getReputation() { return reputation; }
        public void setReputation(Integer reputation) { this.reputation = reputation; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }
}
