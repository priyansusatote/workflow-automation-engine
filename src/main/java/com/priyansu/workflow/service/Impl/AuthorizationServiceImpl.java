package com.priyansu.workflow.service.Impl;

import com.priyansu.workflow.entity.Workflow;
import com.priyansu.workflow.entity.WorkflowExecution;
import com.priyansu.workflow.exception.ResourceNotFoundException;
import com.priyansu.workflow.repository.WorkflowExecutionRepository;
import com.priyansu.workflow.repository.WorkflowRepository;
import com.priyansu.workflow.security.SecurityUtils;
import com.priyansu.workflow.service.AuthorizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.UUID;

//Workflow Ownership
@Service
@RequiredArgsConstructor
public class AuthorizationServiceImpl
        implements AuthorizationService {

    private final WorkflowRepository workflowRepository;

    private final WorkflowExecutionRepository executionRepository;

    @Override
    public void validateWorkflowOwnership(UUID workflowId) {

        UUID currentUserId = SecurityUtils.getCurrentUser().userId();

        Workflow workflow = workflowRepository.findById(workflowId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Workflow not found"
                                ));

        if (!workflow.getUserId().equals(currentUserId)) {

            throw new AccessDeniedException(
                    "You do not have permission to access this resource"
            );
        }
    }

    @Override
    public void validateExecutionOwnership(UUID executionId) {

        WorkflowExecution execution = executionRepository.findById(executionId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Execution not found"
                                ));

        validateWorkflowOwnership(execution.getWorkflowId());
    }
}