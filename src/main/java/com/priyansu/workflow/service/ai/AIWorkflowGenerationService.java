package com.priyansu.workflow.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.priyansu.workflow.dto.ai.AIRequest;
import com.priyansu.workflow.dto.ai.AIResponse;
import com.priyansu.workflow.exception.AIExecutionException;
import com.priyansu.workflow.service.AIService;
import com.priyansu.workflow.service.WorkflowValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIWorkflowGenerationService implements WorkflowGenerationService {

    private final AIService aiService;
    private final ObjectMapper objectMapper;
    private final WorkflowValidationService validationService;

    @Override
    public String generateWorkflow(String userPrompt) { //self-healing AI workflow generation (by validation adding)
        String prompt = """ 
                                You are a workflow generation AI.
                
                                Convert the user's request into STRICT workflow JSON.
                
                                CRITICAL RULES:
                
                                * Output ONLY valid raw JSON
                                * No markdown
                                * No explanations
                                * No comments
                                * No surrounding text
                                * Response must start with {
                                * Response must end with }
                                * Use ONLY supported node types
                                * Never invent new node types
                
                                WORKFLOW RULES:
                
                                * Workflow must contain:
                
                                  * "nodes" array
                                  * "edges" array
                
                                NODE RULES:
                
                                * Every node must contain:
                
                                  * "id"
                                  * "type"
                
                                * Node ids:
                
                                  * must be strings
                                  * must be unique
                                  * use sequential values: "1", "2", "3"
                
                                SUPPORTED NODE TYPES:
                
                                * TRIGGER
                                * ACTION
                                * AI_GENERATE
                                * AI_DECISION
                                * WAIT
                
                                AI NODE RULES:
                
                                * AI_GENERATE and AI_DECISION nodes MUST contain:
                                  {
                                  "config": {
                                  "prompt": "..."
                                  }
                                  }
                
                                * Prompts must use:
                                  {{input.variableName}}
                                  for dynamic workflow input values.
                
                                WAIT NODE RULES:
                
                                * WAIT nodes must contain:
                                  {
                                  "config": {
                                  "duration": "5m"
                                  }
                                  }
                
                                EDGE RULES:
                
                                * Every edge must contain:
                
                                  * "from"
                                  * "to"
                
                                * Edge values must reference valid node ids
                
                                DECISION EDGE RULES:
                
                                * AI_DECISION nodes MUST have:
                
                                  * one edge with:
                                    "condition": "true"
                                  * one edge with:
                                    "condition": "false"
                
                                * Non-decision edges MUST NOT contain "condition"
                
                                - ACTION nodes may optionally contain:
                                {
                                  "config": {
                                    "actionType": "..."
                                  }
                                }
                
                                - WAIT duration must use:
                                  5m
                                  2h
                                  1d
                
                
                                DAG RULES:
                
                                * Workflow must start with TRIGGER node
                                * Workflow must not contain cycles
                                * Every node must be reachable from TRIGGER
                                * No orphan nodes
                
                                JSON FORMAT EXAMPLE:
                                {
                                                "nodes": [
                                                  { "id": "1", "type": "TRIGGER" },
                                                  { "id": "2", "type": "AI_GENERATE", "config": { "prompt": "Summarize {{input.text}}" } },
                                                  { "id": "3", "type": "AI_DECISION", "config": { "prompt": "Is this urgent? {{input.ticket}}" } },
                                                  { "id": "4", "type": "ACTION" },
                                                  { "id": "5", "type": "ACTION" }
                                                ],
                                                "edges": [
                                                  { "from": "1", "to": "2" },
                                                  { "from": "2", "to": "3" },
                                                  { "from": "3", "to": "4", "condition": "true" },
                                                  { "from": "3", "to": "5", "condition": "false" }
                                                ]
                                              }
                               User Request:
                               ----------------
                                %s
                               ----------------
                """.formatted(userPrompt);

        //Build Request
        AIRequest request = new AIRequest(
                prompt,
                null,
                0.1
        );

        //Execute
        AIResponse response = aiService.generate(request);

        //PARSE SAFELY  { AI generates String response so Parsing :Convert JSON text (String) into a JsonNode object so Java can read and validate it [// Convert AI-generated JSON string into JsonNode for validation and processing]
        try {
            JsonNode jsonNode = objectMapper.readTree(
                    response.content()
            );

            //validate generated workflow
            List<String> errors = validationService.validate(jsonNode);
            //if Valid
            if (errors.isEmpty()) {
                return objectMapper.writeValueAsString(jsonNode);
            }

            // Build repair prompt
            String repairPrompt = """
                    The generated workflow JSON is invalid.
                    
                    Validation errors:
                    %s
                    
                    Fix the workflow.
                    
                    RULES:
                    - Return ONLY valid JSON
                    - No markdown
                    - Preserve original workflow intent
                    - Fix ALL validation errors
                    
                    Broken Workflow JSON:
                    %s
                    """.formatted(
                    String.join("\n", errors),
                    jsonNode.toPrettyString()
            );
            // Build repair request
            AIRequest repairRequest = new AIRequest(
                    repairPrompt,
                    null,
                    0.1
            );

            // Call AI again
            AIResponse repairResponse =
                    aiService.generate(repairRequest);

            // Parse repaired workflow
            JsonNode repairedWorkflow = objectMapper.readTree(
                            repairResponse.content()
                    );

            // Validate again
            List<String> repairedErrors =
                    validationService.validate(
                            repairedWorkflow
                    );

            // Final check
            if (!repairedErrors.isEmpty()) {

                throw new AIExecutionException(
                        "AI failed to repair workflow: "
                                + repairedErrors
                );
            }

            // Success
            return objectMapper.writeValueAsString(
                    repairedWorkflow
            );

        } catch (Exception e) {
            log.error(
                    "Failed to parse workflow JSON → {}",
                    response.content()
            );
            throw new IllegalStateException(
                    "Invalid workflow JSON generated by AI"
            );
        }

    }
}
