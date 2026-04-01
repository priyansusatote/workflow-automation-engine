package com.priyansu.workflow.service;

import java.util.UUID;

public interface WorkflowExecutionService {

    void executeWorkflow(UUID workflowId);
}
