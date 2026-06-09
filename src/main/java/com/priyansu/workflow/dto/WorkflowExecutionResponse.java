package com.priyansu.workflow.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record WorkflowExecutionResponse(

        UUID executionId,
        UUID workflowId,
        String status,
        String errorMessage,
        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {
}