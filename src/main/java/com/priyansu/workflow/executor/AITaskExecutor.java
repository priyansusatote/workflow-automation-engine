package com.priyansu.workflow.executor;

import com.fasterxml.jackson.databind.JsonNode;
import com.priyansu.workflow.execution.WorkflowContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AITaskExecutor implements TaskExecutor {

    @Override
    public String getType() {
        return "";
    }

    @Override
    public void execute(JsonNode node, WorkflowContext context) { //1. Read prompt from node JSON 2. Build AIRequest 3. Call AIService 4. Store result in WorkflowContext

    }
}
