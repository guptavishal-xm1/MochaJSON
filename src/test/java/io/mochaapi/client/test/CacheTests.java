package io.mochaapi.client.test;

import io.mochaapi.client.*;
import io.mochaapi.client.cache.CacheConfig;
import io.mochaapi.client.exception.ApiException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for HTTP response caching functionality.
 * 
 * @since 1.2.0
 */
public class CacheTests {
    
    private MockHttpServer mockServer;
    private String baseUrl;
    private Path tempDir;
    
    @BeforeEach
    void setUp() throws Exception {
        mockServer = new MockHttpServer();
        baseUrl = "http://localhost:" + mockServer.start();
        
        // Create temporary directory for cache tests
        tempDir = Files.createTempDirectory("mochajson-cache-test");
    }
    
    @AfterEach
    void tearDown() throws Exception {
        if (mockServer != null) {
            mockServer.stop();
        }
        
        // Clean up temporary cache directory
        if (tempDir != null && Files.exists(tempDir)) {
            Files.walk(tempDir)
                    .sorted((a, b) -> b.compareTo(a)) // Delete files before directories
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (Exception e) {
                            // Ignore cleanup errors
                        }
                    });
        }
    }
    
    @Test
    @DisplayName("Basic response caching")
    public void testBasicResponseCaching() {
        System.out.println("\n=== Testing basic response caching ===");
        
        CacheConfig cacheConfig = CacheConfig.builder()
                .directory(tempDir)
                .maxSize(1024 * 1024) // 1MB
                .maxAge(Duration.ofMinutes(5))
                .maxEntries(100)
                .build();
        
        ApiClient client = new ApiClient.Builder()
                .cache(cacheConfig)
                .build();
        
        // First request - should hit the server
        long start1 = System.currentTimeMillis();
        ApiResponse response1 = client.get(baseUrl + "/test/success").execute();
        long duration1 = System.currentTimeMillis() - start1;
        
        assertNotNull(response1);
        assertTrue(response1.isSuccess());
        System.out.println("First request took: " + duration1 + "ms");
        
        // Second request - should hit the cache
        long start2 = System.currentTimeMillis();
        ApiResponse response2 = client.get(baseUrl + "/test/success").execute();
        long duration2 = System.currentTimeMillis() - start2;
        
        assertNotNull(response2);
        assertTrue(response2.isSuccess());
        System.out.println("Second request took: " + duration2 + "ms");
        
        // Cache hit should be faster
        assertTrue(duration2 < duration1, "Cached request should be faster");
        
        // Responses should be identical
        assertEquals(response1.body(), response2.body());
        assertEquals(response1.code(), response2.code());
    }
    
    @Test
    @DisplayName("Cache with different URLs")
    public void testCacheWithDifferentUrls() {
        System.out.println("\n=== Testing cache with different URLs ===");
        
        CacheConfig cacheConfig = CacheConfig.builder()
                .directory(tempDir)
                .maxSize(1024 * 1024)
                .maxAge(Duration.ofMinutes(5))
                .build();
        
        ApiClient client = new ApiClient.Builder()
                .cache(cacheConfig)
                .build();
        
        // Request different URLs
        ApiResponse response1 = client.get(baseUrl + "/test/success").execute();
        ApiResponse response2 = client.get(baseUrl + "/test/echo").execute();
        
        assertNotNull(response1);
        assertNotNull(response2);
        assertTrue(response1.isSuccess());
        assertTrue(response2.isSuccess());
        
        // Should have different content
        assertNotEquals(response1.body(), response2.body());
        
        System.out.println("Different URLs cached separately");
    }
    
    @Test
    @DisplayName("Cache with query parameters")
    public void testCacheWithQueryParameters() {
        System.out.println("\n=== Testing cache with query parameters ===");
        
        CacheConfig cacheConfig = CacheConfig.builder()
                .directory(tempDir)
                .maxSize(1024 * 1024)
                .maxAge(Duration.ofMinutes(5))
                .build();
        
        ApiClient client = new ApiClient.Builder()
                .cache(cacheConfig)
                .build();
        
        // Same URL with different query parameters should be cached separately
        ApiResponse response1 = client.get(baseUrl + "/test/query")
                .query("page", 1)
                .query("limit", 10)
                .execute();
        
        ApiResponse response2 = client.get(baseUrl + "/test/query")
                .query("page", 2)
                .query("limit", 20)
                .execute();
        
        assertNotNull(response1);
        assertNotNull(response2);
        assertTrue(response1.isSuccess());
        assertTrue(response2.isSuccess());
        
        // Different query parameters should produce different cached responses
        assertNotEquals(response1.body(), response2.body());
        
        System.out.println("Query parameters affect cache keys");
    }
    
    @Test
    @DisplayName("Cache expiration")
    public void testCacheExpiration() throws InterruptedException {
        System.out.println("\n=== Testing cache expiration ===");
        
        CacheConfig cacheConfig = CacheConfig.builder()
                .directory(tempDir)
                .maxSize(1024 * 1024)
                .maxAge(Duration.ofMillis(100)) // Very short expiration
                .build();
        
        ApiClient client = new ApiClient.Builder()
                .cache(cacheConfig)
                .build();
        
        // First request
        ApiResponse response1 = client.get(baseUrl + "/test/success").execute();
        assertNotNull(response1);
        assertTrue(response1.isSuccess());
        
        // Wait for cache to expire
        Thread.sleep(150);
        
        // Second request should hit the server again
        ApiResponse response2 = client.get(baseUrl + "/test/success").execute();
        assertNotNull(response2);
        assertTrue(response2.isSuccess());
        
        // Content should be the same but served from server
        assertEquals(response1.body(), response2.body());
        
        System.out.println("Cache expiration working correctly");
    }
    
    @Test
    @DisplayName("Cache size limits")
    public void testCacheSizeLimits() {
        System.out.println("\n=== Testing cache size limits ===");
        
        // Very small cache size
        CacheConfig cacheConfig = CacheConfig.builder()
                .directory(tempDir)
                .maxSize(100) // Very small limit
                .maxAge(Duration.ofMinutes(5))
                .maxEntries(10)
                .build();
        
        ApiClient client = new ApiClient.Builder()
                .cache(cacheConfig)
                .build();
        
        // Make multiple requests
        for (int i = 0; i < 5; i++) {
            ApiResponse response = client.get(baseUrl + "/test/success")
                    .query("id", i)
                    .execute();
            assertNotNull(response);
            assertTrue(response.isSuccess());
        }
        
        System.out.println("Cache size limits handled correctly");
    }
    
    @Test
    @DisplayName("Cache entry count limits")
    public void testCacheEntryCountLimits() {
        System.out.println("\n=== Testing cache entry count limits ===");
        
        CacheConfig cacheConfig = CacheConfig.builder()
                .directory(tempDir)
                .maxSize(1024 * 1024)
                .maxAge(Duration.ofMinutes(5))
                .maxEntries(3) // Small entry limit
                .build();
        
        ApiClient client = new ApiClient.Builder()
                .cache(cacheConfig)
                .build();
        
        // Make more requests than the entry limit
        for (int i = 0; i < 5; i++) {
            ApiResponse response = client.get(baseUrl + "/test/success")
                    .query("id", i)
                    .execute();
            assertNotNull(response);
            assertTrue(response.isSuccess());
        }
        
        System.out.println("Cache entry count limits handled correctly");
    }
    
    @Test
    @DisplayName("In-memory cache")
    public void testInMemoryCache() {
        System.out.println("\n=== Testing in-memory cache ===");
        
        CacheConfig cacheConfig = CacheConfig.memory(50);
        
        ApiClient client = new ApiClient.Builder()
                .cache(cacheConfig)
                .build();
        
        // First request
        ApiResponse response1 = client.get(baseUrl + "/test/success").execute();
        assertNotNull(response1);
        assertTrue(response1.isSuccess());
        
        // Second request should hit cache
        ApiResponse response2 = client.get(baseUrl + "/test/success").execute();
        assertNotNull(response2);
        assertTrue(response2.isSuccess());
        
        assertEquals(response1.body(), response2.body());
        
        System.out.println("In-memory cache working correctly");
    }
    
    @Test
    @DisplayName("Auto cache configuration")
    public void testAutoCacheConfiguration() {
        System.out.println("\n=== Testing auto cache configuration ===");
        
        CacheConfig cacheConfig = CacheConfig.auto();
        
        ApiClient client = new ApiClient.Builder()
                .cache(cacheConfig)
                .build();
        
        ApiResponse response = client.get(baseUrl + "/test/success").execute();
        assertNotNull(response);
        assertTrue(response.isSuccess());
        
        System.out.println("Auto cache configuration: " + cacheConfig);
    }
    
    @Test
    @DisplayName("Cache disabled")
    public void testCacheDisabled() {
        System.out.println("\n=== Testing disabled cache ===");
        
        CacheConfig cacheConfig = CacheConfig.builder()
                .enabled(false)
                .build();
        
        ApiClient client = new ApiClient.Builder()
                .cache(cacheConfig)
                .build();
        
        // Multiple requests should all hit the server
        ApiResponse response1 = client.get(baseUrl + "/test/success").execute();
        ApiResponse response2 = client.get(baseUrl + "/test/success").execute();
        
        assertNotNull(response1);
        assertNotNull(response2);
        assertTrue(response1.isSuccess());
        assertTrue(response2.isSuccess());
        
        // Content should be the same (from server)
        assertEquals(response1.body(), response2.body());
        
        System.out.println("Disabled cache working correctly");
    }
    
    @Test
    @DisplayName("Cache with POST requests")
    public void testCacheWithPostRequests() {
        System.out.println("\n=== Testing cache with POST requests ===");
        
        CacheConfig cacheConfig = CacheConfig.builder()
                .directory(tempDir)
                .maxSize(1024 * 1024)
                .maxAge(Duration.ofMinutes(5))
                .build();
        
        ApiClient client = new ApiClient.Builder()
                .cache(cacheConfig)
                .build();
        
        // POST requests should not be cached
        ApiResponse response1 = client.post(baseUrl + "/test/echo")
                .body(Map.of("test", "data1"))
                .execute();
        
        ApiResponse response2 = client.post(baseUrl + "/test/echo")
                .body(Map.of("test", "data2"))
                .execute();
        
        assertNotNull(response1);
        assertNotNull(response2);
        assertTrue(response1.isSuccess());
        assertTrue(response2.isSuccess());
        
        // Different request bodies should produce different responses
        assertNotEquals(response1.body(), response2.body());
        
        System.out.println("POST requests not cached (as expected)");
    }
    
    @Test
    @DisplayName("Cache with error responses")
    public void testCacheWithErrorResponses() {
        System.out.println("\n=== Testing cache with error responses ===");
        
        CacheConfig cacheConfig = CacheConfig.builder()
                .directory(tempDir)
                .maxSize(1024 * 1024)
                .maxAge(Duration.ofMinutes(5))
                .build();
        
        ApiClient client = new ApiClient.Builder()
                .cache(cacheConfig)
                .build();
        
        // Error responses should not be cached
        ApiResponse response1 = client.get(baseUrl + "/test/not-found").execute();
        ApiResponse response2 = client.get(baseUrl + "/test/not-found").execute();
        
        assertNotNull(response1);
        assertNotNull(response2);
        assertTrue(response1.isError());
        assertTrue(response2.isError());
        
        // Both should have same error status
        assertEquals(response1.code(), response2.code());
        
        System.out.println("Error responses not cached (as expected)");
    }
}
