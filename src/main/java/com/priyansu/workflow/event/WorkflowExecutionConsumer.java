package com.priyansu.workflow.event;

import com.priyansu.workflow.service.WorkflowExecutionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowExecutionConsumer {

    private final WorkflowExecutionService executionService;

//    // ✅ ADD THIS METHOD
//    @PostConstruct
//    public void init() {
//        log.info("🔥 Consumer bean initialized");
//    }

    @KafkaListener(topics = "workflow-execution", groupId = "workflow-execution-group")
    public void consume(WorkflowExecutionEvent event) {


        log.info("Kafka received -> executionId={}", event.executionId());

        try {
            executionService.executeWorkflowFromKafka(
                    event.workflowId(),
                    event.executionId(),
                    event.input()
            );

        } catch (Exception e) {
            log.error("Execution failed → executionId={}", event.executionId(), e);
            throw e; // important for retry (next step)
        }
    }

}
