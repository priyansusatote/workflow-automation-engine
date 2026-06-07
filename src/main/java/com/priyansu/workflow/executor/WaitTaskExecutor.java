package com.priyansu.workflow.executor;

import com.fasterxml.jackson.databind.JsonNode;
import com.priyansu.workflow.exception.WorkflowValidationException;
import com.priyansu.workflow.execution.WorkflowContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class WaitTaskExecutor implements TaskExecutor { //WAIT executor should only: Validate, Read duration ,Store duration Only

    @Override
    public String getType() {
        return "WAIT";
    }

    //updated phase-2 code: WAIT Executor →  Store duration → WorkflowExecutionService pauses workflow →  Scheduler resumes later
    @Override
    public void execute(JsonNode node, WorkflowContext context) {

        JsonNode config = node.get("config");

        if (config == null || config.isNull()) {
            throw new WorkflowValidationException(
                    "WAIT node missing config"
            );
        }

        JsonNode durationNode = config.get("duration");

        if (durationNode == null || durationNode.isNull()) {
            throw new WorkflowValidationException(
                    "WAIT node missing duration"
            );
        }

        String duration = durationNode.asText();

        log.info(
                "WAIT node reached -> duration={}",
                duration
        );

        // Store duration in context.
        // WorkflowExecutionService will pause execution.
        context.put(
                "waitDuration",
                duration
        );

        context.put(
                node.get("id").asText() + "_result",
                Map.of(
                        "waitDuration", duration
                )
        );
    }


}