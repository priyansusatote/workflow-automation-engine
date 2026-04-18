package com.priyansu.workflow.config;

import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@EnableKafka
@Configuration
public class KafkaErrorConfig { // KAFKA ERROR HANDLER CONFIG

    @Bean
    public DefaultErrorHandler errorHandler(KafkaTemplate<Object, Object> kafkaTemplate) {

        //DLQ(Dead Letter Queue) Publisher ["If retries fail → send message to this topic=DLQ"]
        DeadLetterPublishingRecoverer recoverer =
                new DeadLetterPublishingRecoverer(kafkaTemplate,
                        (record, ex) -> new TopicPartition("workflow-execution-dlq", record.partition()));


        //Retry 3 Times with 2s delay (what this does => Retry 3 times → then send to DLQ)
        FixedBackOff backOff = new FixedBackOff(2000L, 2);  //1 original + 2 Retry = 3 retry

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, backOff);  //"After backOff retries are exhausted → use recoverer → send to DLQ"

        return errorHandler;

    }
}
