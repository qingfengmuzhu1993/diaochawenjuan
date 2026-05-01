package com.smartsurvey.common.ai;

public interface AiProvider {

    String generateSurvey(String prompt, String industry, int questionCount);

    String diagnoseSurvey(String title, String questionsJson);

    String improveQuestion(String questionContent);
}
