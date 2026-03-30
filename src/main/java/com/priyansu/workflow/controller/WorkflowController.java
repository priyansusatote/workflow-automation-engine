package com.priyansu.workflow.controller;

import com.priyansu.workflow.dto.WorkflowRequest;
import com.priyansu.workflow.dto.WorkflowResponse;
import com.priyansu.workflow.service.WorkflowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/workflows")
@RequiredArgsConstructor
public class WorkflowController {

    private final WorkflowService workflowService;

    @PostMapping
    public WorkflowResponse create( @RequestBody @Valid WorkflowRequest request){
        return workflowService.createWorkflow(request);
    }

    @GetMapping
    public List<WorkflowResponse> getAll(){
        return workflowService.getAllWorkflows();
    }

    @GetMapping("/{id}")
    public WorkflowResponse getById(@PathVariable UUID id){
        return workflowService.getWorkflowById(id);
    }

    @PutMapping("/{id}")
    public WorkflowResponse update(
            @PathVariable UUID id,
            @RequestBody @Valid WorkflowRequest request){
        return workflowService.updateWorkflow(id, request);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable UUID id){
        workflowService.deleteWorkflow(id);
        return "Workflow deleted Successfully";
    }

}
