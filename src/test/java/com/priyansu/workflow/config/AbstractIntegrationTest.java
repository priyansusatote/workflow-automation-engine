package com.priyansu.workflow.config;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;

import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;


@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true) //If Docker isn't running: Tests Skipped
public abstract class AbstractIntegrationTest { //reusable infrastructure where every future integration-style test will inherit its container setup.

    /**
     * Shared PostgreSQL Testcontainer used by all integration-style tests.
     * Starts automatically before the first test and stops after the test suite.
     */

    @Container
    @ServiceConnection //Spring Boot automatically configures: datasource, URL, username, password, driver
    protected static final PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:16-alpine");



}
