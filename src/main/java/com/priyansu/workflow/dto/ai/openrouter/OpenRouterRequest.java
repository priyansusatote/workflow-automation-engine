package com.priyansu.workflow.dto.ai.openrouter;

import java.util.List;

//OpenRouterRequest = provider-specific payload
public record OpenRouterRequest(  //This maps EXACTLY to: OpenRouter HTTP request JSON

        String model,
        List<OpenRouterMessage> messages,
        Double temperature

) {
}