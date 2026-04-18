package com.priyansu.worker;



import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.priyansu")
public class WorkerApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(WorkerApplication.class);
        app.setAdditionalProfiles("worker"); // 🔥 IMPORTANT
        app.run(args);
    }
}