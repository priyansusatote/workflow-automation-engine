package com.priyansu.workflow.event;

import java.util.Map;
import java.util.UUID;

public record WorkflowExecutionEvent(  //This is what travels through Kafka
        UUID workflowId,
        UUID executionId,
        Map<String , Object> input
) {
}
