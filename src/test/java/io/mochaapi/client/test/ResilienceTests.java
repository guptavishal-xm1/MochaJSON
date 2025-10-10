package io.mochaapi.client.test;

import io.mochaapi.client.*;
import io.mochaapi.client.config.ConnectionPoolConfig;
import io.mochaapi.client.retry.RetryPolicy;
import io.mochaapi.client.circuitbreaker.CircuitBreaker;
import io.mochaapi.client.exception.ApiException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for resilience features including retry policy, circuit breaker, and connection pooling.
 * 
 * @since 1.2.0
 */
public class ResilienceTests {
    
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
    @DisplayName("Retry policy with exponential backoff")
    public void testRetryPolicyWithExponentialBackoff() throws InterruptedException {
        System.out.println("\n=== Testing retry policy with exponential backoff ===");
        
        AtomicInteger attemptCount = new AtomicInteger(0);
        
        // Create retry policy with fast backoff for testing
        RetryPolicy retryPolicy = RetryPolicy.builder()
                .maxAttempts(3)
                .backoff(io.mochaapi.client.retry.ExponentialBackoff.of(Duration.ofMillis(100), Duration.ofSeconds(1), 2.0, 0.0))
                .retryOn(java.io.IOException.class)
                .retryOn(503, 429)
                .build();
        
        ApiClient client = new ApiClient.Builder()
                .retryPolicy(retryPolicy)
                .build();
        
        long startTime = System.currentTimeMillis();
        
        try {
            // This should fail and retry
            ApiResponse response = client.get(baseUrl + "/test/error")
                    .execute();
            fail("Expected ApiException to be thrown");
        } catch (ApiException e) {
            long duration = System.currentTimeMillis() - startTime;
            System.out.println("Retry completed in: " + duration + "ms");
            System.out.println("Error: " + e.getMessage());
            
            // Should have taken time for retries (at least 100ms + 200ms = 300ms)
            assertTrue(duration >= 300, "Should have taken time for retries");
        }
    }
    
    @Test
    @DisplayName("Circuit breaker opens after failure threshold")
    public void testCircuitBreakerOpensAfterFailureThreshold() {
        System.out.println("\n=== Testing circuit breaker failure threshold ===");
        
        CircuitBreaker circuitBreaker = CircuitBreaker.builder()
                .failureThreshold(3)
                .timeout(Duration.ofSeconds(5))
                .build();
        
        ApiClient client = new ApiClient.Builder()
                .circuitBreaker(circuitBreaker)
                .build();
        
        // Make requests that will fail
        for (int i = 1; i <= 5; i++) {
            try {
                ApiResponse response = client.get(baseUrl + "/test/error")
                        .execute();
                fail("Expected ApiException to be thrown");
            } catch (ApiException e) {
                System.out.println("Attempt " + i + ": " + e.getMessage());
                
                if (i <= 3) {
                    // First 3 attempts should fail normally
                    assertFalse(e.getMessage().contains("Circuit breaker is open"));
                } else {
                    // After 3 failures, circuit should be open
                    assertTrue(e.getMessage().contains("Circuit breaker is open"), 
                            "Circuit breaker should be open after " + i + " failures");
                }
            }
        }
        
        assertEquals(CircuitBreaker.State.OPEN, circuitBreaker.getState());
        assertEquals(3, circuitBreaker.getFailureCount());
    }
    
    @Test
    @DisplayName("Circuit breaker recovers after timeout")
    public void testCircuitBreakerRecoversAfterTimeout() throws InterruptedException {
        System.out.println("\n=== Testing circuit breaker recovery ===");
        
        CircuitBreaker circuitBreaker = CircuitBreaker.builder()
                .failureThreshold(2)
                .timeout(Duration.ofSeconds(1))
                .successThreshold(2)
                .build();
        
        ApiClient client = new ApiClient.Builder()
                .circuitBreaker(circuitBreaker)
                .build();
        
        // Trigger circuit breaker
        for (int i = 0; i < 2; i++) {
            try {
                client.get(baseUrl + "/test/error").execute();
                fail("Expected ApiException");
            } catch (ApiException e) {
                // Expected
            }
        }
        
        assertEquals(CircuitBreaker.State.OPEN, circuitBreaker.getState());
        
        // Wait for timeout
        Thread.sleep(1100);
        
        // Circuit should now be half-open and allow requests
        assertEquals(CircuitBreaker.State.HALF_OPEN, circuitBreaker.getState());
        
        // Make successful requests to close the circuit
        for (int i = 0; i < 2; i++) {
            try {
                ApiResponse response = client.get(baseUrl + "/test/success").execute();
                assertTrue(response.isSuccess());
            } catch (ApiException e) {
                fail("Should not fail after circuit recovery");
            }
        }
        
        assertEquals(CircuitBreaker.State.CLOSED, circuitBreaker.getState());
    }
    
    @Test
    @DisplayName("Connection pool configuration")
    public void testConnectionPoolConfiguration() {
        System.out.println("\n=== Testing connection pool configuration ===");
        
        ConnectionPoolConfig poolConfig = ConnectionPoolConfig.builder()
                .maxIdle(10)
                .keepAlive(Duration.ofMinutes(2))
                .maxConnections(50)
                .build();
        
        ApiClient client = new ApiClient.Builder()
                .connectionPool(poolConfig)
                .build();
        
        // Make multiple requests to test connection reuse
        for (int i = 0; i < 5; i++) {
            try {
                ApiResponse response = client.get(baseUrl + "/test/success").execute();
                assertTrue(response.isSuccess());
                System.out.println("Request " + (i + 1) + " completed successfully");
            } catch (ApiException e) {
                fail("Request should succeed: " + e.getMessage());
            }
        }
        
        System.out.println("Connection pool configuration: " + poolConfig);
    }
    
    @Test
    @DisplayName("Combined retry and circuit breaker")
    public void testCombinedRetryAndCircuitBreaker() throws InterruptedException {
        System.out.println("\n=== Testing combined retry and circuit breaker ===");
        
        RetryPolicy retryPolicy = RetryPolicy.builder()
                .maxAttempts(2)
                .backoff(io.mochaapi.client.retry.FixedBackoff.of(Duration.ofMillis(50)))
                .retryOn(java.io.IOException.class)
                .retryOn(503)
                .build();
        
        CircuitBreaker circuitBreaker = CircuitBreaker.builder()
                .failureThreshold(2)
                .timeout(Duration.ofSeconds(2))
                .build();
        
        ApiClient client = new ApiClient.Builder()
                .retryPolicy(retryPolicy)
                .circuitBreaker(circuitBreaker)
                .build();
        
        // First request should retry then fail, triggering circuit breaker
        try {
            ApiResponse response = client.get(baseUrl + "/test/error").execute();
            fail("Expected ApiException");
        } catch (ApiException e) {
            System.out.println("First request failed: " + e.getMessage());
        }
        
        // Second request should fail fast due to circuit breaker
        try {
            ApiResponse response = client.get(baseUrl + "/test/error").execute();
            fail("Expected ApiException");
        } catch (ApiException e) {
            System.out.println("Second request failed: " + e.getMessage());
            assertTrue(e.getMessage().contains("Circuit breaker is open"));
        }
        
        assertEquals(CircuitBreaker.State.OPEN, circuitBreaker.getState());
    }
    
    @Test
    @DisplayName("Async requests with resilience features")
    public void testAsyncRequestsWithResilience() throws ExecutionException, InterruptedException {
        System.out.println("\n=== Testing async requests with resilience ===");
        
        RetryPolicy retryPolicy = RetryPolicy.fixed(2, Duration.ofMillis(100));
        
        ApiClient client = new ApiClient.Builder()
                .retryPolicy(retryPolicy)
                .build();
        
        // Test successful async request
        CompletableFuture<ApiResponse> future = client.get(baseUrl + "/test/success")
                .executeAsync();
        
        ApiResponse response = future.get();
        assertTrue(response.isSuccess());
        System.out.println("Async request completed successfully");
        
        // Test async request with retry
        CompletableFuture<ApiResponse> retryFuture = client.get(baseUrl + "/test/error")
                .executeAsync();
        
        try {
            retryFuture.get();
            fail("Expected ApiException");
        } catch (ExecutionException e) {
            assertTrue(e.getCause() instanceof ApiException);
            System.out.println("Async retry failed as expected: " + e.getCause().getMessage());
        }
    }
    
    @Test
    @DisplayName("Retry policy with custom conditions")
    public void testRetryPolicyWithCustomConditions() {
        System.out.println("\n=== Testing retry policy with custom conditions ===");
        
        RetryPolicy retryPolicy = RetryPolicy.builder()
                .maxAttempts(3)
                .backoff(io.mochaapi.client.retry.FixedBackoff.of(Duration.ofMillis(50)))
                .retryCondition(e -> {
                    // Only retry on specific error messages
                    return e.getMessage() != null && e.getMessage().contains("timeout");
                })
                .build();
        
        ApiClient client = new ApiClient.Builder()
                .retryPolicy(retryPolicy)
                .build();
        
        try {
            ApiResponse response = client.get(baseUrl + "/test/error").execute();
            fail("Expected ApiException");
        } catch (ApiException e) {
            // Should not retry because error message doesn't contain "timeout"
            System.out.println("Error (no retry): " + e.getMessage());
            assertFalse(e.getMessage().contains("retry"));
        }
    }
    
    @Test
    @DisplayName("Default retry policy")
    public void testDefaultRetryPolicy() {
        System.out.println("\n=== Testing default retry policy ===");
        
        // Test the static factory methods
        RetryPolicy exponentialPolicy = RetryPolicy.exponential(3);
        RetryPolicy fixedPolicy = RetryPolicy.fixed(2, Duration.ofSeconds(1));
        
        assertNotNull(exponentialPolicy);
        assertNotNull(fixedPolicy);
        
        assertEquals(3, exponentialPolicy.getMaxAttempts());
        assertEquals(2, fixedPolicy.getMaxAttempts());
        
        System.out.println("Exponential policy: " + exponentialPolicy);
        System.out.println("Fixed policy: " + fixedPolicy);
    }
    
    @Test
    @DisplayName("Default circuit breaker")
    public void testDefaultCircuitBreaker() {
        System.out.println("\n=== Testing default circuit breaker ===");
        
        CircuitBreaker circuitBreaker = CircuitBreaker.standard();
        
        assertNotNull(circuitBreaker);
        assertEquals(CircuitBreaker.State.CLOSED, circuitBreaker.getState());
        assertEquals(0, circuitBreaker.getFailureCount());
        assertEquals(0, circuitBreaker.getSuccessCount());
        
        System.out.println("Default circuit breaker: " + circuitBreaker);
    }
}
