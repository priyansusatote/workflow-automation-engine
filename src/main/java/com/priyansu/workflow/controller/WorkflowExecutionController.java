package com.priyansu.workflow.controller;

import com.priyansu.workflow.execution.AsyncWorkflowExecutor;
import com.priyansu.workflow.service.WorkflowExecutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/workflows")
@RequiredArgsConstructor
public class WorkflowExecutionController {

    private final WorkflowExecutionService executionService;

    //only owner  of workflow can execute {or Admin)
    @PreAuthorize("hasRole('ADMIN') or @workflowSecurity.isOwner(#workflowId)")
    @PostMapping("/{workflowId}/execute")
    public ResponseEntity<Map<String, Object>> execute(
            @PathVariable UUID workflowId,
            @RequestBody(required = false) Map<String, Object> input
    ) {

        UUID executionId = executionService.triggerWorkflow(
                workflowId,
                input != null ? input : Map.of()
        );

        return ResponseEntity.ok(Map.of(
                "message", "Workflow execution started",
                "executionId", executionId
        ));
    }
    @PreAuthorize("hasRole('ADMIN') or @workflowSecurity.isOwner(#workflowId)")
    @PostMapping("/executions/{id}/resume")
    public String resume(@PathVariable UUID id) {

        executionService.resumeExecution(id);

        return "Execution resumed";
    }

}
