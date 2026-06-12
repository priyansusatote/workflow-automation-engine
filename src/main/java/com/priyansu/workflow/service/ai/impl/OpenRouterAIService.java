package com.priyansu.workflow.service.ai.impl;

import com.priyansu.workflow.exception.AIExecutionException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import com.priyansu.workflow.dto.ai.AIRequest;
import com.priyansu.workflow.dto.ai.AIResponse;
import com.priyansu.workflow.dto.ai.openrouter.OpenRouterMessage;
import com.priyansu.workflow.dto.ai.openrouter.OpenRouterRequest;
import com.priyansu.workflow.dto.ai.openrouter.OpenRouterResponse;
import com.priyansu.workflow.service.AIService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
//This class is:  provider implementation.  Meaning:  "How do we actually talk to OpenRouter?"
public class OpenRouterAIService implements  AIService { //This class will: call OpenRouter ,send prompt , receive response , calculate latency , return AIResponse

    private final WebClient webClient; //Modern async-capable HTTP client.  Better than RestTemplate.

    @Value("${ai.openrouter.api-key}")
    private String apiKey;

    @Value("${ai.openrouter.model}")
    private String defaultModel;

    @Value("${ai.openrouter.url}")
    private String url;

    @Value("${ai.openrouter.timeout-seconds}")
    private Long timeoutSeconds;


    @Override
    @Retry(name = "openRouter")  //resiliance4J
    @CircuitBreaker(
            name = "openRouter",
            fallbackMethod = "fallbackGenerate" //write fallback method of this name
    )
    public AIResponse generate(AIRequest request) {

        //Start Timer
        long start = System.currentTimeMillis();

        //build Payload
        OpenRouterRequest payload = new OpenRouterRequest(
                request.model() != null ? request.model() : defaultModel,
                List.of(
                        new OpenRouterMessage(
                                "user",
                                request.prompt()
                        )
                ),
                  request.temperature() != null ? request.temperature() : 0.7
        );

        log.info("Sending AI request → model={}", payload.model());
        //Call OpenRouter
        OpenRouterResponse response = webClient.post()
                .uri(url)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(payload) //Convert DTO → JSON.
                .retrieve() //Execute request.
                .onStatus(
                        HttpStatusCode::isError,
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .flatMap(error -> {
                                    log.error("OpenRouter error → {}", error);
                                    return Mono.error(new RuntimeException(error));
                                })
                )
                .bodyToMono(OpenRouterResponse.class) //Convert JSON response → DTOs.
                .block(Duration.ofSeconds(timeoutSeconds)); //This temporarily converts:  reactive async → synchronous execution

        //Validate Response (can be null,empty,or invalid)
        if (response == null
                || response.choices() == null
                || response.choices().isEmpty()) {

            throw new RuntimeException("Empty AI response");
        }

        //Extract Content
        String content =
                response.choices()
                        .getFirst()
                        .message()
                        .content();

        //Calculate Latency
        long latency = System.currentTimeMillis() - start;

        //Log
        log.info(
                "AI execution completed → model={}, latency={}ms, promptTokens={}, completionTokens={}",
                payload.model(),
                latency,
                response.usage() != null ? response.usage().prompt_tokens() : 0,
                response.usage() != null ? response.usage().completion_tokens() : 0
        );

        //Return AiResponse
        return new AIResponse(
                content,
                response.usage() != null
                        ? response.usage().prompt_tokens()
                        : null,

                response.usage() != null
                        ? response.usage().completion_tokens()
                        : null,

                latency
        );
    }

    //Fallback Method [if after 3-Retries it fails -> fallback]
    private AIResponse fallbackGenerate(
            AIRequest request,
            Exception ex
    ) {

        log.error(
                "OpenRouter unavailable after retries",
                ex
        );

        throw new AIExecutionException(
                "AI service temporarily unavailable"
        );
    }
}
