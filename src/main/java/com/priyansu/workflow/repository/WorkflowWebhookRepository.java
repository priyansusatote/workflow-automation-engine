package com.priyansu.workflow.repository;

import com.priyansu.workflow.entity.WorkflowWebhook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkflowWebhookRepository extends JpaRepository<WorkflowWebhook, UUID> {


    Optional<WorkflowWebhook> findByWebhookKey(String webhookKey);

    Optional<WorkflowWebhook> findByWorkflowId(UUID workflowId);
}
