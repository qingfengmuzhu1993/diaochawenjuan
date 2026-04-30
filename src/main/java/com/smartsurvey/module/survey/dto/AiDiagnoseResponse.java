package com.smartsurvey.module.survey.dto;
import java.util.List;

public class AiDiagnoseResponse {
    private List<DiagnosisItem> items;
    private Integer estimatedDurationSeconds;

    public List<DiagnosisItem> getItems() { return items; }
    public void setItems(List<DiagnosisItem> items) { this.items = items; }
    public Integer getEstimatedDurationSeconds() { return estimatedDurationSeconds; }
    public void setEstimatedDurationSeconds(Integer estimatedDurationSeconds) { this.estimatedDurationSeconds = estimatedDurationSeconds; }

    public static class DiagnosisItem {
        private String scope;
        private String level;
        private String message;

        public DiagnosisItem() {}
        public DiagnosisItem(String scope, String level, String message) {
            this.scope = scope; this.level = level; this.message = message;
        }
        public String getScope() { return scope; }
        public void setScope(String scope) { this.scope = scope; }
        public String getLevel() { return level; }
        public void setLevel(String level) { this.level = level; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}
