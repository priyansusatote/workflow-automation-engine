package com.priyansu.workflow.dto;

import jakarta.validation.constraints.NotBlank;


public record WorkflowRequest(

        @NotBlank(message = "Name is required")
        String name,

        String description

) {
}
