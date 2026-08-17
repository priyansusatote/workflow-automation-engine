package com.priyansu.workflow.entity;

import com.priyansu.workflow.entity.enums.ExecutionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "workflow_execution")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowExecution extends BaseEntity{   //EXECUTION TRACKING && logging & observability

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID workflowId;

    @Enumerated(EnumType.STRING)
    private ExecutionStatus status;

    @Column(nullable = false)
    private boolean processing;

    private String errorMessage;

    private LocalDateTime resumeAt;

    private String waitingNodeId;
}
