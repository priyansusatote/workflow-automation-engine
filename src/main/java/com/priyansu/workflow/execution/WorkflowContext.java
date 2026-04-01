package com.priyansu.workflow.execution;

import java.util.HashMap;
import java.util.Map;

//context = data flowing through workflow
//Why Map? => Flexible, Dynamic keys, Matches JSON
public class WorkflowContext {

    private final Map<String, Object> data = new HashMap<>();


    public void put(String key, Object value) {

        data.put(key, value);
    }

    public Object get(String key) {

        return data.get(key);
    }

    public Map<String, Object> getAll() {

        return data;
    }

}
