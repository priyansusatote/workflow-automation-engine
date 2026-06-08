package com.priyansu.workflow.service.Impl;

import com.priyansu.workflow.dto.WorkflowWebhookResponse;
import com.priyansu.workflow.entity.WorkflowWebhook;
import com.priyansu.workflow.exception.ResourceNotFoundException;
import com.priyansu.workflow.repository.WorkflowRepository;
import com.priyansu.workflow.repository.WorkflowWebhookRepository;
import com.priyansu.workflow.service.WorkflowExecutionService;
import com.priyansu.workflow.service.WorkflowWebhookService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkflowWebhookServiceImpl implements WorkflowWebhookService {

    private final WorkflowWebhookRepository workflowWebhookRepository;
    private final WorkflowExecutionService workflowExecutionService;
    private final WorkflowRepository workflowRepository;

    @Override
    public WorkflowWebhookResponse createWebhook(UUID workflowId) {

        //check valid workflowId
        workflowRepository.findById(workflowId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Workflow not found"));

        // Check if webhook already exists
        WorkflowWebhook existing = workflowWebhookRepository.findByWorkflowId(workflowId)
                        .orElse(null);

        if (existing != null) {
            return new WorkflowWebhookResponse(
                    existing.getWorkflowId(),
                    existing.getWebhookKey()
            );
        }

        // Create new webhook only if one doesn't exist
        String webhookKey = UUID.randomUUID().toString();

        WorkflowWebhook webhook = new WorkflowWebhook();

        webhook.setWorkflowId(workflowId);
        webhook.setWebhookKey(webhookKey);

        workflowWebhookRepository.save(webhook);

        return new WorkflowWebhookResponse(
                workflowId,
                webhookKey
        );
    }

 // external applications will be able to start your workflows automatically.
    @Override
    public UUID triggerWebhook(String webhookKey, Map<String, Object> payload) {

        WorkflowWebhook webhook = workflowWebhookRepository.findByWebhookKey(webhookKey)
                        .orElseThrow(() -> new ResourceNotFoundException("Webhook not found"));

        return workflowExecutionService.triggerWorkflow(webhook.getWorkflowId(), payload);
    }
}