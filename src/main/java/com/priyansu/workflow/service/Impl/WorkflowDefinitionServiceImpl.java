package com.priyansu.workflow.service.Impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.priyansu.workflow.dto.WorkflowDefinitionRequest;
import com.priyansu.workflow.entity.WorkflowDefinition;
import com.priyansu.workflow.exception.ResourceNotFoundException;
import com.priyansu.workflow.repository.WorkflowDefinitionRepository;
import com.priyansu.workflow.repository.WorkflowRepository;
import com.priyansu.workflow.service.WorkflowDefinitionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;



import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkflowDefinitionServiceImpl implements WorkflowDefinitionService {

    private final WorkflowDefinitionRepository definitionRepository;
    private final WorkflowRepository workflowRepository;
    private final ObjectMapper objectMapper;


    @Override
    public void saveDefinition(UUID workflowId, WorkflowDefinitionRequest request) {
        //check workflow exists
        workflowRepository.findById(workflowId)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow Not Found"));


        try {
            JsonNode jsonNode = objectMapper.valueToTree(request);


            //Finds latest version and include current as +1 next(latest)
            int version = definitionRepository
                    .findTopByWorkflowIdOrderByVersionDesc(workflowId) //Returns latest version
                    .map(def -> def.getVersion() + 1)
                    .orElse(1);

            WorkflowDefinition definition = new WorkflowDefinition();
            definition.setWorkflowId(workflowId);
            definition.setDefinitionJson(jsonNode);
            definition.setVersion(version);

            definitionRepository.save(definition);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error saving workflow definition", e);
        }
    }

    @Override
    public String getDefinition(UUID workflowId) {
        WorkflowDefinition definition = definitionRepository
                .findTopByWorkflowIdOrderByVersionDesc(workflowId)
                .orElseThrow(() -> new ResourceNotFoundException("Definition not found"));

        return definition.getDefinitionJson().toString();
    }
}
