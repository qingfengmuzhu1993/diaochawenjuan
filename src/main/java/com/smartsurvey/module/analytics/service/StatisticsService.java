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
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
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

    public Map<String, Object> crossTabulation(Long surveyId, Long rowQId, Long colQId) {
        Question rowQ = questionMapper.selectById(rowQId);
        Question colQ = questionMapper.selectById(colQId);

        List<Response> responses = responseMapper.selectList(
            new LambdaQueryWrapper<Response>()
                .eq(Response::getSurveyId, surveyId)
                .eq(Response::getStatus, "approved"));

        List<String> rowLabels = extractOptionTexts(rowQ);
        List<String> colLabels = extractOptionTexts(colQ);

        int R = rowLabels.size(), C = colLabels.size();
        int[][] matrix = new int[R][C];

        for (Response resp : responses) {
            Answer rowAns = answerMapper.selectOne(new LambdaQueryWrapper<Answer>()
                .eq(Answer::getResponseId, resp.getId()).eq(Answer::getQuestionId, rowQId));
            Answer colAns = answerMapper.selectOne(new LambdaQueryWrapper<Answer>()
                .eq(Answer::getResponseId, resp.getId()).eq(Answer::getQuestionId, colQId));
            if (rowAns == null || colAns == null) continue;

            int ri = findOptionIndex(rowAns, rowQ);
            int ci = findOptionIndex(colAns, colQ);
            if (ri >= 0 && ci >= 0) matrix[ri][ci]++;
        }

        double chiSquare = computeChiSquare(matrix, R, C);
        int df = (R - 1) * (C - 1);
        double pValue = chiSquarePValue(chiSquare, df);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("rowLabels", rowLabels);
        result.put("colLabels", colLabels);
        result.put("matrix", matrix);
        result.put("chiSquare", Math.round(chiSquare * 1000.0) / 1000.0);
        result.put("degreesOfFreedom", df);
        result.put("pValue", Math.round(pValue * 10000.0) / 10000.0);
        result.put("significant", pValue < 0.05);
        result.put("highlySignificant", pValue < 0.01);
        return result;
    }

    private List<String> extractOptionTexts(Question q) {
        List<String> labels = new ArrayList<>();
        if (q != null && q.getOptions() != null) {
            try {
                JSONArray opts = JSONUtil.parseArray(q.getOptions());
                for (int i = 0; i < opts.size(); i++) {
                    labels.add(opts.getJSONObject(i).getStr("text", ""));
                }
            } catch (Exception ignored) {}
        }
        return labels;
    }

    private int findOptionIndex(Answer a, Question q) {
        if (a.getAnswerOptions() == null || q == null || q.getOptions() == null) return -1;
        try {
            JSONArray selected = JSONUtil.parseArray(a.getAnswerOptions());
            if (selected.isEmpty()) return -1;
            Long optId = Long.valueOf(selected.get(0).toString());
            JSONArray opts = JSONUtil.parseArray(q.getOptions());
            for (int i = 0; i < opts.size(); i++) {
                if (optId.equals(opts.getJSONObject(i).getLong("id"))) return i;
            }
        } catch (Exception ignored) {}
        return -1;
    }

    private double computeChiSquare(int[][] matrix, int R, int C) {
        int total = 0;
        int[] rowSums = new int[R], colSums = new int[C];
        for (int i = 0; i < R; i++) {
            for (int j = 0; j < C; j++) {
                total += matrix[i][j];
                rowSums[i] += matrix[i][j];
                colSums[j] += matrix[i][j];
            }
        }
        if (total == 0) return 0;

        double chi = 0;
        for (int i = 0; i < R; i++) {
            for (int j = 0; j < C; j++) {
                double expected = (double) rowSums[i] * colSums[j] / total;
                if (expected > 0) {
                    double diff = matrix[i][j] - expected;
                    chi += diff * diff / expected;
                }
            }
        }
        return chi;
    }

    private double chiSquarePValue(double x, int df) {
        if (x <= 0 || df <= 0) return 1.0;
        double m = x / df;
        double z = (Math.pow(m, 1.0 / 3) - (1 - 2.0 / (9 * df))) / Math.sqrt(2.0 / (9 * df));
        return 2 * (1 - normalCDF(Math.abs(z)));
    }

    private double normalCDF(double x) {
        return 0.5 * (1 + erf(x / Math.sqrt(2)));
    }

    private double erf(double x) {
        double t = 1.0 / (1.0 + 0.5 * Math.abs(x));
        double tau = t * Math.exp(-x * x - 1.26551223 +
            t * (1.00002368 + t * (0.37409196 + t * (0.09678418 +
            t * (-0.18628806 + t * (0.27886807 + t * (-1.13520398 +
            t * (1.48851587 + t * (-0.82215223 + t * 0.17087277)))))))));
        return x >= 0 ? 1 - tau : tau - 1;
    }
}
