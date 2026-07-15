package com.priyansu.workflow.repository;

import com.priyansu.workflow.config.AbstractIntegrationTest;
import com.priyansu.workflow.entity.Workflow;
import com.priyansu.workflow.entity.enums.WorkflowStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.UUID;

//This talks to a real PostgreSQL database.
//What happens after each Test  finishes? ->@DataJpaTest is automatically "transactional". Every test runs inside its own transaction.
//Spring automatically performs a ROLLBACK after the test completes.

@DataJpaTest //loads only: JPA Hibernate Repositories EntityManager
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class WorkflowRepositoryIT extends AbstractIntegrationTest{

    @Autowired //Because we want the real Spring bean (Repository), not a mock.
    private WorkflowRepository workflowRepository;


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
        boolean exists =  workflowRepository.existsByNameAndUserId(
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

        workflowRepository.saveAndFlush(workflow); //save to DB

        //Act
        boolean exists = workflowRepository.existsByNameAndUserId(
                "False Name",
                userId
        );

        //Assert
        assertThat(exists).isFalse();
    }

}