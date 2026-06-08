package com.priyansu.workflow.controller;

import com.priyansu.workflow.service.WorkflowWebhookService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
public class WebhookController {

    private final WorkflowWebhookService workflowWebhookService;

    // external applications will be able to start your workflows automatically.
    @PostMapping("/{webhookKey}")
    public Map<String, UUID> triggerWebhook(
            @PathVariable String webhookKey,
            @RequestBody Map<String, Object> payload
    ) {

        UUID executionId = workflowWebhookService.triggerWebhook(webhookKey, payload);

        return Map.of("executionId", executionId);
    }

}