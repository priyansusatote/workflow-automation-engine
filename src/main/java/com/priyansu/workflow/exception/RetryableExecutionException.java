package com.priyansu.workflow.exception;



public class RetryableExecutionException
        extends RuntimeException {

    public RetryableExecutionException(String message) {
        super(message);
    }

    public RetryableExecutionException(
            String message,
            Throwable cause
    ) {
        super(message, cause);
    }
}