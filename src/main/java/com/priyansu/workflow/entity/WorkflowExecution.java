package com.priyansu.workflow.entity;

import com.priyansu.workflow.entity.enums.ExecutionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    private String errorMessage;
}
