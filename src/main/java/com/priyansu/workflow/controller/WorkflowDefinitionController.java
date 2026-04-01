package com.priyansu.workflow.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.priyansu.workflow.dto.WorkflowDefinitionRequest;
import com.priyansu.workflow.service.WorkflowDefinitionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


import java.util.UUID;

@RestController
@RequestMapping("/api/v1/workflows/{workflowId}/definition")
@RequiredArgsConstructor
public class WorkflowDefinitionController {

    private final WorkflowDefinitionService definitionService;

    @PostMapping
    public String save(
            @PathVariable UUID workflowId,
            @RequestBody WorkflowDefinitionRequest request
    ){
      definitionService.saveDefinition(workflowId, request);
      return "Definition saved";
    }

    @GetMapping
    public String get(@PathVariable UUID workflowId){
        return definitionService.getDefinition(workflowId);
    }
}
