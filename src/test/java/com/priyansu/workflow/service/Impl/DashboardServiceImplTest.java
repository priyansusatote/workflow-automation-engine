package com.priyansu.workflow.service.Impl;

import com.priyansu.workflow.dto.DashboardResponse;
import com.priyansu.workflow.dto.WorkflowStatsResponse;
import com.priyansu.workflow.entity.Workflow;
import com.priyansu.workflow.entity.enums.ExecutionStatus;
import com.priyansu.workflow.entity.enums.WorkflowStatus;
import com.priyansu.workflow.repository.WorkflowExecutionRepository;
import com.priyansu.workflow.repository.WorkflowRepository;
import com.priyansu.workflow.service.CurrentUserService;
import com.priyansu.workflow.service.DashboardService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class) //Enable Mockito for this test class
class DashboardServiceImplTest {

    @Mock //fake
    private  WorkflowRepository workflowRepository;

    @Mock
    private WorkflowExecutionRepository executionRepository;

    @Mock
    private CurrentUserService currentUserService;


    @InjectMocks  // Creates the class under test and injects all @Mock dependencies into it
    private DashboardServiceImpl dashboardService;

    @Test
    void testGetDashboard_shouldReturnDashboardStatistics() { //Happy Path
        //fake
        UUID userId = UUID.randomUUID();
        List<UUID> workflowIds = List.of(
                UUID.randomUUID(),
                UUID.randomUUID()
        );

        //Arrange
        when(currentUserService.getCurrentUserId()).thenReturn(userId); //Don't call the real method. Just return:userId
        when(workflowRepository.findWorkflowIdsByUserId(userId)).thenReturn(workflowIds);

        when(workflowRepository.countByUserId(userId)).thenReturn(10L);
        when(workflowRepository.countByUserIdAndStatus(userId, WorkflowStatus.ACTIVE)).thenReturn(7L);
        when(workflowRepository.countByUserIdAndStatus(userId, WorkflowStatus.INACTIVE)).thenReturn(3L);

        when(executionRepository.countByWorkflowIdIn(workflowIds)).thenReturn(100L);
        when(executionRepository.countByWorkflowIdInAndStatus(workflowIds, ExecutionStatus.SUCCESS)).thenReturn(80L);
        when(executionRepository.countByWorkflowIdInAndStatus(workflowIds, ExecutionStatus.FAILED)).thenReturn(10L);
        when(executionRepository.countByWorkflowIdInAndStatus(workflowIds, ExecutionStatus.RUNNING)).thenReturn(5L);
        when(executionRepository.countByWorkflowIdInAndStatus(workflowIds, ExecutionStatus.WAITING)).thenReturn(5L);


        //Act
        DashboardResponse response = dashboardService.getDashboard();


        //Assert (Verify)
        assertEquals(10L , response.totalWorkflows());
        assertEquals(7L, response.activeWorkflows());
        assertEquals(3L, response.inactiveWorkflows());

        assertEquals(100L , response.totalExecutions());
        assertEquals(80L,  response.successfulExecutions());
        assertEquals(10L , response.failedExecutions());

        assertEquals(5L , response.runningExecutions());
        assertEquals(5L , response.waitingExecutions());

        assertEquals(80.0, response.successRate());

        //Verify Invocation Count ; verify that the service actually interacted/Did it call it once?  with its dependencies.
        verify(currentUserService).getCurrentUserId();
        verify(workflowRepository).findWorkflowIdsByUserId(userId);
        verify(workflowRepository).countByUserId(userId);
        verify(executionRepository).countByWorkflowIdIn(workflowIds);

    }

    @Test //Sad-No Data scenario
    void testGetDashBoard_shouldReturnZeroStatisticsWhenUserHasNoWorkflows(){
        //fake
        UUID userId = UUID.randomUUID();


        //Arrange
        when(currentUserService.getCurrentUserId()).thenReturn(userId);

        when(workflowRepository.findWorkflowIdsByUserId(userId)).thenReturn(List.of());//orCollections.emptyList() Means :return Empty List (0-workflows)

        when(workflowRepository.countByUserId(userId)).thenReturn(0L);
        when(workflowRepository.countByUserIdAndStatus(userId, WorkflowStatus.ACTIVE)).thenReturn(0L);
        when(workflowRepository.countByUserIdAndStatus(userId, WorkflowStatus.INACTIVE)).thenReturn(0L);



        //Act
        DashboardResponse response = dashboardService.getDashboard();

        //Assert
        assertEquals(0L, response.totalWorkflows());
        assertEquals(0L, response.activeWorkflows());
        assertEquals(0L, response.inactiveWorkflows());
        assertEquals(0L, response.totalExecutions());
        assertEquals(0L, response.successfulExecutions());
        assertEquals(0L, response.failedExecutions());
        assertEquals(0L, response.runningExecutions());
        assertEquals(0L, response.waitingExecutions());
        assertEquals(0.0, response.successRate());

        //this Dependency Never be used
        verifyNoInteractions(executionRepository); //executionRepository should NEVER be called. bcz: workflowIds.isEmpty()

    }

    //Using Production Style- AssertJ in Assert Section
    @Test
    void shouldReturnWorkflowStats_Successfully(){
        //fake
        UUID userId = UUID.randomUUID();
        Workflow workflow = new Workflow(
                UUID.randomUUID(),
                "priyansu",
                "Demo",
                userId,
                WorkflowStatus.ACTIVE
        );
        List<Workflow> workflows = List.of(workflow);

        //Arrange
        when(currentUserService.getCurrentUserId()).thenReturn(userId);
        when(workflowRepository.findByUserId(userId)).thenReturn(workflows);

        when(executionRepository.countByWorkflowId(workflow.getId())).thenReturn(100L);
        when(executionRepository.countByWorkflowIdAndStatus(workflow.getId(),ExecutionStatus.SUCCESS)).thenReturn(80L);
        when(executionRepository.countByWorkflowIdAndStatus(workflow.getId(),ExecutionStatus.FAILED)).thenReturn(20L);

        //Act
        List<WorkflowStatsResponse> response = dashboardService.getWorkflowStats();

        //Assert

        assertThat(response.size()).isEqualTo(1); //assertEquals(1, response.size());
        assertThat(response.getFirst().workflowId()).isEqualTo(workflow.getId());
        assertThat(response.getFirst().successRate()).isEqualTo(80.0);
        assertThat(response.getFirst().successfulExecutions()).isEqualTo(80L);
        assertThat(response.getFirst().failedExecutions()).isEqualTo(20L);
        assertThat(response.getFirst().workflowName()).isEqualTo("priyansu");


        //called Once
        verify(currentUserService).getCurrentUserId();
        verify(workflowRepository).findByUserId(userId);
        verify(executionRepository).countByWorkflowId(workflow.getId());
        verify(executionRepository).countByWorkflowIdAndStatus(workflow.getId(),ExecutionStatus.SUCCESS);
        verify(executionRepository).countByWorkflowIdAndStatus(workflow.getId(),ExecutionStatus.FAILED);

        verifyNoMoreInteractions(
                currentUserService,
                workflowRepository,
                executionRepository
        );

    }


    @Test //SAD
    void shouldReturnZeroSuccessRateWhenWorkflowHasNoExecutions() {

        // Arrange
        UUID userId = UUID.randomUUID();

        Workflow workflow = new Workflow(
                UUID.randomUUID(),
                "Priyansu",
                "Demo",
                userId,
                WorkflowStatus.ACTIVE
        );

        when(currentUserService.getCurrentUserId()).thenReturn(userId);
        when(workflowRepository.findByUserId(userId))
                .thenReturn(List.of(workflow));

        // No executions
        when(executionRepository.countByWorkflowId(workflow.getId()))
                .thenReturn(0L);

        when(executionRepository.countByWorkflowIdAndStatus(
                workflow.getId(),
                ExecutionStatus.SUCCESS))
                .thenReturn(0L);

        when(executionRepository.countByWorkflowIdAndStatus(
                workflow.getId(),
                ExecutionStatus.FAILED))
                .thenReturn(0L);

        // Act
        List<WorkflowStatsResponse> response = dashboardService.getWorkflowStats();

        // Assert
        assertThat(response).hasSize(1);

        WorkflowStatsResponse stats = response.getFirst();

        assertThat(stats.workflowId()).isEqualTo(workflow.getId());
        assertThat(stats.workflowName()).isEqualTo("Priyansu");
        assertThat(stats.totalExecutions()).isZero();
        assertThat(stats.successfulExecutions()).isZero();
        assertThat(stats.failedExecutions()).isZero();
        assertThat(stats.successRate()).isZero();

        verify(currentUserService).getCurrentUserId();
        verify(workflowRepository).findByUserId(userId);
        verify(executionRepository).countByWorkflowId(workflow.getId());
        verify(executionRepository).countByWorkflowIdAndStatus(workflow.getId(), ExecutionStatus.SUCCESS);
        verify(executionRepository).countByWorkflowIdAndStatus(workflow.getId(), ExecutionStatus.FAILED);
        verifyNoMoreInteractions(
                currentUserService,
                workflowRepository,
                executionRepository
        );
    }

}