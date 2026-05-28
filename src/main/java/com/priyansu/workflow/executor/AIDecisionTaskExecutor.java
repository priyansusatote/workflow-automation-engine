package com.priyansu.workflow.executor;

import com.fasterxml.jackson.databind.JsonNode;
import com.priyansu.workflow.dto.ai.AIRequest;
import com.priyansu.workflow.dto.ai.AIResponse;
import com.priyansu.workflow.exception.WorkflowValidationException;
import com.priyansu.workflow.execution.WorkflowContext;
import com.priyansu.workflow.service.AIService;
import com.priyansu.workflow.service.ai.PromptTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AIDecisionTaskExecutor implements TaskExecutor {

    private final AIService aiService;
    private final PromptTemplateService promptTemplateService;

    @Override
    public String getType() {
        return "AI_DECISION";
    }

    @Override
    public void execute(JsonNode node, WorkflowContext context) { //AI decision in True/False ->routes dynamically

        //STEP 1 — READ CONFIG
        JsonNode config = node.get("config");

        if(config == null || config.isNull()) {
            throw new WorkflowValidationException("AI_DECISION node missing config");
        }

        //STEP 2 — READ PROMPT TEMPLATE (prompt is inside config)
        JsonNode promptNode = config.get("prompt");
        if(promptNode == null || promptNode.isNull()) {
            throw new WorkflowValidationException("AI_DECISION node missing prompt");
        }

        String promptTemplate = promptNode.asText();

        //STEP 3 — RENDER PROMPT
        String prompt = promptTemplateService.render(promptTemplate, context);

        prompt += "\nAnswer ONLY true or false."; //Force deterministic answer. bcz we want only True/False

        //STEP 4 — BUILD AI REQUEST
        AIRequest request = new AIRequest( prompt, null, 0.1); //WHY LOW TEMPERATURE=0.1? bcz we want deterministic answer

        //STEP 5 — CALL AI
        AIResponse response = aiService.generate(request);

        //STEP 6 — PARSE BOOLEAN
        String content = response.content()
                .trim()
                .toLowerCase();

        //VALIDATE RESPONSE
        boolean decision;

        if (content.contains("true")) {
            decision = true;
        } else if (content.contains("false")) {
            decision = false;
        } else {
            throw new IllegalStateException(
                    "AI_DECISION returned invalid boolean response: " + content
            );
        }

        //STEP 7 — STORE INTO CONTEXT
        context.put("decisionResult", decision);

        //STEP 8 — LOG RESULT
        log.info("AI decision completed → decision={}", decision);

    }
}
