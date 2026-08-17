package com.priyansu.workflow.executor;

import com.priyansu.workflow.entity.WorkflowExecution;
import com.priyansu.workflow.repository.WorkflowExecutionRepository;
import com.priyansu.workflow.service.WorkflowExecutionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkflowResumeSchedulerTest {

    @Mock WorkflowExecutionRepository executionRepository;
    @Mock WorkflowExecutionService workflowExecutionService;

    @Test
    @DisplayName("Should continue resuming due executions after one execution fails")
    void shouldIsolateResumeFailures() {
        WorkflowExecution first = execution(UUID.randomUUID());
        WorkflowExecution second = execution(UUID.randomUUID());
        when(executionRepository.findByStatusAndResumeAtBefore(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any()
        )).thenReturn(List.of(first, second));
        doThrow(new RuntimeException("resume failed"))
                .when(workflowExecutionService).resumeWaitingExecution(first.getId());

        new WorkflowResumeScheduler(executionRepository, workflowExecutionService)
                .resumeWaitingWorkflows();

        verify(workflowExecutionService).resumeWaitingExecution(first.getId());
        verify(workflowExecutionService).resumeWaitingExecution(second.getId());
    }

    private WorkflowExecution execution(UUID id) {
        WorkflowExecution execution = new WorkflowExecution();
        execution.setId(id);
        return execution;
    }
}
