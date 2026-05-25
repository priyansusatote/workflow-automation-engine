package com.priyansu.workflow.service;


import com.priyansu.workflow.dto.ai.AIRequest;
import com.priyansu.workflow.dto.ai.AIResponse;

public interface AIService { //will be used for all llm models implementation

    AIResponse generate(AIRequest request);
}
