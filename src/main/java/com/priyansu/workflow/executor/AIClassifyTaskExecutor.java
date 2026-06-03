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
        String promptTemplate = config.get("prompt").asText();

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
                
                Return ONLY valid JSON.
                
                Format:
                {
                  "label":"one_of_allowed_labels"
                }
                
                Allowed Labels:
                %s
                
                Input:
                %s
                """.formatted(
                labels,
                prompt
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
            String label =
                    classification.get("label")
                            .asText();
            //check
            if(!labels.contains(label)) {
                throw new WorkflowValidationException("AI returned invalid label: " + label);
            }

            //12: Store in Context
            //context.put("classification", label);
            context.put(node.get("id").asText() + "_result", classification);

        } catch (JsonProcessingException e) {
            throw new AIExecutionException("AI returned invalid JSON");
        }



    }
}