package com.priyansu.workflow.service.Impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.priyansu.workflow.dto.WorkflowExecutionResponse;
import com.priyansu.workflow.entity.TaskExecution;
import com.priyansu.workflow.entity.WorkflowDefinition;
import com.priyansu.workflow.entity.WorkflowExecution;
import com.priyansu.workflow.entity.enums.ExecutionStatus;
import com.priyansu.workflow.event.WorkflowExecutionEvent;
import com.priyansu.workflow.event.WorkflowExecutionProducer;
import com.priyansu.workflow.exception.ResourceNotFoundException;
import com.priyansu.workflow.exception.WorkflowValidationException;
import com.priyansu.workflow.execution.WorkflowContext;
import com.priyansu.workflow.executor.ExecutorFactory;
import com.priyansu.workflow.executor.TaskExecutor;
import com.priyansu.workflow.repository.TaskExecutionRepository;
import com.priyansu.workflow.repository.WorkflowDefinitionRepository;
import com.priyansu.workflow.repository.WorkflowExecutionRepository;
import com.priyansu.workflow.service.WorkflowExecutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


@Service
@Slf4j
@RequiredArgsConstructor
public class WorkflowExecutionServiceImpl implements WorkflowExecutionService {

    private final WorkflowDefinitionRepository definitionRepository;
    private final ObjectMapper objectMapper;
    private final ExecutorFactory executorFactory;
    private final WorkflowExecutionRepository executionRepository;
    private final TaskExecutionRepository taskExecutionRepository;
    private final WorkflowExecutionProducer producer;


    // "ExecutorService with a fixed thread pool is used to control concurrency, reuse threads, and prevent resource exhaustion caused by creating too many threads manually."
    private final ExecutorService executorService = Executors.newFixedThreadPool(5); //max 5 parallel threads , Tasks beyond 5 → go into queue
    //  Without ExecutorService:Every request creates a new thread,  Server crashes under load

//    @Override
//    public void executeWorkflow(UUID workflowId) {
//
//        //  STEP 1: CREATE EXECUTION RECORD (track workflow run)
//        WorkflowExecution execution = new WorkflowExecution();
//        execution.setWorkflowId(workflowId);
//        execution.setStatus(ExecutionStatus.RUNNING);
//
//        execution = executionRepository.save(execution);
//
//        try {
//            WorkflowDefinition definition = definitionRepository
//                    .findTopByWorkflowIdOrderByVersionDesc(workflowId)
//                    .orElseThrow(() -> new ResourceNotFoundException("Definition not found"));
//
//            JsonNode json = definition.getDefinitionJson();
//
//            JsonNode nodes = json.get("nodes");
//            JsonNode edges = json.get("edges");
//
//            //  Step 2: Find start node (TRIGGER)
//            JsonNode startNode = null;
//
//            for (JsonNode node : nodes) {
//                if ("TRIGGER".equals(node.get("type").asText())) {
//                    startNode = node;
//                    break;
//                }
//            }
//            if (startNode == null) {
//                throw new RuntimeException("No trigger node found");
//            }
//
//            //  Step 3: Initialize context
//            WorkflowContext context = new WorkflowContext();
//
//            //  Step 4: Start execution (recursive + parallel inside)
//            executeNode(startNode, nodes, edges, context, execution.getId());
//
//            //✅ Success
//            execution.setStatus(ExecutionStatus.SUCCESS);
//
//
//        } catch (Exception e) {
//            // ❌ FAILURE CASE
//            execution.setStatus(ExecutionStatus.FAILED);
//            execution.setErrorMessage(e.getMessage());
//
//            throw e;
//        } finally {
//            //  ALWAYS SAVE FINAL STATE (SUCCESS / FAILED)
//            executionRepository.save(execution);
//        }
//    }

    public void executeWorkflowFromKafka(
            UUID workflowId,
            UUID executionId,
            Map<String, Object> input
    ) {

        WorkflowExecution execution = executionRepository
                .findById(executionId)
                .orElseThrow(() -> new ResourceNotFoundException("Execution not found"));

        // 🔥 IDEMPOTENCY CHECK {Same message can come twice}{Idempotency ensures that repeated requests or events do not cause duplicate side effects.} [Ensures a Thing(operation) to happens Once , Should only happen once even if repeated"
        if (execution.getStatus() == ExecutionStatus.SUCCESS) {
            return;
        }

        try {
            log.info("Starting workflow execution → executionId={}", executionId);

            execution.setStatus(ExecutionStatus.RUNNING);
            executionRepository.save(execution);

            WorkflowDefinition definition = definitionRepository
                    .findTopByWorkflowIdOrderByVersionDesc(workflowId)
                    .orElseThrow(() -> new ResourceNotFoundException("Definition not found"));

            JsonNode json = definition.getDefinitionJson();
            JsonNode nodes = json.get("nodes");
            JsonNode edges = json.get("edges");

            // find trigger node
            JsonNode startNode = null;
            for (JsonNode node : nodes) {
                if ("TRIGGER".equals(node.get("type").asText())) {
                    startNode = node;
                    break;
                }
            }

            if (startNode == null) {
                throw new WorkflowValidationException("No trigger node found");
            }

            //  Use input here (IMPORTANT)
            WorkflowContext context = new WorkflowContext(input);

            executeNode(startNode, nodes, edges, context, executionId);

            // reload latest state from DB
            execution = executionRepository
                    .findById(executionId)
                    .orElseThrow(() ->
                            new ResourceNotFoundException("Execution not found")
                    );

            // only mark SUCCESS if workflow is not waiting
            if (execution.getStatus() != ExecutionStatus.WAITING) {

                execution.setStatus(
                        ExecutionStatus.SUCCESS
                );
            }


        } catch (Exception e) {
            execution.setStatus(ExecutionStatus.FAILED);
            execution.setErrorMessage(e.getMessage());
            throw e;

        } finally {
            executionRepository.save(execution);
        }
    }


    private void executeNode(JsonNode currentNode,
                             JsonNode nodes,
                             JsonNode edges,
                             WorkflowContext context,
                             UUID executionId) {

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
            log.info("Thread={} Executing nodeType={} nodeId={}",
                    Thread.currentThread().getName(), type, nodeId);

            //  Strategy Pattern used here (Execute current node)
            TaskExecutor executor = executorFactory.getExecutor(type); //TRIGGER, ACTION, WAIT etc...
            executor.execute(currentNode, context);

            // WAIT nodes pause workflow execution
            if ("WAIT".equals(type)) {
                pauseExecution(
                        executionId,
                        currentNode,
                        context
                );
                return;
            }


            task.setStatus(ExecutionStatus.SUCCESS);
            task.setLogMessage("Executed successfully");


            task.setOutputData(context.getData());  //Every node stores its data

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
                if ("DECISION".equals(type) || "RULE".equals(type)) {

                    boolean decision;
                    if ("DECISION".equals(type)) {
                        decision = (boolean) context.get("decisionResult");
                    } else {
                        decision = (boolean) context.get("ruleResult");
                    }


                    JsonNode conditionNode = edge.get("condition");

                    // ❗ skip edges without condition
                    if (conditionNode == null) continue;

                    String condition = conditionNode.asText();

                    if (String.valueOf(decision).equals(condition)) {

                        String nextId = edge.get("to").asText();
                        nextNodes.add(findNodeById(nodes, nextId));

                        //log
                        log.info("RULE/DECISION matched -> node={}, decision={}, edgeCondition={}, nextNode={}",
                                nodeId,
                                decision,
                                condition,
                                edge.get("to").asText());
                    }

                } else {
                    // 🔥 NORMAL FLOW (no condition required, ALL edges allowed)

                    String nextId = edge.get("to").asText();
                    //log
                    log.info("Adding next node -> {}", nextId);
                    nextNodes.add(findNodeById(nodes, nextId));
                }
            }
        }

        log.info(
                "Node={} discovered {} next nodes",
                nodeId,
                nextNodes.size()
        );

        // 🔥 PARALLEL EXECUTION {Execute → find ALL next nodes → run in parallel}
        List<Future<?>> futures = new ArrayList<>(); //Future = a handle/reference to an async task [Helps you: wait for completion,get result,catch exceptions]

        for (JsonNode nextNode : nextNodes) { //submit tasks in parallel
            futures.add(executorService.submit(() ->  //(.submit):sends task to thread pool,Tasks run concurrently (max 5 at a time)
                    executeNode(nextNode, nodes, edges, context, executionId)
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


    @Override
    public UUID triggerWorkflow(UUID workflowId, Map<String, Object> input) {

        WorkflowExecution execution = createExecution(workflowId);

        WorkflowExecutionEvent event = new WorkflowExecutionEvent(
                workflowId,
                execution.getId(),
                input
        );

        producer.sendExecutionEvent(event);

        return execution.getId();
    }


    public void resumeExecution(UUID executionId) {

        //  STEP 1: FETCH EXECUTION RECORD [Get existing workflow execution (must exist to resume)
        WorkflowExecution execution = executionRepository
                .findById(executionId)
                .orElseThrow(() -> new ResourceNotFoundException("Execution not found"));

        //  STEP 2: EXTRACT WORKFLOW ID [Required to fetch latest workflow definition
        UUID workflowId = execution.getWorkflowId();

        try {
            log.info("Resuming workflow → executionId={}", executionId);

            // STEP 3: MARK EXECUTION AS RUNNING [Important: update status before resuming execution
            execution.setStatus(ExecutionStatus.RUNNING);
            executionRepository.save(execution);

            // STEP 4: LOAD LATEST WORKFLOW DEFINITION [Always resume using latest version of DAG
            WorkflowDefinition definition = definitionRepository
                    .findTopByWorkflowIdOrderByVersionDesc(workflowId)
                    .orElseThrow(() -> new ResourceNotFoundException("Definition not found"));

            JsonNode json = definition.getDefinitionJson();
            JsonNode nodes = json.get("nodes");
            JsonNode edges = json.get("edges");

            // STEP 5: FETCH TASK EXECUTION HISTORY [Used to identify where execution failed
            List<TaskExecution> tasks =
                    taskExecutionRepository.findByWorkflowExecutionIdOrderByCreatedAt(executionId);

            // STEP 6: Restore latest workflow context
            Map<String, Object> lastContext = tasks.stream()
                    .filter(t -> t.getOutputData() != null)
                    .reduce((first, second) -> second)
                    .map(TaskExecution::getOutputData)
                    .orElse(new HashMap<>());

            WorkflowContext context = new WorkflowContext(new HashMap<>(lastContext));

            JsonNode resumeNode;

            // WAITING workflow
            if (execution.getWaitingNodeId() != null) {

                log.info(
                        "Resuming WAIT workflow from node={}",
                        execution.getWaitingNodeId()
                );

                JsonNode waitingNode = findNodeById(nodes, execution.getWaitingNodeId());

                // find next node after WAIT
                String nextNodeId = null;

                for (JsonNode edge : edges) {

                    if (edge.get("from").asText()
                            .equals(waitingNode.get("id").asText())) {

                        nextNodeId = edge.get("to").asText();
                        break;
                    }
                }

                if (nextNodeId == null) {
                    throw new ResourceNotFoundException(
                            "No next node found after WAIT node"
                    );
                }

                resumeNode = findNodeById(nodes, nextNodeId);

                execution.setWaitingNodeId(null);
                execution.setResumeAt(null);

            } else {

                // STEP 6: FIND LAST FAILED NODE  [We resume from the most recent FAILED task (not from start)
                TaskExecution failedTask = tasks.stream()
                        .filter(t -> t.getStatus() == ExecutionStatus.FAILED)
                        .reduce((first, second) -> second)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "No failed task found"
                                ));

                // STEP 7: LOCATE FAILED NODE IN DAG
                resumeNode = findNodeById(nodes, failedTask.getNodeId());
            }

            // STEP 9: RESUME EXECUTION FROM FAILED/WAITING NODE [This will continue normal traversal (including parallel flow)
            executeNode(
                    resumeNode,
                    nodes,
                    edges,
                    context,
                    executionId
            );

            //  STEP 10: MARK EXECUTION AS SUCCESS [If no exception occurs, workflow completed successfully
            execution.setStatus(ExecutionStatus.SUCCESS);

        } catch (Exception e) {

            //  STEP 11: HANDLE FAILURE DURING RESUME [If resume fails again → mark execution as FAILED
            execution.setStatus(ExecutionStatus.FAILED);
            execution.setErrorMessage(e.getMessage());

            // IMPORTANT: rethrow to trigger Kafka retry / DLQ if needed
            throw e;

        } finally {

            // STEP 12: PERSIST FINAL STATE  [Ensures DB always reflects latest execution status
            executionRepository.save(execution);
        }
    }


    private WorkflowExecution createExecution(UUID workflowId) {
        WorkflowExecution execution = new WorkflowExecution();
        execution.setWorkflowId(workflowId);
        execution.setStatus(ExecutionStatus.RUNNING);
        return executionRepository.save(execution);
    }


    private JsonNode findNodeById(JsonNode nodes, String id) {
        for (JsonNode node : nodes) {
            if (node.get("id").asText().equals(id)) {
                return node;
            }
        }
        throw new RuntimeException("Node not found: " + id);
    }


    private void pauseExecution(
            UUID executionId,
            JsonNode waitNode,
            WorkflowContext context) {

        WorkflowExecution execution =
                executionRepository
                        .findById(executionId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Execution not found"
                                ));

        String duration =
                (String) context.get("waitDuration");

        LocalDateTime resumeAt =
                LocalDateTime.now()
                        .plusSeconds(
                                parseSeconds(duration)
                        );

        execution.setStatus(
                ExecutionStatus.WAITING
        );

        execution.setResumeAt(resumeAt);

        execution.setWaitingNodeId(
                waitNode.get("id").asText()
        );

        executionRepository.save(execution);

        log.info(
                "Workflow paused -> executionId={}, resumeAt={}",
                executionId,
                resumeAt
        );
    }

    //helper
    private long parseSeconds(String duration) {

        if (duration.endsWith("s")) {

            return Long.parseLong(
                    duration.replace("s", "")
            );
        }

        throw new WorkflowValidationException(
                "Unsupported WAIT duration: "
                        + duration
        );
    }


    //Get Execution Details
    @Override
    public WorkflowExecutionResponse getExecution(UUID executionId) {

        WorkflowExecution execution = executionRepository.findById(executionId)
                        .orElseThrow(() -> new ResourceNotFoundException("Execution not found"));

        return new WorkflowExecutionResponse(
                execution.getId(),
                execution.getWorkflowId(),
                execution.getStatus().name(),
                execution.getErrorMessage(),
                execution.getCreatedAt(),
                execution.getUpdatedAt()
        );
    }

}
