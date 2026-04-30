package com.smartsurvey.module.survey.service;

import cn.hutool.json.JSONUtil;
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
    private final SurveyMapper surveyMapper;
    private final QuestionMapper questionMapper;
    private final SurveyLogicService surveyLogicService;

    public AiGenerationService(SurveyMapper surveyMapper, QuestionMapper questionMapper,
                                SurveyLogicService surveyLogicService) {
        this.surveyMapper = surveyMapper;
        this.questionMapper = questionMapper;
        this.surveyLogicService = surveyLogicService;
    }

    public AiGenerateResponse generateSurvey(AiGenerateRequest req) {
        AiGenerateResponse resp = new AiGenerateResponse();

        CreateSurveyRequest draft = new CreateSurveyRequest();
        draft.setTitle(generateTitle(req.getPrompt()));
        draft.setDescription("本问卷由AI自动生成，基于您的需求：" + req.getPrompt());

        int questionCount = req.getQuestionCount() != null ? Math.min(req.getQuestionCount(), 20) : 8;
        List<CreateSurveyRequest.QuestionItem> questions = generateQuestions(req.getPrompt(),
            req.getIndustry(), questionCount);
        draft.setQuestions(questions);

        resp.setSurveyDraft(draft);
        resp.setAiScore(7.5);
        resp.setEstimatedTimeMinutes(estimateTime(questions));
        resp.setSuggestions(generateSuggestions(questions));
        return resp;
    }

    public AiDiagnoseResponse diagnoseSurvey(Long surveyId) {
        Survey survey = surveyMapper.selectById(surveyId);
        List<Question> questions = questionMapper.selectBySurveyId(surveyId);
        AiDiagnoseResponse resp = new AiDiagnoseResponse();
        List<AiDiagnoseResponse.DiagnosisItem> items = new ArrayList<>();

        if (survey.getTitle() == null || survey.getTitle().trim().length() < 5) {
            items.add(new AiDiagnoseResponse.DiagnosisItem("title", "warning", "标题过短，建议至少5个字"));
        }

        boolean hasDemographic = questions.stream().anyMatch(q ->
            q.getContent() != null && (q.getContent().contains("年龄") || q.getContent().contains("性别")));
        if (!hasDemographic && questions.size() >= 5) {
            items.add(new AiDiagnoseResponse.DiagnosisItem("demographics", "suggestion",
                "建议添加年龄或性别等人口统计问题以支持交叉分析"));
        }

        if (questions.size() < 3) {
            items.add(new AiDiagnoseResponse.DiagnosisItem("count", "error", "题目数量不足（至少3题）"));
        }

        int duration = surveyLogicService.estimateDuration(questions);
        if (duration > 900) {
            items.add(new AiDiagnoseResponse.DiagnosisItem("duration", "warning",
                "预计答题时长" + (duration / 60) + "分钟，超过15分钟建议精简或增加奖励"));
        }

        for (Question q : questions) {
            if (("single".equals(q.getType()) || "multiple".equals(q.getType()))
                    && (q.getOptions() == null || q.getOptions().length() < 3)) {
                items.add(new AiDiagnoseResponse.DiagnosisItem("q" + q.getOrderIndex(), "error",
                    "第" + (q.getOrderIndex() + 1) + "题选项数量不足"));
            }
        }

        resp.setItems(items);
        resp.setEstimatedDurationSeconds(duration);
        return resp;
    }

    public String suggestQuestionImprovement(Long questionId) {
        Question q = questionMapper.selectById(questionId);
        if (q == null) return "题目不存在";
        StringBuilder sb = new StringBuilder();
        if (q.getContent() != null && q.getContent().length() > 100) {
            sb.append("题目偏长，建议精简到50字以内；");
        }
        if (q.getContent() != null && (q.getContent().contains("不") || q.getContent().contains("是否"))) {
            sb.append("部分措辞可能带有引导性，建议使用中性表述；");
        }
        return sb.length() > 0 ? sb.toString() : "未发现明显问题";
    }

    private String generateTitle(String prompt) {
        if (prompt.contains("远程办公")) return "远程办公态度调研";
        if (prompt.contains("满意度")) return "用户满意度调查";
        if (prompt.contains("购买") || prompt.contains("消费")) return "消费者购买意愿调研";
        return prompt.length() > 30 ? prompt.substring(0, 30) + "..." : prompt;
    }

    private List<CreateSurveyRequest.QuestionItem> generateQuestions(String prompt, String industry, int count) {
        List<CreateSurveyRequest.QuestionItem> questions = new ArrayList<>();
        // Generate a basic demographic section
        CreateSurveyRequest.QuestionItem q1 = new CreateSurveyRequest.QuestionItem();
        q1.setType("single"); q1.setContent("您的年龄段是？"); q1.setOrderIndex(0);
        q1.setOptions("[{\"id\":1,\"text\":\"18-25岁\"},{\"id\":2,\"text\":\"26-35岁\"},{\"id\":3,\"text\":\"36-45岁\"},{\"id\":4,\"text\":\"46岁以上\"}]");
        questions.add(q1);

        CreateSurveyRequest.QuestionItem q2 = new CreateSurveyRequest.QuestionItem();
        q2.setType("single"); q2.setContent("您的性别是？"); q2.setOrderIndex(1);
        q2.setOptions("[{\"id\":1,\"text\":\"男\"},{\"id\":2,\"text\":\"女\"}]");
        questions.add(q2);

        CreateSurveyRequest.QuestionItem q3 = new CreateSurveyRequest.QuestionItem();
        q3.setType("rating"); q3.setContent("您对当前相关服务的整体满意度如何？（1-5分）"); q3.setOrderIndex(2);
        questions.add(q3);

        CreateSurveyRequest.QuestionItem q4 = new CreateSurveyRequest.QuestionItem();
        q4.setType("single"); q4.setContent("您最看重以下哪个方面？"); q4.setOrderIndex(3);
        q4.setOptions("[{\"id\":1,\"text\":\"价格\"},{\"id\":2,\"text\":\"质量\"},{\"id\":3,\"text\":\"服务\"},{\"id\":4,\"text\":\"便利性\"}]");
        questions.add(q4);

        CreateSurveyRequest.QuestionItem q5 = new CreateSurveyRequest.QuestionItem();
        q5.setType("multiple"); q5.setContent("您通过哪些渠道了解相关信息？（多选）"); q5.setOrderIndex(4);
        q5.setOptions("[{\"id\":1,\"text\":\"社交媒体\"},{\"id\":2,\"text\":\"朋友推荐\"},{\"id\":3,\"text\":\"搜索引擎\"},{\"id\":4,\"text\":\"广告\"}]");
        questions.add(q5);

        CreateSurveyRequest.QuestionItem q6 = new CreateSurveyRequest.QuestionItem();
        q6.setType("essay"); q6.setContent("您有什么建议或想法想分享吗？"); q6.setOrderIndex(5);
        questions.add(q6);

        return questions.subList(0, Math.min(count, questions.size()));
    }

    private int estimateTime(List<CreateSurveyRequest.QuestionItem> questions) {
        int seconds = questions.size() * 15;
        return Math.max(1, seconds / 60);
    }

    private List<String> generateSuggestions(List<CreateSurveyRequest.QuestionItem> questions) {
        List<String> suggestions = new ArrayList<>();
        suggestions.add("建议根据实际调研目的调整题目措辞");
        if (questions.size() < 8) {
            suggestions.add("当前题目较少，可以考虑增加更多维度的问题");
        }
        return suggestions;
    }
}
