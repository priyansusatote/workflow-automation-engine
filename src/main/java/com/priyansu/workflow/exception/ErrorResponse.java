package com.priyansu.workflow.exception;

import java.time.LocalDateTime;

public record ErrorResponse(
        String message,
        LocalDateTime timestamp,
        int status
) {}