package com.priyansu.workflow.repository;

import com.priyansu.workflow.entity.WorkflowExecution;
import com.priyansu.workflow.entity.enums.ExecutionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface WorkflowExecutionRepository extends JpaRepository<WorkflowExecution, UUID> {

   // Finds all workflow executions that are in WAITING status and whose resume time has already passed.
    List<WorkflowExecution> findByStatusAndResumeAtBefore(ExecutionStatus status, LocalDateTime now);
}
