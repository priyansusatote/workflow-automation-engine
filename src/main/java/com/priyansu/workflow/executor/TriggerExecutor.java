package com.priyansu.workflow.executor;

import com.fasterxml.jackson.databind.JsonNode;
import com.priyansu.workflow.execution.WorkflowContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TriggerExecutor implements TaskExecutor {


    @Override
    public String getType() {
        return "TRIGGER";
    }

    @Override
    public void execute(JsonNode node, WorkflowContext context) {
        log.info("Trigger executed");

        context.put("started", true);

    }
}
