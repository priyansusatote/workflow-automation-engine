package com.priyansu.workflow.repository;

import com.priyansu.workflow.config.AbstractIntegrationTest;
import com.priyansu.workflow.entity.Workflow;
import com.priyansu.workflow.entity.enums.WorkflowStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.UUID;

//This talks to a real PostgreSQL database.
//What happens after each Test  finishes? ->@DataJpaTest is automatically "transactional". Every test runs inside its own transaction.
//Spring automatically performs a ROLLBACK after the test completes.

@DataJpaTest //loads only: JPA Hibernate Repositories EntityManager
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class WorkflowRepositoryIT extends AbstractIntegrationTest{

    @Autowired //Because we want the real Spring bean (Repository), not a mock.
    private WorkflowRepository workflowRepository;


    @Nested
    class ExistsByNameAndUserIdTest {

        @Test
        @DisplayName("Should return true when workflow exists for given user")
        void testExistsByNameAndUserId_ShouldReturnTrue() {

            //Arrange
            UUID userId = UUID.randomUUID();

            Workflow workflow = new Workflow();
            workflow.setName("Invoice Workflow");
            workflow.setUserId(userId);
            workflow.setStatus(WorkflowStatus.ACTIVE);

            workflowRepository.save(workflow); //using Real Repository

            //Act
            boolean exists = workflowRepository.existsByNameAndUserId(
                    "Invoice Workflow",
                    userId
            );

            //Assert
            assertThat(exists).isTrue();

        }

        @Test //Sad
        @DisplayName("Should Return False for Workflow not Exists for Given User")
        void test_existsByNameAndUserId_ShouldReturnFalse() {
            //Arrange
            UUID userId = UUID.randomUUID();

            Workflow workflow = new Workflow();
            workflow.setName("Invoice Workflow");
            workflow.setUserId(userId);
            workflow.setStatus(WorkflowStatus.ACTIVE);

            workflowRepository.save(workflow); //save to DB

            //Act
            boolean exists = workflowRepository.existsByNameAndUserId(
                    "False Name",
                    userId
            );

            //Assert
            assertThat(exists).isFalse();
        }
    }

    @Nested
    class FindByUserIdTest {

        @Test
        @DisplayName("Should return only workflows belonging to the given user")
        void shouldReturnOnlyUserWorkflows() {

            //Arrange

            UUID user1 = UUID.randomUUID();
            UUID user2 = UUID.randomUUID();

            //Save 4 Workflows
            Workflow workflow1 = saveWorkflow(
                            "Invoice",
                            user1,
                            WorkflowStatus.ACTIVE);

            Workflow workflow2 = saveWorkflow(
                            "Email",
                            user1,
                            WorkflowStatus.ACTIVE);

            Workflow workflow3 = saveWorkflow(
                            "Slack",
                            user1,
                            WorkflowStatus.INACTIVE);

            Workflow workflow4 =saveWorkflow(
                    "HR",
                    user2,
                    WorkflowStatus.ACTIVE);


            //Act
            List<Workflow> workflows = workflowRepository.findByUserId(user1);


            //Assert
            assertThat(workflows.size()).isEqualTo(3);

            assertThat(workflows)
                    .extracting(Workflow::getName)
                    .containsExactlyInAnyOrder("Invoice", "Email", "Slack");

            assertThat(workflows)
                    .allMatch(
                            workflow -> workflow.getUserId().equals(user1));


        }

        @Test //Sad
        @DisplayName("Should return empty list when user has no workflows")
        void shouldReturnEmptyListWhenUserHasNoWorkflows() {

            //Arrange
            UUID requestedUserId = UUID.randomUUID();
            UUID existingUserId = UUID.randomUUID();

            Workflow workflow = new  Workflow();

                    workflow.setUserId(existingUserId);
                    workflow.setName("Invoice");
                    workflow.setStatus(WorkflowStatus.ACTIVE);



            workflowRepository.save(workflow);


            //Act
            List<Workflow> workflows = workflowRepository.findByUserId(requestedUserId);

            //Assert
            assertThat(workflows).isEmpty();
        }
    }

    @Nested
    class FindWorkflowIdsByUserIdTest {

        @Test
        @DisplayName("Should Return List of WorkflowIds Belonging to Given User")
        void shouldReturnWorkflowIdsSuccessfully_forGivenUser() {

            //Arrange

            UUID user1 = UUID.randomUUID();
            UUID user2 = UUID.randomUUID();


            //Save 4 Workflows

            Workflow workflow1 = saveWorkflow(            //do not Set .id for entity which The entity uses @GeneratedValue,
                            "Invoice",
                            user1,
                            WorkflowStatus.ACTIVE
                    );

            Workflow workflow2 = saveWorkflow(
                            "Email",
                            user1,
                            WorkflowStatus.ACTIVE
                    );

            Workflow workflow3 = saveWorkflow(
                    "HR",
                    user2,
                    WorkflowStatus.ACTIVE
            );


            //Act
            List<UUID> results = workflowRepository.findWorkflowIdsByUserId(user1);

            //Assert
            assertThat(results.size()).isEqualTo(2);
            assertThat(results).containsExactlyInAnyOrder(workflow1.getId(), workflow2.getId());

        }

        @Test //sad
        @DisplayName("Should Return Empty List of WorkflowIds for 0 Workflows Exists for That User")
        void shouldReturnEmptyListOfWorkFlowIds_WhenUserHasNoWorkflowIds() {


            //Arrange
            UUID requestedUserId = UUID.randomUUID();
            UUID existingUserId = UUID.randomUUID();

            Workflow workflow = new  Workflow();

            workflow.setUserId(existingUserId);
            workflow.setName("Invoice");
            workflow.setStatus(WorkflowStatus.ACTIVE);


            workflowRepository.save(workflow);

            //Act
            List<UUID> results = workflowRepository.findWorkflowIdsByUserId(requestedUserId);


            //Assert
            assertThat(results).isEmpty();

        }
    }

    @Nested
    class CountByUserIdTest {


        @Test
        @DisplayName("Should return total workflow count for given user")
        void shouldReturnTotalWorkflowCountSuccessfully_forGivenUser() {

            //Arrange
            UUID user1 = UUID.randomUUID();
            UUID user2 = UUID.randomUUID();

            Workflow workflow1 = saveWorkflow(
                    "Invoice",
                    user1,
                    WorkflowStatus.ACTIVE);

            Workflow workflow2 = saveWorkflow(
                    "Email",
                    user1,
                    WorkflowStatus.ACTIVE);

            Workflow workflow3 = saveWorkflow(
                    "Slack",
                    user1,
                    WorkflowStatus.INACTIVE);

            Workflow workflow4 =saveWorkflow(
                    "HR",
                    user2,
                    WorkflowStatus.ACTIVE);

            //Act
            long total = workflowRepository.countByUserId(user1);

            //Assert
            assertThat(total).isEqualTo(3);

        }

        @Test //Sad
        @DisplayName("Should Return Zero when user has no Workflows")
        void shouldReturnZeroWhenUserHasNoWorkflows() {

            //Arrange
            UUID userId = UUID.randomUUID();

            //Act
            long total = workflowRepository.countByUserId(userId);

            //Assert
            assertThat(total).isZero();

        }
    }

    @Nested
    class CountByUserIdAndStatusTest {


        @Test //For ACTIVE Status
        @DisplayName("Should return active workflow count for given user")
        void shouldReturnActiveWorkflowCountForUser() {

            //Arrange
            UUID user1 = UUID.randomUUID();
            UUID user2 = UUID.randomUUID();

            Workflow workflow1 = saveWorkflow(
                    "Invoice",
                    user1,
                    WorkflowStatus.ACTIVE);

            Workflow workflow2 = saveWorkflow(
                    "Email",
                    user1,
                    WorkflowStatus.ACTIVE);

            Workflow workflow3 = saveWorkflow(
                    "Slack",
                    user1,
                    WorkflowStatus.INACTIVE);

            Workflow workflow4 =saveWorkflow(
                    "HR",
                    user2,
                    WorkflowStatus.ACTIVE);


            //Act
            long total = workflowRepository.countByUserIdAndStatus(user1, WorkflowStatus.ACTIVE);


            //Assert
            assertThat(total).isEqualTo(2);


        }

        @Test //For INACTIVE Status
        @DisplayName("Should return inactive workflow count for given user")
        void shouldReturnInActiveWorkflowCountForUser() {

            //Arrange
            UUID user1 = UUID.randomUUID();
            UUID user2 = UUID.randomUUID();

            Workflow workflow1 = saveWorkflow(
                    "Invoice",
                    user1,
                    WorkflowStatus.ACTIVE);

            Workflow workflow2 = saveWorkflow(
                    "Email",
                    user1,
                    WorkflowStatus.ACTIVE);

            Workflow workflow3 = saveWorkflow(
                    "Slack",
                    user1,
                    WorkflowStatus.INACTIVE);

            Workflow workflow4 =saveWorkflow(
                    "HR",
                    user2,
                    WorkflowStatus.ACTIVE);

            //Act
            long total = workflowRepository.countByUserIdAndStatus(user1, WorkflowStatus.INACTIVE);


            //Assert
            assertThat(total).isEqualTo(1);


        }
    }


    //Helper Methods To Reduce BoilerPlate Codes
    private Workflow saveWorkflow(
            String name,
            UUID userId,
            WorkflowStatus status
    ) {

        Workflow workflow = new Workflow();

        workflow.setName(name);
        workflow.setUserId(userId);
        workflow.setStatus(status);

        return workflowRepository.saveAndFlush(workflow);
    }

      /* using this helper (Arrange Becomes)
    Workflow workflow1 =
           saveWorkflow(
                   "Invoice",
                    user1,
                    ACTIVE
                  );
*/
    }


