package io.mochaapi.client;

import io.mochaapi.client.exception.ApiException;

/**
 * Interface for intercepting HTTP responses after they are received.
 * Allows developers to add logging, error handling, or modify response data.
 * 
 * <p>Example usage:</p>
 * <pre>
 * ApiClient client = new ApiClient.Builder()
 *     .addResponseInterceptor(response -> {
 *         System.out.println("Received response: " + response.code());
 *         return response;
 *     })
 *     .build();
 * </pre>
 * 
 * @since 1.1.0
 */
@FunctionalInterface
public interface ResponseInterceptor {
    
    /**
     * Intercepts and optionally modifies a response after it is received.
     * 
     * @param response the original response
     * @return the (possibly modified) response
     * @throws ApiException if the interceptor needs to abort processing
     */
    ApiResponse intercept(ApiResponse response) throws ApiException;
    
    /**
     * Creates a simple logging interceptor that logs response details.
     * 
     * @param logger the logger function to use
     * @return a response interceptor that logs responses
     */
    static ResponseInterceptor logging(java.util.function.Consumer<String> logger) {
        return response -> {
            logger.accept(String.format("Response: %d %s", response.code(), response.getStatusDescription()));
            if (!response.headers().isEmpty()) {
                logger.accept("Headers: " + response.headers());
            }
            String body = response.body();
            if (body != null && !body.isEmpty()) {
                int bodyLength = body.length();
                if (bodyLength > 200) {
                    logger.accept("Body: " + body.substring(0, 200) + "... (" + bodyLength + " chars)");
                } else {
                    logger.accept("Body: " + body);
                }
            }
            return response;
        };
    }
    
    /**
     * Creates an error handling interceptor that throws exceptions for error status codes.
     * 
     * @return a response interceptor that handles errors
     */
    static ResponseInterceptor throwOnError() {
        return response -> {
            if (response.isError()) {
                throw new ApiException(String.format("HTTP %d: %s", 
                    response.code(), response.getStatusDescription()));
            }
            return response;
        };
    }
    
    /**
     * Creates a retry interceptor that automatically retries on certain status codes.
     * 
     * @param retryableStatusCodes the status codes that should trigger a retry
     * @param maxRetries the maximum number of retries
     * @return a response interceptor that handles retries
     */
    static ResponseInterceptor retryOnStatus(int[] retryableStatusCodes, int maxRetries) {
        return response -> {
            for (int code : retryableStatusCodes) {
                if (response.code() == code) {
                    // Note: This is a simplified retry interceptor
                    // A full implementation would need access to the original request
                    // and retry mechanism. This is just for demonstration.
                    throw new ApiException("Retry needed for status code: " + code);
                }
            }
            return response;
        };
    }
}
