package com.priyansu.workflow.executor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.priyansu.workflow.exception.WorkflowValidationException;
import com.priyansu.workflow.execution.WorkflowContext;
import com.priyansu.workflow.service.ai.PromptTemplateService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExecutorBehaviorTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock PromptTemplateService promptTemplateService;

    @Test
    @DisplayName("Should mark the workflow as started when the trigger executes")
    void shouldExecuteTrigger() throws Exception {
        WorkflowContext context = new WorkflowContext(Map.of());

        new TriggerExecutor().execute(objectMapper.readTree("{\"id\":\"start\"}"), context);

        assertThat(context.get("started")).isEqualTo(true);
    }

    @Test
    @DisplayName("Should store wait duration in the workflow context")
    void shouldExecuteWait() throws Exception {
        WorkflowContext context = new WorkflowContext(Map.of());

        new WaitTaskExecutor().execute(objectMapper.readTree(
                "{\"id\":\"wait\",\"config\":{\"duration\":\"5m\"}}"), context);

        assertThat(context.get("waitDuration")).isEqualTo("5m");
        assertThat(context.get("wait_result")).isEqualTo(Map.of("waitDuration", "5m"));
    }

    @Test
    @DisplayName("Should reject a wait node without duration")
    void shouldRejectInvalidWait() throws Exception {
        assertThatThrownBy(() -> new WaitTaskExecutor().execute(
                objectMapper.readTree("{\"id\":\"wait\",\"config\":{}}"),
                new WorkflowContext(Map.of())))
                .isInstanceOf(WorkflowValidationException.class)
                .hasMessage("WAIT node missing duration");
    }

    @Test
    @DisplayName("Should evaluate a rendered rule and store its result")
    void shouldExecuteRule() throws Exception {
        WorkflowContext context = new WorkflowContext(Map.of());
        when(promptTemplateService.render("{{amount}} > 100", context)).thenReturn("150 > 100");

        new RuleTaskExecutor(promptTemplateService).execute(objectMapper.readTree(
                "{\"id\":\"rule\",\"config\":{\"expression\":\"{{amount}} > 100\"}}"), context);

        assertThat(context.get("ruleResult")).isEqualTo(true);
        assertThat(context.get("rule_result")).isEqualTo(Map.of("result", true, "expression", "150 > 100"));
    }

    @Test
    @DisplayName("Should resolve registered executors and reject unknown types")
    void shouldResolveExecutors() {
        TaskExecutor trigger = new TriggerExecutor();
        ExecutorFactory factory = new ExecutorFactory(List.of(trigger));

        assertThat(factory.getExecutor("TRIGGER")).isSameAs(trigger);
        assertThatThrownBy(() -> factory.getExecutor("UNKNOWN"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("No executor found for type UNKNOWN");
    }

    @Test
    @DisplayName("Should reject an action node without an action type")
    void shouldRejectMalformedActionConfiguration() throws Exception {
        assertThatThrownBy(() -> new ActionExecutor().execute(
                objectMapper.readTree("{\"id\":\"action\",\"config\":{}}"),
                new WorkflowContext(Map.of())))
                .isInstanceOf(WorkflowValidationException.class)
                .hasMessage("ACTION node missing actionType");
    }

    @Test
    @DisplayName("Should reject a decision node without configuration")
    void shouldRejectMalformedDecisionConfiguration() throws Exception {
        assertThatThrownBy(() -> new DecisionExecutor().execute(
                objectMapper.readTree("{\"id\":\"decision\"}"),
                new WorkflowContext(Map.of())))
                .isInstanceOf(WorkflowValidationException.class)
                .hasMessage("DECISION node missing config");
    }

    @Test
    @DisplayName("Should reject an HTTP action node without a URL")
    void shouldRejectMalformedHttpConfiguration() throws Exception {
        HttpActionTaskExecutor executor = new HttpActionTaskExecutor(
                new RestTemplate(),
                promptTemplateService,
                objectMapper
        );

        assertThatThrownBy(() -> executor.execute(
                objectMapper.readTree("{\"id\":\"http\",\"config\":{\"method\":\"POST\"}}"),
                new WorkflowContext(Map.of())))
                .isInstanceOf(WorkflowValidationException.class)
                .hasMessage("HTTP_ACTION missing url");
    }
}
