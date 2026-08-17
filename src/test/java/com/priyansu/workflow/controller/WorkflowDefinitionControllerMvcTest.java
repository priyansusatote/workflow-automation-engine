package com.priyansu.workflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.priyansu.workflow.dto.WorkflowDefinitionRequest;
import com.priyansu.workflow.exception.GlobalExceptionHandler;
import com.priyansu.workflow.exception.ResourceNotFoundException;
import com.priyansu.workflow.exception.WorkflowValidationException;
import com.priyansu.workflow.service.WorkflowDefinitionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class WorkflowDefinitionControllerMvcTest {

    private final WorkflowDefinitionService definitionService = mock(WorkflowDefinitionService.class);
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new WorkflowDefinitionController(definitionService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("Should save a workflow definition")
    void shouldSaveDefinition() throws Exception {
        UUID workflowId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/workflows/{workflowId}/definition", workflowId)
                        .contentType("application/json")
                        .content("{\"nodes\":[],\"edges\":[]}"))
                .andExpect(status().isOk())
                .andExpect(content().string("Definition saved"));

        verify(definitionService).saveDefinition(eq(workflowId), any(WorkflowDefinitionRequest.class));
    }

    @Test
    @DisplayName("Should retrieve a workflow definition")
    void shouldGetDefinition() throws Exception {
        UUID workflowId = UUID.randomUUID();
        when(definitionService.getDefinition(workflowId)).thenReturn("{\"nodes\":[],\"edges\":[]}");

        mockMvc.perform(get("/api/v1/workflows/{workflowId}/definition", workflowId))
                .andExpect(status().isOk())
                .andExpect(content().string("{\"nodes\":[],\"edges\":[]}"));
    }

    @Test
    @DisplayName("Should map definition validation failures to bad request")
    void shouldMapDefinitionValidationFailure() throws Exception {
        UUID workflowId = UUID.randomUUID();
        doThrow(new WorkflowValidationException("Workflow validation failed"))
                .when(definitionService).saveDefinition(eq(workflowId), any());

        mockMvc.perform(post("/api/v1/workflows/{workflowId}/definition", workflowId)
                        .contentType("application/json")
                        .content("{\"nodes\":[],\"edges\":[]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Workflow validation failed"));
    }

    @Test
    @DisplayName("Should map missing definitions to not found")
    void shouldMapMissingDefinition() throws Exception {
        UUID workflowId = UUID.randomUUID();
        when(definitionService.getDefinition(workflowId))
                .thenThrow(new ResourceNotFoundException("Definition not found"));

        mockMvc.perform(get("/api/v1/workflows/{workflowId}/definition", workflowId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
