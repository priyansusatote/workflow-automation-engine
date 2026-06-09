package com.priyansu.workflow.service.Impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.priyansu.workflow.dto.WorkflowDefinitionRequest;
import com.priyansu.workflow.entity.WorkflowDefinition;
import com.priyansu.workflow.exception.ResourceNotFoundException;
import com.priyansu.workflow.exception.WorkflowValidationException;
import com.priyansu.workflow.repository.WorkflowDefinitionRepository;
import com.priyansu.workflow.repository.WorkflowRepository;
import com.priyansu.workflow.service.WorkflowDefinitionService;
import com.priyansu.workflow.service.WorkflowValidationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowDefinitionServiceImpl implements WorkflowDefinitionService {

    private final WorkflowDefinitionRepository definitionRepository;
    private final WorkflowRepository workflowRepository;
    private final ObjectMapper objectMapper;
    private final WorkflowValidationService workflowValidationService;


    @Override
    @Transactional
    public void saveDefinition(UUID workflowId, WorkflowDefinitionRequest request) {
        //check workflow exists
        workflowRepository.findById(workflowId)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow Not Found"));


        JsonNode definitionJson = objectMapper.valueToTree(request);

        List<String> validationErrors =
                workflowValidationService.validate(definitionJson);

        if (!validationErrors.isEmpty()) {
            log.warn(
                    "Workflow validation failed -> workflowId={}, errors={}",
                    workflowId,
                    validationErrors
            );

            throw new WorkflowValidationException(
                    "Workflow validation failed: "
                            + String.join(", ", validationErrors)
            );
        }


        //Finds latest version and include current as +1 next(latest)
        int nextVersion = definitionRepository
                .findTopByWorkflowIdOrderByVersionDesc(workflowId) //Returns latest version
                .map(def -> def.getVersion() + 1)
                .orElse(1);

        WorkflowDefinition definition = new WorkflowDefinition();
        definition.setWorkflowId(workflowId);
        definition.setDefinitionJson(definitionJson);
        definition.setVersion(nextVersion);


        log.info( //log before saving
                "Saving workflow definition -> workflowId={}, version={}",
                workflowId,
                nextVersion
        );

        definitionRepository.save(definition);

        log.info(
                "Workflow definition saved -> workflowId={}, version={}",
                workflowId,
                nextVersion
        );

    }

    @Override
    public String getDefinition(UUID workflowId) {
        WorkflowDefinition definition = definitionRepository
                .findTopByWorkflowIdOrderByVersionDesc(workflowId)
                .orElseThrow(() -> new ResourceNotFoundException("Definition not found"));

        return definition.getDefinitionJson().toString();
    }
}
