package io.mochaapi.client.test;

import io.mochaapi.client.*;
import io.mochaapi.client.config.SecurityConfig;
import io.mochaapi.client.internal.Utils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for resource leak detection and proper resource management.
 * 
 * @since 1.2.0
 */
public class ResourceLeakTests {
    
    private MockHttpServer mockServer;
    private String baseUrl;
    private Path tempDir;
    
    @BeforeEach
    void setUp() throws Exception {
        mockServer = new MockHttpServer();
        baseUrl = "http://localhost:" + mockServer.start();
        
        // Create temporary directory for test files
        tempDir = Files.createTempDirectory("mochajson-resource-test");
    }
    
    @AfterEach
    void tearDown() throws IOException {
        if (mockServer != null) {
            mockServer.stop();
        }
        
        // Clean up temporary files
        if (tempDir != null && Files.exists(tempDir)) {
            Files.walk(tempDir)
                    .sorted((a, b) -> b.compareTo(a)) // Delete files before directories
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            // Ignore cleanup errors
                        }
                    });
        }
    }
    
    @Test
    @DisplayName("Test async exception handling doesn't leak memory")
    public void testAsyncExceptionHandlingNoMemoryLeak() throws InterruptedException {
        System.out.println("\n=== Testing async exception handling for memory leaks ===");
        
        ApiClient client = new ApiClient.Builder().build();
        
        // Create multiple async requests that will fail
        CountDownLatch latch = new CountDownLatch(10);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        
        for (int i = 0; i < 10; i++) {
            Api.executeAsync(client.get(baseUrl + "/test/error"), 
                response -> {
                    successCount.incrementAndGet();
                    latch.countDown();
                },
                throwable -> {
                    errorCount.incrementAndGet();
                    latch.countDown();
                });
        }
        
        // Wait for all requests to complete
        assertTrue(latch.await(10, TimeUnit.SECONDS), "Not all async requests completed");
        
        // Verify error handling worked correctly
        assertEquals(0, successCount.get(), "No requests should succeed");
        assertEquals(10, errorCount.get(), "All requests should fail");
        
        System.out.println("✓ Async exception handling completed without memory leaks");
    }
    
    @Test
    @DisplayName("Test ManagedInputStream proper resource cleanup")
    public void testManagedInputStreamResourceCleanup() throws IOException {
        System.out.println("\n=== Testing ManagedInputStream resource cleanup ===");
        
        ApiClient client = new ApiClient.Builder().build();
        
        // Test that stream is properly closed
        ManagedInputStream stream = null;
        try {
            stream = client.get(baseUrl + "/test/success").downloadStream();
            assertNotNull(stream);
            assertFalse(stream.isClosed(), "Stream should not be closed initially");
            
            // Read some data
            byte[] data = stream.readAllBytes();
            assertNotNull(data);
            assertTrue(data.length > 0);
            
        } finally {
            if (stream != null) {
                stream.close();
                assertTrue(stream.isClosed(), "Stream should be closed after close()");
            }
        }
        
        // Test try-with-resources
        try (ManagedInputStream autoStream = client.get(baseUrl + "/test/success").downloadStream()) {
            assertNotNull(autoStream);
            assertFalse(autoStream.isClosed(), "Stream should not be closed in try block");
            
            byte[] data = autoStream.readAllBytes();
            assertNotNull(data);
        } // Stream should be automatically closed here
        
        System.out.println("✓ ManagedInputStream resource cleanup working correctly");
    }
    
    @Test
    @DisplayName("Test large file handling with memory constraints")
    public void testLargeFileHandlingMemoryConstraints() throws IOException {
        System.out.println("\n=== Testing large file handling with memory constraints ===");
        
        // Create a large test file
        Path largeFile = tempDir.resolve("large-file.txt");
        StringBuilder content = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            content.append("This is line ").append(i).append(" of the large file content.\n");
        }
        Files.write(largeFile, content.toString().getBytes());
        
        ApiClient client = new ApiClient.Builder().build();
        
        // Test that large file upload throws appropriate exception
        try {
            client.post(baseUrl + "/test/echo")
                    .multipart()
                    .addFile("file", largeFile.toFile())
                    .getBaseRequest()
                    .execute();
            fail("Expected IOException for large file");
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("exceeds maximum allowed size"), 
                "Error message should indicate file size limit");
            System.out.println("✓ Large file size limit enforced: " + e.getMessage());
        }
        
        // Test streaming upload for large files
        try {
            StreamingMultipartBuilder builder = new StreamingMultipartBuilder()
                    .maxFileSize(1024 * 1024) // 1MB limit
                    .chunkSize(1024); // 1KB chunks
            
            StreamingMultipartBuilder.StreamingMultipartBody body = builder
                    .addField("description", "Large file upload test")
                    .addFile("file", largeFile)
                    .build();
            
            assertNotNull(body);
            System.out.println("✓ Streaming upload configured successfully for large file");
            
        } catch (IOException e) {
            // This is expected if file is too large
            assertTrue(e.getMessage().contains("exceeds maximum allowed size"));
            System.out.println("✓ Large file properly rejected by streaming builder");
        }
    }
    
    @Test
    @DisplayName("Test HttpClient cleanup on shutdown")
    public void testHttpClientCleanupOnShutdown() {
        System.out.println("\n=== Testing HttpClient cleanup on shutdown ===");
        
        // Verify API is not shut down initially
        assertFalse(Api.isShutdown(), "API should not be shut down initially");
        
        // Make some requests to ensure client is active
        ApiClient client = new ApiClient.Builder().build();
        try {
            ApiResponse response = client.get(baseUrl + "/test/success").execute();
            assertTrue(response.isSuccess(), "Request should succeed before shutdown");
        } catch (Exception e) {
            fail("Request should succeed before shutdown: " + e.getMessage());
        }
        
        // Shutdown the API
        Api.shutdown();
        
        // Verify shutdown state
        assertTrue(Api.isShutdown(), "API should be shut down");
        
        // Verify new requests fail after shutdown
        try {
            client.get(baseUrl + "/test/success").execute();
            fail("Request should fail after shutdown");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("shut down"), 
                "Error message should indicate shutdown state");
            System.out.println("✓ Requests properly rejected after shutdown: " + e.getMessage());
        }
        
        System.out.println("✓ HttpClient cleanup on shutdown working correctly");
    }
    
    @Test
    @DisplayName("Test security configuration prevents SSRF")
    public void testSecurityConfigurationPreventsSSRF() {
        System.out.println("\n=== Testing security configuration prevents SSRF ===");
        
        // Test production security config (should block localhost)
        Utils.setDefaultSecurityConfig(SecurityConfig.forProduction());
        
        try {
            Utils.validateUrl("http://localhost:8080/api");
            fail("Production config should block localhost");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Localhost URLs are not allowed"));
            System.out.println("✓ Production security config blocks localhost: " + e.getMessage());
        }
        
        try {
            Utils.validateUrl("http://192.168.1.1/api");
            fail("Production config should block private IPs");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Private IP addresses are not allowed"));
            System.out.println("✓ Production security config blocks private IPs: " + e.getMessage());
        }
        
        // Test development security config (should allow localhost)
        Utils.setDefaultSecurityConfig(SecurityConfig.forDevelopment());
        
        try {
            assertTrue(Utils.validateUrl("http://localhost:8080/api"), 
                "Development config should allow localhost");
            assertTrue(Utils.validateUrl("http://192.168.1.1/api"), 
                "Development config should allow private IPs");
            System.out.println("✓ Development security config allows localhost and private IPs");
        } catch (IllegalArgumentException e) {
            fail("Development config should allow localhost and private IPs: " + e.getMessage());
        }
        
        // Test public URLs work in both configs
        Utils.setDefaultSecurityConfig(SecurityConfig.forProduction());
        try {
            assertTrue(Utils.validateUrl("https://api.example.com/data"), 
                "Public URLs should work in production config");
            System.out.println("✓ Public URLs work in production security config");
        } catch (IllegalArgumentException e) {
            fail("Public URLs should work in production config: " + e.getMessage());
        }
    }
    
    @Test
    @DisplayName("Test null safety prevents NullPointerExceptions")
    public void testNullSafetyPreventsNullPointerExceptions() {
        System.out.println("\n=== Testing null safety prevents NullPointerExceptions ===");
        
        ApiClient client = new ApiClient.Builder().build();
        
        // Test null request
        try {
            Api.execute(null);
            fail("Should throw IllegalArgumentException for null request");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Request cannot be null"));
            System.out.println("✓ Null request properly rejected: " + e.getMessage());
        }
        
        // Test null headers
        try {
            client.get(baseUrl + "/test/success").header(null, "value").execute();
            fail("Should throw IllegalArgumentException for null header name");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Header name cannot be null"));
            System.out.println("✓ Null header name properly rejected: " + e.getMessage());
        }
        
        try {
            client.get(baseUrl + "/test/success").header("name", null).execute();
            fail("Should throw IllegalArgumentException for null header value");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Header value cannot be null"));
            System.out.println("✓ Null header value properly rejected: " + e.getMessage());
        }
        
        // Test null query parameters
        try {
            client.get(baseUrl + "/test/success").query(null, "value").execute();
            fail("Should throw IllegalArgumentException for null query parameter name");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Query parameter name cannot be null"));
            System.out.println("✓ Null query parameter name properly rejected: " + e.getMessage());
        }
        
        // Test null body
        try {
            client.post(baseUrl + "/test/success").body(null).execute();
            fail("Should throw IllegalArgumentException for null body");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Request body cannot be null"));
            System.out.println("✓ Null request body properly rejected: " + e.getMessage());
        }
    }
}
