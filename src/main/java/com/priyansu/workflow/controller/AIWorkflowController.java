package com.priyansu.workflow.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.priyansu.workflow.dto.ai.WorkflowGenerationRequest;
import com.priyansu.workflow.service.ai.WorkflowGenerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai/workflows")
@RequiredArgsConstructor
public class AIWorkflowController {

    private final WorkflowGenerationService workflowGenerationService;

    @PostMapping(
            value = "/generate",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<String> generate(@RequestBody WorkflowGenerationRequest request){
        String workflow =
                workflowGenerationService.generateWorkflow(
                        request.prompt()
                );

        return ResponseEntity.ok(workflow);
    }
}
