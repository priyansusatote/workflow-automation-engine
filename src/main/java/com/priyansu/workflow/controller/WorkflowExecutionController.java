package com.priyansu.workflow.controller;

import com.priyansu.workflow.service.WorkflowExecutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/workflows")
@RequiredArgsConstructor
public class WorkflowExecutionController {

    private final WorkflowExecutionService executionService;

    @PostMapping("/{workflowId}/execute")
    public String execute(@PathVariable UUID workflowId) {
        executionService.executeWorkflow(workflowId);
        return "Workflow execution started";
    }

}
