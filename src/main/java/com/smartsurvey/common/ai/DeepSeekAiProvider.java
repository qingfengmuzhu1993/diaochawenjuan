package com.smartsurvey.common.ai;

import org.springframework.web.client.RestTemplate;

public class DeepSeekAiProvider extends AbstractAiProvider {

    public DeepSeekAiProvider(RestTemplate restTemplate, String apiKey,
                               String baseUrl, String model) {
        super(restTemplate, apiKey, baseUrl, model);
    }
}
