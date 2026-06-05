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

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AIClassifyTaskExecutor implements TaskExecutor {

    private final AIService aiService;
    private final PromptTemplateService promptTemplateService;
    private final ObjectMapper objectMapper;

    @Override
    public String getType() {
        return "AI_CLASSIFY";
    }


    @Override
    public void execute(JsonNode node, WorkflowContext context) {

        //1: Read Config
        JsonNode config = node.get("config");

        if (config == null || config.isNull()) {
            throw new WorkflowValidationException(
                    "AI_CLASSIFY node missing config"
            );
        }

        //2: Read prompt (which is inside Config)
        JsonNode promptNode = config.get("prompt");
        if (promptNode == null || promptNode.isNull()) {
            throw new WorkflowValidationException(
                    "AI_CLASSIFY node missing prompt"
            );
        }
        String promptTemplate = promptNode.asText();

        //3: Render
        String prompt = promptTemplateService.render(promptTemplate, context);

        //4: Read Labels
        JsonNode labelsNode = config.get("labels");

        //5:Validation
        if (labelsNode == null || !labelsNode.isArray()) {
            throw new WorkflowValidationException(
                    "AI_CLASSIFY node missing labels"
            );
        }

        //6: Convert Labels
        List<String> labels = new ArrayList<>();
        for (JsonNode label : labelsNode) {
            labels.add(label.asText());
        }


        //7: Build Classification Prompt
        String classificationPrompt = """
                Classify the input.
                
                Allowed Labels:
                %s
                
                Return ONLY valid JSON.
                
                Format:
                {
                  "label":"one_of_allowed_labels"
                }
                
                Input:
                %s
                """.formatted(
                labels,
                prompt
        );

        //log
        log.info("Executing AI classification -> nodeId={}, labels={}",
                node.get("id").asText(),
                labels
        );
        //8: Call AI
        AIRequest request = new AIRequest(
                classificationPrompt,
                null,
                0.1
        );


        //9:Execute
        AIResponse response = aiService.generate(request);


        try {
            //10: Parse Response
            JsonNode classification = objectMapper.readTree(response.content());

            //11: Validate Label
            JsonNode labelNode = classification.get("label");
            if (labelNode == null || labelNode.isNull()) {
                throw new AIExecutionException(
                        "AI response missing label field"
                );
            }
            String label = labelNode.asText();


            //check
            if (!labels.contains(label)) {
                throw new WorkflowValidationException("AI_CLASSIFY returned invalid label: " + label);
            }

            //12: Store in Context
            context.put(node.get("id").asText() + "_result", classification);

            //log
            log.info(
                    "AI classification completed -> nodeId={}, label={}",
                    node.get("id").asText(),
                    label
            );

        } catch (JsonProcessingException e) {
            throw new AIExecutionException("AI returned invalid JSON");
        }


    }
}