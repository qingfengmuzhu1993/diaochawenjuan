package com.smartsurvey.module.survey.dto;
import java.util.List;

public class AiGenerateResponse {
    private CreateSurveyRequest surveyDraft;
    private Double aiScore;
    private Integer estimatedTimeMinutes;
    private List<String> suggestions;

    public CreateSurveyRequest getSurveyDraft() { return surveyDraft; }
    public void setSurveyDraft(CreateSurveyRequest surveyDraft) { this.surveyDraft = surveyDraft; }
    public Double getAiScore() { return aiScore; }
    public void setAiScore(Double aiScore) { this.aiScore = aiScore; }
    public Integer getEstimatedTimeMinutes() { return estimatedTimeMinutes; }
    public void setEstimatedTimeMinutes(Integer estimatedTimeMinutes) { this.estimatedTimeMinutes = estimatedTimeMinutes; }
    public List<String> getSuggestions() { return suggestions; }
    public void setSuggestions(List<String> suggestions) { this.suggestions = suggestions; }
}
