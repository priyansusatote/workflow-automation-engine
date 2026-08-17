package com.priyansu.workflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.priyansu.workflow.dto.ExecutionSummaryResponse;
import com.priyansu.workflow.dto.TaskExecutionResponse;
import com.priyansu.workflow.dto.WorkflowExecutionResponse;
import com.priyansu.workflow.entity.enums.ExecutionStatus;
import com.priyansu.workflow.exception.GlobalExceptionHandler;
import com.priyansu.workflow.exception.ResourceNotFoundException;
import com.priyansu.workflow.exception.WorkflowValidationException;
import com.priyansu.workflow.service.WorkflowExecutionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.data.web.config.SpringDataJacksonConfiguration;
import org.springframework.data.web.config.SpringDataWebSettings;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class WorkflowExecutionControllerMvcTest {

    private final WorkflowExecutionService executionService = mock(WorkflowExecutionService.class);
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        objectMapper.registerModule(new SpringDataJacksonConfiguration.PageModule(
                new SpringDataWebSettings(EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
        ));

        mockMvc = MockMvcBuilders.standaloneSetup(new WorkflowExecutionController(executionService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    @DisplayName("Should trigger a workflow with request input")
    void shouldTriggerWorkflow() throws Exception {
        UUID workflowId = UUID.randomUUID();
        UUID executionId = UUID.randomUUID();
        when(executionService.triggerWorkflow(workflowId, Map.of("amount", 10))).thenReturn(executionId);

        mockMvc.perform(post("/api/v1/workflows/{workflowId}/execute", workflowId)
                        .contentType("application/json")
                        .content("{\"amount\":10}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Workflow execution started"))
                .andExpect(jsonPath("$.executionId").value(executionId.toString()));
    }

    @Test
    @DisplayName("Should pass an empty input map when execution request has no body")
    void shouldTriggerWorkflowWithoutInput() throws Exception {
        UUID workflowId = UUID.randomUUID();
        UUID executionId = UUID.randomUUID();
        when(executionService.triggerWorkflow(workflowId, Map.of())).thenReturn(executionId);

        mockMvc.perform(post("/api/v1/workflows/{workflowId}/execute", workflowId))
                .andExpect(status().isOk());

        verify(executionService).triggerWorkflow(workflowId, Map.of());
    }

    @Test
    @DisplayName("Should map inactive workflow rejection to bad request")
    void shouldMapInactiveWorkflowRejection() throws Exception {
        UUID workflowId = UUID.randomUUID();
        when(executionService.triggerWorkflow(eq(workflowId), anyMap()))
                .thenThrow(new WorkflowValidationException("Workflow is not active"));

        mockMvc.perform(post("/api/v1/workflows/{workflowId}/execute", workflowId)
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Workflow is not active"));
    }

    @Test
    @DisplayName("Should retrieve an execution")
    void shouldGetExecution() throws Exception {
        UUID executionId = UUID.randomUUID();
        UUID workflowId = UUID.randomUUID();
        when(executionService.getExecution(executionId)).thenReturn(
                new WorkflowExecutionResponse(executionId, workflowId, "SUCCESS", null, null, null));

        mockMvc.perform(get("/api/v1/workflows/executions/{executionId}", executionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.executionId").value(executionId.toString()))
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @DisplayName("Should map missing executions to not found")
    void shouldMapMissingExecution() throws Exception {
        UUID executionId = UUID.randomUUID();
        when(executionService.getExecution(executionId))
                .thenThrow(new ResourceNotFoundException("Execution not found"));

        mockMvc.perform(get("/api/v1/workflows/executions/{executionId}", executionId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Execution not found"));
    }

    @Test
    @DisplayName("Should return execution task history including an empty result")
    void shouldGetExecutionTasks() throws Exception {
        UUID executionId = UUID.randomUUID();
        when(executionService.getExecutionTasks(executionId)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/workflows/executions/{executionId}/tasks", executionId))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(executionService).getExecutionTasks(executionId);
    }

    @Test
    @DisplayName("Should list executions with workflow and status filters")
    void shouldListExecutions() throws Exception {
        UUID workflowId = UUID.randomUUID();
        when(executionService.getExecutions(eq(workflowId), eq(ExecutionStatus.FAILED), any()))
                .thenReturn(new PageImpl<>(List.of(new ExecutionSummaryResponse(
                        UUID.randomUUID(), workflowId, "Invoice", ExecutionStatus.FAILED, null, null
                ))));

        mockMvc.perform(get("/api/v1/workflows/executions")
                        .param("workflowId", workflowId.toString())
                        .param("status", "FAILED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].workflowName").value("Invoice"))
                .andExpect(jsonPath("$.content[0].status").value("FAILED"));
    }

    @Test
    @DisplayName("Should map an invalid execution status to bad request")
    void shouldMapInvalidEnumToBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/workflows/executions")
                        .param("status", "NOT_A_STATUS"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid value for parameter 'status'"));

        verifyNoInteractions(executionService);
    }

    @Test
    @DisplayName("Should resume an execution")
    void shouldResumeExecution() throws Exception {
        UUID executionId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/workflows/executions/{id}/resume", executionId))
                .andExpect(status().isOk())
                .andExpect(content().json("\"Execution resumed\""));

        verify(executionService).resumeExecution(executionId);
    }
}
