package io.mochaapi.client;

import io.mochaapi.client.internal.DefaultHttpClientEngine;
import io.mochaapi.client.exception.ApiException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Main entry point for HTTP API requests.
 * Provides a simple, chainable API for making HTTP calls with automatic JSON handling.
 * 
 * <p>Example usage:</p>
 * <pre>
 * User user = Api.get("https://api.example.com/user")
 *                .query("id", 12)
 *                .execute()
 *                .json()
 *                .to(User.class);
 * </pre>
 * 
 * @since 1.1.0 Enhanced with virtual threads and improved async support
 * @since 1.3.0 Simplified API with improved resource management
 */
public class Api {
    
    private static final HttpClientEngine DEFAULT_ENGINE = new DefaultHttpClientEngine();
    private static final Executor DEFAULT_EXECUTOR = createDefaultExecutor();
    private static volatile boolean shutdown = false;
    
    static {
        // Register shutdown hook to properly close resources
        Runtime.getRuntime().addShutdownHook(new Thread(Api::shutdown));
    }
    
    /**
     * Creates a GET request to the specified URL.
     * 
     * @param url the target URL
     * @return ApiRequest builder for chaining
     */
    public static ApiRequest get(String url) {
        return new ApiRequest(url, "GET");
    }
    
    /**
     * Creates a POST request to the specified URL.
     * 
     * @param url the target URL
     * @return ApiRequest builder for chaining
     */
    public static ApiRequest post(String url) {
        return new ApiRequest(url, "POST");
    }
    
    /**
     * Creates a PUT request to the specified URL.
     * 
     * @param url the target URL
     * @return ApiRequest builder for chaining
     */
    public static ApiRequest put(String url) {
        return new ApiRequest(url, "PUT");
    }
    
    /**
     * Creates a DELETE request to the specified URL.
     * 
     * @param url the target URL
     * @return ApiRequest builder for chaining
     */
    public static ApiRequest delete(String url) {
        return new ApiRequest(url, "DELETE");
    }
    
    /**
     * Creates a PATCH request to the specified URL.
     * 
     * @param url the target URL
     * @return ApiRequest builder for chaining
     */
    public static ApiRequest patch(String url) {
        return new ApiRequest(url, "PATCH");
    }
    
    /**
     * Executes a request synchronously and returns the response.
     * 
     * @param request the configured request
     * @return ApiResponse containing the result
     * @throws ApiException if the request fails
     * @throws IllegalStateException if the API has been shut down
     */
    public static ApiResponse execute(ApiRequest request) {
        if (shutdown) {
            throw new IllegalStateException("Api has been shut down");
        }
        return DEFAULT_ENGINE.execute(request);
    }
    
    /**
     * Executes a request asynchronously and returns a CompletableFuture.
     * 
     * @param request the configured request
     * @return CompletableFuture containing the response
     * @throws IllegalStateException if the API has been shut down
     */
    public static CompletableFuture<ApiResponse> executeAsync(ApiRequest request) {
        if (shutdown) {
            CompletableFuture<ApiResponse> future = new CompletableFuture<>();
            future.completeExceptionally(new IllegalStateException("Api has been shut down"));
            return future;
        }
        return CompletableFuture.supplyAsync(() -> execute(request), DEFAULT_EXECUTOR);
    }
    
    /**
     * Executes a request asynchronously with a callback.
     * 
     * @param request the configured request
     * @param callback function to handle the response
     * @throws IllegalArgumentException if request or callback is null
     */
    public static void executeAsync(ApiRequest request, Consumer<ApiResponse> callback) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        if (callback == null) {
            throw new IllegalArgumentException("Callback cannot be null");
        }
        
        executeAsync(request)
            .thenAccept(callback)
            .exceptionally(throwable -> {
                // Log error instead of throwing to prevent memory leaks
                System.err.println("Async request failed: " + throwable.getMessage());
                throwable.printStackTrace();
                return null;
            });
    }
    
    /**
     * Executes a request asynchronously with both success and error callbacks.
     * 
     * @param request the configured request
     * @param successCallback function to handle successful responses
     * @param errorCallback function to handle errors
     * @throws IllegalArgumentException if request, successCallback, or errorCallback is null
     */
    public static void executeAsync(ApiRequest request, Consumer<ApiResponse> successCallback, Consumer<Throwable> errorCallback) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        if (successCallback == null) {
            throw new IllegalArgumentException("Success callback cannot be null");
        }
        if (errorCallback == null) {
            throw new IllegalArgumentException("Error callback cannot be null");
        }
        
        executeAsync(request)
            .thenAccept(successCallback)
            .exceptionally(throwable -> {
                try {
                    errorCallback.accept(throwable);
                } catch (Exception e) {
                    System.err.println("Error in error callback: " + e.getMessage());
                    e.printStackTrace();
                }
                return null;
            });
    }
    
    /**
     * Creates the default executor using virtual threads if available (Java 21+).
     * Falls back to cached thread pool for older Java versions.
     * 
     * @return the default executor
     * @since 1.3.0
     */
    private static Executor createDefaultExecutor() {
        try {
            // Try to use virtual threads (Java 21+)
            return Executors.newVirtualThreadPerTaskExecutor();
        } catch (Exception e) {
            // Fallback to cached thread pool for older Java versions
            return Executors.newCachedThreadPool();
        }
    }
    
    /**
     * Shuts down the API and releases all resources.
     * This method is called automatically when the JVM shuts down.
     * It can also be called manually to clean up resources.
     * 
     * @since 1.3.0
     */
    public static synchronized void shutdown() {
        if (shutdown) {
            return; // Already shut down
        }
        
        shutdown = true;
        
        try {
            // Shutdown the executor if it's a thread pool
            if (DEFAULT_EXECUTOR instanceof java.util.concurrent.ExecutorService) {
                java.util.concurrent.ExecutorService executorService = (java.util.concurrent.ExecutorService) DEFAULT_EXECUTOR;
                executorService.shutdown();
                
                // Wait for tasks to complete with timeout
                if (!executorService.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                    // Wait a bit more for tasks to respond to being cancelled
                    if (!executorService.awaitTermination(1, java.util.concurrent.TimeUnit.SECONDS)) {
                        System.err.println("Executor did not terminate gracefully");
                    }
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Interrupted while shutting down executor");
        }
        
        // Shutdown the HTTP client engine if it supports shutdown
        if (DEFAULT_ENGINE instanceof AutoCloseable) {
            try {
                ((AutoCloseable) DEFAULT_ENGINE).close();
            } catch (Exception e) {
                System.err.println("Error shutting down HTTP client engine: " + e.getMessage());
            }
        }
        
        System.out.println("MochaJSON API has been shut down");
    }
    
    /**
     * Checks if the API has been shut down.
     * 
     * @return true if shut down, false otherwise
     * @since 1.3.0
     */
    public static boolean isShutdown() {
        return shutdown;
    }
}
