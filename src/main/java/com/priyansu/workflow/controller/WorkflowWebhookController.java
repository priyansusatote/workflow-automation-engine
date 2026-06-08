package com.priyansu.workflow.controller;

import com.priyansu.workflow.dto.WorkflowWebhookResponse;
import com.priyansu.workflow.entity.WorkflowWebhook;
import com.priyansu.workflow.repository.WorkflowWebhookRepository;
import com.priyansu.workflow.service.WorkflowWebhookService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/workflows")
@RequiredArgsConstructor
public class WorkflowWebhookController {

    private final WorkflowWebhookRepository workflowWebhookRepository;
    private final WorkflowWebhookService workflowWebhookService;

    @PostMapping("/{workflowId}/webhook")
    public WorkflowWebhookResponse createWebhook(
            @PathVariable UUID workflowId
    ) {

        return workflowWebhookService.createWebhook(workflowId);
    }
}