package com.priyansu.workflow.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "workflow_webhook")
public class WorkflowWebhook {


    @Id @GeneratedValue
    private UUID id;

    private UUID workflowId;

    @Column(unique = true)
    private String webhookKey;

   // private String description; //added and removed for Learning Flyway (v2, v3 migrations)

}