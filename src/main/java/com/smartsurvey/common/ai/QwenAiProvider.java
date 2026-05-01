package com.smartsurvey.common.ai;

import org.springframework.web.client.RestTemplate;

public class QwenAiProvider extends AbstractAiProvider {

    public QwenAiProvider(RestTemplate restTemplate, String apiKey,
                           String baseUrl, String model) {
        super(restTemplate, apiKey, baseUrl, model);
    }
}
