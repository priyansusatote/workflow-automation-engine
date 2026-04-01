package com.priyansu.workflow.executor;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ExecutorFactory {

    private final Map<String, TaskExecutor> executorMap = new HashMap<>();

    public ExecutorFactory(List<TaskExecutor> executors) {
        for (TaskExecutor executor : executors) {
            executorMap.put(executor.getType(), executor);
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
