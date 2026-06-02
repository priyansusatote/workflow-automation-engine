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
    public List<String> validate(JsonNode workflow) {

        //store all validation errors [helpful to send back to AI and AI corrects it]
        List<String> errors = new ArrayList<>();

        //1: Check Nodes/Edges Exists
        JsonNode nodes = workflow.get("nodes");
        JsonNode edges = workflow.get("edges");

        if (nodes == null || !nodes.isArray()) {
            errors.add("workflow missing nodes array");
        }
        if (edges == null || !edges.isArray()) {
            errors.add("workflow missing edges array");
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
                "ACTION",
                "AI_GENERATE",
                "AI_DECISION",
                "WAIT",
                "AI_EXTRACT"
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


        //5:VALIDATE TRIGGER EXISTS
        boolean hasTrigger = false;

        for (JsonNode node : nodes) {
            String type = node.get("type").asText();
            if ("TRIGGER".equals(type)) {
                hasTrigger = true;
            }
        }
        if (!hasTrigger) {
            errors.add(
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
                    errors.add(
                            "AI_DECISION node must have exactly 2 outgoing edges"
                    );
                }

                // Validate conditions
                Set<String> conditions = new HashSet<>();

                for (JsonNode edge : outgoing) {
                    JsonNode conditionNode = edge.get("condition");

                    if (conditionNode == null) {
                        errors.add(
                                "Decision edge missing condition"
                        );
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

        return errors;
    }
}
