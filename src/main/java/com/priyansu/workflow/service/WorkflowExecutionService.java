package com.priyansu.workflow.service;

import com.priyansu.workflow.dto.ExecutionSummaryResponse;
import com.priyansu.workflow.dto.TaskExecutionResponse;
import com.priyansu.workflow.dto.WorkflowExecutionResponse;
import com.priyansu.workflow.entity.enums.ExecutionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface WorkflowExecutionService {

    //not used from now , bcz Kafka using
    //void executeWorkflow(UUID workflowId);

    UUID triggerWorkflow(UUID workflowId, Map<String, Object> input);

    //Kafka-Method
    void executeWorkflowFromKafka(UUID uuid, UUID uuid1, Map<String, Object> input);

    void resumeExecution(UUID id);

    //GET Execution Details
    WorkflowExecutionResponse getExecution(UUID executionId);

    List<TaskExecutionResponse> getExecutionTasks(UUID executionId);

    Page<ExecutionSummaryResponse> getExecutions(
            UUID workflowId,
            ExecutionStatus status,
            Pageable pageable
    );
}
