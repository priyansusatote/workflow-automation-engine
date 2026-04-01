package com.priyansu.workflow.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.priyansu.workflow.dto.WorkflowDefinitionRequest;


import java.util.UUID;

public interface WorkflowDefinitionService {
    void saveDefinition(UUID workflowId, WorkflowDefinitionRequest request);

    String getDefinition(UUID workflowId);
}
