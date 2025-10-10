package io.mochaapi.client.test;

import io.mochaapi.client.*;
import io.mochaapi.client.cache.CacheConfig;
import io.mochaapi.client.cache.HttpCache;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for thread safety and concurrent access validation.
 * 
 * @since 1.2.0
 */
public class ConcurrencyTests {
    
    private MockHttpServer mockServer;
    private String baseUrl;
    
    @BeforeEach
    void setUp() throws Exception {
        mockServer = new MockHttpServer();
        baseUrl = "http://localhost:" + mockServer.start();
    }
    
    @AfterEach
    void tearDown() throws IOException {
        if (mockServer != null) {
            mockServer.stop();
        }
    }
    
    @Test
    @DisplayName("Test concurrent cache access")
    public void testConcurrentCacheAccess() throws InterruptedException {
        System.out.println("\n=== Testing concurrent cache access ===");
        
        CacheConfig config = new CacheConfig.Builder()
                .enabled(true)
                .maxSize(1024 * 1024) // 1MB
                .maxEntries(100)
                .maxAge(java.time.Duration.ofMinutes(5))
                .build();
        
        HttpCache cache = HttpCache.create(config);
        
        int threadCount = 10;
        int operationsPerThread = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        
        // Create test response
        ApiResponse testResponse = new ApiResponse(200, "test data", 
                java.util.Map.of("Content-Type", "application/json"), null);
        
        // Run concurrent operations
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < operationsPerThread; j++) {
                        String key = "key-" + threadId + "-" + j;
                        
                        // Put operation
                        cache.put(key, testResponse, new io.mochaapi.client.JacksonJsonMapper());
                        
                        // Get operation
                        ApiResponse cached = cache.get(key, new io.mochaapi.client.JacksonJsonMapper());
                        if (cached != null) {
                            successCount.incrementAndGet();
                        }
                        
                        // Remove operation
                        cache.remove(key);
                    }
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                    System.err.println("Thread " + threadId + " error: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }
        
        // Wait for all threads to complete
        assertTrue(latch.await(30, TimeUnit.SECONDS), "Not all threads completed in time");
        
        // Verify results
        int totalOperations = threadCount * operationsPerThread;
        System.out.println("Total operations: " + totalOperations);
        System.out.println("Successful operations: " + successCount.get());
        System.out.println("Error operations: " + errorCount.get());
        
        assertTrue(successCount.get() > 0, "Some cache operations should succeed");
        assertEquals(0, errorCount.get(), "No cache operations should fail");
        
        executor.shutdown();
        System.out.println("✓ Concurrent cache access working correctly");
    }
    
    @Test
    @DisplayName("Test concurrent request execution")
    public void testConcurrentRequestExecution() throws InterruptedException {
        System.out.println("\n=== Testing concurrent request execution ===");
        
        ApiClient client = new ApiClient.Builder().build();
        
        int threadCount = 20;
        int requestsPerThread = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        AtomicReference<Exception> lastError = new AtomicReference<>();
        
        // Run concurrent requests
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < requestsPerThread; j++) {
                        ApiResponse response = client.get(baseUrl + "/test/success")
                                .query("thread", threadId)
                                .query("request", j)
                                .execute();
                        
                        if (response.isSuccess()) {
                            successCount.incrementAndGet();
                        } else {
                            errorCount.incrementAndGet();
                        }
                        
                        // Small delay to avoid overwhelming the server
                        Thread.sleep(10);
                    }
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                    lastError.set(e);
                    System.err.println("Thread " + threadId + " error: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }
        
        // Wait for all threads to complete
        assertTrue(latch.await(60, TimeUnit.SECONDS), "Not all requests completed in time");
        
        // Verify results
        int totalRequests = threadCount * requestsPerThread;
        System.out.println("Total requests: " + totalRequests);
        System.out.println("Successful requests: " + successCount.get());
        System.out.println("Failed requests: " + errorCount.get());
        
        assertTrue(successCount.get() > 0, "Some requests should succeed");
        
        if (errorCount.get() > 0) {
            System.err.println("Last error: " + lastError.get());
        }
        
        executor.shutdown();
        System.out.println("✓ Concurrent request execution working correctly");
    }
    
    @Test
    @DisplayName("Test async request concurrency")
    public void testAsyncRequestConcurrency() throws InterruptedException {
        System.out.println("\n=== Testing async request concurrency ===");
        
        int requestCount = 50;
        CountDownLatch latch = new CountDownLatch(requestCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        AtomicReference<Exception> lastError = new AtomicReference<>();
        
        // Submit multiple async requests
        for (int i = 0; i < requestCount; i++) {
            final int requestId = i;
            Api.executeAsync(Api.get(baseUrl + "/test/success").query("id", requestId),
                response -> {
                    successCount.incrementAndGet();
                    latch.countDown();
                },
                throwable -> {
                    errorCount.incrementAndGet();
                    lastError.set(new Exception(throwable));
                    latch.countDown();
                });
        }
        
        // Wait for all requests to complete
        assertTrue(latch.await(30, TimeUnit.SECONDS), "Not all async requests completed in time");
        
        // Verify results
        System.out.println("Total async requests: " + requestCount);
        System.out.println("Successful requests: " + successCount.get());
        System.out.println("Failed requests: " + errorCount.get());
        
        assertTrue(successCount.get() > 0, "Some async requests should succeed");
        
        if (errorCount.get() > 0) {
            System.err.println("Last error: " + lastError.get());
        }
        
        System.out.println("✓ Async request concurrency working correctly");
    }
    
    @Test
    @DisplayName("Test thread pool management")
    public void testThreadPoolManagement() throws InterruptedException {
        System.out.println("\n=== Testing thread pool management ===");
        
        // Test that we can create multiple clients without issues
        ApiClient[] clients = new ApiClient[10];
        for (int i = 0; i < clients.length; i++) {
            clients[i] = new ApiClient.Builder().build();
        }
        
        // Test concurrent requests from different clients
        int requestsPerClient = 5;
        ExecutorService executor = Executors.newFixedThreadPool(clients.length);
        CountDownLatch latch = new CountDownLatch(clients.length);
        AtomicInteger successCount = new AtomicInteger(0);
        
        for (int i = 0; i < clients.length; i++) {
            final int clientId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < requestsPerClient; j++) {
                        ApiResponse response = clients[clientId].get(baseUrl + "/test/success")
                                .query("client", clientId)
                                .query("request", j)
                                .execute();
                        
                        if (response.isSuccess()) {
                            successCount.incrementAndGet();
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Client " + clientId + " error: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }
        
        // Wait for all clients to complete
        assertTrue(latch.await(30, TimeUnit.SECONDS), "Not all clients completed in time");
        
        // Verify results
        int totalRequests = clients.length * requestsPerClient;
        System.out.println("Total requests from multiple clients: " + totalRequests);
        System.out.println("Successful requests: " + successCount.get());
        
        assertTrue(successCount.get() > 0, "Some requests should succeed");
        
        executor.shutdown();
        System.out.println("✓ Thread pool management working correctly");
    }
    
    @Test
    @DisplayName("Test cache eviction under concurrent load")
    public void testCacheEvictionUnderConcurrentLoad() throws InterruptedException {
        System.out.println("\n=== Testing cache eviction under concurrent load ===");
        
        CacheConfig config = new CacheConfig.Builder()
                .enabled(true)
                .maxSize(1024) // Very small cache to force eviction
                .maxEntries(10) // Very few entries to force eviction
                .maxAge(java.time.Duration.ofMinutes(1))
                .build();
        
        HttpCache cache = HttpCache.create(config);
        
        int threadCount = 5;
        int operationsPerThread = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger putCount = new AtomicInteger(0);
        AtomicInteger getCount = new AtomicInteger(0);
        
        // Create test response
        ApiResponse testResponse = new ApiResponse(200, "test data", 
                java.util.Map.of("Content-Type", "application/json"), null);
        
        // Run concurrent operations that will trigger eviction
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < operationsPerThread; j++) {
                        String key = "key-" + threadId + "-" + j;
                        
                        // Put operation (will trigger eviction due to small cache)
                        cache.put(key, testResponse, new io.mochaapi.client.JacksonJsonMapper());
                        putCount.incrementAndGet();
                        
                        // Get operation
                        ApiResponse cached = cache.get(key, new io.mochaapi.client.JacksonJsonMapper());
                        if (cached != null) {
                            getCount.incrementAndGet();
                        }
                        
                        // Small delay
                        Thread.sleep(1);
                    }
                } catch (Exception e) {
                    System.err.println("Thread " + threadId + " error: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }
        
        // Wait for all threads to complete
        assertTrue(latch.await(30, TimeUnit.SECONDS), "Not all threads completed in time");
        
        // Verify results
        System.out.println("Put operations: " + putCount.get());
        System.out.println("Get operations (hits): " + getCount.get());
        System.out.println("Cache size: " + cache.getCurrentSize());
        System.out.println("Cache entries: " + cache.getEntryCount());
        
        assertTrue(putCount.get() > 0, "Some put operations should succeed");
        assertTrue(cache.getEntryCount() <= 10, "Cache should respect entry limit");
        assertTrue(cache.getCurrentSize() <= 1024, "Cache should respect size limit");
        
        executor.shutdown();
        System.out.println("✓ Cache eviction under concurrent load working correctly");
    }
}
