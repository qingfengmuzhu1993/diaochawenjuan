package com.smartsurvey.module.analytics.controller;

import com.smartsurvey.common.dto.ApiResponse;
import com.smartsurvey.module.response.entity.Answer;
import com.smartsurvey.module.response.entity.Response;
import com.smartsurvey.module.response.mapper.AnswerMapper;
import com.smartsurvey.module.response.mapper.ResponseMapper;
import com.smartsurvey.module.survey.entity.Question;
import com.smartsurvey.module.survey.mapper.QuestionMapper;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/v1/analytics")
public class ExportController {
    private final ResponseMapper responseMapper;
    private final AnswerMapper answerMapper;
    private final QuestionMapper questionMapper;

    public ExportController(ResponseMapper responseMapper, AnswerMapper answerMapper, QuestionMapper questionMapper) {
        this.responseMapper = responseMapper; this.answerMapper = answerMapper; this.questionMapper = questionMapper;
    }

    @GetMapping("/surveys/{surveyId}/export/csv")
    public void exportCsv(@PathVariable Long surveyId, HttpServletResponse response) throws Exception {
        List<Question> questions = questionMapper.selectBySurveyId(surveyId);
        questions.sort(Comparator.comparingInt(Question::getOrderIndex));

        List<Response> responses = responseMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Response>()
                .eq(Response::getSurveyId, surveyId).eq(Response::getStatus, "approved"));

        // Build CSV header
        StringBuilder header = new StringBuilder();
        header.append("回答ID,提交时间,耗时(秒),渠道,质量评分");
        for (Question q : questions) {
            header.append(",").append(escapeCsv(q.getContent()));
        }

        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=survey_" + surveyId + ".csv");
        PrintWriter writer = response.getWriter();
        // BOM for Excel UTF-8 compatibility
        writer.print('﻿');
        writer.println(header.toString());

        for (Response r : responses) {
            StringBuilder row = new StringBuilder();
            row.append(r.getId()).append(",")
               .append(r.getEndTime() != null ? r.getEndTime().toString() : "").append(",")
               .append(r.getDurationSeconds() != null ? r.getDurationSeconds() : "").append(",")
               .append(r.getChannel() != null ? r.getChannel() : "").append(",")
               .append(r.getQualityScore() != null ? r.getQualityScore() : "");

            for (Question q : questions) {
                row.append(",");
                List<Answer> answers = answerMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Answer>()
                        .eq(Answer::getResponseId, r.getId())
                        .eq(Answer::getQuestionId, q.getId()));
                if (!answers.isEmpty()) {
                    Answer a = answers.get(0);
                    if (a.getAnswerText() != null) {
                        row.append(escapeCsv(a.getAnswerText()));
                    } else if (a.getAnswerOptions() != null) {
                        row.append(escapeCsv(a.getAnswerOptions()));
                    } else if (a.getAnswerRating() != null) {
                        row.append(a.getAnswerRating());
                    }
                }
            }
            writer.println(row.toString());
        }
        writer.flush();
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
