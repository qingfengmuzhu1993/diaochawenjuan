package com.smartsurvey.module.marketplace.service;

import com.smartsurvey.module.survey.entity.Question;
import com.smartsurvey.module.survey.entity.Survey;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AuditService {

    public AuditResult autoAudit(Survey survey, List<Question> questions) {
        AuditResult result = new AuditResult();
        List<String> issues = new ArrayList<>();

        if (survey.getTitle() == null || survey.getTitle().trim().isEmpty()) {
            issues.add("缺少问卷标题");
        }
        if (questions == null || questions.size() < 3) {
            issues.add("题目数量不足（至少3题）");
        }
        if (survey.getRewardPerResponse() != null && survey.getRewardPerResponse().doubleValue() < 0.5) {
            issues.add("每份奖励金额不能低于0.5元");
        }

        boolean needsManual = survey.getRewardPerResponse() != null
                && survey.getRewardPerResponse().doubleValue() >= 10.0;

        result.setPassed(issues.isEmpty());
        result.setIssues(issues);
        result.setNeedsManualReview(needsManual);
        return result;
    }

    public static class AuditResult {
        private boolean passed;
        private List<String> issues;
        private boolean needsManualReview;

        public boolean isPassed() { return passed; }
        public void setPassed(boolean passed) { this.passed = passed; }
        public List<String> getIssues() { return issues; }
        public void setIssues(List<String> issues) { this.issues = issues; }
        public boolean isNeedsManualReview() { return needsManualReview; }
        public void setNeedsManualReview(boolean needsManualReview) { this.needsManualReview = needsManualReview; }
    }
}
