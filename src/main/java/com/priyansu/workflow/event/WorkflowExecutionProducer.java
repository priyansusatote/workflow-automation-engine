package com.priyansu.workflow.event;


import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowExecutionProducer {

    private final KafkaTemplate<String, WorkflowExecutionEvent> kafkaTemplate;  //KafkaTemplate :Connects to Kafka broker,Serializes data,Sends message to topic

    private static final String TOPIC = "workflow-execution";


    public void sendExecutionEvent(WorkflowExecutionEvent event) {
        log.info("Producing event → workflowId={}, executionId={}",
                event.workflowId(), event.executionId());

        kafkaTemplate.send(TOPIC, event.workflowId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("❌ Kafka send failed", ex);
                    } else {
                        log.info("✅ Kafka send success → offset={}",
                                result.getRecordMetadata().offset());
                    }
                });
    }

}
