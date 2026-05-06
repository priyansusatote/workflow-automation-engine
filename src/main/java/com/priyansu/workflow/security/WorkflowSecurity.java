package com.priyansu.workflow.security;

import com.priyansu.workflow.repository.WorkflowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class WorkflowSecurity {

    private final WorkflowRepository workflowRepository;

    public boolean isOwner(UUID workflowId) {

        UUID userId = SecurityUtils.getCurrentUser().userId();

        return workflowRepository.findById(workflowId)
                .map(w -> w.getUserId().equals(userId))
                .orElse(false);
    }
}