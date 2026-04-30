package com.smartsurvey.module.user.dto;

public class UpdateProfileRequest {
    private String bio;
    private String tags;
    private String avatarUrl;

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
}
