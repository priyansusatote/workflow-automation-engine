package com.priyansu.workflow.executor;

import com.priyansu.workflow.entity.Workflow;
import com.priyansu.workflow.entity.WorkflowExecution;
import com.priyansu.workflow.entity.enums.ExecutionStatus;
import com.priyansu.workflow.repository.WorkflowExecutionRepository;
import com.priyansu.workflow.service.WorkflowExecutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class WorkflowResumeScheduler {

    private final WorkflowExecutionRepository executionRepository;
    private final WorkflowExecutionService workflowExecutionService;

    @Scheduled(fixedRate = 5000)
    public void resumeWaitingWorkflows() {

        List<WorkflowExecution> waiting =
                executionRepository.findByStatusAndResumeAtBefore(
                                ExecutionStatus.WAITING,
                                LocalDateTime.now()
                        );

        for (WorkflowExecution execution : waiting) {

            log.info(
                    "Scheduler resuming execution={}",
                    execution.getId()
            );

            workflowExecutionService.resumeExecution(
                    execution.getId()
            );
        }
    }

}