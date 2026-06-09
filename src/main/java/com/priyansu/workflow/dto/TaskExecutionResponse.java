package com.priyansu.workflow.dto;

import java.time.LocalDateTime;
import java.util.Map;

public record TaskExecutionResponse(

        String nodeId,
        String nodeType,
        String status,
        String logMessage,
        Map<String, Object> outputData,
        LocalDateTime createdAt

) {
}