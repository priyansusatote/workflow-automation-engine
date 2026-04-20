package com.priyansu.workflow.entity;

import com.priyansu.workflow.entity.enums.ExecutionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "task_execution")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaskExecution extends BaseEntity { //for: each NODE-LEVEL LOGGING & EXECUTION HISTORY

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID workflowExecutionId;

    private String nodeId;
    private String nodeType;

    @Enumerated(EnumType.STRING)
    private ExecutionStatus status;

    @Column(columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> outputData; //Store context snapshot at each node

    private String logMessage;
}
