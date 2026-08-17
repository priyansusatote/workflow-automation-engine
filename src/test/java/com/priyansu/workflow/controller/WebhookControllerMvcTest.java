package com.priyansu.workflow.controller;

import com.priyansu.workflow.exception.GlobalExceptionHandler;
import com.priyansu.workflow.exception.ResourceNotFoundException;
import com.priyansu.workflow.service.WorkflowWebhookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class WebhookControllerMvcTest {

    private final WorkflowWebhookService webhookService = mock(WorkflowWebhookService.class);
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new WebhookController(webhookService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("Should trigger a workflow through its webhook key")
    void shouldTriggerWebhook() throws Exception {
        UUID executionId = UUID.randomUUID();
        when(webhookService.triggerWebhook("webhook-key", Map.of("amount", 10)))
                .thenReturn(executionId);

        mockMvc.perform(post("/api/v1/webhooks/{webhookKey}", "webhook-key")
                        .contentType("application/json")
                        .content("{\"amount\":10}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.executionId").value(executionId.toString()));

        verify(webhookService).triggerWebhook("webhook-key", Map.of("amount", 10));
    }

    @Test
    @DisplayName("Should map an invalid webhook key to not found")
    void shouldMapInvalidWebhookKey() throws Exception {
        when(webhookService.triggerWebhook(eq("missing"), anyMap()))
                .thenThrow(new ResourceNotFoundException("Webhook not found"));

        mockMvc.perform(post("/api/v1/webhooks/{webhookKey}", "missing")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Webhook not found"));
    }
}
