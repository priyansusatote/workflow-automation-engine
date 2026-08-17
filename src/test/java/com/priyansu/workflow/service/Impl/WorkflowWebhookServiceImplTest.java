package com.priyansu.workflow.service.Impl;

import com.priyansu.workflow.entity.Workflow;
import com.priyansu.workflow.entity.WorkflowWebhook;
import com.priyansu.workflow.exception.ResourceNotFoundException;
import com.priyansu.workflow.repository.WorkflowRepository;
import com.priyansu.workflow.repository.WorkflowWebhookRepository;
import com.priyansu.workflow.service.WorkflowExecutionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkflowWebhookServiceImplTest {

    @Mock WorkflowWebhookRepository webhookRepository;
    @Mock WorkflowExecutionService executionService;
    @Mock WorkflowRepository workflowRepository;
    @InjectMocks WorkflowWebhookServiceImpl webhookService;

    @Test
    @DisplayName("Should create and persist a webhook for an existing workflow")
    void shouldCreateWebhook() {
        UUID workflowId = UUID.randomUUID();
        when(workflowRepository.findById(workflowId)).thenReturn(Optional.of(new Workflow()));
        when(webhookRepository.findByWorkflowId(workflowId)).thenReturn(Optional.empty());

        var response = webhookService.createWebhook(workflowId);

        assertThat(response.workflowId()).isEqualTo(workflowId);
        assertThat(response.webhookKey()).isNotBlank();
        verify(webhookRepository).save(argThat(webhook ->
                workflowId.equals(webhook.getWorkflowId())
                        && response.webhookKey().equals(webhook.getWebhookKey())));
    }

    @Test
    @DisplayName("Should return the existing webhook instead of creating a second one")
    void shouldReuseExistingWebhook() {
        UUID workflowId = UUID.randomUUID();
        WorkflowWebhook existing = new WorkflowWebhook();
        existing.setWorkflowId(workflowId);
        existing.setWebhookKey("existing-key");
        when(workflowRepository.findById(workflowId)).thenReturn(Optional.of(new Workflow()));
        when(webhookRepository.findByWorkflowId(workflowId)).thenReturn(Optional.of(existing));

        var response = webhookService.createWebhook(workflowId);

        assertThat(response.webhookKey()).isEqualTo("existing-key");
        verify(webhookRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject webhook creation for a missing workflow")
    void shouldRejectMissingWorkflow() {
        UUID workflowId = UUID.randomUUID();
        when(workflowRepository.findById(workflowId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> webhookService.createWebhook(workflowId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Workflow not found");
    }

    @Test
    @DisplayName("Should trigger the workflow associated with a webhook key")
    void shouldTriggerWorkflow() {
        UUID workflowId = UUID.randomUUID();
        UUID executionId = UUID.randomUUID();
        WorkflowWebhook webhook = new WorkflowWebhook();
        webhook.setWorkflowId(workflowId);
        when(webhookRepository.findByWebhookKey("key")).thenReturn(Optional.of(webhook));
        when(executionService.triggerWorkflow(workflowId, Map.of("amount", 10))).thenReturn(executionId);

        UUID result = webhookService.triggerWebhook("key", Map.of("amount", 10));

        assertThat(result).isEqualTo(executionId);
        verify(executionService).triggerWorkflow(workflowId, Map.of("amount", 10));
    }

    @Test
    @DisplayName("Should reject an unknown webhook key")
    void shouldRejectUnknownWebhook() {
        when(webhookRepository.findByWebhookKey("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> webhookService.triggerWebhook("missing", Map.of()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Webhook not found");
        verifyNoInteractions(executionService);
    }
}
