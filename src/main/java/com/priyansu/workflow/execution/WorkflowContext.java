package com.priyansu.workflow.execution;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

//context = data flowing through workflow  , [WorkflowContext = shared memory between workflow nodes]
//Why Map? => Flexible, Dynamic keys, Matches JSON
public class WorkflowContext {

    private final Map<String, Object> data = new ConcurrentHashMap<>();


    public WorkflowContext(Map<String, Object> input) { //Constructor
        if (input != null) {
            data.putAll(input);
        }
    }


    public void put(String key, Object value) {

        data.put(key, value);
    }

    public Object get(String key) {

        return data.get(key);
    }

    public Map<String, Object> getData() {
        return new HashMap<>(data);
    }


}
