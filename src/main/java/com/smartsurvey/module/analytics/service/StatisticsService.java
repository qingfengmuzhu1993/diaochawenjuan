package com.smartsurvey.module.analytics.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartsurvey.module.response.entity.Answer;
import com.smartsurvey.module.response.entity.Response;
import com.smartsurvey.module.response.mapper.AnswerMapper;
import com.smartsurvey.module.response.mapper.ResponseMapper;
import com.smartsurvey.module.survey.entity.Question;
import com.smartsurvey.module.survey.entity.Survey;
import com.smartsurvey.module.survey.mapper.QuestionMapper;
import com.smartsurvey.module.survey.mapper.SurveyMapper;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatisticsService {
    private final ResponseMapper responseMapper;
    private final AnswerMapper answerMapper;
    private final QuestionMapper questionMapper;
    private final SurveyMapper surveyMapper;

    public StatisticsService(ResponseMapper responseMapper, AnswerMapper answerMapper,
                              QuestionMapper questionMapper, SurveyMapper surveyMapper) {
        this.responseMapper = responseMapper; this.answerMapper = answerMapper;
        this.questionMapper = questionMapper; this.surveyMapper = surveyMapper;
    }

    public Map<String, Object> getStatistics(Long surveyId) {
        Survey survey = surveyMapper.selectById(surveyId);
        List<Response> responses = responseMapper.selectList(new LambdaQueryWrapper<Response>()
            .eq(Response::getSurveyId, surveyId).eq(Response::getStatus, "approved"));
        List<Question> questions = questionMapper.selectBySurveyId(surveyId);

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("surveyId", surveyId);
        stats.put("totalResponses", responses.size());
        stats.put("targetQuota", survey.getTargetQuota());
        double avgDuration = responses.stream().filter(r -> r.getDurationSeconds() != null)
            .mapToInt(Response::getDurationSeconds).average().orElse(0);
        stats.put("avgDurationSeconds", Math.round(avgDuration));

        List<Map<String, Object>> questionStats = new ArrayList<>();
        for (Question q : questions) {
            Map<String, Object> qs = new LinkedHashMap<>();
            qs.put("questionId", q.getId());
            qs.put("content", q.getContent());
            qs.put("type", q.getType());
            List<Answer> answers = answerMapper.selectByResponseId(null);
            // Get all answers for this question across all responses
            List<Answer> qAnswers = new ArrayList<>();
            for (Response r : responses) {
                List<Answer> ra = answerMapper.selectByResponseId(r.getId());
                for (Answer a : ra) {
                    if (a.getQuestionId().equals(q.getId())) qAnswers.add(a);
                }
            }

            if ("single".equals(q.getType()) || "multiple".equals(q.getType()) || "judge".equals(q.getType())) {
                Map<String, Long> dist = new LinkedHashMap<>();
                for (Answer a : qAnswers) {
                    String key = a.getAnswerOptions() != null ? a.getAnswerOptions() : "未答";
                    dist.put(key, dist.getOrDefault(key, 0L) + 1);
                }
                qs.put("distribution", dist);
            } else if ("rating".equals(q.getType())) {
                double avgRating = qAnswers.stream().filter(a -> a.getAnswerRating() != null)
                    .mapToInt(Answer::getAnswerRating).average().orElse(0);
                qs.put("avgRating", Math.round(avgRating * 10.0) / 10.0);
            } else if ("essay".equals(q.getType()) || "fill".equals(q.getType())) {
                qs.put("responseCount", qAnswers.size());
            }
            questionStats.add(qs);
        }
        stats.put("questionAnalysis", questionStats);
        return stats;
    }
}
