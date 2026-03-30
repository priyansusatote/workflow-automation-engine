package com.priyansu.workflow.service.Impl;

import com.priyansu.workflow.dto.WorkflowRequest;
import com.priyansu.workflow.dto.WorkflowResponse;
import com.priyansu.workflow.entity.Workflow;
import com.priyansu.workflow.exception.DuplicateResourceException;
import com.priyansu.workflow.exception.ResourceNotFoundException;
import com.priyansu.workflow.mapper.WorkflowMapper;
import com.priyansu.workflow.repository.WorkflowRepository;
import com.priyansu.workflow.service.WorkflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkflowServiceImpl implements WorkflowService {

    private final WorkflowRepository workflowRepository;
    private final WorkflowMapper workflowMapper;

    //temp for testing
    private final UUID DEFAULT_USER_ID =
            UUID.fromString("550e8400-e29b-41d4-a716-446655440000");


    @Override
    public WorkflowResponse createWorkflow(WorkflowRequest request) {
        //check if already exists
        if (workflowRepository.existsByNameAndUserId(request.name(), DEFAULT_USER_ID)) {
            throw new DuplicateResourceException("Workflow already exists");
        }

        Workflow workflow = workflowMapper.toEntity(request);

        // TEMP FIX (simulate logged-in user)
        workflow.setUserId(DEFAULT_USER_ID);

        Workflow savedWorkflow = workflowRepository.save(workflow);

        return workflowMapper.toWorkflowResponse(savedWorkflow);
    }

    @Override
    public List<WorkflowResponse> getAllWorkflows() {
        return workflowRepository.findAll()
                .stream()
                .map(workflowMapper::toWorkflowResponse)
                .toList();
    }

    @Override
    public WorkflowResponse getWorkflowById(UUID id) {
        Workflow workflow = workflowRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow not found"));

        return workflowMapper.toWorkflowResponse(workflow);
    }

    @Override
    public WorkflowResponse updateWorkflow(UUID id, WorkflowRequest request) {
        Workflow workflow = workflowRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow not found"));

        workflow.setName(request.name());
        workflow.setDescription(request.description());

        Workflow updated = workflowRepository.save(workflow);

        return workflowMapper.toWorkflowResponse(updated);
    }

    @Override
    public void deleteWorkflow(UUID id) {
        Workflow workflow = workflowRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow not found"));

        workflowRepository.delete(workflow);
    }
}
