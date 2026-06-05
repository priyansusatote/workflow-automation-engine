package com.priyansu.workflow.executor;

import com.fasterxml.jackson.databind.JsonNode;
import com.priyansu.workflow.exception.WorkflowValidationException;
import com.priyansu.workflow.execution.WorkflowContext;
import com.priyansu.workflow.service.ai.PromptTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class RuleTaskExecutor implements TaskExecutor {

    private final PromptTemplateService promptTemplateService;

    @Override
    public String getType() {
        return "RULE";
    }

    @Override
    public void execute(JsonNode node, WorkflowContext context) {

        // 1. Read Config
        JsonNode config = node.get("config");

        if (config == null || config.isNull()) {
            throw new WorkflowValidationException(
                    "RULE node missing config"
            );
        }

        // 2. Read Expression
        JsonNode expressionNode = config.get("expression");

        if (expressionNode == null || expressionNode.isNull()) {
            throw new WorkflowValidationException(
                    "RULE node missing expression"
            );
        }

        String expression = expressionNode.asText();

        // 3. Render
        String rendered =
                promptTemplateService.render(expression, context);

        log.info("RULE expression rendered -> {}", rendered);

        // Example:
        // 15000 > 10000

        boolean result;

        try {

            if (rendered.contains(">")) { //for phase 1: supported only : ">" later support all

                String[] parts = rendered.split(">");

                double left =
                        Double.parseDouble(parts[0].trim());

                double right =
                        Double.parseDouble(parts[1].trim());

                result = left > right;

            } else {
                throw new WorkflowValidationException(
                        "Unsupported operator in RULE expression: "
                                + rendered
                );
            }

        } catch (NumberFormatException e) {

            throw new WorkflowValidationException(
                    "Invalid RULE expression: " + rendered
            );
        }

        // 4. Store Result

        context.put("ruleResult", result);

        context.put(
                node.get("id").asText() + "_result",
                Map.of(
                        "result", result,
                        "expression", rendered
                )
        );

        log.info(
                "RULE evaluated -> expression={}, result={}",
                rendered,
                result
        );
    }
}