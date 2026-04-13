package com.priyansu.workflow.service.Impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.priyansu.workflow.entity.TaskExecution;
import com.priyansu.workflow.entity.WorkflowDefinition;
import com.priyansu.workflow.entity.WorkflowExecution;
import com.priyansu.workflow.entity.enums.ExecutionStatus;
import com.priyansu.workflow.exception.ResourceNotFoundException;
import com.priyansu.workflow.execution.WorkflowContext;
import com.priyansu.workflow.executor.ExecutorFactory;
import com.priyansu.workflow.executor.TaskExecutor;
import com.priyansu.workflow.repository.TaskExecutionRepository;
import com.priyansu.workflow.repository.WorkflowDefinitionRepository;
import com.priyansu.workflow.repository.WorkflowExecutionRepository;
import com.priyansu.workflow.service.WorkflowExecutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


@Service
@RequiredArgsConstructor
public class WorkflowExecutionServiceImpl implements WorkflowExecutionService {

    private final WorkflowDefinitionRepository definitionRepository;
    private final ObjectMapper objectMapper;
    private final ExecutorFactory executorFactory;
    private final WorkflowExecutionRepository executionRepository;
    private final TaskExecutionRepository taskExecutionRepository;


    // "ExecutorService with a fixed thread pool is used to control concurrency, reuse threads, and prevent resource exhaustion caused by creating too many threads manually."
    private final ExecutorService executorService = Executors.newFixedThreadPool(5); //max 5 parallel threads , Tasks beyond 5 → go into queue
    //  Without ExecutorService:Every request creates a new thread,  Server crashes under load

    @Override
    public void executeWorkflow(UUID workflowId) {

        //  STEP 1: CREATE EXECUTION RECORD (track workflow run)
        WorkflowExecution execution = new WorkflowExecution();
        execution.setWorkflowId(workflowId);
        execution.setStatus(ExecutionStatus.RUNNING);

        execution = executionRepository.save(execution);

        try {
            WorkflowDefinition definition = definitionRepository
                    .findTopByWorkflowIdOrderByVersionDesc(workflowId)
                    .orElseThrow(() -> new ResourceNotFoundException("Definition not found"));

            JsonNode json = definition.getDefinitionJson();

            JsonNode nodes = json.get("nodes");
            JsonNode edges = json.get("edges");

            //  Step 2: Find start node (TRIGGER)
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

            //  Step 3: Initialize context
            WorkflowContext context = new WorkflowContext();

            //  Step 4: Start execution (recursive + parallel inside)
            executeNode(startNode, nodes, edges, context, execution.getId() );

            //✅ Success
            execution.setStatus(ExecutionStatus.SUCCESS);


        } catch (Exception e) {
            // ❌ FAILURE CASE
            execution.setStatus(ExecutionStatus.FAILED);
            execution.setErrorMessage(e.getMessage());

            throw e;
        } finally {
            //  ALWAYS SAVE FINAL STATE (SUCCESS / FAILED)
            executionRepository.save(execution);
        }
    }


    private void executeNode(JsonNode currentNode,
                             JsonNode nodes,
                             JsonNode edges,
                             WorkflowContext context, UUID executionId) {

        String nodeId = currentNode.get("id").asText();
        String type = currentNode.get("type").asText();

        //log (store current-node details)
        TaskExecution task = new TaskExecution();
        task.setWorkflowExecutionId(executionId);
        task.setNodeId(nodeId);
        task.setNodeType(type);
        task.setStatus(ExecutionStatus.RUNNING);
        taskExecutionRepository.save(task);

        try {
            System.out.println( Thread.currentThread().getName() + "Executing: " + type + " (ID: " + nodeId + ")");

            //  Strategy Pattern used here (Execute current node)
            TaskExecutor executor = executorFactory.getExecutor(type); //TRIGGER, ACTION, etc...
            executor.execute(currentNode, context);

            task.setStatus(ExecutionStatus.SUCCESS);
            task.setLogMessage("Executed successfully");

        } catch (Exception e) {
            task.setStatus(ExecutionStatus.FAILED);
            task.setLogMessage(e.getMessage());

            throw e;
        } finally {
            taskExecutionRepository.save(task);
        }


        List<JsonNode> nextNodes = new ArrayList<>();

        //find all next nodes
        //  Move to next node(s) [For normal nodes:Always go to next node] & [For decision node:Check decisionResult: Match condition & Follow correct edge only]
        for (JsonNode edge : edges) {

            if (edge.get("from").asText().equals(nodeId)) {

                // 🔥 DECISION NODE HANDLING
                if ("DECISION".equals(type)) {

                    boolean decision = (boolean) context.get("decisionResult");

                    JsonNode conditionNode = edge.get("condition");

                    // ❗ skip edges without condition
                    if (conditionNode == null) continue;

                    String condition = conditionNode.asText();

                    if (String.valueOf(decision).equals(condition)) {

                        String nextId = edge.get("to").asText();
                        nextNodes.add(findNodeById(nodes, nextId));

                    }

                } else {
                    // 🔥 NORMAL FLOW (no condition required, ALL edges allowed)

                    String nextId = edge.get("to").asText();
                    nextNodes.add(findNodeById(nodes, nextId));
                }
            }
        }
        // 🔥 PARALLEL EXECUTION {Execute → find ALL next nodes → run in parallel}
        List<Future<?>> futures = new ArrayList<>(); //Future = a handle/reference to an async task [Helps you: wait for completion,get result,catch exceptions]

        for (JsonNode nextNode : nextNodes) { //submit tasks in parallel
            futures.add(executorService.submit(() ->  //(.submit):sends task to thread pool,Tasks run concurrently (max 5 at a time)
                    executeNode(nextNode, nodes, edges, context, executionId )
            ));
        }

        // 🔥 WAIT FOR ALL TO COMPLETE
        for (Future<?> future : futures) {
            try {
                future.get(); //Wait for all tasks to complete , All parallel tasks finish before moving forward
            } catch (Exception e) {
                throw new RuntimeException("Parallel execution failed", e);
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
