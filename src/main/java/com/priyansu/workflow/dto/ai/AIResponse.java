package com.priyansu.workflow.dto.ai;

//normalized AI response [No matter which provider used,workflow engine gets SAME structure]
public record AIResponse(

        String content,
        Integer promptTokens,
        Integer completionTokens,
        Long latencyMs

) {
}
