package com.priyansu.workflow.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;

import org.hibernate.type.SqlTypes;


import java.util.UUID;

@Entity
@Table(name = "workflow_definition")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowDefinition extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "workflow_id", nullable = false)
    private UUID workflowId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "definition_json", columnDefinition = "jsonb", nullable = false) //JSONB ("JSON Binary") is a data type in PostgreSQL and other databases that stores JSON data in an optimized binary format
    private JsonNode definitionJson;

    private Integer version;
}
