package com.smartsurvey.module.survey.service;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.smartsurvey.common.ai.AiProvider;
import com.smartsurvey.common.exception.BusinessException;
import com.smartsurvey.common.exception.ErrorCode;
import com.smartsurvey.module.survey.dto.*;
import com.smartsurvey.module.survey.entity.Question;
import com.smartsurvey.module.survey.entity.Survey;
import com.smartsurvey.module.survey.mapper.QuestionMapper;
import com.smartsurvey.module.survey.mapper.SurveyMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AiGenerationService {

    private static final Logger log = LoggerFactory.getLogger(AiGenerationService.class);

    private final AiProvider aiProvider;
    private final SurveyMapper surveyMapper;
    private final QuestionMapper questionMapper;
    private final SurveyLogicService surveyLogicService;

    public AiGenerationService(AiProvider aiProvider, SurveyMapper surveyMapper,
                                QuestionMapper questionMapper,
                                SurveyLogicService surveyLogicService) {
        this.aiProvider = aiProvider;
        this.surveyMapper = surveyMapper;
        this.questionMapper = questionMapper;
        this.surveyLogicService = surveyLogicService;
    }

    public AiGenerateResponse generateSurvey(AiGenerateRequest req) {
        AiGenerateResponse resp = new AiGenerateResponse();
        int questionCount = req.getQuestionCount() != null ? Math.min(req.getQuestionCount(), 20) : 8;
        String industry = req.getIndustry() != null ? req.getIndustry() : "通用";

        try {
            String llmResponse = aiProvider.generateSurvey(
                req.getPrompt(), industry, questionCount);
            JSONObject result = parseJsonResponse(llmResponse);

            CreateSurveyRequest draft = new CreateSurveyRequest();
            draft.setTitle(result.getStr("title", "AI生成的问卷"));
            draft.setDescription(result.getStr("description",
                "本问卷由AI自动生成，基于您的需求：" + req.getPrompt()));

            JSONArray questionsJson = result.getJSONArray("questions");
            List<CreateSurveyRequest.QuestionItem> questions = new ArrayList<>();
            if (questionsJson != null) {
                for (int i = 0; i < questionsJson.size(); i++) {
                    JSONObject qj = questionsJson.getJSONObject(i);
                    CreateSurveyRequest.QuestionItem qi = new CreateSurveyRequest.QuestionItem();
                    qi.setType(qj.getStr("type", "single"));
                    qi.setContent(qj.getStr("content"));
                    qi.setRequired(qj.getInt("required", 1));
                    qi.setOrderIndex(i);

                    JSONArray optionsArr = qj.getJSONArray("options");
                    if (optionsArr != null) {
                        qi.setOptions(optionsArr.toString());
                    }
                    questions.add(qi);
                }
            }
            draft.setQuestions(questions);

            resp.setSurveyDraft(draft);
            resp.setAiScore(8.0);
            resp.setEstimatedTimeMinutes(questions.size() * 20 / 60 + 1);

            List<String> suggestions = generateSuggestions(result, questions);
            resp.setSuggestions(suggestions);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("AI generation failed", e);
            throw new BusinessException(ErrorCode.AI_GENERATION_FAILED);
        }
        return resp;
    }

    public AiDiagnoseResponse diagnoseSurvey(Long surveyId) {
        Survey survey = surveyMapper.selectById(surveyId);
        List<Question> questions = questionMapper.selectBySurveyId(surveyId);

        try {
            JSONArray questionsArray = new JSONArray();
            for (Question q : questions) {
                JSONObject qj = new JSONObject();
                qj.set("type", q.getType());
                qj.set("content", q.getContent());
                qj.set("orderIndex", q.getOrderIndex());
                questionsArray.add(qj);
            }

            String llmResponse = aiProvider.diagnoseSurvey(
                survey.getTitle(), questionsArray.toString());
            JSONObject result = parseJsonResponse(llmResponse);

            AiDiagnoseResponse resp = new AiDiagnoseResponse();
            JSONArray itemsArr = result.getJSONArray("items");
            List<AiDiagnoseResponse.DiagnosisItem> items = new ArrayList<>();
            if (itemsArr != null) {
                for (int i = 0; i < itemsArr.size(); i++) {
                    JSONObject item = itemsArr.getJSONObject(i);
                    items.add(new AiDiagnoseResponse.DiagnosisItem(
                        item.getStr("scope"),
                        item.getStr("level"),
                        item.getStr("message")));
                }
            }
            resp.setItems(items);
            resp.setEstimatedDurationSeconds(
                surveyLogicService.estimateDuration(questions));
            return resp;

        } catch (Exception e) {
            log.error("AI diagnosis failed", e);
            throw new BusinessException(ErrorCode.AI_GENERATION_FAILED);
        }
    }

    public String suggestQuestionImprovement(Long questionId) {
        Question q = questionMapper.selectById(questionId);
        if (q == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }

        try {
            String llmResponse = aiProvider.improveQuestion(q.getContent());
            JSONObject result = parseJsonResponse(llmResponse);

            String improved = result.getStr("improved");
            String reason = result.getStr("reason");

            StringBuilder sb = new StringBuilder();
            sb.append("【优化建议】").append(improved).append("\n");
            sb.append("【原因】").append(reason);
            return sb.toString();

        } catch (Exception e) {
            log.error("AI improvement suggestion failed", e);
            throw new BusinessException(ErrorCode.AI_GENERATION_FAILED);
        }
    }

    private JSONObject parseJsonResponse(String llmResponse) {
        String json = llmResponse.trim();
        if (json.startsWith("```json")) {
            json = json.substring(7);
        }
        if (json.startsWith("```")) {
            json = json.substring(3);
        }
        if (json.endsWith("```")) {
            json = json.substring(0, json.length() - 3);
        }
        json = json.trim();
        return JSONUtil.parseObj(json);
    }

    private List<String> generateSuggestions(JSONObject result,
            List<CreateSurveyRequest.QuestionItem> questions) {
        List<String> suggestions = new ArrayList<>();
        JSONArray aiSuggestions = result.getJSONArray("suggestions");
        if (aiSuggestions != null) {
            for (int i = 0; i < aiSuggestions.size(); i++) {
                suggestions.add(aiSuggestions.get(i).toString());
            }
        }
        if (suggestions.isEmpty()) {
            suggestions.add("建议根据实际调研目的调整题目措辞");
        }
        return suggestions;
    }
}
