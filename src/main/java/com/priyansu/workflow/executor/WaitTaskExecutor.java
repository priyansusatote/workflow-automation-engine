package com.priyansu.workflow.executor;

import com.fasterxml.jackson.databind.JsonNode;
import com.priyansu.workflow.exception.WorkflowValidationException;
import com.priyansu.workflow.execution.WorkflowContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class WaitTaskExecutor implements TaskExecutor {

    @Override
    public String getType() {
        return "WAIT";
    }

    @Override
    public void execute(JsonNode node, WorkflowContext context) {

        JsonNode config = node.get("config");

        if (config == null || config.isNull()) {
            throw new WorkflowValidationException(
                    "WAIT node missing config"
            );
        }

        String duration = config.get("duration").asText();

        long millis = parseDuration(duration);

        log.info(
                "WAIT started -> duration={}",
                duration
        );

        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new RuntimeException(
                    "WAIT interrupted"
            );
        }

        context.put(
                node.get("id").asText() + "_result",
                Map.of(
                        "waited", duration
                )
        );

        log.info(
                "WAIT completed -> duration={}",
                duration
        );
    }


    private long parseDuration(String duration) {

        if (duration.endsWith("s")) {

            return Long.parseLong(
                    duration.replace("s", "")
            ) * 1000;

        }

        throw new WorkflowValidationException(
                "Unsupported WAIT duration: "
                        + duration
        );
    }
}