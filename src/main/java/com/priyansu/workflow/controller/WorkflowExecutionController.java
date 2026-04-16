package com.priyansu.workflow.controller;

import com.priyansu.workflow.execution.AsyncWorkflowExecutor;
import com.priyansu.workflow.service.WorkflowExecutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/workflows")
@RequiredArgsConstructor
public class WorkflowExecutionController {

    private final WorkflowExecutionService executionService;

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

}
