package com.priyansu.workflow.dto;

public record DashboardResponse(

        long totalWorkflows,
        long activeWorkflows,
        long inactiveWorkflows,

        long totalExecutions,

        long successfulExecutions,
        long failedExecutions,
        long runningExecutions,
        long waitingExecutions,

        double successRate

) {
}