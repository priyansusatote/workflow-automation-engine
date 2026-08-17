package com.priyansu.workflow.controller;

import com.priyansu.workflow.dto.WorkflowWebhookResponse;
import com.priyansu.workflow.exception.GlobalExceptionHandler;
import com.priyansu.workflow.exception.ResourceNotFoundException;
import com.priyansu.workflow.repository.WorkflowWebhookRepository;
import com.priyansu.workflow.service.WorkflowWebhookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class WorkflowWebhookControllerMvcTest {

    private final WorkflowWebhookRepository webhookRepository = mock(WorkflowWebhookRepository.class);
    private final WorkflowWebhookService webhookService = mock(WorkflowWebhookService.class);
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new WorkflowWebhookController(webhookRepository, webhookService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("Should create or reuse a workflow webhook")
    void shouldCreateWebhook() throws Exception {
        UUID workflowId = UUID.randomUUID();
        when(webhookService.createWebhook(workflowId))
                .thenReturn(new WorkflowWebhookResponse(workflowId, "webhook-key"));

        mockMvc.perform(post("/api/v1/workflows/{workflowId}/webhook", workflowId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.workflowId").value(workflowId.toString()))
                .andExpect(jsonPath("$.webhookKey").value("webhook-key"));

        verify(webhookService).createWebhook(workflowId);
    }

    @Test
    @DisplayName("Should map missing workflow webhook resources to not found")
    void shouldMapMissingWebhookResource() throws Exception {
        UUID workflowId = UUID.randomUUID();
        when(webhookService.createWebhook(workflowId))
                .thenThrow(new ResourceNotFoundException("Workflow not found"));

        mockMvc.perform(post("/api/v1/workflows/{workflowId}/webhook", workflowId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Workflow not found"));
    }
}
