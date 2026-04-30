package com.smartsurvey.module.response.dto;

import java.util.List;

public class SubmitAnswerRequest {
    private List<AnswerItem> answers;

    public List<AnswerItem> getAnswers() { return answers; }
    public void setAnswers(List<AnswerItem> answers) { this.answers = answers; }

    public static class AnswerItem {
        private Long questionId;
        private String answerText;
        private List<Long> answerOptions;
        private Integer answerRating;

        public Long getQuestionId() { return questionId; }
        public void setQuestionId(Long questionId) { this.questionId = questionId; }
        public String getAnswerText() { return answerText; }
        public void setAnswerText(String answerText) { this.answerText = answerText; }
        public List<Long> getAnswerOptions() { return answerOptions; }
        public void setAnswerOptions(List<Long> answerOptions) { this.answerOptions = answerOptions; }
        public Integer getAnswerRating() { return answerRating; }
        public void setAnswerRating(Integer answerRating) { this.answerRating = answerRating; }
    }
}
