package com.smartsurvey.module.survey.dto;
import javax.validation.constraints.NotBlank;

public class AiGenerateRequest {
    @NotBlank(message = "请描述您的调研需求")
    private String prompt;
    private String industry;
    private Integer questionCount;

    public String getPrompt() { return prompt; }
    public void setPrompt(String prompt) { this.prompt = prompt; }
    public String getIndustry() { return industry; }
    public void setIndustry(String industry) { this.industry = industry; }
    public Integer getQuestionCount() { return questionCount; }
    public void setQuestionCount(Integer questionCount) { this.questionCount = questionCount; }
}
