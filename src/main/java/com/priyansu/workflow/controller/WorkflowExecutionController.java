package com.priyansu.workflow.controller;

import com.priyansu.workflow.execution.AsyncWorkflowExecutor;
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

   // Async executor that triggers background workflow execution
    private final AsyncWorkflowExecutor asyncExecutor;

    @PostMapping("/{workflowId}/execute")
    public String execute(@PathVariable UUID workflowId) {

        // Triggers asynchronous execution of the workflow.
        // This call returns immediately without waiting for the workflow to complete.
        asyncExecutor.execute(workflowId);

        // Immediate response to client indicating background processing has started
        return "Workflow execution started asynchronously";
    }

}
