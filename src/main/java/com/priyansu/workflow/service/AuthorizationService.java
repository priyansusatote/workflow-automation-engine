package com.priyansu.workflow.service;

import java.util.UUID;


//Ownership Validator
public interface AuthorizationService {

    void validateWorkflowOwnership(UUID workflowId);

    void validateExecutionOwnership(UUID executionId);

}