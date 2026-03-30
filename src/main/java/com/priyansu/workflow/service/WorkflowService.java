package com.priyansu.workflow.service;


import com.priyansu.workflow.dto.WorkflowRequest;
import com.priyansu.workflow.dto.WorkflowResponse;
import jakarta.validation.Valid;

public interface WorkflowService {

    WorkflowResponse createWorkflow( WorkflowRequest request);
}
