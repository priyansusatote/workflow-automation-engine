package com.priyansu.workflow.dto.ai.openrouter;

public record OpenRouterUsage(
        Integer prompt_tokens,
        Integer completion_tokens
) {
}