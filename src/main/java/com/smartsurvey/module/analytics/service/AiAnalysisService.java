package com.smartsurvey.module.analytics.service;

import com.smartsurvey.module.response.entity.Answer;
import com.smartsurvey.module.response.mapper.AnswerMapper;
import com.smartsurvey.module.response.mapper.ResponseMapper;
import com.smartsurvey.module.survey.entity.Question;
import com.smartsurvey.module.survey.mapper.QuestionMapper;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class AiAnalysisService {
    private final ResponseMapper responseMapper;
    private final AnswerMapper answerMapper;
    private final QuestionMapper questionMapper;

    public AiAnalysisService(ResponseMapper responseMapper, AnswerMapper answerMapper, QuestionMapper questionMapper) {
        this.responseMapper = responseMapper; this.answerMapper = answerMapper; this.questionMapper = questionMapper;
    }

    public Map<String, Object> getKeyFindings(Long surveyId) {
        List<Map<String, String>> findings = new ArrayList<>();
        List<Question> questions = questionMapper.selectBySurveyId(surveyId);

        for (Question q : questions) {
            if ("rating".equals(q.getType())) {
                Map<String, String> finding = new LinkedHashMap<>();
                finding.put("scope", "q" + q.getId());
                finding.put("confidence", "medium");
                finding.put("message", "评分题『" + truncate(q.getContent(), 25) + "』的平均分需要关注");
                findings.add(finding);
            }
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("findings", findings);
        return result;
    }

    public Map<String, Object> analyzeSentiment(Long surveyId, Long questionId) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("positive", 0); result.put("negative", 0); result.put("neutral", 0);
        Question q = questionMapper.selectById(questionId);
        if (q != null) {
            result.put("questionContent", q.getContent());
        }
        return result;
    }

    private String truncate(String s, int len) {
        return s != null && s.length() > len ? s.substring(0, len) + "..." : s;
    }
}
