package com.smartsurvey.module.analytics.service;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartsurvey.common.ai.AiProvider;
import com.smartsurvey.module.analytics.service.StatisticsService;
import com.smartsurvey.module.response.entity.Answer;
import com.smartsurvey.module.response.entity.Response;
import com.smartsurvey.module.response.mapper.AnswerMapper;
import com.smartsurvey.module.response.mapper.ResponseMapper;
import com.smartsurvey.module.survey.entity.Question;
import com.smartsurvey.module.survey.entity.Survey;
import com.smartsurvey.module.survey.mapper.QuestionMapper;
import com.smartsurvey.module.survey.mapper.SurveyMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class AiAnalysisService {
    private static final Logger log = LoggerFactory.getLogger(AiAnalysisService.class);
    private final ResponseMapper responseMapper;
    private final AnswerMapper answerMapper;
    private final QuestionMapper questionMapper;
    private final AiProvider aiProvider;
    private final StatisticsService statisticsService;
    private final SurveyMapper surveyMapper;

    public AiAnalysisService(ResponseMapper responseMapper, AnswerMapper answerMapper,
                             QuestionMapper questionMapper, AiProvider aiProvider,
                             StatisticsService statisticsService, SurveyMapper surveyMapper) {
        this.responseMapper = responseMapper;
        this.answerMapper = answerMapper;
        this.questionMapper = questionMapper;
        this.aiProvider = aiProvider;
        this.statisticsService = statisticsService;
        this.surveyMapper = surveyMapper;
    }

    public Map<String, Object> getKeyFindings(Long surveyId) {
        List<Question> questions = questionMapper.selectBySurveyId(surveyId);
        Long approvedCount = responseMapper.selectCount(
            new LambdaQueryWrapper<Response>()
                .eq(Response::getSurveyId, surveyId).eq(Response::getStatus, "approved"));

        JSONArray stats = new JSONArray();
        for (Question q : questions) {
            if (Arrays.asList("single", "multiple", "judge").contains(q.getType())) {
                List<Answer> answers = answerMapper.selectByQuestionId(q.getId());
                Map<String, Integer> distribution = new LinkedHashMap<>();
                for (Answer a : answers) {
                    if (a.getAnswerOptions() != null) {
                        JSONArray selected = JSONUtil.parseArray(a.getAnswerOptions());
                        for (int i = 0; i < selected.size(); i++) {
                            String key = String.valueOf(selected.get(i));
                            distribution.merge(key, 1, Integer::sum);
                        }
                    }
                }
                JSONObject stat = new JSONObject();
                stat.set("questionId", q.getId());
                stat.set("content", q.getContent());
                stat.set("type", q.getType());
                stat.set("distribution", distribution);
                stats.add(stat);
            } else if ("rating".equals(q.getType())) {
                List<Answer> answers = answerMapper.selectByQuestionId(q.getId());
                double sum = 0; int count = 0;
                for (Answer a : answers) {
                    if (a.getAnswerRating() != null) { sum += a.getAnswerRating(); count++; }
                }
                JSONObject stat = new JSONObject();
                stat.set("questionId", q.getId());
                stat.set("content", q.getContent());
                stat.set("type", "rating");
                stat.set("average", count > 0 ? Math.round(sum / count * 100.0) / 100.0 : 0);
                stat.set("count", count);
                stats.add(stat);
            }
        }

        if (stats.isEmpty()) {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("findings", new ArrayList<>());
            return result;
        }

        try {
            String systemPrompt = "你是一个专业的问卷数据分析师。请根据统计数据提取关键发现。";
            String userPrompt = "问卷ID: " + surveyId + ", 有效回答: " + approvedCount + "份\n"
                + "统计数据: " + stats.toString() + "\n\n"
                + "请提取3-5条关键发现。返回严格JSON格式: {\"findings\":[{\"message\":\"发现描述\",\"confidence\":\"high|medium\",\"questionId\":1},...]}";
            String response = aiProvider.analyze(systemPrompt, userPrompt);
            JSONObject parsed = JSONUtil.parseObj(cleanJson(response));
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("findings", parsed.get("findings") != null ? parsed.get("findings") : new ArrayList<>());
            return result;
        } catch (Exception e) {
            List<Map<String, Object>> fallback = new ArrayList<>();
            for (int i = 0; i < stats.size(); i++) {
                JSONObject stat = stats.getJSONObject(i);
                if ("rating".equals(stat.getStr("type")) && stat.getDouble("average") < 3.0) {
                    Map<String, Object> f = new LinkedHashMap<>();
                    f.put("message", "评分题『" + truncate(stat.getStr("content"), 25) + "』平均分偏低(" + stat.getDouble("average") + ")，建议关注");
                    f.put("confidence", "medium");
                    f.put("questionId", stat.getLong("questionId"));
                    fallback.add(f);
                }
            }
            if (fallback.isEmpty()) {
                Map<String, Object> f = new LinkedHashMap<>();
                f.put("message", "回收了" + approvedCount + "份有效回答，数据质量良好");
                f.put("confidence", "low");
                f.put("questionId", null);
                fallback.add(f);
            }
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("findings", fallback);
            return result;
        }
    }

    public Map<String, Object> analyzeSentiment(Long surveyId, Long questionId) {
        Question q = questionMapper.selectById(questionId);
        List<Answer> answers = answerMapper.selectByQuestionId(questionId);

        List<String> texts = new ArrayList<>();
        for (Answer a : answers) {
            if (a.getAnswerText() != null && !a.getAnswerText().trim().isEmpty()) {
                texts.add(a.getAnswerText());
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("questionContent", q != null ? q.getContent() : "");
        result.put("total", texts.size());

        if (texts.isEmpty()) {
            result.put("positive", 0); result.put("negative", 0); result.put("neutral", 0);
            result.put("quotes", new JSONObject());
            return result;
        }

        try {
            JSONArray textArray = new JSONArray();
            for (String t : texts) { textArray.add(t); }

            String systemPrompt = "你是一个情感分析专家。对每条文本做三分类：positive/negative/neutral。返回严格JSON。";
            String userPrompt = "分析以下文本的情感倾向：\n" + textArray.toString()
                + "\n\n返回JSON: {\"results\":[{\"index\":0,\"sentiment\":\"positive\",\"score\":0.9},...],"
                + "\"summary\":{\"positive\":68,\"negative\":12,\"neutral\":20},"
                + "\"topQuotes\":{\"positive\":[\"典型正面1\"],\"negative\":[\"典型负面1\"]}}";

            String response = aiProvider.analyze(systemPrompt, userPrompt);
            JSONObject parsed = JSONUtil.parseObj(cleanJson(response));
            JSONObject summary = parsed.getJSONObject("summary");
            result.put("positive", summary != null ? summary.getInt("positive", 0) : 0);
            result.put("negative", summary != null ? summary.getInt("negative", 0) : 0);
            result.put("neutral", summary != null ? summary.getInt("neutral", 0) : 0);
            result.put("quotes", parsed.get("topQuotes") != null ? parsed.get("topQuotes") : new JSONObject());
        } catch (Exception e) {
            result.put("positive", 0); result.put("negative", 0); result.put("neutral", texts.size());
            result.put("quotes", new JSONObject());
        }
        return result;
    }

    private String cleanJson(String raw) {
        String s = raw.trim();
        if (s.startsWith("```json")) s = s.substring(7);
        if (s.startsWith("```")) s = s.substring(3);
        if (s.endsWith("```")) s = s.substring(0, s.length() - 3);
        return s.trim();
    }

    private String truncate(String s, int len) {
        return s != null && s.length() > len ? s.substring(0, len) + "..." : s;
    }

    public Map<String, Object> generateReport(Long surveyId) {
        Map<String, Object> statistics = statisticsService.getStatistics(surveyId);
        Map<String, Object> findings = getKeyFindings(surveyId);
        Survey survey = surveyMapper.selectById(surveyId);

        try {
            String systemPrompt = "你是一个专业的调研报告撰写专家。请根据提供的统计数据生成一份专业的调研分析报告。返回严格JSON格式。";
            String userPrompt = String.format(
                "问卷标题：%s\n样本量：%s\n统计数据：%s\n关键发现：%s\n\n" +
                "请生成一份结构化的调研报告，包含以下章节：\n" +
                "1. 调研概况（样本量、完成率）\n" +
                "2. 核心发现（3-5条最重要的结论）\n" +
                "3. 逐题分析（每道题的数据解读）\n" +
                "4. 行动建议（基于数据的具体建议）\n\n" +
                "返回JSON格式：{\"summary\":\"概况描述\",\"sections\":[{\"title\":\"章节标题\",\"content\":\"章节内容\"},...]}",
                survey != null ? survey.getTitle() : "问卷", statistics.get("totalResponses"),
                JSONUtil.toJsonStr(statistics), JSONUtil.toJsonStr(findings));

            String response = aiProvider.analyze(systemPrompt, userPrompt);
            JSONObject parsed = JSONUtil.parseObj(cleanJson(response));

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("surveyTitle", survey != null ? survey.getTitle() : "");
            result.put("sampleSize", statistics.get("totalResponses"));
            result.put("generatedAt", LocalDateTime.now().toString());
            result.put("sections", parsed.get("sections"));
            result.put("summary", parsed.getStr("summary", ""));
            return result;
        } catch (Exception e) {
            log.error("AI report generation failed for survey {}", surveyId, e);
            return buildFallbackReport(survey, statistics);
        }
    }

    private Map<String, Object> buildFallbackReport(Survey survey, Map<String, Object> statistics) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("surveyTitle", survey != null ? survey.getTitle() : "");
        result.put("sampleSize", statistics.get("totalResponses"));
        result.put("generatedAt", LocalDateTime.now().toString());
        result.put("summary", "共回收" + statistics.get("totalResponses") + "份有效问卷。");

        List<Map<String, String>> sections = new ArrayList<>();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> qaList = (List<Map<String, Object>>) statistics.get("questionAnalysis");
        if (qaList != null) {
            for (Map<String, Object> qa : qaList) {
                Map<String, String> section = new LinkedHashMap<>();
                section.put("title", (String) qa.getOrDefault("content", ""));
                if (qa.get("distribution") != null) {
                    section.put("content", "选项分布：" + qa.get("distribution").toString());
                } else if (qa.get("avgRating") != null) {
                    section.put("content", "平均评分：" + qa.get("avgRating").toString());
                } else {
                    section.put("content", "共" + qa.getOrDefault("responseCount", 0) + "条回复");
                }
                sections.add(section);
            }
        }
        result.put("sections", sections);
        return result;
    }
}
