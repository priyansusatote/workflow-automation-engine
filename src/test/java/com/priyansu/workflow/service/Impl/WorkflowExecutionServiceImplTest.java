package com.priyansu.workflow.service.Impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.priyansu.workflow.entity.Workflow;
import com.priyansu.workflow.entity.TaskExecution;
import com.priyansu.workflow.entity.WorkflowDefinition;
import com.priyansu.workflow.entity.WorkflowExecution;
import com.priyansu.workflow.entity.enums.ExecutionStatus;
import com.priyansu.workflow.entity.enums.WorkflowStatus;
import com.priyansu.workflow.event.WorkflowExecutionEvent;
import com.priyansu.workflow.event.WorkflowExecutionProducer;
import com.priyansu.workflow.exception.ResourceNotFoundException;
import com.priyansu.workflow.exception.WorkflowValidationException;
import com.priyansu.workflow.executor.ExecutorFactory;
import com.priyansu.workflow.executor.TriggerExecutor;
import com.priyansu.workflow.executor.WaitTaskExecutor;
import com.priyansu.workflow.repository.TaskExecutionRepository;
import com.priyansu.workflow.repository.WorkflowDefinitionRepository;
import com.priyansu.workflow.repository.WorkflowExecutionRepository;
import com.priyansu.workflow.repository.WorkflowRepository;
import com.priyansu.workflow.service.AuthorizationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InOrder;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkflowExecutionServiceImplTest {

    @Mock WorkflowDefinitionRepository definitionRepository;
    @Mock ObjectMapper objectMapper;
    @Mock ExecutorFactory executorFactory;
    @Mock WorkflowExecutionRepository executionRepository;
    @Mock TaskExecutionRepository taskExecutionRepository;
    @Mock WorkflowExecutionProducer producer;
    @Mock AuthorizationService authorizationService;
    @Mock WorkflowRepository workflowRepository;
    @InjectMocks WorkflowExecutionServiceImpl executionService;

    @Test
    @DisplayName("Should create a running execution and publish an event for an active workflow")
    void shouldTriggerActiveWorkflow() {
        UUID workflowId = UUID.randomUUID();
        UUID executionId = UUID.randomUUID();
        Map<String, Object> input = Map.of("customerId", "customer-1");
        Workflow workflow = workflow(workflowId, WorkflowStatus.ACTIVE);

        when(workflowRepository.findById(workflowId)).thenReturn(Optional.of(workflow));
        when(executionRepository.save(any(WorkflowExecution.class))).thenAnswer(invocation -> {
            WorkflowExecution execution = invocation.getArgument(0);
            execution.setId(executionId);
            return execution;
        });

        UUID result = executionService.triggerWorkflow(workflowId, input);

        assertThat(result).isEqualTo(executionId);

        InOrder order = inOrder(authorizationService, workflowRepository, executionRepository, producer);
        order.verify(authorizationService).validateWorkflowOwnership(workflowId);
        order.verify(workflowRepository).findById(workflowId);
        order.verify(executionRepository).save(argThat(execution ->
                workflowId.equals(execution.getWorkflowId())
                        && execution.getStatus() == ExecutionStatus.RUNNING));
        order.verify(producer).sendExecutionEvent(new WorkflowExecutionEvent(workflowId, executionId, input));
    }

    @Test
    @DisplayName("Should reject an inactive workflow before creating an execution or publishing an event")
    void shouldRejectInactiveWorkflow() {
        UUID workflowId = UUID.randomUUID();
        when(workflowRepository.findById(workflowId))
                .thenReturn(Optional.of(workflow(workflowId, WorkflowStatus.INACTIVE)));

        assertThatThrownBy(() -> executionService.triggerWorkflow(workflowId, Map.of()))
                .isInstanceOf(WorkflowValidationException.class)
                .hasMessage("Workflow is not active");

        verify(authorizationService).validateWorkflowOwnership(workflowId);
        verify(workflowRepository).findById(workflowId);
        verify(executionRepository, never()).save(any());
        verifyNoInteractions(producer);
    }

    @Test
    @DisplayName("Should preserve ownership not-found failures before checking workflow state")
    void shouldPropagateOwnershipNotFound() {
        UUID workflowId = UUID.randomUUID();
        doThrow(new ResourceNotFoundException("Workflow not found"))
                .when(authorizationService).validateWorkflowOwnership(workflowId);

        assertThatThrownBy(() -> executionService.triggerWorkflow(workflowId, Map.of()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Workflow not found");

        verify(authorizationService).validateWorkflowOwnership(workflowId);
        verifyNoInteractions(workflowRepository, executionRepository, producer);
    }

    @Test
    @DisplayName("Should preserve workflow not-found behavior after ownership validation")
    void shouldRejectMissingWorkflowAfterOwnershipValidation() {
        UUID workflowId = UUID.randomUUID();
        when(workflowRepository.findById(workflowId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> executionService.triggerWorkflow(workflowId, Map.of()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Workflow not found");

        verify(executionRepository, never()).save(any());
        verifyNoInteractions(producer);
    }

    @Test
    @DisplayName("Should skip duplicate Kafka processing when the execution cannot be claimed")
    void shouldSkipDuplicateKafkaProcessing() {
        UUID workflowId = UUID.randomUUID();
        UUID executionId = UUID.randomUUID();
        WorkflowExecution execution = execution(workflowId, executionId, ExecutionStatus.RUNNING);

        when(executionRepository.findById(executionId)).thenReturn(Optional.of(execution));
        when(executionRepository.claimForProcessing(eq(executionId), anyList())).thenReturn(0);

        executionService.executeWorkflowFromKafka(workflowId, executionId, Map.of());

        verify(executionRepository).claimForProcessing(eq(executionId), eq(List.of(
                ExecutionStatus.RUNNING,
                ExecutionStatus.FAILED
        )));
        verifyNoInteractions(definitionRepository, executorFactory, taskExecutionRepository);
    }

    @Test
    @DisplayName("Should allow only one concurrent Kafka delivery to claim an execution")
    void shouldAllowOnlyOneConcurrentKafkaClaim() throws Exception {
        UUID workflowId = UUID.randomUUID();
        UUID executionId = UUID.randomUUID();
        WorkflowExecution execution = execution(workflowId, executionId, ExecutionStatus.RUNNING);
        CountDownLatch firstClaimed = new CountDownLatch(1);
        CountDownLatch releaseFirst = new CountDownLatch(1);
        AtomicInteger claims = new AtomicInteger();

        when(executionRepository.findById(executionId)).thenReturn(Optional.of(execution));
        when(executionRepository.claimForProcessing(eq(executionId), anyList()))
                .thenAnswer(invocation -> {
                    if (claims.incrementAndGet() == 1) {
                        firstClaimed.countDown();
                        releaseFirst.await(2, TimeUnit.SECONDS);
                        return 1;
                    }
                    return 0;
                });
        when(definitionRepository.findTopByWorkflowIdOrderByVersionDesc(workflowId))
                .thenThrow(new ResourceNotFoundException("Definition not found"));

        ExecutorService callers = Executors.newFixedThreadPool(2);
        Future<?> first = callers.submit(() ->
                executionService.executeWorkflowFromKafka(workflowId, executionId, Map.of()));
        assertThat(firstClaimed.await(2, TimeUnit.SECONDS)).isTrue();
        Future<?> second = callers.submit(() ->
                executionService.executeWorkflowFromKafka(workflowId, executionId, Map.of()));

        assertThat(second.get()).isNull();
        releaseFirst.countDown();
        assertThatThrownBy(first::get)
                .isInstanceOf(ExecutionException.class);

        callers.shutdownNow();
        verify(definitionRepository).findTopByWorkflowIdOrderByVersionDesc(workflowId);
    }

    @Test
    @DisplayName("Should skip concurrent resume when the execution cannot be claimed")
    void shouldSkipConcurrentResume() {
        UUID workflowId = UUID.randomUUID();
        UUID executionId = UUID.randomUUID();
        WorkflowExecution execution = execution(workflowId, executionId, ExecutionStatus.WAITING);

        when(executionRepository.findById(executionId)).thenReturn(Optional.of(execution));
        when(executionRepository.claimForProcessing(eq(executionId), anyList())).thenReturn(0);

        executionService.resumeWaitingExecution(executionId);

        verify(executionRepository).claimForProcessing(eq(executionId), eq(List.of(
                ExecutionStatus.WAITING,
                ExecutionStatus.FAILED
        )));
        verifyNoInteractions(definitionRepository, executorFactory, taskExecutionRepository);
    }

    @ParameterizedTest
    @CsvSource({"5s, 5", "2m, 120"})
    @DisplayName("Should persist a waiting execution and waiting task context for supported durations")
    void shouldPersistWaitingState(String duration, long expectedSeconds) throws Exception {
        UUID workflowId = UUID.randomUUID();
        UUID executionId = UUID.randomUUID();
        WorkflowExecution execution = execution(workflowId, executionId, ExecutionStatus.RUNNING);

        WorkflowDefinition definition = new WorkflowDefinition();
        definition.setDefinitionJson(new ObjectMapper().readTree("""
                {
                  "nodes": [
                    {"id":"trigger","type":"TRIGGER"},
                    {"id":"wait","type":"WAIT","config":{"duration":"%s"}}
                  ],
                  "edges": [{"from":"trigger","to":"wait"}]
                }
                """.formatted(duration)));

        when(executionRepository.findById(executionId)).thenReturn(Optional.of(execution));
        when(executionRepository.claimForProcessing(eq(executionId), anyList())).thenReturn(1);
        when(definitionRepository.findTopByWorkflowIdOrderByVersionDesc(workflowId))
                .thenReturn(Optional.of(definition));
        when(executorFactory.getExecutor("TRIGGER")).thenReturn(new TriggerExecutor());
        when(executorFactory.getExecutor("WAIT")).thenReturn(new WaitTaskExecutor());
        when(taskExecutionRepository.save(any(TaskExecution.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        executionService.executeWorkflowFromKafka(workflowId, executionId, Map.of());

        assertThat(execution.getStatus()).isEqualTo(ExecutionStatus.WAITING);
        assertThat(execution.isProcessing()).isFalse();
        assertThat(execution.getWaitingNodeId()).isEqualTo("wait");
        assertThat(Duration.between(LocalDateTime.now(), execution.getResumeAt()).getSeconds())
                .isBetween(expectedSeconds - 2, expectedSeconds + 1);

        ArgumentCaptor<TaskExecution> taskCaptor = ArgumentCaptor.forClass(TaskExecution.class);
        verify(taskExecutionRepository, atLeast(2)).save(taskCaptor.capture());
        TaskExecution waitingTask = taskCaptor.getAllValues().stream()
                .filter(task -> "wait".equals(task.getNodeId()))
                .reduce((first, second) -> second)
                .orElseThrow();

        assertThat(waitingTask.getStatus()).isEqualTo(ExecutionStatus.WAITING);
        assertThat(waitingTask.getOutputData()).containsEntry("waitDuration", duration);
    }

    private Workflow workflow(UUID id, WorkflowStatus status) {
        Workflow workflow = new Workflow();
        workflow.setId(id);
        workflow.setStatus(status);
        return workflow;
    }

    private WorkflowExecution execution(UUID workflowId, UUID executionId, ExecutionStatus status) {
        WorkflowExecution execution = new WorkflowExecution();
        execution.setWorkflowId(workflowId);
        execution.setId(executionId);
        execution.setStatus(status);
        return execution;
    }
}
