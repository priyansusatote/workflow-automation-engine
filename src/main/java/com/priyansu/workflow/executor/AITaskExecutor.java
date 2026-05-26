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
public class AITaskExecutor implements TaskExecutor {

    private final AIService aiService;
    private final PromptTemplateService promptTemplateService;

    @Override
    public String getType() {
        return "AI_GENERATE";
    }

    @Override
    public void execute(JsonNode node, WorkflowContext context) { //1. Read prompt from node JSON 2. Build AIRequest 3. Call AIService 4. Store result in WorkflowContext

        //Read Node data
        String nodeId =  node.get("id").asText();

        JsonNode config = node.get("config");

        if (config == null || config.isNull()) {
            throw new WorkflowValidationException(
                    "AI_GENERATE node missing config"
            );
        }

        JsonNode promptNode = config.get("prompt");

        if (promptNode == null || promptNode.isNull()) {
            throw new WorkflowValidationException(
                    "AI_GENERATE node missing required field: prompt"
            );
        }

        String promptTemplate = promptNode.asText(); //RAW TEMPLATE
        //RENDER FINAL PROMPT
        String prompt = promptTemplateService.render(
                promptTemplate,
                context
        );
        log.info("Rendered AI prompt → {}", prompt);


        String model = config.has("model") ? config.get("model").asText() : null; //for future flexibility. [different workflow nodes→ different LLMs

        //LOG START
        log.info("Executing AI node → nodeId={}, model={}", nodeId, model);

        //BUILD AIRequest
        AIRequest request = new AIRequest(prompt, model, 0.7); //Later:Temperature may also come from node JSON.

        //CALL AI SERVICE
        AIResponse response = aiService.generate(request);

        //STORE RESULT INTO CONTEXT
        context.put(
                nodeId + "_result",
                response.content()
        );

        //LOG RESULT
        log.info( "AI node completed → nodeId={}, latency={}ms", nodeId, response.latencyMs());
    }
}
