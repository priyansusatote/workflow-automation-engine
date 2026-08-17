package com.priyansu.workflow.executor;

import com.fasterxml.jackson.databind.JsonNode;
import com.priyansu.workflow.exception.WorkflowValidationException;
import com.priyansu.workflow.execution.WorkflowContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class ActionExecutor implements TaskExecutor {

    @Override
    public String getType() {
        return "ACTION";
    }

    @Override
    public void execute(JsonNode node, WorkflowContext context) {
        log.info("Action executed");


        //save to Context
        JsonNode config = node.get("config");
        if (config == null || config.isNull()) {
            throw new WorkflowValidationException("ACTION node missing config");
        }

        JsonNode actionTypeNode = config.get("actionType");
        if (actionTypeNode == null || actionTypeNode.isNull() || actionTypeNode.asText().isBlank()) {
            throw new WorkflowValidationException("ACTION node missing actionType");
        }

        String actionType = actionTypeNode.asText();

        context.put(
                node.get("id").asText() + "_result",
                Map.of(
                        "actionType", actionType,
                        "executed", true
                )
        );
    }
}
