package com.propmanager.core.exception;

/**
 * Thrown when a write operation would violate a uniqueness or state
 * constraint — for example, registering an organization name that is
 * already taken, or suspending an organization that is already suspended.
 *
 * Maps to HTTP 409 Conflict via GlobalExceptionHandler.
 */
public class ConflictException extends BaseException {

    public ConflictException(String errorCode, String message) {
        super(errorCode, message);
    }
}