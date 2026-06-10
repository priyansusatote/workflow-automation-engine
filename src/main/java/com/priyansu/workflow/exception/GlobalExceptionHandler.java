package com.priyansu.workflow.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, Object> handleNotFound(ResourceNotFoundException ex) {
        return Map.of(
                "timestamp", LocalDateTime.now(),
                "status", 404,
                "message", ex.getMessage()
        );
    }

    @ExceptionHandler(DuplicateResourceException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, Object> handleDuplicate(DuplicateResourceException ex) {
        return Map.of(
                "timestamp", LocalDateTime.now(),
                "status", 409,
                "message", ex.getMessage()
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex
    ) {

        ErrorResponse error = new ErrorResponse(
                ex.getMessage(),
                LocalDateTime.now(),
                HttpStatus.FORBIDDEN.value()

        );

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(error);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, Object> handleGeneric(Exception ex) {
        return Map.of(
                "timestamp", LocalDateTime.now(),
                "status", 500,
                "message", ex.getMessage()
        );
    }


    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<?> handleUserExists(UserAlreadyExistsException ex) {
        return build(400, ex.getMessage());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<?> handleInvalid(InvalidCredentialsException ex) {
        return build(401, ex.getMessage());
    }

    private ResponseEntity<?> build(int status, String message) {
        return ResponseEntity.status(status)
                .body(Map.of(
                        "status", status,
                        "error", HttpStatus.valueOf(status).getReasonPhrase(),
                        "message", message
                ));
    }

    @ExceptionHandler(WorkflowValidationException.class)
    public ResponseEntity<com.priyansu.workflow.exception.ErrorResponse> handleWorkflowValidation(
            WorkflowValidationException ex
    ) {

        return ResponseEntity.badRequest()
                .body(
                        new ErrorResponse(
                                ex.getMessage(),
                                LocalDateTime.now(),
                                400
                        )
                );
    }
}