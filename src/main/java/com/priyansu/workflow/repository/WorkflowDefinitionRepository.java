package com.priyansu.workflow.repository;

import com.priyansu.workflow.entity.WorkflowDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WorkflowDefinitionRepository extends JpaRepository<WorkflowDefinition, UUID> {

    //Returns latest version {When user runs workflow → always latest logic}
    Optional<WorkflowDefinition> findTopByWorkflowIdOrderByVersionDesc(UUID workflowId);
}
