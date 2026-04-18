package com.priyansu.worker.consumer;

import com.priyansu.workflow.event.WorkflowExecutionEvent;
import com.priyansu.workflow.service.WorkflowExecutionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Profile("worker")
@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowExecutionConsumer {  //Spring auto uses error handler (our kafkaErrorConfig)

    private final WorkflowExecutionService executionService;


    @KafkaListener(topics = "workflow-execution",
                   groupId = "worker-group")
    public void consume(WorkflowExecutionEvent event) {

        log.info("Kafka Worker received -> executionId={}", event.executionId());

        try {
//            //Temporary Test failure : if (true) throw new RuntimeException("Test failure");

            executionService.executeWorkflowFromKafka(
                    event.workflowId(),
                    event.executionId(),
                    event.input()
            );

        } catch (Exception e) {
            log.error("Execution failed → executionId={}", event.executionId(), e);
            throw e; // Required for retry (DLQ : which I handled in KafkaErrorConfig)
        }
    }

}
