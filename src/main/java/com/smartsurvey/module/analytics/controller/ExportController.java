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
        List<Response> responses = responseMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Response>()
                .eq(Response::getSurveyId, surveyId).eq(Response::getStatus, "approved"));

        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=survey_" + surveyId + ".csv");
        PrintWriter writer = response.getWriter();
        writer.println("回答ID,提交时间,耗时(秒)");
        for (Response r : responses) {
            writer.println(r.getId() + "," + r.getEndTime() + "," + r.getDurationSeconds());
        }
        writer.flush();
    }
}
