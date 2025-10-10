package io.mochaapi.client.test;

import io.mochaapi.client.*;
import io.mochaapi.client.exception.ApiException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mockito;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for ApiClient functionality.
 * Tests the new v1.1.0 features including interceptors, timeouts, and async operations.
 */
public class ApiClientTests {
    
    private MockHttpServer mockServer;
    private ApiClient client;
    private String baseUrl;
    
    @BeforeEach
    void setUp() throws Exception {
        mockServer = new MockHttpServer();
        baseUrl = "http://localhost:" + mockServer.start();
        
        // Create client with default configuration
        client = new ApiClient.Builder()
                .connectTimeout(Duration.ofSeconds(5))
                .readTimeout(Duration.ofSeconds(10))
                .build();
    }
    
    @AfterEach
    void tearDown() {
        if (mockServer != null) {
            mockServer.stop();
        }
    }
    
    @Test
    @DisplayName("GET request with success response")
    void testGetRequestSuccess() {
        ApiResponse response = client.get(baseUrl + "/test/success").execute();
        
        assertNotNull(response);
        assertEquals(200, response.code());
        assertTrue(response.isSuccess());
        assertTrue(response.isOk());
        
        Map<String, Object> data = response.toMap();
        assertEquals(1, data.get("id"));
        assertEquals("Test User", data.get("name"));
        assertEquals("test@example.com", data.get("email"));
    }
    
    @Test
    @DisplayName("POST request with JSON body")
    void testPostRequestWithBody() {
        Map<String, Object> requestData = Map.of(
            "name", "John Doe",
            "email", "john@example.com"
        );
        
        ApiResponse response = client.post(baseUrl + "/test/echo")
                .body(requestData)
                .execute();
        
        assertNotNull(response);
        assertEquals(200, response.code());
        assertTrue(response.isSuccess());
        
        // The mock server echoes the request body
        Map<String, Object> responseData = response.toMap();
        assertEquals("John Doe", responseData.get("name"));
        assertEquals("john@example.com", responseData.get("email"));
    }
    
    @Test
    @DisplayName("GET request with query parameters")
    void testGetRequestWithQueryParams() {
        ApiResponse response = client.get(baseUrl + "/test/query")
                .query("page", 1)
                .query("limit", 10)
                .query("search", "test query")
                .execute();
        
        assertNotNull(response);
        assertEquals(200, response.code());
        
        Map<String, Object> data = response.toMap();
        String query = (String) data.get("query");
        assertTrue(query.contains("page=1"));
        assertTrue(query.contains("limit=10"));
        assertTrue(query.contains("search=test+query"));
    }
    
    @Test
    @DisplayName("Error response handling")
    void testErrorResponse() {
        ApiResponse response = client.get(baseUrl + "/test/error").execute();
        
        assertNotNull(response);
        assertEquals(500, response.code());
        assertTrue(response.isError());
        assertTrue(response.isServerError());
        assertFalse(response.isSuccess());
    }
    
    @Test
    @DisplayName("404 Not Found response")
    void testNotFoundResponse() {
        ApiResponse response = client.get(baseUrl + "/test/not-found").execute();
        
        assertNotNull(response);
        assertEquals(404, response.code());
        assertTrue(response.isError());
        assertTrue(response.isClientError());
        assertTrue(response.isNotFound());
    }
    
    @Test
    @DisplayName("Async request execution")
    void testAsyncRequest() throws ExecutionException, InterruptedException, TimeoutException {
        CompletableFuture<ApiResponse> future = client.get(baseUrl + "/test/success").executeAsync();
        
        ApiResponse response = future.get(5, TimeUnit.SECONDS);
        
        assertNotNull(response);
        assertEquals(200, response.code());
        assertTrue(response.isSuccess());
    }
    
    @Test
    @DisplayName("Async request with callback")
    void testAsyncRequestWithCallback() throws InterruptedException {
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
    @DisplayName("Request interceptor functionality")
    void testRequestInterceptor() {
        AtomicInteger interceptorCallCount = new AtomicInteger(0);
        
        ApiClient clientWithInterceptor = new ApiClient.Builder()
                .addRequestInterceptor(request -> {
                    interceptorCallCount.incrementAndGet();
                    return request.header("X-Test-Header", "test-value");
                })
                .build();
        
        ApiResponse response = clientWithInterceptor.get(baseUrl + "/test/success").execute();
        
        assertEquals(1, interceptorCallCount.get());
        assertEquals(200, response.code());
    }
    
    @Test
    @DisplayName("Response interceptor functionality")
    void testResponseInterceptor() {
        AtomicInteger interceptorCallCount = new AtomicInteger(0);
        
        ApiClient clientWithInterceptor = new ApiClient.Builder()
                .addResponseInterceptor(response -> {
                    interceptorCallCount.incrementAndGet();
                    return response;
                })
                .build();
        
        ApiResponse response = clientWithInterceptor.get(baseUrl + "/test/success").execute();
        
        assertEquals(1, interceptorCallCount.get());
        assertEquals(200, response.code());
    }
    
    @Test
    @DisplayName("Multiple interceptors execution order")
    void testMultipleInterceptors() {
        StringBuilder executionOrder = new StringBuilder();
        
        ApiClient clientWithInterceptors = new ApiClient.Builder()
                .addRequestInterceptor(request -> {
                    executionOrder.append("1");
                    return request;
                })
                .addRequestInterceptor(request -> {
                    executionOrder.append("2");
                    return request;
                })
                .addResponseInterceptor(response -> {
                    executionOrder.append("3");
                    return response;
                })
                .addResponseInterceptor(response -> {
                    executionOrder.append("4");
                    return response;
                })
                .build();
        
        ApiResponse response = clientWithInterceptors.get(baseUrl + "/test/success").execute();
        
        assertEquals(200, response.code());
        assertEquals("1234", executionOrder.toString());
    }
    
    @Test
    @DisplayName("Custom timeout configuration")
    void testCustomTimeouts() {
        ApiClient clientWithTimeouts = new ApiClient.Builder()
                .connectTimeout(Duration.ofMillis(100))
                .readTimeout(Duration.ofMillis(100))
                .build();
        
        // This should timeout quickly due to the short timeout
        assertThrows(ApiException.class, () -> {
            clientWithTimeouts.get(baseUrl + "/test/timeout").execute();
        });
    }
    
    @Test
    @DisplayName("Request interceptor can modify request")
    void testRequestInterceptorModification() {
        ApiClient clientWithInterceptor = new ApiClient.Builder()
                .addRequestInterceptor(request -> {
                    return request.header("Authorization", "Bearer test-token")
                                   .query("timestamp", System.currentTimeMillis());
                })
                .build();
        
        // We can't easily verify the headers were added in this test setup,
        // but we can verify the request still works
        ApiResponse response = clientWithInterceptor.get(baseUrl + "/test/success").execute();
        assertEquals(200, response.code());
    }
    
    @Test
    @DisplayName("Response interceptor can throw exception")
    void testResponseInterceptorException() {
        ApiClient clientWithInterceptor = new ApiClient.Builder()
                .addResponseInterceptor(response -> {
                    if (response.code() == 200) {
                        throw new ApiException("Interceptor blocked successful response");
                    }
                    return response;
                })
                .build();
        
        assertThrows(ApiException.class, () -> {
            clientWithInterceptor.get(baseUrl + "/test/success").execute();
        });
    }
    
    @Test
    @DisplayName("Request interceptor can throw exception")
    void testRequestInterceptorException() {
        ApiClient clientWithInterceptor = new ApiClient.Builder()
                .addRequestInterceptor(request -> {
                    throw new ApiException("Request blocked by interceptor");
                })
                .build();
        
        assertThrows(ApiException.class, () -> {
            clientWithInterceptor.get(baseUrl + "/test/success").execute();
        });
    }
    
    @Test
    @DisplayName("Builder pattern configuration")
    void testBuilderPattern() {
        ApiClient.Builder builder = new ApiClient.Builder()
                .connectTimeout(Duration.ofSeconds(1))
                .readTimeout(Duration.ofSeconds(2))
                .writeTimeout(Duration.ofSeconds(3));
        
        ApiClient client = builder.build();
        assertNotNull(client);
        
        // Test that the client works
        ApiResponse response = client.get(baseUrl + "/test/success").execute();
        assertEquals(200, response.code());
    }
    
    @Test
    @DisplayName("Enable logging configuration")
    void testEnableLogging() {
        // This test mainly verifies that logging can be enabled without errors
        ApiClient clientWithLogging = new ApiClient.Builder()
                .enableLogging()
                .build();
        
        ApiResponse response = clientWithLogging.get(baseUrl + "/test/success").execute();
        assertEquals(200, response.code());
    }
}
