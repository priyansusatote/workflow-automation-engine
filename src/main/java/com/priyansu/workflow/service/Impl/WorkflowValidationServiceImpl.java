package com.priyansu.workflow.service.Impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.priyansu.workflow.exception.WorkflowValidationException;
import com.priyansu.workflow.service.WorkflowValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowValidationServiceImpl implements WorkflowValidationService {


    @Override
    public List<String> validate(JsonNode workflow) {

        //store all validation errors [helpful to send back to AI and AI corrects it]
        List<String> errors = new ArrayList<>();

        //1: Check Nodes/Edges Exists
        JsonNode nodes = workflow.get("nodes");
        JsonNode edges = workflow.get("edges");

        if (nodes == null || !nodes.isArray()) {
            errors.add("workflow missing nodes array");
            nodes = JsonNodeFactory.instance.arrayNode();
        }
        if (edges == null || !edges.isArray()) {
            errors.add("workflow missing edges array");
            edges = JsonNodeFactory.instance.arrayNode();
        }

        //2: Unique Nodes Ids
        Set<String> nodeIds = new HashSet<>();
        for (JsonNode node : nodes) {
            String id = node.get("id").asText();

            if (!nodeIds.add(id)) {
                errors.add(
                        "Duplicate node id: " + id
                );
            }
        }

        //3: VALIDATE NODE TYPES
        Set<String> supportedTypes = Set.of(
                "TRIGGER",
                "WEBHOOK_TRIGGER",

                "ACTION",
                "HTTP_ACTION",

                "AI_GENERATE",
                "AI_DECISION",
                "AI_EXTRACT",
                "AI_CLASSIFY",

                "RULE",
                "WAIT"
        );
        //VALIDATE
        for (JsonNode node : nodes) {
            String type = node.get("type").asText();

            if (!supportedTypes.contains(type)) {
                errors.add(
                        "Unsupported node type: " + type
                );
            }
        }

        //4:VALIDATE EDGE REFERENCES
        for (JsonNode edge : edges) {

            String from = edge.get("from").asText();
            String to = edge.get("to").asText();

            if (!nodeIds.contains(from)) {
                errors.add(
                        "Edge references missing node: " + from
                );
            }

            if (!nodeIds.contains(to)) {
                errors.add(
                        "Edge references missing node: " + to
                );
            }
        }
        //4.1 Duplicate Edge Validation
        Set<String> edgeKeys = new HashSet<>();

        for (JsonNode edge : edges) {
            String key = edge.get("from").asText()
                    + "->"
                    + edge.get("to").asText();

            if (!edgeKeys.add(key)) {
                errors.add("Duplicate edge: " + key);
            }
        }



        //5: VALIDATE EXACTLY ONE TRIGGER
        int triggerCount = 0;

        for (JsonNode node : nodes) {
            String type = node.get("type").asText();
            if ("TRIGGER".equals(type)
                    || "WEBHOOK_TRIGGER".equals(type)) {
                triggerCount++;
            }
        }

        if (triggerCount != 1) {
            errors.add("Workflow must contain exactly one trigger node");
        }

        //5.1 : Reachability Validation (No Orphan Nodes)
        //Step1: Find Trigger ID
        String triggerId = null;

        for (JsonNode node : nodes) {

            String type = node.get("type").asText();

            if ("TRIGGER".equals(type) || "WEBHOOK_TRIGGER".equals(type)) {

                triggerId = node.get("id").asText();
                break;
            }
        }
       // Step 2 — DFS/BFS Setup
        Set<String> visited = new HashSet<>();

        Queue<String> queue = new LinkedList<>();

        if (triggerId != null) {
            queue.add(triggerId);
            visited.add(triggerId);
        }
        //Step 3: Traverse Graph
        while (!queue.isEmpty()) {

            String current = queue.poll();

            for (JsonNode edge : edges) {
                String from = edge.get("from").asText();
                String to = edge.get("to").asText();

                if (nodeIds.contains(from)
                        && nodeIds.contains(to)
                        && from.equals(current)
                        && !visited.contains(to)) {
                    visited.add(to);
                    queue.add(to);
                }
            }
        }
        //Step 4: Check all Nodes
        for (JsonNode node : nodes) {

            String nodeId = node.get("id").asText();

            if (!visited.contains(nodeId)) {
                errors.add("Unreachable node: " + nodeId);
            }
        }




        //6: VALIDATE REQUIRED CONFIGS
        for (JsonNode node : nodes) {

            String type = node.get("type").asText();
            JsonNode config = node.get("config");

            switch (type) {
                case "AI_GENERATE" -> {
                    if (config == null
                            || config.get("prompt") == null) {
                        errors.add("AI_GENERATE missing config.prompt");
                    }
                }

                case "AI_DECISION" -> {
                    if (config == null || config.get("prompt") == null) {
                        errors.add("AI_DECISION missing config.prompt");
                    }
                }

                case "AI_EXTRACT" -> {
                    if (config == null || config.get("prompt") == null || config.get("schema") == null) {
                        errors.add("AI_EXTRACT missing prompt or schema");
                    }
                }

                case "AI_CLASSIFY" -> {
                    if (config == null || config.get("prompt") == null || config.get("labels") == null) {
                        errors.add("AI_CLASSIFY missing prompt or labels");
                    }
                }
                case "ACTION" -> {
                    if (config == null || config.get("actionType") == null) {

                        errors.add("ACTION missing actionType");
                    }
                }

                case "RULE" -> {
                    if (config == null || config.get("expression") == null) {
                        errors.add("RULE missing expression"
                        );
                    }
                }

                case "WAIT" -> {
                    if (config == null || config.get("duration") == null) {
                        errors.add("WAIT missing duration");
                    }
                }

                case "HTTP_ACTION" -> {
                    if (config == null || config.get("method") == null || config.get("url") == null) {
                        errors.add("HTTP_ACTION missing method or url");
                    }
                }
            }
        }

        //6:VALIDATE AI_DECISION EDGES
        for (JsonNode node : nodes) {
            String type = node.get("type").asText();
            String id = node.get("id").asText();

            //validate AI_DECISION nodes
            if ("AI_DECISION".equals(type)) {

                //find outgoing edges
                List<JsonNode> outgoing = new ArrayList<>();
                for (JsonNode edge : edges) {

                    if (edge.get("from").asText().equals(id)) {
                        outgoing.add(edge);
                    }
                }

                // Must have exactly 2 outgoing edges [one for true, and one for false]
                if (outgoing.size() != 2) {
                    errors.add("AI_DECISION node must have exactly 2 outgoing edges");
                }

                // Validate conditions
                Set<String> conditions = new HashSet<>();

                for (JsonNode edge : outgoing) {
                    JsonNode conditionNode = edge.get("condition");

                    if (conditionNode == null || conditionNode.isNull()) {
                        errors.add("Decision edge missing condition");
                        continue;
                    }

                    conditions.add(conditionNode.asText());
                }

                //Final check: Must contain true and false
                if (!conditions.contains("true")
                        || !conditions.contains("false")) {

                    errors.add(
                            "Decision node must contain true/false edges"
                    );
                }


            }
        }
        //Cycle Detection Check

        //s1 : Build Adjacency List
        Map<String, List<String>> graph = new HashMap<>();

        for (String nodeId : nodeIds) {
            graph.put(nodeId, new ArrayList<>());
        }

        for (JsonNode edge : edges) {
            String from = edge.get("from").asText();
            String to = edge.get("to").asText();

            if (nodeIds.contains(from) && nodeIds.contains(to)) {
                graph.get(from).add(to);
            }
        }

        //S2 : DFS Sets
        Set<String> Visited2 = new HashSet<>();
        Set<String> recursionStack = new HashSet<>();

        //S3: Run DFS From Every Node
        for (String nodeId : nodeIds) {
            if (hasCycle(
                    nodeId,
                    graph,
                    Visited2,
                    recursionStack
            )) {
                errors.add("Workflow contains cycle involving node: " + nodeId);
                break;
            }
        }


        return errors;
    }





    //Helper
    private boolean hasCycle(
            String node,
            Map<String, List<String>> graph,
            Set<String> visited,
            Set<String> recursionStack
    ) {

        if (recursionStack.contains(node)) { //If current node is already in current DFS path: found Cycle
            return true;
        }

        if (visited.contains(node)) { //We already checked this node earlier and know it's safe. no need to explore Again
            return false;
        }

        visited.add(node);

        recursionStack.add(node);

        for (String neighbor : graph.get(node)) {

            if (hasCycle(
                    neighbor,
                    graph,
                    visited,
                    recursionStack
            )) {
                return true;
            }
        }

        recursionStack.remove(node);

        return false;
    }

}



