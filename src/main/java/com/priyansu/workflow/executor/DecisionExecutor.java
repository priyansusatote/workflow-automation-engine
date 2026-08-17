package com.priyansu.workflow.executor;

import com.fasterxml.jackson.databind.JsonNode;
import com.priyansu.workflow.exception.WorkflowValidationException;
import com.priyansu.workflow.execution.WorkflowContext;
import org.springframework.stereotype.Component;

@Component
public class DecisionExecutor implements TaskExecutor {  // WHAT THIS DOES: Reads condition, Evaluates it, Stores result in context


    @Override
    public String getType() {
        return "DECISION";
    }

    @Override
    public void execute(JsonNode node, WorkflowContext context) {

        JsonNode config = node.get("config");

        if (config == null || config.isNull()) {
            throw new WorkflowValidationException("DECISION node missing config");
        }

        JsonNode fieldNode = config.get("field");
        JsonNode operatorNode = config.get("operator");
        JsonNode valueNode = config.get("value");

        if (fieldNode == null || fieldNode.isNull() || fieldNode.asText().isBlank()) {
            throw new WorkflowValidationException("DECISION node missing field");
        }
        if (operatorNode == null || operatorNode.isNull() || operatorNode.asText().isBlank()) {
            throw new WorkflowValidationException("DECISION node missing operator");
        }
        if (valueNode == null || valueNode.isNull() || !valueNode.isNumber()) {
            throw new WorkflowValidationException("DECISION node missing numeric value");
        }

        String field = fieldNode.asText();
        String operator = operatorNode.asText();
        int value = valueNode.asInt();

        if (!operator.equals(">") && !operator.equals("<") && !operator.equals("==")) {
            throw new WorkflowValidationException("DECISION node unsupported operator: " + operator);
        }

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
