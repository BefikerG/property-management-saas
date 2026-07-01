package com.propmanager.core.exception;

/**
 * Thrown when a requested resource does not exist or belongs to a
 * different tenant (in which case 404 is returned rather than 403
 * to avoid confirming the existence of another tenant's data).
 *
 * Maps to HTTP 404 Not Found via GlobalExceptionHandler.
 */
public class ResourceNotFoundException extends BaseException {

    public ResourceNotFoundException(String errorCode, String message) {
        super(errorCode, message);
    }
}