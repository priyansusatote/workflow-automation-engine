package com.priyansu.workflow.dto;


import java.util.List;
import java.util.Map;

public record WorkflowDefinitionRequest(

        List<Node> nodes,
        List<Edge> edges

) {

    public record Node(
            String id,
            String type,
            Map<String, Object> config
    ) {
    }

    public record Edge(
            String from,
            String to
    ) {
    }
}
