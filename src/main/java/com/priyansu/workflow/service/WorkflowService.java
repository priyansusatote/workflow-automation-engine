package com.priyansu.workflow.service;


import com.priyansu.workflow.dto.WorkflowRequest;
import com.priyansu.workflow.dto.WorkflowResponse;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

public interface WorkflowService {

    WorkflowResponse createWorkflow( WorkflowRequest request);

    List<WorkflowResponse> getAllWorkflows();

    WorkflowResponse getWorkflowById(UUID id);

    WorkflowResponse updateWorkflow(UUID id,  WorkflowRequest request);

    void deleteWorkflow(UUID id);
}
