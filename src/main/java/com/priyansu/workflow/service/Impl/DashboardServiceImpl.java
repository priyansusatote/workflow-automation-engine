package com.priyansu.workflow.service.Impl;

import com.priyansu.workflow.dto.DashboardResponse;
import com.priyansu.workflow.dto.WorkflowStatsResponse;
import com.priyansu.workflow.entity.Workflow;
import com.priyansu.workflow.entity.enums.ExecutionStatus;
import com.priyansu.workflow.entity.enums.WorkflowStatus;
import com.priyansu.workflow.repository.WorkflowExecutionRepository;
import com.priyansu.workflow.repository.WorkflowRepository;
import com.priyansu.workflow.security.SecurityUtils;
import com.priyansu.workflow.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final WorkflowRepository workflowRepository;

    private final WorkflowExecutionRepository executionRepository;

    @Override
    public DashboardResponse getDashboard() {

        UUID currentUserId = SecurityUtils.getCurrentUser().userId();

        List<UUID> workflowIds = workflowRepository.findWorkflowIdsByUserId(currentUserId);


        // Workflow Metrics
        long totalWorkflows = workflowRepository.countByUserId(currentUserId);

        long activeWorkflows = workflowRepository.countByUserIdAndStatus(currentUserId, WorkflowStatus.ACTIVE);

        long inactiveWorkflows =
                workflowRepository.countByUserIdAndStatus(
                        currentUserId,
                        WorkflowStatus.INACTIVE
                );

        // Execution Metrics
        long totalExecutions = 0;
        long successfulExecutions = 0;
        long failedExecutions = 0;
        long runningExecutions = 0;
        long waitingExecutions = 0;

        if (!workflowIds.isEmpty()) {

            totalExecutions =
                    executionRepository.countByWorkflowIdIn(
                            workflowIds
                    );

            successfulExecutions =
                    executionRepository
                            .countByWorkflowIdInAndStatus(
                                    workflowIds,
                                    ExecutionStatus.SUCCESS
                            );

            failedExecutions =
                    executionRepository
                            .countByWorkflowIdInAndStatus(
                                    workflowIds,
                                    ExecutionStatus.FAILED
                            );

            runningExecutions =
                    executionRepository
                            .countByWorkflowIdInAndStatus(
                                    workflowIds,
                                    ExecutionStatus.RUNNING
                            );

            waitingExecutions =
                    executionRepository
                            .countByWorkflowIdInAndStatus(
                                    workflowIds,
                                    ExecutionStatus.WAITING
                            );
        }

        double successRate = 0;

        if (totalExecutions > 0) {

            successRate =
                    (successfulExecutions * 100.0)
                            / totalExecutions;
        }

        return new DashboardResponse(
                totalWorkflows,
                activeWorkflows,
                inactiveWorkflows,
                totalExecutions,
                successfulExecutions,
                failedExecutions,
                runningExecutions,
                waitingExecutions,
                Math.round(successRate * 100.0) / 100.0
        );
    }
    @Override
    public List<WorkflowStatsResponse> getWorkflowStats() {

        UUID currentUserId = SecurityUtils.getCurrentUser().userId();

        List<Workflow> workflows = workflowRepository.findByUserId(currentUserId);

        List<WorkflowStatsResponse> response = new ArrayList<>();

        for (Workflow workflow : workflows) {

            long total = executionRepository.countByWorkflowId(
                            workflow.getId()
                    );

            long success = executionRepository.countByWorkflowIdAndStatus(
                            workflow.getId(),
                            ExecutionStatus.SUCCESS
                    );

            long failed = executionRepository.countByWorkflowIdAndStatus(
                            workflow.getId(),
                            ExecutionStatus.FAILED
                    );

            double successRate = total == 0
                            ? 0
                            : (success * 100.0) / total;

            response.add(
                    new WorkflowStatsResponse(
                            workflow.getId(),
                            workflow.getName(),
                            total,
                            success,
                            failed,
                            Math.round(successRate * 100.0) / 100.0
                    )
            );
        }

        //Sort By most-used workflow
        response.sort(
                Comparator.comparingLong(
                        WorkflowStatsResponse::totalExecutions
                ).reversed()
        );

        return response;
    }
}