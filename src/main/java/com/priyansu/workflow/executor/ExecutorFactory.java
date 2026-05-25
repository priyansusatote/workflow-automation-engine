package com.priyansu.workflow.executor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class ExecutorFactory {

    private final Map<String, TaskExecutor> executorMap = new HashMap<>();

    public ExecutorFactory(List<TaskExecutor> executors) {
        for (TaskExecutor executor : executors) {
            log.info(//temp
                    "Registering executor type={}",
                    executor.getType()
            );

            executorMap.put(executor.getType(), executor); //"type" → executor mapping [ex:TRIGGER → TriggerTaskExecutor ACTION → ActionTaskExecutor AI_GENERATE → AITaskExecutor   //WHY THIS IS GOOD DESIGN  Because later adding new nodes becomes SUPER easy.  Example:  AI_DECISION WAIT WEBHOOK EMAIL CLASSIFIER
        }
    }

    public TaskExecutor getExecutor(String type) {
        TaskExecutor executor = executorMap.get(type);

        if (executor == null) {
            throw new RuntimeException("No executor found for type " + type);
        }

        return executor;
    }

}
