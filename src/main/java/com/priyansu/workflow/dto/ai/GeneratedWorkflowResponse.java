package com.priyansu.workflow.dto.ai;


import java.util.Map;
import java.util.UUID;

public record GeneratedWorkflowResponse(

        UUID workflowId,
        String workflowName,
        Map<String, Object> workflowJson


) {
}