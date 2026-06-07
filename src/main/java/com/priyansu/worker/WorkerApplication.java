package com.priyansu.worker;



import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.priyansu")
@EnableScheduling
public class WorkerApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(WorkerApplication.class);
        app.setAdditionalProfiles("worker"); // 🔥 IMPORTANT
        app.run(args);
    }
}