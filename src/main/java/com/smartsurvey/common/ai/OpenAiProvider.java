package com.smartsurvey.common.ai;

import org.springframework.web.client.RestTemplate;

public class OpenAiProvider extends AbstractAiProvider {

    public OpenAiProvider(RestTemplate restTemplate, String apiKey,
                           String baseUrl, String model) {
        super(restTemplate, apiKey, baseUrl, model);
    }
}
