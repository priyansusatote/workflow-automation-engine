package com.priyansu.workflow.service;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

public interface WorkflowValidationService {

    List<String> validate(JsonNode workflow);
}
