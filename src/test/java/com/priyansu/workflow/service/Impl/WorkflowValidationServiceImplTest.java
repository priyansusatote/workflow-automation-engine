package com.priyansu.workflow.service.Impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WorkflowValidationServiceImplTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final WorkflowValidationServiceImpl validationService = new WorkflowValidationServiceImpl();

    @Test
    @DisplayName("Should accept a connected workflow with one trigger and valid node configuration")
    void shouldAcceptValidWorkflow() throws Exception {
        JsonNode workflow = objectMapper.readTree("""
                {
                  "nodes": [
                    {"id":"start", "type":"TRIGGER", "config":{}},
                    {"id":"action", "type":"ACTION", "config":{"actionType":"EMAIL"}}
                  ],
                  "edges": [{"from":"start", "to":"action"}]
                }
                """);

        List<String> errors = validationService.validate(workflow);

        assertThat(errors).isEmpty();
    }

    @Test
    @DisplayName("Should report missing nodes array without throwing")
    void shouldReportMissingNodesArray() throws Exception {
        List<String> errors = validationService.validate(objectMapper.readTree("{\"edges\": []}"));

        assertThat(errors).contains("workflow missing nodes array");
    }

    @Test
    @DisplayName("Should report null nodes array without throwing")
    void shouldReportNullNodesArray() throws Exception {
        List<String> errors = validationService.validate(objectMapper.readTree("{\"nodes\": null, \"edges\": []}"));

        assertThat(errors).contains("workflow missing nodes array");
    }

    @Test
    @DisplayName("Should report missing edges array without throwing")
    void shouldReportMissingEdgesArray() throws Exception {
        List<String> errors = validationService.validate(objectMapper.readTree("{\"nodes\": []}"));

        assertThat(errors).contains("workflow missing edges array");
    }

    @Test
    @DisplayName("Should report null edges array without throwing")
    void shouldReportNullEdgesArray() throws Exception {
        List<String> errors = validationService.validate(objectMapper.readTree("{\"nodes\": [], \"edges\": null}"));

        assertThat(errors).contains("workflow missing edges array");
    }

    @Test
    @DisplayName("Should report both nodes and edges when both arrays are missing")
    void shouldReportBothMissingArrays() throws Exception {
        List<String> errors = validationService.validate(objectMapper.readTree("{}"));

        assertThat(errors).contains(
                "workflow missing nodes array",
                "workflow missing edges array"
        );
    }

    @Test
    @DisplayName("Should report both nodes and edges when both arrays are null")
    void shouldReportBothNullArrays() throws Exception {
        List<String> errors = validationService.validate(objectMapper.readTree("{\"nodes\": null, \"edges\": null}"));

        assertThat(errors).contains(
                "workflow missing nodes array",
                "workflow missing edges array"
        );
    }

    @Test
    @DisplayName("Should report duplicate nodes, unsupported types, missing edge targets, and unreachable nodes")
    void shouldReportGraphErrors() throws Exception {
        JsonNode workflow = objectMapper.readTree("""
                {
                  "nodes": [
                    {"id":"start", "type":"TRIGGER", "config":{}},
                    {"id":"start", "type":"UNKNOWN", "config":{}},
                    {"id":"orphan", "type":"WAIT", "config":{"duration":"5s"}}
                  ],
                  "edges": []
                }
                """);

        List<String> errors = validationService.validate(workflow);

        assertThat(errors).contains(
                "Duplicate node id: start",
                "Unsupported node type: UNKNOWN",
                "Unreachable node: orphan"
        );
    }

    @Test
    @DisplayName("Should report an unknown edge source node")
    void shouldReportUnknownEdgeSource() throws Exception {
        JsonNode workflow = objectMapper.readTree("""
                {
                  "nodes": [
                    {"id":"start", "type":"TRIGGER", "config":{}},
                    {"id":"action", "type":"ACTION", "config":{"actionType":"EMAIL"}}
                  ],
                  "edges": [{"from":"unknown", "to":"action"}]
                }
                """);

        List<String> errors = validationService.validate(workflow);

        assertThat(errors).contains("Edge references missing node: unknown");
    }

    @Test
    @DisplayName("Should report an unknown edge target node")
    void shouldReportUnknownEdgeTarget() throws Exception {
        JsonNode workflow = objectMapper.readTree("""
                {
                  "nodes": [
                    {"id":"start", "type":"TRIGGER", "config":{}},
                    {"id":"action", "type":"ACTION", "config":{"actionType":"EMAIL"}}
                  ],
                  "edges": [{"from":"start", "to":"unknown"}]
                }
                """);

        List<String> errors = validationService.validate(workflow);

        assertThat(errors).contains("Edge references missing node: unknown");
    }

    @Test
    @DisplayName("Should accept an edge when both endpoint nodes exist")
    void shouldAcceptValidEdgeEndpoints() throws Exception {
        JsonNode workflow = objectMapper.readTree("""
                {
                  "nodes": [
                    {"id":"start", "type":"TRIGGER", "config":{}},
                    {"id":"action", "type":"ACTION", "config":{"actionType":"EMAIL"}}
                  ],
                  "edges": [{"from":"start", "to":"action"}]
                }
                """);

        List<String> errors = validationService.validate(workflow);

        assertThat(errors).isEmpty();
    }

    @Test
    @DisplayName("Should report missing required configuration and invalid decision edges")
    void shouldReportConfigurationErrors() throws Exception {
        JsonNode workflow = objectMapper.readTree("""
                {
                  "nodes": [
                    {"id":"start", "type":"TRIGGER", "config":{}},
                    {"id":"decision", "type":"AI_DECISION", "config":{}}
                  ],
                  "edges": [{"from":"start", "to":"decision"}]
                }
                """);

        List<String> errors = validationService.validate(workflow);

        assertThat(errors).contains(
                "AI_DECISION missing config.prompt",
                "AI_DECISION node must have exactly 2 outgoing edges",
                "Decision node must contain true/false edges"
        );
    }

    @Test
    @DisplayName("Should report duplicate edges and cycles")
    void shouldReportDuplicateEdgesAndCycles() throws Exception {
        JsonNode workflow = objectMapper.readTree("""
                {
                  "nodes": [
                    {"id":"start", "type":"TRIGGER", "config":{}},
                    {"id":"action", "type":"ACTION", "config":{"actionType":"EMAIL"}}
                  ],
                  "edges": [
                    {"from":"start", "to":"action"},
                    {"from":"start", "to":"action"},
                    {"from":"action", "to":"start"}
                  ]
                }
                """);

        List<String> errors = validationService.validate(workflow);

        assertThat(errors).contains("Duplicate edge: start->action")
                .anyMatch(error -> error.startsWith("Workflow contains cycle involving node:"));
    }
}
