package com.priyansu.workflow.repository;

import com.priyansu.workflow.entity.Workflow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WorkflowRepository extends JpaRepository<Workflow, UUID> {

    boolean existsByNameAndUserId(String name, UUID userId);
}
