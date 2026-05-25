package com.priyansu.workflow.dto.ai.openrouter;

import java.util.List;

public record OpenRouterResponse(

        List<OpenRouterChoice> choices,
        OpenRouterUsage usage

) {
}