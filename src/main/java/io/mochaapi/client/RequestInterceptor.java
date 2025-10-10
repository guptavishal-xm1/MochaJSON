package io.mochaapi.client;

import io.mochaapi.client.exception.ApiException;
import java.util.Map;

/**
 * Interface for intercepting and modifying HTTP requests before they are sent.
 * Allows developers to add logging, authentication, or modify request details.
 * 
 * <p>Example usage:</p>
 * <pre>
 * ApiClient client = new ApiClient.Builder()
 *     .addRequestInterceptor(request -> {
 *         System.out.println("Sending request to: " + request.getUrl());
 *         return request;
 *     })
 *     .build();
 * </pre>
 * 
 * @since 1.1.0
 */
@FunctionalInterface
public interface RequestInterceptor {
    
    /**
     * Intercepts and optionally modifies a request before it is sent.
     * 
     * @param request the original request
     * @return the (possibly modified) request to send
     * @throws ApiException if the interceptor needs to abort the request
     */
    ApiRequest intercept(ApiRequest request) throws ApiException;
    
    /**
     * Creates a simple logging interceptor that logs request details.
     * 
     * @param logger the logger function to use
     * @return a request interceptor that logs requests
     */
    static RequestInterceptor logging(java.util.function.Consumer<String> logger) {
        return request -> {
            logger.accept(String.format("Request: %s %s", request.getMethod(), request.getUrl()));
            if (!request.getHeaders().isEmpty()) {
                logger.accept("Headers: " + request.getHeaders());
            }
            if (!request.getQueryParams().isEmpty()) {
                logger.accept("Query params: " + request.getQueryParams());
            }
            if (request.getBody() != null) {
                logger.accept("Body: " + request.getBody());
            }
            return request;
        };
    }
    
    /**
     * Creates an authentication interceptor that adds a Bearer token to requests.
     * 
     * @param tokenProvider function that provides the current token
     * @return a request interceptor that adds authorization headers
     */
    static RequestInterceptor bearerAuth(java.util.function.Supplier<String> tokenProvider) {
        return request -> {
            String token = tokenProvider.get();
            if (token != null && !token.isEmpty()) {
                request.header("Authorization", "Bearer " + token);
            }
            return request;
        };
    }
    
    /**
     * Creates a header interceptor that adds custom headers to all requests.
     * 
     * @param headers the headers to add
     * @return a request interceptor that adds headers
     */
    static RequestInterceptor addHeaders(Map<String, String> headers) {
        return request -> {
            headers.forEach(request::header);
            return request;
        };
    }
}
