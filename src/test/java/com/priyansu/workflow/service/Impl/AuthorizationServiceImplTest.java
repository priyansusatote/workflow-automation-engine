package com.priyansu.workflow.service.Impl;

import com.priyansu.workflow.entity.Workflow;
import com.priyansu.workflow.entity.WorkflowExecution;
import com.priyansu.workflow.exception.ResourceNotFoundException;
import com.priyansu.workflow.repository.WorkflowExecutionRepository;
import com.priyansu.workflow.repository.WorkflowRepository;
import com.priyansu.workflow.security.JwtUserPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthorizationServiceImplTest {

    @Mock WorkflowRepository workflowRepository;
    @Mock WorkflowExecutionRepository executionRepository;
    @InjectMocks AuthorizationServiceImpl authorizationService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should allow the authenticated workflow owner")
    void shouldAllowWorkflowOwner() {
        UUID userId = UUID.randomUUID();
        UUID workflowId = UUID.randomUUID();
        authenticate(userId, "ROLE_USER");
        when(workflowRepository.findById(workflowId)).thenReturn(Optional.of(workflow(workflowId, userId)));

        authorizationService.validateWorkflowOwnership(workflowId);

        verify(workflowRepository).findById(workflowId);
    }

    @Test
    @DisplayName("Should deny access to another user's workflow")
    void shouldDenyOtherUsersWorkflow() {
        UUID workflowId = UUID.randomUUID();
        authenticate(UUID.randomUUID(), "ROLE_USER");
        when(workflowRepository.findById(workflowId))
                .thenReturn(Optional.of(workflow(workflowId, UUID.randomUUID())));

        assertThatThrownBy(() -> authorizationService.validateWorkflowOwnership(workflowId))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("Should report a missing workflow")
    void shouldReportMissingWorkflow() {
        UUID workflowId = UUID.randomUUID();
        authenticate(UUID.randomUUID(), "ROLE_USER");
        when(workflowRepository.findById(workflowId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authorizationService.validateWorkflowOwnership(workflowId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Workflow not found");
    }

    @Test
    @DisplayName("Should validate ownership through an execution's workflow")
    void shouldValidateExecutionOwnership() {
        UUID userId = UUID.randomUUID();
        UUID executionId = UUID.randomUUID();
        UUID workflowId = UUID.randomUUID();
        authenticate(userId, "ROLE_USER");
        WorkflowExecution execution = new WorkflowExecution();
        execution.setId(executionId);
        execution.setWorkflowId(workflowId);
        when(executionRepository.findById(executionId)).thenReturn(Optional.of(execution));
        when(workflowRepository.findById(workflowId)).thenReturn(Optional.of(workflow(workflowId, userId)));

        authorizationService.validateExecutionOwnership(executionId);

        verify(executionRepository).findById(executionId);
        verify(workflowRepository).findById(workflowId);
    }

    private void authenticate(UUID userId, String authority) {
        JwtUserPrincipal principal = new JwtUserPrincipal(
                userId,
                "user@example.com",
                List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority(authority))
        );
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );
    }

    private Workflow workflow(UUID workflowId, UUID userId) {
        Workflow workflow = new Workflow();
        workflow.setId(workflowId);
        workflow.setUserId(userId);
        return workflow;
    }
}
