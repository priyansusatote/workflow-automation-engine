package com.priyansu.workflow.dto.ai;


public record GenerateWorkflowRequest(

        String workflowName,
        String prompt

) {
}