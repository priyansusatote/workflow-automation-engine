package com.priyansu.workflow.service.Impl;

import com.priyansu.workflow.dto.WorkflowRequest;
import com.priyansu.workflow.dto.WorkflowResponse;
import com.priyansu.workflow.entity.Workflow;
import com.priyansu.workflow.mapper.WorkflowMapper;
import com.priyansu.workflow.repository.WorkflowRepository;
import com.priyansu.workflow.service.WorkflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkflowServiceImpl implements WorkflowService {

    private final WorkflowRepository workflowRepository;
    private final WorkflowMapper workflowMapper;

    @Override
    public WorkflowResponse createWorkflow(WorkflowRequest request) {
        Workflow workflow = workflowMapper.toEntity(request);

        // TEMP FIX (simulate logged-in user)
        workflow.setUserId(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"));

        Workflow savedWorkflow = workflowRepository.save(workflow);

        return workflowMapper.toWorkflowResponse(savedWorkflow);
    }
}
