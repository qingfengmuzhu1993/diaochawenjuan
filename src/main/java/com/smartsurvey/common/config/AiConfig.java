package com.smartsurvey.common.config;

import com.smartsurvey.common.ai.AiProvider;
import com.smartsurvey.common.ai.DeepSeekAiProvider;
import com.smartsurvey.common.ai.OpenAiProvider;
import com.smartsurvey.common.ai.QwenAiProvider;
import com.smartsurvey.common.ai.ZhipuAiProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class AiConfig {

    @Value("${ai.provider}")
    private String provider;

    @Value("${ai.deepseek.api-key}")
    private String deepseekApiKey;
    @Value("${ai.deepseek.base-url}")
    private String deepseekBaseUrl;
    @Value("${ai.deepseek.model}")
    private String deepseekModel;

    @Value("${ai.openai.api-key}")
    private String openaiApiKey;
    @Value("${ai.openai.base-url}")
    private String openaiBaseUrl;
    @Value("${ai.openai.model}")
    private String openaiModel;

    @Value("${ai.qwen.api-key}")
    private String qwenApiKey;
    @Value("${ai.qwen.base-url}")
    private String qwenBaseUrl;
    @Value("${ai.qwen.model}")
    private String qwenModel;

    @Value("${ai.zhipu.api-key}")
    private String zhipuApiKey;
    @Value("${ai.zhipu.base-url}")
    private String zhipuBaseUrl;
    @Value("${ai.zhipu.model}")
    private String zhipuModel;

    @Bean
    public RestTemplate aiRestTemplate() {
        return new RestTemplateBuilder()
            .setConnectTimeout(Duration.ofSeconds(10))
            .setReadTimeout(Duration.ofSeconds(60))
            .build();
    }

    @Bean
    public AiProvider aiProvider(RestTemplate aiRestTemplate) {
        switch (provider) {
            case "deepseek":
                return new DeepSeekAiProvider(aiRestTemplate, deepseekApiKey, deepseekBaseUrl, deepseekModel);
            case "openai":
                return new OpenAiProvider(aiRestTemplate, openaiApiKey, openaiBaseUrl, openaiModel);
            case "qwen":
                return new QwenAiProvider(aiRestTemplate, qwenApiKey, qwenBaseUrl, qwenModel);
            case "zhipu":
                return new ZhipuAiProvider(aiRestTemplate, zhipuApiKey, zhipuBaseUrl, zhipuModel);
            default:
                throw new IllegalArgumentException("Unsupported AI provider: " + provider);
        }
    }
}
