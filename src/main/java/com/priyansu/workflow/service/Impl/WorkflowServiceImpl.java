package com.priyansu.workflow.service.Impl;

import com.priyansu.workflow.dto.WorkflowRequest;
import com.priyansu.workflow.dto.WorkflowResponse;
import com.priyansu.workflow.entity.Workflow;
import com.priyansu.workflow.entity.enums.WorkflowStatus;
import com.priyansu.workflow.exception.DuplicateResourceException;
import com.priyansu.workflow.exception.ResourceNotFoundException;
import com.priyansu.workflow.mapper.WorkflowMapper;
import com.priyansu.workflow.repository.WorkflowRepository;
import com.priyansu.workflow.security.SecurityUtils;
import com.priyansu.workflow.service.CurrentUserService;
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
    private final CurrentUserService currentUserService;



    @Override
    public WorkflowResponse createWorkflow(WorkflowRequest request) {

        UUID userId = currentUserService.getCurrentUserId(); // current user

        //check if already exists
        if (workflowRepository.existsByNameAndUserId(request.name(), userId)) {
            throw new DuplicateResourceException("Workflow already exists");
        }

        Workflow workflow = workflowMapper.toEntity(request);

        // (simulate logged-in user) set ownership of workflow
        workflow.setUserId(userId);

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

    @Override
    public List<Workflow> getMyWorkflows() {
        UUID userId = SecurityUtils.getCurrentUser().userId();

        return workflowRepository.findByUserId(userId);
    }

    @Override
    public WorkflowResponse activateWorkflow(UUID id) {
        Workflow workflow = workflowRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow not found"));

        workflow.setStatus(WorkflowStatus.ACTIVE);
        Workflow updated = workflowRepository.save(workflow);

        return workflowMapper.toWorkflowResponse(updated);
    }

    @Override
    public WorkflowResponse deactivateWorkflow(UUID id) {
        Workflow workflow = workflowRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow not found"));

        workflow.setStatus(WorkflowStatus.INACTIVE);
        Workflow updated = workflowRepository.save(workflow);

        return workflowMapper.toWorkflowResponse(updated);
    }
}
