package com.propmanager.core.exception;

import lombok.Getter;

/**
 * Abstract root of the platform exception hierarchy.
 *
 * Every domain-specific exception extends either ResourceNotFoundException,
 * ConflictException, or ValidationException — all of which extend this class.
 * The GlobalExceptionHandler maps each subtype to the correct HTTP status.
 *
 * errorCode is a machine-readable string constant (e.g. "ORGANIZATION_NOT_FOUND")
 * returned in the error response body for client-side error handling.
 */
@Getter
public abstract class BaseException extends RuntimeException {

    private final String errorCode;

    protected BaseException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}