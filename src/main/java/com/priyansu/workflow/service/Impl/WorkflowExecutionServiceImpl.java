package com.priyansu.workflow.service.Impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.priyansu.workflow.entity.Workflow;
import com.priyansu.workflow.entity.WorkflowDefinition;
import com.priyansu.workflow.exception.ResourceNotFoundException;
import com.priyansu.workflow.execution.WorkflowContext;
import com.priyansu.workflow.executor.ExecutorFactory;
import com.priyansu.workflow.executor.TaskExecutor;
import com.priyansu.workflow.repository.WorkflowDefinitionRepository;
import com.priyansu.workflow.service.WorkflowExecutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;


@Service
@RequiredArgsConstructor
public class WorkflowExecutionServiceImpl implements WorkflowExecutionService {

    private final WorkflowDefinitionRepository definitionRepository;
    private final ObjectMapper objectMapper;
    private final ExecutorFactory executorFactory;

    @Override
    public void executeWorkflow(UUID workflowId) {

        WorkflowDefinition definition = definitionRepository
                .findTopByWorkflowIdOrderByVersionDesc(workflowId)
                .orElseThrow(() -> new ResourceNotFoundException("Definition not found"));

        JsonNode json = definition.getDefinitionJson();

        JsonNode nodes = json.get("nodes");
        JsonNode edges = json.get("edges");

        //  Step 1: Find start node (TRIGGER)
        JsonNode startNode = null;

        for (JsonNode node : nodes) {
            if ("TRIGGER".equals(node.get("type").asText())) {
                startNode = node;
                break;
            }
        }

        if (startNode == null) {
            throw new RuntimeException("No trigger node found");
        }

        //  Step 2: Initialize context
        WorkflowContext context = new WorkflowContext();

        //  Step 3: Start execution
        executeNode(startNode, nodes, edges, context);
    }



    private void executeNode(JsonNode currentNode,
                             JsonNode nodes,
                             JsonNode edges,
                             WorkflowContext context) {

        String nodeId = currentNode.get("id").asText();
        String type = currentNode.get("type").asText();

        System.out.println("Executing: " + type + " (ID: " + nodeId + ")");

        //  Strategy Pattern used here
        TaskExecutor executor = executorFactory.getExecutor(type); //TRIGGER, ACTION, etc...
        executor.execute(currentNode, context);

        //  Move to next node(s)
        for (JsonNode edge : edges) {
            if (edge.get("from").asText().equals(nodeId)) {

                String nextId = edge.get("to").asText();
                JsonNode nextNode = findNodeById(nodes, nextId);

                executeNode(nextNode, nodes, edges, context);
            }
        }
    }

    private JsonNode findNodeById(JsonNode nodes, String id) {
        for (JsonNode node : nodes) {
            if (node.get("id").asText().equals(id)) {
                return node;
            }
        }
        throw new RuntimeException("Node not found: " + id);
    }
}
