package com.smartsurvey.module.marketplace.dto;

public class MarketplaceQuery {
    private String sort; // recommended/newest/highest_reward/ending_soon
    private String category;
    private Double minReward;
    private Double maxReward;
    private Integer maxDuration;
    private int page = 1;
    private int size = 20;

    public String getSort() { return sort; }
    public void setSort(String sort) { this.sort = sort; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public Double getMinReward() { return minReward; }
    public void setMinReward(Double minReward) { this.minReward = minReward; }
    public Double getMaxReward() { return maxReward; }
    public void setMaxReward(Double maxReward) { this.maxReward = maxReward; }
    public Integer getMaxDuration() { return maxDuration; }
    public void setMaxDuration(Integer maxDuration) { this.maxDuration = maxDuration; }
    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
}
