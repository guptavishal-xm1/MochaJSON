package io.mochaapi.client;

import io.mochaapi.client.exception.ApiException;
import java.util.Map;

/**
 * Interface for executing HTTP requests.
 * Provides a way to abstract the underlying HTTP client implementation.
 */
public interface HttpClientEngine {
    
    /**
     * Executes an HTTP request and returns the response.
     * 
     * @param request the configured request
     * @return ApiResponse containing the result
     * @throws ApiException if the request fails
     */
    ApiResponse execute(ApiRequest request);
}
