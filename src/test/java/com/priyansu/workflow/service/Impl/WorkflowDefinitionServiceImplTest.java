package com.priyansu.workflow.service.Impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.priyansu.workflow.dto.WorkflowDefinitionRequest;
import com.priyansu.workflow.entity.Workflow;
import com.priyansu.workflow.entity.WorkflowDefinition;
import com.priyansu.workflow.exception.ResourceNotFoundException;
import com.priyansu.workflow.exception.WorkflowValidationException;
import com.priyansu.workflow.repository.WorkflowDefinitionRepository;
import com.priyansu.workflow.repository.WorkflowRepository;
import com.priyansu.workflow.service.AuthorizationService;
import com.priyansu.workflow.service.WorkflowValidationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkflowDefinitionServiceImplTest {

    @Mock WorkflowDefinitionRepository definitionRepository;
    @Mock WorkflowRepository workflowRepository;
    @Mock ObjectMapper objectMapper;
    @Mock WorkflowValidationService validationService;
    @Mock AuthorizationService authorizationService;
    @InjectMocks WorkflowDefinitionServiceImpl definitionService;

    @Test
    @DisplayName("Should save a valid definition as the next version")
    void shouldSaveNextDefinitionVersion() {
        UUID workflowId = UUID.randomUUID();
        WorkflowDefinitionRequest request = request();
        JsonNode json = new ObjectMapper().createObjectNode();
        WorkflowDefinition latest = new WorkflowDefinition();
        latest.setVersion(3);

        when(workflowRepository.findById(workflowId)).thenReturn(Optional.of(new Workflow()));
        when(objectMapper.valueToTree(request)).thenReturn(json);
        when(validationService.validate(json)).thenReturn(List.of());
        when(definitionRepository.findTopByWorkflowIdOrderByVersionDesc(workflowId)).thenReturn(Optional.of(latest));

        definitionService.saveDefinition(workflowId, request);

        verify(authorizationService).validateWorkflowOwnership(workflowId);
        verify(definitionRepository).save(argThat(definition ->
                workflowId.equals(definition.getWorkflowId())
                        && json.equals(definition.getDefinitionJson())
                        && definition.getVersion() == 4));
    }

    @Test
    @DisplayName("Should reject an invalid definition without saving it")
    void shouldRejectInvalidDefinition() {
        UUID workflowId = UUID.randomUUID();
        WorkflowDefinitionRequest request = request();
        JsonNode json = new ObjectMapper().createObjectNode();
        when(workflowRepository.findById(workflowId)).thenReturn(Optional.of(new Workflow()));
        when(objectMapper.valueToTree(request)).thenReturn(json);
        when(validationService.validate(json)).thenReturn(List.of("bad graph"));

        assertThatThrownBy(() -> definitionService.saveDefinition(workflowId, request))
                .isInstanceOf(WorkflowValidationException.class)
                .hasMessageContaining("bad graph");

        verify(definitionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should fail when the workflow does not exist")
    void shouldFailWhenWorkflowMissing() {
        UUID workflowId = UUID.randomUUID();
        when(workflowRepository.findById(workflowId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> definitionService.saveDefinition(workflowId, request()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Workflow Not Found");

        verifyNoInteractions(objectMapper, validationService, definitionRepository);
    }

    @Test
    @DisplayName("Should return the latest stored definition as JSON")
    void shouldGetLatestDefinition() {
        UUID workflowId = UUID.randomUUID();
        JsonNode json = new ObjectMapper().createObjectNode().put("version", 2);
        WorkflowDefinition definition = new WorkflowDefinition();
        definition.setDefinitionJson(json);
        when(definitionRepository.findTopByWorkflowIdOrderByVersionDesc(workflowId)).thenReturn(Optional.of(definition));

        String result = definitionService.getDefinition(workflowId);

        assertThat(result).isEqualTo("{\"version\":2}");
        verify(authorizationService).validateWorkflowOwnership(workflowId);
    }

    @Test
    @DisplayName("Should fail when no definition exists")
    void shouldFailWhenDefinitionMissing() {
        UUID workflowId = UUID.randomUUID();
        when(definitionRepository.findTopByWorkflowIdOrderByVersionDesc(workflowId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> definitionService.getDefinition(workflowId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Definition not found");
    }

    private WorkflowDefinitionRequest request() {
        return new WorkflowDefinitionRequest(
                List.of(new WorkflowDefinitionRequest.Node("start", "TRIGGER", Map.of())),
                List.of()
        );
    }
}
