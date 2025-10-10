package io.mochaapi.client.exception;

/**
 * Exception thrown when JSON serialization or deserialization fails.
 * Provides clear error messages for JSON parsing issues.
 */
public class JsonException extends RuntimeException {
    
    /**
     * Creates a new JsonException with the specified message.
     * 
     * @param message the error message
     */
    public JsonException(String message) {
        super(message);
    }
    
    /**
     * Creates a new JsonException with the specified message and cause.
     * 
     * @param message the error message
     * @param cause the underlying cause
     */
    public JsonException(String message, Throwable cause) {
        super(message, cause);
    }
}
