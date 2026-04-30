package com.smartsurvey.module.survey.controller;

import com.smartsurvey.common.dto.ApiResponse;
import com.smartsurvey.module.survey.dto.CreateSurveyRequest;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/v1/templates")
public class TemplateController {

    @GetMapping
    public ApiResponse<List<Map<String, Object>>> listTemplates(
            @RequestParam(required = false) String category) {
        List<Map<String, Object>> templates = getBuiltinTemplates();
        if (category != null) {
            templates.removeIf(t -> !category.equals(t.get("category")));
        }
        return ApiResponse.ok(templates);
    }

    @GetMapping("/{id}")
    public ApiResponse<CreateSurveyRequest> getTemplate(@PathVariable String id) {
        List<Map<String, Object>> templates = getBuiltinTemplates();
        @SuppressWarnings("unchecked")
        CreateSurveyRequest survey = templates.stream()
            .filter(t -> id.equals(t.get("id")))
            .findFirst()
            .map(t -> (CreateSurveyRequest) t.get("survey"))
            .orElse(null);
        return ApiResponse.ok(survey);
    }

    private List<Map<String, Object>> getBuiltinTemplates() {
        List<Map<String, Object>> list = new ArrayList<>();

        // Template 1: Customer Satisfaction
        Map<String, Object> t1 = new HashMap<>();
        t1.put("id", "tpl_customer_satisfaction");
        t1.put("title", "客户满意度调查");
        t1.put("category", "market");
        t1.put("description", "适用于产品/服务满意度调研");
        t1.put("questionCount", 8);
        t1.put("estimatedMinutes", 3);
        CreateSurveyRequest s1 = new CreateSurveyRequest();
        s1.setTitle("客户满意度调查");
        s1.setDescription("感谢您使用我们的产品/服务，请花几分钟分享您的体验。");

        List<CreateSurveyRequest.QuestionItem> qs1 = new ArrayList<>();
        CreateSurveyRequest.QuestionItem qi1 = new CreateSurveyRequest.QuestionItem();
        qi1.setType("rating"); qi1.setContent("总体满意度（1-5分）"); qi1.setOrderIndex(0);
        qs1.add(qi1);
        CreateSurveyRequest.QuestionItem qi2 = new CreateSurveyRequest.QuestionItem();
        qi2.setType("single"); qi2.setContent("您使用我们的产品/服务多久了？"); qi2.setOrderIndex(1);
        qi2.setOptions("[{\"id\":1,\"text\":\"不到1个月\"},{\"id\":2,\"text\":\"1-6个月\"},{\"id\":3,\"text\":\"6个月以上\"}]");
        qs1.add(qi2);
        CreateSurveyRequest.QuestionItem qi3 = new CreateSurveyRequest.QuestionItem();
        qi3.setType("essay"); qi3.setContent("您最希望我们改进的地方是？"); qi3.setOrderIndex(2);
        qs1.add(qi3);
        s1.setQuestions(qs1);
        t1.put("survey", s1);
        list.add(t1);

        // Template 2: Academic Research
        Map<String, Object> t2 = new HashMap<>();
        t2.put("id", "tpl_academic_research");
        t2.put("title", "学术研究问卷");
        t2.put("category", "academic");
        t2.put("description", "适用于学术论文数据收集");
        t2.put("questionCount", 10);
        t2.put("estimatedMinutes", 5);
        CreateSurveyRequest s2 = new CreateSurveyRequest();
        s2.setTitle("学术调研");
        s2.setDescription("本问卷仅用于学术研究，所有数据将严格保密。");
        List<CreateSurveyRequest.QuestionItem> qs2 = new ArrayList<>();
        CreateSurveyRequest.QuestionItem aq1 = new CreateSurveyRequest.QuestionItem();
        aq1.setType("single"); aq1.setContent("您的学历是？"); aq1.setOrderIndex(0);
        aq1.setOptions("[{\"id\":1,\"text\":\"高中及以下\"},{\"id\":2,\"text\":\"本科\"},{\"id\":3,\"text\":\"硕士\"},{\"id\":4,\"text\":\"博士\"}]");
        qs2.add(aq1);
        CreateSurveyRequest.QuestionItem aq2 = new CreateSurveyRequest.QuestionItem();
        aq2.setType("rating"); aq2.setContent("请对以下维度打分（1-5）"); aq2.setOrderIndex(1);
        qs2.add(aq2);
        s2.setQuestions(qs2);
        t2.put("survey", s2);
        list.add(t2);

        // Template 3: Employee Engagement
        Map<String, Object> t3 = new HashMap<>();
        t3.put("id", "tpl_employee_engagement");
        t3.put("title", "员工敬业度调查");
        t3.put("category", "hr");
        t3.put("description", "适用于企业内部员工满意度/敬业度调研");
        t3.put("questionCount", 10);
        t3.put("estimatedMinutes", 5);
        CreateSurveyRequest s3 = new CreateSurveyRequest();
        s3.setTitle("员工敬业度调查");
        s3.setDescription("本问卷完全匿名，请真实作答。");
        s3.setIsAnonymous(1);
        List<CreateSurveyRequest.QuestionItem> qs3 = new ArrayList<>();
        CreateSurveyRequest.QuestionItem eq1 = new CreateSurveyRequest.QuestionItem();
        eq1.setType("rating"); eq1.setContent("您对当前工作的整体满意度（1-5分）"); eq1.setOrderIndex(0);
        qs3.add(eq1);
        CreateSurveyRequest.QuestionItem eq2 = new CreateSurveyRequest.QuestionItem();
        eq2.setType("essay"); eq2.setContent("您对公司的建议是什么？"); eq2.setOrderIndex(1);
        qs3.add(eq2);
        s3.setQuestions(qs3);
        t3.put("survey", s3);
        list.add(t3);

        return list;
    }
}
