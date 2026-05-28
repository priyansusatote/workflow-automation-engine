package com.priyansu.workflow.service.Impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.priyansu.workflow.exception.WorkflowValidationException;
import com.priyansu.workflow.service.WorkflowValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowValidationServiceImpl implements WorkflowValidationService {


    @Override
    public void validate(JsonNode workflow) {

        //1: Check Nodes/Edges Exists
        JsonNode nodes = workflow.get("nodes");
        JsonNode edges = workflow.get("edges");

        if (nodes == null || !nodes.isArray()) {
            throw new WorkflowValidationException("workflow missing nodes array");
        }
        if (edges == null || !edges.isArray()) {
            throw new WorkflowValidationException("workflow missing edges array");
        }

        //2: Unique Nodes Ids
        Set<String> nodeIds = new HashSet<>();
        for (JsonNode node : nodes) {
            String id = node.get("id").asText();

            if (!nodeIds.add(id)) {
                throw new WorkflowValidationException(
                        "Duplicate node id: " + id
                );
            }
        }

        //3: VALIDATE NODE TYPES
        Set<String> supportedTypes = Set.of(
                "TRIGGER",
                "ACTION",
                "AI_GENERATE",
                "AI_DECISION",
                "WAIT"
        );
        //VALIDATE
        for (JsonNode node : nodes) {
            String type = node.get("type").asText();

            if (!supportedTypes.contains(type)) {
                throw new WorkflowValidationException(
                        "Unsupported node type: " + type
                );
            }
        }

        //4:VALIDATE EDGE REFERENCES
        for (JsonNode edge : edges) {

            String from = edge.get("from").asText();
            String to = edge.get("to").asText();

            if (!nodeIds.contains(from)) {
                throw new WorkflowValidationException(
                        "Edge references missing node: " + from
                );
            }

            if (!nodeIds.contains(to)) {
                throw new WorkflowValidationException(
                        "Edge references missing node: " + to
                );
            }
        }


        //5:VALIDATE TRIGGER EXISTS
        boolean hasTrigger = false;

        for (JsonNode node : nodes) {
            String type = node.get("type").asText();
            if ("TRIGGER".equals(type)) {
                hasTrigger = true;
            }
        }
        if (!hasTrigger) {
            throw new WorkflowValidationException(
                    "Workflow must contain TRIGGER node"
            );
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
                    throw new WorkflowValidationException(
                            "AI_DECISION node must have exactly 2 outgoing edges"
                    );
                }

                // Validate conditions
                Set<String> conditions = new HashSet<>();

                for (JsonNode edge : outgoing) {
                    JsonNode conditionNode = edge.get("condition");

                    if (conditionNode == null) {
                        throw new WorkflowValidationException(
                                "Decision edge missing condition"
                        );
                    }

                    conditions.add(conditionNode.asText());
                }

                //Final check: Must contain true and false
                if (!conditions.contains("true")
                        || !conditions.contains("false")) {

                    throw new WorkflowValidationException(
                            "Decision node must contain true/false edges"
                    );
                }


            }
        }


    }
}
