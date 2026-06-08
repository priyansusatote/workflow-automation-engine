package com.priyansu.workflow.dto;

import java.util.UUID;

public record WorkflowWebhookResponse(
        UUID workflowId,
        String webhookKey
) {
}