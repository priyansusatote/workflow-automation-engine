package com.priyansu.workflow.repository;

import com.priyansu.workflow.entity.TaskExecution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TaskExecutionRepository extends JpaRepository<TaskExecution, UUID> {

    List<TaskExecution> findByWorkflowExecutionIdOrderByCreatedAt(UUID executionId);
}
