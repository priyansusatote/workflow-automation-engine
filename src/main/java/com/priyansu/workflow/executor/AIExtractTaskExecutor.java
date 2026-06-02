package com.priyansu.workflow.executor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.priyansu.workflow.dto.ai.AIRequest;
import com.priyansu.workflow.dto.ai.AIResponse;
import com.priyansu.workflow.exception.AIExecutionException;
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
public class AIExtractTaskExecutor implements TaskExecutor {

    private final AIService aiService;
    private final PromptTemplateService promptTemplateService;
    private final ObjectMapper objectMapper;


    @Override
    public String getType() {
        return "AI_EXTRACT";
    }

    @Override
    public void execute(JsonNode node, WorkflowContext context) {

        //1:Read config [Exactly like AI_GENERATE.]
        JsonNode config = node.get("config");
        if (config == null || config.isNull()) {
            throw new WorkflowValidationException("AI_EXTRACT node in missing");
        }

        //2: Read prompt
        String promptTemplate = config.get("prompt").asText();

        //3: Render
        String prompt = promptTemplateService.render(promptTemplate, context);

        //4: Read schema [data of given input like amount, vendor,... under Schema]
        JsonNode schemaNode = config.get("schema");

        //5: Validate
        if (schemaNode == null) {
            throw new WorkflowValidationException("AI_EXTRACT node missing schema");
        }

        //6: Build extraction prompt [we force structured Json Output]
        String extractionPrompt = """
                Extract structured data.
                
                Return ONLY valid JSON.
                
                Schema:
                %s
                
                Input:
                %s
                """.formatted(
                schemaNode.toPrettyString(),
                       prompt
        );

        //7: Call AI
        AIRequest request = new AIRequest(
                        extractionPrompt,
                        null,
                        0.1
                );

        //8: Execute
        AIResponse response = aiService.generate(request);


        try {
            //9: Parse JSON
            JsonNode extracted = objectMapper.readTree(response.content());
            //10 : Store in Context
            context.put(node.get("id").asText() + "_result", extracted);

            //log
            log.info("AI extraction completed → nodeId={}", node.get("id").asText());

        } catch (JsonProcessingException e) {
            throw new AIExecutionException("AI returned invalid JSON");
        }



    }
}