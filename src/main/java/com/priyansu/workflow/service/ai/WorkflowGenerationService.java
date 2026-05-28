package com.priyansu.workflow.service.ai;

import com.fasterxml.jackson.databind.JsonNode;

public interface WorkflowGenerationService{

    String generateWorkflow(String prompt);
}
