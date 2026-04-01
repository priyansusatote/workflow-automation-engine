package com.priyansu.workflow.executor;

import com.fasterxml.jackson.databind.JsonNode;
import com.priyansu.workflow.execution.WorkflowContext;
import org.springframework.stereotype.Component;

@Component
public class ActionExecutor implements TaskExecutor {

    @Override
    public String getType() {
        return "ACTION";
    }

    @Override
    public void execute(JsonNode node, WorkflowContext context) {
        System.out.println("Action executed");

        context.put("actionDone", true);
    }
}
