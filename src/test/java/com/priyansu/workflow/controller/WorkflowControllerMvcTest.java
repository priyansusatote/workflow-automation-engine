package com.priyansu.workflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.priyansu.workflow.dto.WorkflowRequest;
import com.priyansu.workflow.dto.WorkflowResponse;
import com.priyansu.workflow.entity.Workflow;
import com.priyansu.workflow.entity.enums.WorkflowStatus;
import com.priyansu.workflow.exception.DuplicateResourceException;
import com.priyansu.workflow.exception.GlobalExceptionHandler;
import com.priyansu.workflow.exception.ResourceNotFoundException;
import com.priyansu.workflow.service.WorkflowDefinitionService;
import com.priyansu.workflow.service.WorkflowService;
import com.priyansu.workflow.service.WorkflowValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class WorkflowControllerMvcTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final WorkflowService workflowService = mock(WorkflowService.class);
    private final WorkflowDefinitionService definitionService = mock(WorkflowDefinitionService.class);
    private final WorkflowValidationService validationService = mock(WorkflowValidationService.class);
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() throws Exception {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(new WorkflowController(
                        workflowService,
                        definitionService,
                        validationService,
                        objectMapper
                ))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    @DisplayName("Should create a workflow and return its response")
    void shouldCreateWorkflow() throws Exception {
        UUID workflowId = UUID.randomUUID();
        WorkflowResponse response = response(workflowId, "Invoice workflow");
        when(workflowService.createWorkflow(new WorkflowRequest("Invoice workflow", "Demo")))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/workflows")
                        .contentType("application/json")
                        .content("{\"name\":\"Invoice workflow\",\"description\":\"Demo\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(workflowId.toString()))
                .andExpect(jsonPath("$.name").value("Invoice workflow"));

        verify(workflowService).createWorkflow(new WorkflowRequest("Invoice workflow", "Demo"));
    }

    @Test
    @DisplayName("Should reject a workflow request with a blank name")
    void shouldRejectInvalidCreateRequest() throws Exception {
        mockMvc.perform(post("/api/v1/workflows")
                        .contentType("application/json")
                        .content("{\"name\":\" \"}"))
                        .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.message").value("name: Name is required"));

        verifyNoInteractions(workflowService);
    }

    @Test
    @DisplayName("Should map duplicate workflow errors to conflict")
    void shouldMapDuplicateWorkflowToConflict() throws Exception {
        when(workflowService.createWorkflow(any()))
                .thenThrow(new DuplicateResourceException("Workflow already exists"));

        mockMvc.perform(post("/api/v1/workflows")
                        .contentType("application/json")
                        .content("{\"name\":\"Invoice workflow\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.message").value("Workflow already exists"));
    }

    @Test
    @DisplayName("Should map malformed JSON to bad request")
    void shouldMapMalformedJsonToBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/workflows")
                        .contentType("application/json")
                        .content("{\"name\":"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Malformed request body"));

        verifyNoInteractions(workflowService);
    }

    @Test
    @DisplayName("Should map an invalid workflow UUID to bad request")
    void shouldMapInvalidUuidToBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/workflows/not-a-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid value for parameter 'id'"));

        verifyNoInteractions(workflowService);
    }

    @Test
    @DisplayName("Should map authorization failures to forbidden")
    void shouldMapAuthorizationFailureToForbidden() throws Exception {
        UUID workflowId = UUID.randomUUID();
        when(workflowService.getWorkflowById(workflowId))
                .thenThrow(new org.springframework.security.access.AccessDeniedException("Workflow access denied"));

        mockMvc.perform(get("/api/v1/workflows/{id}", workflowId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Workflow access denied"));
    }

    @Test
    @DisplayName("Should sanitize unexpected exceptions")
    void shouldSanitizeUnexpectedException() throws Exception {
        UUID workflowId = UUID.randomUUID();
        when(workflowService.getWorkflowById(workflowId))
                .thenThrow(new RuntimeException("database password leaked"));

        mockMvc.perform(get("/api/v1/workflows/{id}", workflowId))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Unexpected internal error"));
    }

    @Test
    @DisplayName("Should list the current user's workflows")
    void shouldListWorkflows() throws Exception {
        Workflow workflow = new Workflow();
        workflow.setId(UUID.randomUUID());
        workflow.setName("Invoice workflow");
        workflow.setStatus(WorkflowStatus.ACTIVE);
        when(workflowService.getMyWorkflows()).thenReturn(List.of(workflow));

        mockMvc.perform(get("/api/v1/workflows"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Invoice workflow"));

        verify(workflowService).getMyWorkflows();
    }

    @Test
    @DisplayName("Should retrieve a workflow by id")
    void shouldGetWorkflow() throws Exception {
        UUID workflowId = UUID.randomUUID();
        when(workflowService.getWorkflowById(workflowId)).thenReturn(response(workflowId, "Invoice workflow"));

        mockMvc.perform(get("/api/v1/workflows/{id}", workflowId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(workflowId.toString()))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("Should map missing workflow errors to not found")
    void shouldMapMissingWorkflowToNotFound() throws Exception {
        UUID workflowId = UUID.randomUUID();
        when(workflowService.getWorkflowById(workflowId))
                .thenThrow(new ResourceNotFoundException("Workflow not found"));

        mockMvc.perform(get("/api/v1/workflows/{id}", workflowId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Workflow not found"));
    }

    @Test
    @DisplayName("Should update a workflow")
    void shouldUpdateWorkflow() throws Exception {
        UUID workflowId = UUID.randomUUID();
        when(workflowService.updateWorkflow(workflowId, new WorkflowRequest("Updated", "Description")))
                .thenReturn(response(workflowId, "Updated"));

        mockMvc.perform(put("/api/v1/workflows/{id}", workflowId)
                        .contentType("application/json")
                        .content("{\"name\":\"Updated\",\"description\":\"Description\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));

        verify(workflowService).updateWorkflow(workflowId, new WorkflowRequest("Updated", "Description"));
    }

    @Test
    @DisplayName("Should delete a workflow")
    void shouldDeleteWorkflow() throws Exception {
        UUID workflowId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/workflows/{id}", workflowId))
                .andExpect(status().isOk())
                .andExpect(content().string("Workflow deleted Successfully"));

        verify(workflowService).deleteWorkflow(workflowId);
    }

    @Test
    @DisplayName("Should return validation errors when no definition exists")
    void shouldReportMissingDefinitionDuringValidation() throws Exception {
        UUID workflowId = UUID.randomUUID();
        when(definitionService.getDefinition(workflowId))
                .thenThrow(new ResourceNotFoundException("Definition not found"));

        mockMvc.perform(post("/api/v1/workflows/{id}/validate", workflowId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.errors[0]").value("No definition found for this workflow. Save a definition first."));

        verifyNoInteractions(validationService);
    }

    @Test
    @DisplayName("Should return workflow validation results")
    void shouldValidateDefinition() throws Exception {
        UUID workflowId = UUID.randomUUID();
        when(definitionService.getDefinition(workflowId)).thenReturn("{\"nodes\":[],\"edges\":[]}");
        when(validationService.validate(any())).thenReturn(List.of("Workflow must contain exactly one trigger node"));

        mockMvc.perform(post("/api/v1/workflows/{id}/validate", workflowId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.errors[0]").value("Workflow must contain exactly one trigger node"));

        verify(validationService).validate(any());
    }

    private WorkflowResponse response(UUID id, String name) {
        return new WorkflowResponse(id, name, "Description", UUID.randomUUID(), WorkflowStatus.ACTIVE, null, null);
    }
}
