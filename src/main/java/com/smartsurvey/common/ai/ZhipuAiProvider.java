package com.smartsurvey.common.ai;

import org.springframework.web.client.RestTemplate;

public class ZhipuAiProvider extends AbstractAiProvider {

    public ZhipuAiProvider(RestTemplate restTemplate, String apiKey,
                            String baseUrl, String model) {
        super(restTemplate, apiKey, baseUrl, model);
    }
}
