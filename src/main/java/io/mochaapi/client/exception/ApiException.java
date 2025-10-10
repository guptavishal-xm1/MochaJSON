package io.mochaapi.client.exception;

/**
 * Base exception for API-related errors.
 * Thrown when HTTP requests fail or encounter network issues.
 */
public class ApiException extends RuntimeException {
    
    /**
     * Creates a new ApiException with the specified message.
     * 
     * @param message the error message
     */
    public ApiException(String message) {
        super(message);
    }
    
    /**
     * Creates a new ApiException with the specified message and cause.
     * 
     * @param message the error message
     * @param cause the underlying cause
     */
    public ApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
