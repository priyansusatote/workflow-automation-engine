package com.priyansu.workflow.executor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.priyansu.workflow.exception.WorkflowValidationException;
import com.priyansu.workflow.execution.WorkflowContext;
import com.priyansu.workflow.service.ai.PromptTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class HttpActionTaskExecutor implements TaskExecutor {

    private final RestTemplate restTemplate;
    private final PromptTemplateService promptTemplateService;
    private final ObjectMapper objectMapper;

    @Override
    public String getType() {
        return "HTTP_ACTION";
    }


    @Override
    public void execute(JsonNode node, WorkflowContext context) {

       //1:Read Config
        JsonNode config = node.get("config");
        if(config == null || config.isNull()) {
            throw new WorkflowValidationException("HTTP_ACTION missing config");
        }

        //2: Read Method + URL
        String method = config.get("method").asText();
        String url = config.get("url").asText();

        //3: Body
        JsonNode bodyNode = config.get("body");
        //convert to string
        String bodyJson = null;
        try {
            bodyJson = objectMapper.writeValueAsString(bodyNode);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                    "Failed to serialize HTTP request body", e);
        }

        //4: Render Variable
        bodyJson = promptTemplateService.render(
                        bodyJson,
                        context
                );

        //5: HTTP Request [for phase-1 Supports POST only]
        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(
                        bodyJson,
                        headers
                );

        //6: Execute
        ResponseEntity<String> response = restTemplate.exchange(
                        url,
                        HttpMethod.POST,
                        request,
                        String.class
                );

        //7: Store Result
        context.put(node.get("id").asText() + "_result",
                Map.of(
                        "statusCode",
                        response.getStatusCode().value(),
                        "responseBody",
                        response.getBody()
                )
        );



    }
}