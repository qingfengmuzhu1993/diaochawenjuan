package com.smartsurvey.common.ai;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

public abstract class AbstractAiProvider implements AiProvider {

    protected final RestTemplate restTemplate;
    protected final String apiKey;
    protected final String baseUrl;
    protected final String model;

    protected AbstractAiProvider(RestTemplate restTemplate, String apiKey, String baseUrl, String model) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.model = model;
    }

    @Override
    public String generateSurvey(String prompt, String industry, int questionCount) {
        String systemPrompt = buildSurveySystemPrompt();
        String userPrompt = buildSurveyUserPrompt(prompt, industry, questionCount);
        return callChatApi(systemPrompt, userPrompt);
    }

    @Override
    public String diagnoseSurvey(String title, String questionsJson) {
        String systemPrompt = "你是一个专业的问卷调研诊断专家。请根据提供的问卷信息，给出针对性的诊断建议。";

        String userPrompt = String.format(
            "请诊断以下问卷：\n标题：%s\n题目列表：%s\n\n" +
            "请从以下维度诊断：\n" +
            "1. 题目数量是否合适\n" +
            "2. 题型多样性\n" +
            "3. 逻辑结构是否合理\n" +
            "4. 是否有引导性问题\n" +
            "5. 预计答题时长是否合理\n\n" +
            "返回JSON格式：{\"score\": 8.5, \"items\": [{\"scope\": \"...\", \"level\": \"suggestion|warning|error\", \"message\": \"...\"}], \"summary\": \"...\"}",
            title, questionsJson);

        return callChatApi(systemPrompt, userPrompt);
    }

    @Override
    public String improveQuestion(String questionContent) {
        String systemPrompt = "你是一个专业的问卷设计专家。请对给定的题目进行优化，使其更清晰、中立、有效。";

        String userPrompt = String.format(
            "请优化以下问卷题目：\n%s\n\n" +
            "返回JSON格式：{\"original\": \"...\", \"improved\": \"...\", \"reason\": \"...\"}",
            questionContent);

        return callChatApi(systemPrompt, userPrompt);
    }

    private String buildSurveySystemPrompt() {
        return "你是一个专业的问卷调研专家。请严格按照要求生成调查问卷，并以严格的JSON格式返回。\n\n" +
               "题目类型可选：single_choice(单选), multiple_choice(多选), rating(量表1-5), essay(开放题)\n\n" +
               "要求：\n" +
               "1. 题目类型多样化\n" +
               "2. 每道题有2-6个选项\n" +
               "3. 逻辑上由浅入深（先基本问题，再深入问题）\n" +
               "4. 必须返回严格JSON，不要包含任何其他文字\n" +
               "5. 题目数量必须准确符合要求";
    }

    private String buildSurveyUserPrompt(String prompt, String industry, int questionCount) {
        return String.format(
            "需求描述：%s\n行业领域：%s\n题目数量：%d\n\n" +
            "请生成一份完整的调查问卷。\n\n" +
            "返回JSON格式：\n" +
            "{\n" +
            "  \"title\": \"问卷标题（简洁有力，10-20字）\",\n" +
            "  \"description\": \"问卷说明（1-2句话）\",\n" +
            "  \"questions\": [\n" +
            "    {\n" +
            "      \"type\": \"single_choice\",\n" +
            "      \"content\": \"题目内容\",\n" +
            "      \"required\": 1,\n" +
            "      \"options\": [{\"id\":1,\"text\":\"选项A\"},{\"id\":2,\"text\":\"选项B\"}]\n" +
            "    }\n" +
            "  ]\n" +
            "}",
            prompt, industry, questionCount);
    }

    protected String callChatApi(String systemPrompt, String userPrompt) {
        String url = baseUrl + "/chat/completions";

        JSONObject requestBody = new JSONObject();
        requestBody.set("model", model);

        JSONArray messages = new JSONArray();

        JSONObject systemMsg = new JSONObject();
        systemMsg.set("role", "system");
        systemMsg.set("content", systemPrompt);
        messages.add(systemMsg);

        JSONObject userMsg = new JSONObject();
        userMsg.set("role", "user");
        userMsg.set("content", userPrompt);
        messages.add(userMsg);

        requestBody.set("messages", messages);
        requestBody.set("temperature", 0.7);
        requestBody.set("max_tokens", 4096);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("AI API call failed: " + response.getStatusCode());
        }

        JSONObject respJson = JSONUtil.parseObj(response.getBody());
        JSONArray choices = respJson.getJSONArray("choices");
        if (choices == null || choices.isEmpty()) {
            throw new RuntimeException("AI returned empty response");
        }

        String content = choices.getJSONObject(0)
            .getJSONObject("message")
            .getStr("content");

        if (content == null || content.isBlank()) {
            throw new RuntimeException("AI returned empty content");
        }

        return content;
    }
}
