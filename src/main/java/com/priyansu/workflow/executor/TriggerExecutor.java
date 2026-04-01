package com.priyansu.workflow.executor;

import com.fasterxml.jackson.databind.JsonNode;
import com.priyansu.workflow.execution.WorkflowContext;
import org.springframework.stereotype.Component;

@Component
public class TriggerExecutor implements TaskExecutor {


    @Override
    public String getType() {
        return "TRIGGER";
    }

    @Override
    public void execute(JsonNode node, WorkflowContext context) {
        System.out.println("Trigger executed");

        context.put("started", true);

        //temporary
        context.put("amount", 15000);
    }
}
