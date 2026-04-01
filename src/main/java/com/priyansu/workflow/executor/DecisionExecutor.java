package com.priyansu.workflow.executor;

import com.fasterxml.jackson.databind.JsonNode;
import com.priyansu.workflow.execution.WorkflowContext;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class DecisionExecutor implements TaskExecutor {  // WHAT THIS DOES: Reads condition, Evaluates it, Stores result in context


    @Override
    public String getType() {
        return "DECISION";
    }

    @Override
    public void execute(JsonNode node, WorkflowContext context) {

        JsonNode config = node.get("config");

        String field = config.get("field").asText();
        String operator = config.get("operator").asText();
        int value = config.get("value").asInt();

        Object contextValue = context.get(field);

        boolean result = false;

        if (contextValue instanceof Integer) {
            int actual = (Integer) contextValue;

            switch (operator) {
                case ">":
                    result = actual > value;
                    break;
                case "<":
                    result = actual < value;
                    break;
                case "==":
                    result = actual == value;
                    break;
            }
        }


        System.out.println("Actual: " + contextValue + " Operator: " + operator + " Value: " + value);

        System.out.println("Decision result: " + result);

        // Store result in context
        context.put("decisionResult", result);
    }
}
