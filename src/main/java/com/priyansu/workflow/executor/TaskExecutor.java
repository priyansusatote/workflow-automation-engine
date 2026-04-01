package com.priyansu.workflow.executor;

import com.fasterxml.jackson.databind.JsonNode;
import com.priyansu.workflow.execution.WorkflowContext;

//what this does: Each node type has its own class , No if-else mess , Easily extensible
public interface TaskExecutor {

    String getType(); //TRIGGER, ACTION, etc...

    void execute(JsonNode node, WorkflowContext context);
}
