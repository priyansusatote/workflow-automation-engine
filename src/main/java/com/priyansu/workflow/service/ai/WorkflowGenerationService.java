package com.priyansu.workflow.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.priyansu.workflow.dto.ai.GeneratedWorkflowResponse;

import java.util.UUID;

public interface WorkflowGenerationService{

    String generateWorkflow(String prompt);

    //Auto-save AI Generated Workflows
    GeneratedWorkflowResponse generateAndSaveWorkflow(
            String workflowName,
            String userPrompt
    );
}
