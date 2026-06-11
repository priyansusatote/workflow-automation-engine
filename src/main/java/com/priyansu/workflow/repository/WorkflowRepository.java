package com.priyansu.workflow.repository;

import com.priyansu.workflow.dto.WorkflowResponse;
import com.priyansu.workflow.entity.Workflow;
import com.priyansu.workflow.entity.enums.ExecutionStatus;
import com.priyansu.workflow.entity.enums.WorkflowStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface WorkflowRepository extends JpaRepository<Workflow, UUID> {

    boolean existsByNameAndUserId(String name, UUID userId);

    List<Workflow> findByUserId(UUID userId);

    List<UUID> findIdsByUserId(UUID userId);

    @Query("""
                select w.id
                from Workflow w
                where w.userId = :userId
            """)
    List<UUID> findWorkflowIdsByUserId(UUID userId);

    //Dashboard

    long countByUserId(UUID userId);

    long countByUserIdAndStatus(
            UUID userId,
            WorkflowStatus status
    );

}
