package com.smartsurvey.module.analytics.controller;

import com.smartsurvey.common.dto.ApiResponse;
import com.smartsurvey.module.analytics.service.AiAnalysisService;
import com.smartsurvey.module.analytics.service.StatisticsService;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

@RestController
@RequestMapping("/api/v1/analytics")
public class AnalyticsController {
    private final StatisticsService statisticsService;
    private final AiAnalysisService aiAnalysisService;

    public AnalyticsController(StatisticsService statisticsService, AiAnalysisService aiAnalysisService) {
        this.statisticsService = statisticsService; this.aiAnalysisService = aiAnalysisService;
    }

    @GetMapping("/surveys/{surveyId}")
    public ApiResponse<Map<String, Object>> getStatistics(@PathVariable Long surveyId) {
        return ApiResponse.ok(statisticsService.getStatistics(surveyId));
    }

    @GetMapping("/surveys/{surveyId}/findings")
    public ApiResponse<Map<String, Object>> getFindings(@PathVariable Long surveyId) {
        return ApiResponse.ok(aiAnalysisService.getKeyFindings(surveyId));
    }

    @GetMapping("/surveys/{surveyId}/sentiment/{questionId}")
    public ApiResponse<Map<String, Object>> getSentiment(@PathVariable Long surveyId, @PathVariable Long questionId) {
        return ApiResponse.ok(aiAnalysisService.analyzeSentiment(surveyId, questionId));
    }

    @GetMapping("/surveys/{surveyId}/cross")
    public ApiResponse<Map<String, Object>> crossTabulation(
            @PathVariable Long surveyId,
            @RequestParam Long rowQuestionId,
            @RequestParam Long colQuestionId) {
        return ApiResponse.ok(statisticsService.crossTabulation(surveyId, rowQuestionId, colQuestionId));
    }

    @PostMapping("/surveys/{surveyId}/report")
    public ApiResponse<Map<String, Object>> generateReport(@PathVariable Long surveyId) {
        return ApiResponse.ok(aiAnalysisService.generateReport(surveyId));
    }

    @GetMapping("/surveys/{surveyId}/report/export")
    public void exportReportPdf(@PathVariable Long surveyId, HttpServletResponse response) throws Exception {
        Map<String, Object> report = aiAnalysisService.generateReport(surveyId);
        String html = buildReportHtml(report);

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=report_" + surveyId + ".pdf");

        org.xhtmlrenderer.pdf.ITextRenderer renderer = new org.xhtmlrenderer.pdf.ITextRenderer();
        renderer.setDocumentFromString(html);
        renderer.layout();
        renderer.createPDF(response.getOutputStream(), false);
        renderer.finishPDF();
        response.getOutputStream().flush();
    }

    private String buildReportHtml(Map<String, Object> report) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><meta charset='UTF-8'><style>");
        sb.append("body{font-family:SimSun,sans-serif;padding:20px}");
        sb.append("h1{color:#134E4A;text-align:center;font-size:18px}");
        sb.append("h4{color:#0D9488;margin-top:16px;font-size:14px}");
        sb.append("p{line-height:1.8;color:#333;font-size:12px}");
        sb.append(".summary{background:#F5FAF8;padding:12px;border-radius:8px;margin:12px 0}");
        sb.append("</style></head><body>");
        sb.append("<h1>").append(escapeHtml(String.valueOf(report.get("surveyTitle")))).append(" - 调研报告</h1>");
        sb.append("<p>样本量：").append(report.get("sampleSize")).append(" | 生成时间：").append(report.get("generatedAt")).append("</p>");
        sb.append("<div class='summary'>").append(escapeHtml(String.valueOf(report.get("summary")))).append("</div>");

        @SuppressWarnings("unchecked")
        List<Map<String, String>> sections = (List<Map<String, String>>) report.get("sections");
        if (sections != null) {
            for (Map<String, String> sec : sections) {
                sb.append("<h4>").append(escapeHtml(sec.get("title"))).append("</h4>");
                sb.append("<p>").append(escapeHtml(sec.get("content"))).append("</p>");
            }
        }
        sb.append("</body></html>");
        return sb.toString();
    }

    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
