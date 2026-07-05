package com.priyansu.workflow.dto;

import com.priyansu.workflow.entity.enums.ExecutionStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record ExecutionSummaryResponse(

        UUID executionId,

        UUID workflowId,

        String workflowName,

        ExecutionStatus status,

        LocalDateTime createdAt,

        LocalDateTime updatedAt

) {
}