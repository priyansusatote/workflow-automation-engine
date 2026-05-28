package com.priyansu.workflow.service;

import com.fasterxml.jackson.databind.JsonNode;

public interface WorkflowValidationService {

    void validate(JsonNode workflow);
}
