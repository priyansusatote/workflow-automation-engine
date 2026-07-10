package com.priyansu.workflow.service.Impl;

import com.priyansu.workflow.dto.WorkflowRequest;
import com.priyansu.workflow.dto.WorkflowResponse;
import com.priyansu.workflow.entity.Workflow;
import com.priyansu.workflow.entity.enums.WorkflowStatus;
import com.priyansu.workflow.exception.DuplicateResourceException;
import com.priyansu.workflow.exception.ResourceNotFoundException;
import com.priyansu.workflow.mapper.WorkflowMapper;
import com.priyansu.workflow.repository.WorkflowRepository;
import com.priyansu.workflow.service.CurrentUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import org.mockito.ArgumentCaptor;

@ExtendWith(MockitoExtension.class)
class WorkflowServiceImplTest {

    @Mock
    WorkflowRepository workflowRepository;

    @Mock
    WorkflowMapper workflowMapper;

    @Mock
    CurrentUserService currentUserService;

    @InjectMocks
    WorkflowServiceImpl workflowServiceImpl;


    private UUID userId;
    private UUID workflowId;

    @BeforeEach  //Now every test already has userId & workflowId
    void setUp() {

        userId = UUID.randomUUID();
        workflowId = UUID.randomUUID();

    }

    @DisplayName("Create Workflow")
    @Nested
    class CreateWorkflowTests {

        @Test
        void should_CreateWorkflowSuccessfully() {

            //Arrange                                        //here used first method without Helper but after this reduced code using Helper
            WorkflowRequest request = new WorkflowRequest(   //can be used by Helper Function => WorkflowRequest request = createRequest();
                    "Invoice Approval",
                    "Demo workflow"
            );
            //Entity
            Workflow workflow = new Workflow();
            workflow.setName(request.name());
            workflow.setDescription(request.description());
            //saved entity
            Workflow savedWorkflow = new Workflow();
            savedWorkflow.setId(workflowId);
            savedWorkflow.setUserId(userId);
            savedWorkflow.setName(request.name());
            savedWorkflow.setDescription(request.description());
            //response DTO
            WorkflowResponse response = new WorkflowResponse(
                    savedWorkflow.getId(),
                    savedWorkflow.getName(),
                    savedWorkflow.getDescription(),
                    savedWorkflow.getUserId(),
                    WorkflowStatus.ACTIVE,
                    LocalDateTime.now(),
                    LocalDateTime.now()
            );


            when(currentUserService.getCurrentUserId()).thenReturn(userId);
            when(workflowRepository.existsByNameAndUserId(request.name(), userId)).thenReturn(false);
            //mapper
            when(workflowMapper.toEntity(request)).thenReturn(workflow);
            when(workflowRepository.save(workflow)).thenReturn(savedWorkflow);
            when(workflowMapper.toWorkflowResponse(savedWorkflow)).thenReturn(response);

            //Act
            WorkflowResponse result = workflowServiceImpl.createWorkflow(request);

            //Assert
            assertNotNull(result);
            assertThat(result.id()).isEqualTo(savedWorkflow.getId());
            assertThat(result.name()).isEqualTo("Invoice Approval");
            assertThat(result.description()).isEqualTo("Demo workflow");

            verify(currentUserService).getCurrentUserId(); //called only once
            verify(workflowRepository, times(1)).existsByNameAndUserId(request.name(), userId);
            verify(workflowMapper, times(1)).toEntity(request);

            verify(workflowMapper, times(1)).toWorkflowResponse(savedWorkflow);


            //create Captor (to verify passed Object/data in .save)
            ArgumentCaptor<Workflow> captor = ArgumentCaptor.forClass(Workflow.class);

            verify(workflowRepository).save(captor.capture());
            Workflow captured = captor.getValue();

            assertThat(captured.getUserId()).isEqualTo(userId);
            assertThat(captured.getName()).isEqualTo("Invoice Approval");


        }

        //SAD
        @Test
        void should_shouldThrowDuplicateResourceExceptionWhenWorkflowAlreadyExists() {

            WorkflowRequest request = createRequest();

            //Arrange
            when(currentUserService.getCurrentUserId()).thenReturn(userId);
            when(workflowRepository.existsByNameAndUserId(request.name(), userId)).thenReturn(true);

            //act + Assert
            assertThatThrownBy(() -> workflowServiceImpl.createWorkflow(request))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessage("Workflow already exists");

            verify(currentUserService, times(1)).getCurrentUserId();
            verify(workflowMapper, times(0)).toEntity(request);
            verifyNoInteractions(workflowMapper);
            verify(workflowRepository, times(1)).existsByNameAndUserId(request.name(), userId);

            verify(workflowRepository, never()).save(any());
            verifyNoMoreInteractions(
                    currentUserService,
                    workflowRepository
            );
        }

    }

    @DisplayName("Get All Workflows")
    @Nested
    class GetAllWorkflowsTests {

        @Test
        void should_ReturnListOfAllWorkflowSuccessfully_whenWorkflowExists() {

            //Arrange
            Workflow workflow = createWorkflow(); //using helper reduced code

            WorkflowResponse response = createResponse();

            when(workflowRepository.findAll()).thenReturn(List.of(workflow));
            when(workflowMapper.toWorkflowResponse(workflow)).thenReturn(response);

            //Act
            List<WorkflowResponse> result = workflowServiceImpl.getAllWorkflows();

            //Assert
            assertThat(result.size()).isEqualTo(1);
            assertThat(result.getFirst()).isEqualTo(response);
            verify(workflowMapper, times(1)).toWorkflowResponse(workflow);
            verify(workflowRepository, times(1)).findAll();
            verifyNoMoreInteractions(
                    workflowMapper,
                    workflowRepository
            );

        }

        @Test//Sad
        void should_ReturnEmptyListWhenNoWorkflowExists() {
            //Arrange
            when(workflowRepository.findAll()).thenReturn(List.of());

            //Act
            List<WorkflowResponse> result = workflowServiceImpl.getAllWorkflows();

            //Assert
            assertThat(result).isEmpty();

            verify(workflowRepository).findAll();
            verifyNoInteractions(workflowMapper);
            verifyNoMoreInteractions(workflowRepository);
        }
    }

    @DisplayName("Get Workflow")
    @Nested
    class GetWorkflowByIdTests {

        @Test
        void should_ReturnWorkflowByIdWhenWorkflowExists() {
            //Arrange
            Workflow workflow = createWorkflow();

            WorkflowResponse response = createResponse();

            when(workflowRepository.findById(workflow.getId())).thenReturn(Optional.of(workflow));
            when(workflowMapper.toWorkflowResponse(workflow)).thenReturn(response);

            //ACT
            WorkflowResponse result = workflowServiceImpl.getWorkflowById(workflow.getId());

            //Assert
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(workflow.getId());
            assertThat(result.name()).isEqualTo(workflow.getName());
            assertThat(result.description()).isEqualTo(workflow.getDescription());

            verify(workflowMapper).toWorkflowResponse(workflow); //called only once
            verify(workflowRepository).findById(workflow.getId());
        }

        @Test//Sad
        void shouldThrowResourceNotFoundWhenGettingWorkflow() {

            //Arrange

            when(workflowRepository.findById(workflowId)).thenReturn(Optional.empty());


            //ACT +  Assert
            assertThatThrownBy(() -> workflowServiceImpl.getWorkflowById(workflowId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Workflow not found");

            verify(workflowRepository).findById(workflowId);
            verifyNoInteractions(workflowMapper);
        }
    }

    @DisplayName("Update Workflow")
    @Nested
    class UpdateWorkflowTests {
        @Test
        void shouldUpdateWorkflowSuccessfully() {
            //Arrange


            Workflow workflow = createWorkflow();

            WorkflowRequest request =
                    new WorkflowRequest(
                            "Updated Workflow",
                            "Updated Description"
                    );

            Workflow updatedWorkflow = workflow;
            WorkflowResponse response = new WorkflowResponse(
                    workflowId,
                    "Updated Workflow",
                    "Updated Description",
                    updatedWorkflow.getUserId(),
                    updatedWorkflow.getStatus(),
                    LocalDateTime.now(),
                    LocalDateTime.now()
            );

            when(workflowRepository.findById(workflowId)).thenReturn(Optional.of(workflow));
            when(workflowRepository.save(any())).thenReturn(updatedWorkflow);
            when(workflowMapper.toWorkflowResponse(updatedWorkflow)).thenReturn(response);

            //Act
            WorkflowResponse result = workflowServiceImpl.updateWorkflow(workflowId, request);

            //Assert
            assertThat(result).isNotNull();
            assertThat(result.name()).isEqualTo("Updated Workflow");
            assertThat(result.description()).isEqualTo("Updated Description");

            //capture-inspect what was actually saved
            ArgumentCaptor<Workflow> captor = ArgumentCaptor.forClass(Workflow.class);

            verify(workflowRepository).save(captor.capture());

            Workflow savedWorkflow = captor.getValue();

            assertThat(savedWorkflow.getName()).isEqualTo("Updated Workflow");
            assertThat(savedWorkflow.getDescription()).isEqualTo("Updated Description");

            verify(workflowRepository, times(1)).findById(workflowId);
            verify(workflowMapper).toWorkflowResponse(updatedWorkflow);

        }

        @Test//Sad
        void shouldThrowResourceNotFoundWhenUpdatingWorkflow() {

            WorkflowRequest request =
                    new WorkflowRequest(
                            "Updated Workflow",
                            "Updated Description"
                    );

            //Arrange
            when(workflowRepository.findById(workflowId)).thenReturn(Optional.empty());

            //Act + Assert
            assertThatThrownBy(() -> workflowServiceImpl.updateWorkflow(workflowId, request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Workflow not found");

            verify(workflowRepository).findById(workflowId);
            verifyNoInteractions(workflowMapper);
            verifyNoMoreInteractions(workflowRepository);
        }
    }

    @DisplayName("Delete Workflow")
    @Nested
    class DeleteWorkflowTests {
        //For void return-Type methods We Don't Write AssertEquals() we "Verify"
        @Test
        void shouldDeleteWorkflowSuccessfully() {
            //Arrange

            Workflow workflow = createWorkflow();

            when(workflowRepository.findById(workflowId)).thenReturn(Optional.of(workflow));

            //Act
            workflowServiceImpl.deleteWorkflow(workflowId);

            //Assert
            verify(workflowRepository).findById(workflowId);
            verify(workflowRepository).delete(workflow);
        }

        @Test//Sad
        void shouldThrowResourceNotFoundWhenDeletingWorkflow() {
            //Arrange

            when(workflowRepository.findById(workflowId)).thenReturn(Optional.empty());

            //Act + Assert
            assertThatThrownBy(() -> workflowServiceImpl.deleteWorkflow(workflowId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Workflow not found");

            verify(workflowRepository).findById(workflowId);
            verify(workflowRepository, never()).delete(any());

        }
    }

    //Helper Methods
    private WorkflowRequest createRequest(){
         return new WorkflowRequest(
                 "Invoice Approval",
                 "Demo workflow"
         );
    }

    private Workflow createWorkflow(){

        Workflow  workflow = new Workflow();

        workflow.setId(workflowId);
        workflow.setName("Invoice Approval");
        workflow.setDescription("Demo workflow");
        workflow.setUserId(userId);
        workflow.setStatus(WorkflowStatus.ACTIVE);

        return workflow;
    }

    private WorkflowResponse createResponse() {

        return new WorkflowResponse(
                workflowId,
                "Invoice Approval",
                "Demo workflow",
                userId,
                WorkflowStatus.ACTIVE,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

}