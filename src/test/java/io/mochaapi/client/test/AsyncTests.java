package io.mochaapi.client.test;

import io.mochaapi.client.*;
import io.mochaapi.client.exception.ApiException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.DisplayName;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for async functionality and virtual threads.
 * Tests CompletableFuture-based async operations and thread safety.
 */
public class AsyncTests {
    
    private MockHttpServer mockServer;
    private String baseUrl;
    
    @BeforeEach
    void setUp() throws Exception {
        mockServer = new MockHttpServer();
        baseUrl = "http://localhost:" + mockServer.start();
    }
    
    @AfterEach
    void tearDown() {
        if (mockServer != null) {
            mockServer.stop();
        }
    }
    
    @Test
    @DisplayName("Basic async request execution")
    void testBasicAsyncRequest() throws ExecutionException, InterruptedException, TimeoutException {
        ApiClient client = new ApiClient.Builder().build();
        
        CompletableFuture<ApiResponse> future = client.get(baseUrl + "/test/success").executeAsync();
        
        ApiResponse response = future.get(5, TimeUnit.SECONDS);
        
        assertNotNull(response);
        assertEquals(200, response.code());
        assertTrue(response.isSuccess());
    }
    
    @Test
    @DisplayName("Multiple concurrent async requests")
    void testMultipleConcurrentAsyncRequests() throws InterruptedException, ExecutionException, TimeoutException {
        ApiClient client = new ApiClient.Builder().build();
        
        int requestCount = 10;
        CompletableFuture<ApiResponse>[] futures = new CompletableFuture[requestCount];
        
        // Start multiple concurrent requests
        for (int i = 0; i < requestCount; i++) {
            futures[i] = client.get(baseUrl + "/test/success").executeAsync();
        }
        
        // Wait for all requests to complete
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures);
        allFutures.get(10, TimeUnit.SECONDS);
        
        // Verify all requests succeeded
        for (CompletableFuture<ApiResponse> future : futures) {
            ApiResponse response = future.get();
            assertEquals(200, response.code());
            assertTrue(response.isSuccess());
        }
    }
    
    @Test
    @DisplayName("Async request with callback")
    void testAsyncRequestWithCallback() throws InterruptedException {
        ApiClient client = new ApiClient.Builder().build();
        
        AtomicReference<ApiResponse> responseRef = new AtomicReference<>();
        AtomicInteger callbackCount = new AtomicInteger(0);
        
        client.get(baseUrl + "/test/success")
                .async(response -> {
                    responseRef.set(response);
                    callbackCount.incrementAndGet();
                });
        
        // Wait for callback to be executed
        Thread.sleep(1000);
        
        assertEquals(1, callbackCount.get());
        assertNotNull(responseRef.get());
        assertEquals(200, responseRef.get().code());
    }
    
    @Test
    @DisplayName("Async request error handling")
    void testAsyncRequestErrorHandling() throws InterruptedException {
        ApiClient client = new ApiClient.Builder().build();
        
        AtomicReference<Throwable> errorRef = new AtomicReference<>();
        
        CompletableFuture<ApiResponse> future = client.get(baseUrl + "/test/error").executeAsync();
        
        future.exceptionally(throwable -> {
            errorRef.set(throwable);
            return null;
        });
        
        // Wait for the request to complete
        Thread.sleep(1000);
        
        // The request should succeed but return an error status code
        // The error handling is in the response, not in the async execution
        assertTrue(future.isDone());
        assertDoesNotThrow(() -> {
            ApiResponse response = future.get();
            assertEquals(500, response.code());
        });
    }
    
    @Test
    @DisplayName("Async request timeout")
    void testAsyncRequestTimeout() {
        ApiClient client = new ApiClient.Builder()
                .readTimeout(java.time.Duration.ofMillis(100))
                .build();
        
        CompletableFuture<ApiResponse> future = client.get(baseUrl + "/test/timeout").executeAsync();
        
        // This should timeout quickly
        assertThrows(TimeoutException.class, () -> {
            future.get(200, TimeUnit.MILLISECONDS);
        });
    }
    
    @Test
    @DisplayName("Async request with interceptors")
    void testAsyncRequestWithInterceptors() throws ExecutionException, InterruptedException, TimeoutException {
        AtomicInteger requestInterceptorCount = new AtomicInteger(0);
        AtomicInteger responseInterceptorCount = new AtomicInteger(0);
        
        ApiClient client = new ApiClient.Builder()
                .addRequestInterceptor(request -> {
                    requestInterceptorCount.incrementAndGet();
                    return request;
                })
                .addResponseInterceptor(response -> {
                    responseInterceptorCount.incrementAndGet();
                    return response;
                })
                .build();
        
        CompletableFuture<ApiResponse> future = client.get(baseUrl + "/test/success").executeAsync();
        
        ApiResponse response = future.get(5, TimeUnit.SECONDS);
        
        assertEquals(200, response.code());
        assertEquals(1, requestInterceptorCount.get());
        assertEquals(1, responseInterceptorCount.get());
    }
    
    @Test
    @DisplayName("Async request chaining")
    void testAsyncRequestChaining() throws ExecutionException, InterruptedException, TimeoutException {
        ApiClient client = new ApiClient.Builder().build();
        
        CompletableFuture<ApiResponse> future = client.get(baseUrl + "/test/success")
                .executeAsync()
                .thenApply(response -> {
                    // Chain additional processing
                    assertNotNull(response);
                    assertEquals(200, response.code());
                    return response;
                });
        
        ApiResponse response = future.get(5, TimeUnit.SECONDS);
        
        assertNotNull(response);
        assertEquals(200, response.code());
    }
    
    @Test
    @DisplayName("Async request with CompletableFuture composition")
    void testAsyncRequestComposition() throws ExecutionException, InterruptedException, TimeoutException {
        ApiClient client = new ApiClient.Builder().build();
        
        CompletableFuture<ApiResponse> firstRequest = client.get(baseUrl + "/test/success").executeAsync();
        
        CompletableFuture<String> composedFuture = firstRequest.thenApply(response -> {
            assertEquals(200, response.code());
            return response.toMap().get("name").toString();
        });
        
        String result = composedFuture.get(5, TimeUnit.SECONDS);
        
        assertEquals("Test User", result);
    }
    
    @Test
    @DisplayName("Mixed sync and async requests")
    void testMixedSyncAndAsyncRequests() throws ExecutionException, InterruptedException, TimeoutException {
        ApiClient client = new ApiClient.Builder().build();
        
        // Execute synchronous request
        ApiResponse syncResponse = client.get(baseUrl + "/test/success").execute();
        assertEquals(200, syncResponse.code());
        
        // Execute asynchronous request
        CompletableFuture<ApiResponse> asyncFuture = client.get(baseUrl + "/test/success").executeAsync();
        ApiResponse asyncResponse = asyncFuture.get(5, TimeUnit.SECONDS);
        assertEquals(200, asyncResponse.code());
        
        // Both should work correctly
        assertNotNull(syncResponse);
        assertNotNull(asyncResponse);
    }
    
    @Test
    @DisplayName("Thread safety of async operations")
    void testThreadSafety() throws InterruptedException {
        ApiClient client = new ApiClient.Builder().build();
        
        int threadCount = 5;
        int requestsPerThread = 2;
        AtomicInteger successCount = new AtomicInteger(0);
        
        Thread[] threads = new Thread[threadCount];
        
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < requestsPerThread; j++) {
                    try {
                        CompletableFuture<ApiResponse> future = client.get(baseUrl + "/test/success").executeAsync();
                        ApiResponse response = future.get(5, TimeUnit.SECONDS);
                        if (response.code() == 200) {
                            successCount.incrementAndGet();
                        }
                    } catch (Exception e) {
                        fail("Thread safety test failed: " + e.getMessage());
                    }
                }
            });
            threads[i].start();
        }
        
        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join(10000); // 10 second timeout
        }
        
        int expectedSuccessCount = threadCount * requestsPerThread;
        assertEquals(expectedSuccessCount, successCount.get());
    }
    
    @Test
    @DisplayName("Async request cancellation")
    void testAsyncRequestCancellation() {
        ApiClient client = new ApiClient.Builder().build();
        
        CompletableFuture<ApiResponse> future = client.get(baseUrl + "/test/timeout").executeAsync();
        
        // Cancel the request
        boolean cancelled = future.cancel(true);
        
        assertTrue(cancelled);
        assertTrue(future.isCancelled());
    }
    
    @Test
    @DisplayName("Async request with custom executor")
    void testAsyncRequestWithCustomExecutor() throws ExecutionException, InterruptedException, TimeoutException {
        java.util.concurrent.Executor customExecutor = java.util.concurrent.Executors.newSingleThreadExecutor();
        
        ApiClient client = new ApiClient.Builder()
                .executor(customExecutor)
                .build();
        
        CompletableFuture<ApiResponse> future = client.get(baseUrl + "/test/success").executeAsync();
        
        ApiResponse response = future.get(5, TimeUnit.SECONDS);
        
        assertEquals(200, response.code());
        
        // Clean up
        if (customExecutor instanceof java.util.concurrent.ExecutorService) {
            ((java.util.concurrent.ExecutorService) customExecutor).shutdown();
        }
    }
}
