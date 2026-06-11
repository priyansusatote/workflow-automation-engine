package com.priyansu.workflow.dto;

import java.util.UUID;

public record WorkflowStatsResponse(  // we want: Which workflow runs the most? , fails the most? is healthiest?

        UUID workflowId,

        String workflowName,

        long totalExecutions,

        long successfulExecutions,

        long failedExecutions,

        double successRate

) {
}