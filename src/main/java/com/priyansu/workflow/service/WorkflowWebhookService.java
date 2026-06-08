package com.priyansu.workflow.service;

import com.priyansu.workflow.dto.WorkflowWebhookResponse;

import java.util.Map;
import java.util.UUID;

public interface WorkflowWebhookService {

    WorkflowWebhookResponse createWebhook(UUID workflowId);

    UUID triggerWebhook(String webhookKey, Map<String, Object> payload);
}