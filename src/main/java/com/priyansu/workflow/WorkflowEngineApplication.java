package com.priyansu.workflow;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.Arrays;

//@EnableAsync  // Enables asynchronous method execution capability, Allows methods annotated with @Async to run in a separate thread pool,  instead of blocking the main request thread.

@SpringBootApplication
@EnableKafka
public class WorkflowEngineApplication {

	public static void main(String[] args) {
		SpringApplication.run(WorkflowEngineApplication.class, args);

	}

	@Autowired
	private Environment environment;

	@PostConstruct
	public void init() {
		System.out.println("🔥 ACTIVE PROFILES: " + Arrays.toString(environment.getActiveProfiles()));
	}
}
