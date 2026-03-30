package com.priyansu.workflow.dto;

import com.priyansu.workflow.entity.enums.WorkflowStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record WorkflowResponse(

        UUID id,
        String name,
        String description,
        UUID userId,
        WorkflowStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {
}
