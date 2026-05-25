package com.priyansu.workflow.dto.ai;

//provider-independent AI request
public record AIRequest(

        String prompt,
        String model,
        Double temperature

        ) {
}
