package com.smartsurvey.module.survey.service;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.smartsurvey.module.survey.entity.Question;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SurveyLogicService {

    public Integer resolveNextQuestionIndex(Integer currentIndex, List<Question> allQuestions,
                                             Map<Long, String> previousAnswers) {
        if (currentIndex == null || currentIndex + 1 >= allQuestions.size()) return null;

        Question currentQ = allQuestions.get(currentIndex);
        // Check jump logic
        if (currentQ.getLogicJump() != null && !currentQ.getLogicJump().isEmpty()) {
            JSONArray jumps = JSONUtil.parseArray(currentQ.getLogicJump());
            for (int i = 0; i < jumps.size(); i++) {
                JSONObject jump = jumps.getJSONObject(i);
                Long optionId = jump.getLong("option_id");
                Integer jumpTo = jump.getInt("jump_to");
                String selected = previousAnswers.get(currentQ.getId());
                if (selected != null && selected.contains(String.valueOf(optionId))) {
                    return jumpTo;
                }
            }
        }

        // Check show/hide on next questions
        int nextIdx = currentIndex + 1;
        while (nextIdx < allQuestions.size()) {
            if (shouldShow(allQuestions.get(nextIdx), previousAnswers)) {
                return nextIdx;
            }
            nextIdx++;
        }
        return null;
    }

    private boolean shouldShow(Question q, Map<Long, String> previousAnswers) {
        if (q.getLogicShow() == null || q.getLogicShow().isEmpty()) return true;
        JSONObject logic = JSONUtil.parseObj(q.getLogicShow());
        String operator = logic.getStr("operator", "AND");
        JSONArray conditions = logic.getJSONArray("conditions");
        if (conditions == null) return true;

        for (int i = 0; i < conditions.size(); i++) {
            JSONObject cond = conditions.getJSONObject(i);
            Long questionId = cond.getLong("questionId");
            String expectedValue = cond.getStr("value");
            String actualValue = previousAnswers.get(questionId);
            boolean met = actualValue != null && actualValue.contains(expectedValue);
            if ("AND".equals(operator) && !met) return false;
            if ("OR".equals(operator) && met) return true;
        }
        return "AND".equals(operator);
    }

    public List<Question> getOrderedQuestions(List<Question> allQuestions) {
        return allQuestions.stream()
            .sorted(Comparator.comparingInt(Question::getOrderIndex))
            .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getOrderedOptions(Question question) {
        if (question.getOptions() == null || question.getOptions().isEmpty()) return new ArrayList<>();
        JSONArray options = JSONUtil.parseArray(question.getOptions());
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 0; i < options.size(); i++) {
            JSONObject opt = options.getJSONObject(i);
            Map<String, Object> m = new HashMap<>();
            m.put("id", opt.get("id"));
            m.put("text", opt.get("text"));
            m.put("fixed", opt.getBool("fixed", false));
            result.add(m);
        }

        if (question.getIsRandomOptions() != null && question.getIsRandomOptions() == 1) {
            List<Map<String, Object>> fixed = result.stream()
                .filter(m -> Boolean.TRUE.equals(m.get("fixed"))).collect(Collectors.toList());
            List<Map<String, Object>> random = result.stream()
                .filter(m -> !Boolean.TRUE.equals(m.get("fixed"))).collect(Collectors.toList());
            Collections.shuffle(random);
            random.addAll(fixed);
            return random;
        }
        return result;
    }

    public int estimateDuration(List<Question> questions) {
        int seconds = 0;
        for (Question q : questions) {
            switch (q.getType()) {
                case "single": seconds += 8; break;
                case "multiple": seconds += 12; break;
                case "judge": seconds += 5; break;
                case "fill": seconds += 15; break;
                case "essay": seconds += 40; break;
                case "rating": seconds += 10; break;
                case "matrix":
                    JSONArray opts = q.getOptions() != null ? JSONUtil.parseArray(q.getOptions()) : new JSONArray();
                    seconds += 30 + 5 * Math.min(opts.size(), 10);
                    break;
                case "ranking": seconds += 20; break;
            }
        }
        return seconds;
    }
}
