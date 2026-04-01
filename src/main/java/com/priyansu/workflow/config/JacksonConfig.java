package com.priyansu.workflow.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {  //explicitly telling springBoot :“Use this ObjectMapper”

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}