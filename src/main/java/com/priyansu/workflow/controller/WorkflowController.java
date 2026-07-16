package com.priyansu.workflow.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.priyansu.workflow.dto.WorkflowRequest;
import com.priyansu.workflow.dto.WorkflowResponse;
import com.priyansu.workflow.entity.Workflow;
import com.priyansu.workflow.exception.ResourceNotFoundException;
import com.priyansu.workflow.service.WorkflowDefinitionService;
import com.priyansu.workflow.service.WorkflowService;
import com.priyansu.workflow.service.WorkflowValidationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/workflows")
@RequiredArgsConstructor
public class WorkflowController {

    private final WorkflowService workflowService;
    private final WorkflowDefinitionService definitionService;
    private final WorkflowValidationService validationService;
    private final ObjectMapper objectMapper;

    @PostMapping
    public WorkflowResponse create( @RequestBody @Valid WorkflowRequest request){
        return workflowService.createWorkflow(request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public List<WorkflowResponse> getAll(){
        return workflowService.getAllWorkflows();
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping
    public List<Workflow> getMyWorkflows() {
        return workflowService.getMyWorkflows();
    }

    @GetMapping("/{id}")
    public WorkflowResponse getById(@PathVariable UUID id){
        return workflowService.getWorkflowById(id);
    }

    @PreAuthorize("@workflowSecurity.isOwner(#id)")
    @PutMapping("/{id}")
    public WorkflowResponse update(
            @PathVariable UUID id,
            @RequestBody @Valid WorkflowRequest request){
        return workflowService.updateWorkflow(id, request);
    }

    @PreAuthorize("hasRole('ADMIN') or @workflowSecurity.isOwner(#id)")
    @DeleteMapping("/{id}")
    public String delete(@PathVariable UUID id){
        workflowService.deleteWorkflow(id);
        return "Workflow deleted Successfully";
    }

    @PreAuthorize("@workflowSecurity.isOwner(#id)")
    @PutMapping("/{id}/activate")
    public WorkflowResponse activate(@PathVariable UUID id) {
        return workflowService.activateWorkflow(id);
    }

    @PreAuthorize("@workflowSecurity.isOwner(#id)")
    @PutMapping("/{id}/deactivate")
    public WorkflowResponse deactivate(@PathVariable UUID id) {
        return workflowService.deactivateWorkflow(id);
    }

    @PreAuthorize("@workflowSecurity.isOwner(#id)")
    @PostMapping("/{id}/validate")
    public Map<String, Object> validate(@PathVariable UUID id) {
        String definitionJson;
        try {
            definitionJson = definitionService.getDefinition(id);
        } catch (ResourceNotFoundException e) {
            return Map.of(
                    "valid", false,
                    "errors", List.of("No definition found for this workflow. Save a definition first.")
            );
        }

        try {
            JsonNode jsonNode = objectMapper.readTree(definitionJson);
            List<String> errors = validationService.validate(jsonNode);

            return Map.of(
                    "valid", errors.isEmpty(),
                    "errors", errors
            );
        } catch (Exception e) {
            return Map.of(
                    "valid", false,
                    "errors", List.of("Invalid definition JSON: " + e.getMessage())
            );
        }
    }

}
