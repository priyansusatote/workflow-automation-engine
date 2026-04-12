package com.priyansu.workflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync  // Enables asynchronous method execution capability, Allows methods annotated with @Async to run in a separate thread pool,  instead of blocking the main request thread.
@SpringBootApplication
public class WorkflowEngineApplication {

	public static void main(String[] args) {
		SpringApplication.run(WorkflowEngineApplication.class, args);
	}

}
