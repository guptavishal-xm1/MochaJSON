package io.mochaapi.client;

import io.mochaapi.client.internal.DefaultHttpClientEngine;
import io.mochaapi.client.exception.ApiException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Main client for making HTTP API requests with advanced configuration.
 * Provides a builder pattern for configuring timeouts, interceptors, logging, and more.
 * 
 * <p>Example usage:</p>
 * <pre>
 * ApiClient client = new ApiClient.Builder()
 *     .connectTimeout(Duration.ofSeconds(10))
 *     .readTimeout(Duration.ofSeconds(30))
 *     .enableLogging()
 *     .addRequestInterceptor(RequestInterceptor.bearerAuth(() -> getToken()))
 *     .build();
 * 
 * ApiResponse response = client.get("https://api.example.com/data")
 *     .query("page", 1)
 *     .execute();
 * </pre>
 * 
 * @since 1.2.0
 */
public class ApiClient {
    
    private final HttpClientEngine engine;
    private final Executor executor;
    private final List<RequestInterceptor> requestInterceptors;
    private final List<ResponseInterceptor> responseInterceptors;
    private final boolean loggingEnabled;
    private final BasicRetry retry;
    private final boolean allowLocalhost;
    
    private ApiClient(Builder builder) {
        this.engine = builder.engine;
        this.executor = builder.executor;
        this.requestInterceptors = new ArrayList<>(builder.requestInterceptors);
        this.responseInterceptors = new ArrayList<>(builder.responseInterceptors);
        this.loggingEnabled = builder.loggingEnabled;
        this.retry = builder.retry;
        this.allowLocalhost = builder.allowLocalhost;
    }
    
    /**
     * Creates a GET request to the specified URL.
     * 
     * @param url the target URL
     * @return ApiRequest builder for chaining
     */
    public ApiRequest get(String url) {
        return new ApiRequest(url, "GET", this);
    }
    
    /**
     * Creates a POST request to the specified URL.
     * 
     * @param url the target URL
     * @return ApiRequest builder for chaining
     */
    public ApiRequest post(String url) {
        return new ApiRequest(url, "POST", this);
    }
    
    /**
     * Creates a PUT request to the specified URL.
     * 
     * @param url the target URL
     * @return ApiRequest builder for chaining
     */
    public ApiRequest put(String url) {
        return new ApiRequest(url, "PUT", this);
    }
    
    /**
     * Creates a DELETE request to the specified URL.
     * 
     * @param url the target URL
     * @return ApiRequest builder for chaining
     */
    public ApiRequest delete(String url) {
        return new ApiRequest(url, "DELETE", this);
    }
    
    /**
     * Creates a PATCH request to the specified URL.
     * 
     * @param url the target URL
     * @return ApiRequest builder for chaining
     */
    public ApiRequest patch(String url) {
        return new ApiRequest(url, "PATCH", this);
    }
    
    /**
     * Executes a request synchronously and returns the response.
     * Applies retry logic if configured.
     * 
     * @param request the configured request
     * @return ApiResponse containing the result
     * @throws ApiException if the request fails
     */
    public ApiResponse execute(ApiRequest request) {
        return executeWithRetry(request, 1);
    }
    
    /**
     * Executes a request with retry logic if configured.
     * 
     * @param request the configured request
     * @param attemptNumber current attempt number (1-based)
     * @return ApiResponse containing the result
     * @throws ApiException if the request fails
     */
    private ApiResponse executeWithRetry(ApiRequest request, int attemptNumber) {
        try {
            ApiRequest processedRequest = applyRequestInterceptors(request);
            ApiResponse response = engine.execute(processedRequest);
            return applyResponseInterceptors(response);
        } catch (Exception e) {
            // Check if we should retry
            if (retry != null && retry.shouldRetry(attemptNumber)) {
                try {
                    Thread.sleep(retry.getDelay().toMillis());
                    return executeWithRetry(request, attemptNumber + 1);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new ApiException("Request interrupted during retry", ie);
                }
            }
            
            // Re-throw the original exception
            if (e instanceof ApiException) {
                throw (ApiException) e;
            } else {
                throw new ApiException("Request failed: " + e.getMessage(), e);
            }
        }
    }
    
    /**
     * Executes a request asynchronously and returns a CompletableFuture.
     * 
     * @param request the configured request
     * @return CompletableFuture containing the response
     */
    public CompletableFuture<ApiResponse> executeAsync(ApiRequest request) {
        return CompletableFuture.supplyAsync(() -> execute(request), executor);
    }
    
    /**
     * Executes a request asynchronously with a callback.
     * 
     * @param request the configured request
     * @param callback function to handle the response
     */
    public void executeAsync(ApiRequest request, Consumer<ApiResponse> callback) {
        executeAsync(request)
            .thenAccept(callback)
            .exceptionally(throwable -> {
                throw new ApiException("Async request failed: " + throwable.getMessage(), throwable);
            });
    }
    
    private ApiRequest applyRequestInterceptors(ApiRequest request) {
        ApiRequest processedRequest = request;
        for (RequestInterceptor interceptor : requestInterceptors) {
            processedRequest = interceptor.intercept(processedRequest);
        }
        return processedRequest;
    }
    
    private ApiResponse applyResponseInterceptors(ApiResponse response) {
        ApiResponse processedResponse = response;
        for (ResponseInterceptor interceptor : responseInterceptors) {
            processedResponse = interceptor.intercept(processedResponse);
        }
        return processedResponse;
    }
    
    /**
     * Builder class for creating configured ApiClient instances.
     */
    public static class Builder {
        private HttpClientEngine engine;
        private Executor executor;
        private Duration connectTimeout = Duration.ofSeconds(30);
        private Duration readTimeout = Duration.ofSeconds(30);
        private Duration writeTimeout = Duration.ofSeconds(30);
        private final List<RequestInterceptor> requestInterceptors = new ArrayList<>();
        private final List<ResponseInterceptor> responseInterceptors = new ArrayList<>();
        private boolean loggingEnabled = false;
        private BasicRetry retry;
        private boolean allowLocalhost = false;
        
        /**
         * Sets the connection timeout.
         * 
         * @param timeout the connection timeout
         * @return this builder for chaining
         */
        public Builder connectTimeout(Duration timeout) {
            this.connectTimeout = timeout;
            return this;
        }
        
        /**
         * Sets the read timeout.
         * 
         * @param timeout the read timeout
         * @return this builder for chaining
         */
        public Builder readTimeout(Duration timeout) {
            this.readTimeout = timeout;
            return this;
        }
        
        /**
         * Sets the write timeout.
         * 
         * @param timeout the write timeout
         * @return this builder for chaining
         */
        public Builder writeTimeout(Duration timeout) {
            this.writeTimeout = timeout;
            return this;
        }
        
        /**
         * Sets a custom HTTP client engine.
         * 
         * @param engine the custom engine
         * @return this builder for chaining
         */
        public Builder engine(HttpClientEngine engine) {
            this.engine = engine;
            return this;
        }
        
        /**
         * Sets a custom executor for async operations.
         * 
         * @param executor the custom executor
         * @return this builder for chaining
         */
        public Builder executor(Executor executor) {
            this.executor = executor;
            return this;
        }
        
        /**
         * Adds a request interceptor.
         * 
         * @param interceptor the request interceptor
         * @return this builder for chaining
         */
        public Builder addRequestInterceptor(RequestInterceptor interceptor) {
            this.requestInterceptors.add(interceptor);
            return this;
        }
        
        /**
         * Adds a response interceptor.
         * 
         * @param interceptor the response interceptor
         * @return this builder for chaining
         */
        public Builder addResponseInterceptor(ResponseInterceptor interceptor) {
            this.responseInterceptors.add(interceptor);
            return this;
        }
        
        /**
         * Enables logging for requests and responses.
         * 
         * @return this builder for chaining
         */
        public Builder enableLogging() {
            this.loggingEnabled = true;
            return this;
        }
        
        /**
         * Sets basic retry configuration.
         * 
         * @param retry the retry configuration
         * @return this builder for chaining
         */
        public Builder retry(BasicRetry retry) {
            this.retry = retry;
            return this;
        }
        
        /**
         * Enables basic retry with standard configuration (3 attempts, 1-second delay).
         * 
         * @return this builder for chaining
         */
        public Builder enableRetry() {
            this.retry = BasicRetry.standard();
            return this;
        }
        
        /**
         * Enables basic retry with custom attempts and delay.
         * 
         * @param maxAttempts maximum number of attempts
         * @param delay delay between attempts
         * @return this builder for chaining
         */
        public Builder enableRetry(int maxAttempts, Duration delay) {
            this.retry = new BasicRetry(maxAttempts, delay);
            return this;
        }
        
        /**
         * Allows localhost URLs (useful for development).
         * 
         * @param allowLocalhost true to allow localhost URLs
         * @return this builder for chaining
         */
        public Builder allowLocalhost(boolean allowLocalhost) {
            this.allowLocalhost = allowLocalhost;
            return this;
        }
        
        
        /**
         * Builds the configured ApiClient.
         * 
         * @return the configured ApiClient
         */
        public ApiClient build() {
            // Set defaults if not provided
            if (engine == null) {
                this.engine = new DefaultHttpClientEngine(connectTimeout, readTimeout, writeTimeout);
            }
            
            if (executor == null) {
                // Use virtual threads if available (Java 21+), otherwise cached thread pool
                try {
                    this.executor = Executors.newVirtualThreadPerTaskExecutor();
                } catch (Exception e) {
                    // Fallback to cached thread pool for older Java versions
                    this.executor = Executors.newCachedThreadPool();
                }
            }
            
            // Add logging interceptors if enabled
            if (loggingEnabled) {
                addDefaultLoggingInterceptors();
            }
            
            // Set default security configuration
            Utils.setDefaultSecurityConfig(new io.mochaapi.client.config.SecurityConfig(allowLocalhost));
            
            return new ApiClient(this);
        }
        
        private void addDefaultLoggingInterceptors() {
            // Add simple console logging as default
            Consumer<String> logger = System.out::println;
            requestInterceptors.add(RequestInterceptor.logging(logger));
            responseInterceptors.add(ResponseInterceptor.logging(logger));
        }
    }
}
