package com.priyansu.workflow.service;

import com.priyansu.workflow.dto.WorkflowExecutionResponse;

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
}
