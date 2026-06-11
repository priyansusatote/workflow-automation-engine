package com.priyansu.workflow.repository;

import com.priyansu.workflow.entity.WorkflowExecution;
import com.priyansu.workflow.entity.enums.ExecutionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface WorkflowExecutionRepository extends JpaRepository<WorkflowExecution, UUID> {

    // Finds all workflow executions that are in WAITING status and whose resume time has already passed.
    List<WorkflowExecution> findByStatusAndResumeAtBefore(ExecutionStatus status, LocalDateTime now);

    // Execution Monitoring APIs

    Page<WorkflowExecution> findAll(Pageable pageable);

    Page<WorkflowExecution> findByStatus(
            ExecutionStatus status,
            Pageable pageable
    );

    Page<WorkflowExecution> findByWorkflowId(
            UUID workflowId,
            Pageable pageable
    );

    Page<WorkflowExecution> findByWorkflowIdAndStatus(
            UUID workflowId,
            ExecutionStatus status,
            Pageable pageable
    );


    //Find all workflow executions whose workflowId exists in the given list.
    Page<WorkflowExecution> findByWorkflowIdIn(
            List<UUID> workflowIds,
            Pageable pageable
    );

    //Find all executions:  whose workflowId is in the list AND whose status matches
    Page<WorkflowExecution> findByWorkflowIdInAndStatus(
            List<UUID> workflowIds,
            ExecutionStatus status,
            Pageable pageable
    );

    //for Dashboard

    long countByWorkflowIdIn(
            List<UUID> workflowIds
    );

    long countByWorkflowIdInAndStatus(
            List<UUID> workflowIds,
            ExecutionStatus status
    );

    long countByWorkflowId(UUID workflowId);

    long countByWorkflowIdAndStatus(
            UUID workflowId,
            ExecutionStatus status
    );

}
