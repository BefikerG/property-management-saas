package com.propmanager.core.exception.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Standardized error response envelope returned by the GlobalExceptionHandler
 * for every non-2xx response across the entire platform.
 */
@Data
@Builder
public class ErrorResponse {

    private String          errorCode;
    private String          message;
    private LocalDateTime   timestamp;
    private List<String>    validationErrors;

    public static ErrorResponse of(String errorCode, String message) {
        return ErrorResponse.builder()
            .errorCode(errorCode)
            .message(message)
            .timestamp(LocalDateTime.now())
            .build();
    }

    public static ErrorResponse ofValidation(String errorCode, List<String> errors) {
        return ErrorResponse.builder()
            .errorCode(errorCode)
            .message("Request validation failed.")
            .validationErrors(errors)
            .timestamp(LocalDateTime.now())
            .build();
    }
}