package com.smartsurvey.module.user.dto;

public class UserProfileResponse {
    private Long id;
    private String username;
    private String avatarUrl;
    private String bio;
    private String tags;
    private Integer level;
    private Integer reputation;
    private String role;
    private boolean verified;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    public Integer getLevel() { return level; }
    public void setLevel(Integer level) { this.level = level; }
    public Integer getReputation() { return reputation; }
    public void setReputation(Integer reputation) { this.reputation = reputation; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }
}
