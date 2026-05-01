# AI 集成设计方案

**日期**: 2026-04-30
**状态**: 已确认

## 概述

将现有假的 AI 实现（硬编码模板/关键词匹配）替换为真实 LLM 调用。默认使用 DeepSeek，支持通过配置文件切换 OpenAI、通义千问、智谱。

## 设计决策

| 决策项 | 选择 |
|--------|------|
| 返回方式 | 一次性返回（非流式 SSE） |
| 架构方式 | 统一 Provider 接口 + yml 切换 |
| 默认提供商 | DeepSeek |
| 通信方式 | RestTemplate（Spring 内置，无额外依赖） |

## 多提供商架构

定义 `AiProvider` 接口，每个提供商一个实现类。yml 中配置 `ai.provider` 决定使用哪个。Spring 根据配置自动装配对应 Bean。

```
AiGenerationService (业务层)
        │
        ▼
   AiProvider (接口)
        │
  ┌─────┼─────┬──────────┐
  │     │     │          │
DeepSeek OpenAI 通义千问  智谱
```

## yml 配置设计

```yaml
# application-dev.yml 新增段
ai:
  provider: deepseek       # deepseek | openai | qwen | zhipu
  deepseek:
    api-key: sk-xxx
    base-url: https://api.deepseek.com
    model: deepseek-chat
  openai:
    api-key: sk-xxx
    base-url: https://api.openai.com
    model: gpt-3.5-turbo
  qwen:
    api-key: sk-xxx
    base-url: https://dashscope.aliyuncs.com
    model: qwen-turbo
  zhipu:
    api-key: sk-xxx
    base-url: https://open.bigmodel.cn
    model: glm-4-flash
```

切换提供商只需改 `ai.provider` 值，配置好对应 API Key 即可。

## AI 功能（复用现有 3 个端点）

| 端点 | 方法 | 说明 |
|------|------|------|
| `/api/v1/ai/generate-survey` | POST | AI 根据需求描述+行业+题数生成结构化问卷 |
| `/api/v1/ai/diagnose/{surveyId}` | POST | AI 诊断整份问卷，返回评分和改进建议 |
| `/api/v1/ai/suggest-improvement/{questionId}` | GET | AI 对单道题目给出改写优化建议 |

## Provider 接口定义

```java
public interface AiProvider {
    String generateSurvey(String prompt, String industry, int questionCount);
    String diagnoseSurvey(String title, String questionsJson);
    String improveQuestion(String questionContent);
}
```

每个方法返回 LLM 的原始 JSON 字符串，`AiGenerationService` 负责解析为业务对象。

## Prompt 设计（生成问卷）

LLM 要求返回严格 JSON，系统 prompt 指定角色、格式要求、题目类型多样性。用户 prompt 传入需求描述、行业、题目数量。

返回 JSON 结构：
```json
{
  "title": "问卷标题",
  "description": "问卷说明",
  "questions": [
    {
      "type": "single_choice",
      "content": "题目内容",
      "required": 1,
      "options": ["选项A", "选项B"]
    }
  ]
}
```

## 文件变更清单

| 文件 | 操作 | 说明 |
|------|------|------|
| `application-dev.yml` | 修改 | 新增 ai 配置段 |
| `AiProvider.java` | 新建 | AI 提供商接口 |
| `AiConfig.java` | 新建 | 根据配置装配 Provider Bean + RestTemplate |
| `DeepSeekAiProvider.java` | 新建 | DeepSeek 实现 |
| `OpenAiProvider.java` | 新建 | OpenAI 实现 |
| `QwenAiProvider.java` | 新建 | 通义千问实现 |
| `ZhipuAiProvider.java` | 新建 | 智谱实现 |
| `AiGenerationService.java` | 重写 | 替换硬编码为 LLM 调用 |
| `AiController.java` | 修改 | 增强错误处理 |
| `ErrorCode.java` | 修改 | 将 AI_GENERATION_FAILED 错误码实际使用 |

## 不在范围内

- 流式 SSE 返回
- 前端 AI 交互改动
- AI 对话/多轮交互
- 模型微调
