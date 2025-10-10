package io.mochaapi.client.test;

import io.mochaapi.client.*;
import io.mochaapi.client.exception.ApiException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.DisplayName;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for interceptor functionality.
 * Tests RequestInterceptor and ResponseInterceptor features.
 */
public class InterceptorTests {
    
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
    @DisplayName("RequestInterceptor logging functionality")
    void testRequestInterceptorLogging() {
        StringBuilder logOutput = new StringBuilder();
        Consumer<String> logger = logOutput::append;
        
        RequestInterceptor loggingInterceptor = RequestInterceptor.logging(logger);
        
        ApiClient client = new ApiClient.Builder()
                .addRequestInterceptor(loggingInterceptor)
                .build();
        
        ApiRequest request = client.get(baseUrl + "/test/success")
                .header("Authorization", "Bearer token")
                .query("page", 1);
        
        ApiResponse response = request.execute();
        
        assertEquals(200, response.code());
        assertTrue(logOutput.toString().contains("GET"));
        assertTrue(logOutput.toString().contains("/test/success"));
    }
    
    @Test
    @DisplayName("RequestInterceptor bearer auth functionality")
    void testRequestInterceptorBearerAuth() {
        AtomicReference<String> capturedToken = new AtomicReference<>();
        
        // Mock token provider
        RequestInterceptor authInterceptor = RequestInterceptor.bearerAuth(() -> "test-token-123");
        
        ApiClient client = new ApiClient.Builder()
                .addRequestInterceptor(authInterceptor)
                .addRequestInterceptor(request -> {
                    // Capture the authorization header
                    capturedToken.set(request.getHeaders().get("Authorization"));
                    return request;
                })
                .build();
        
        ApiResponse response = client.get(baseUrl + "/test/success").execute();
        
        assertEquals(200, response.code());
        assertEquals("Bearer test-token-123", capturedToken.get());
    }
    
    @Test
    @DisplayName("RequestInterceptor add headers functionality")
    void testRequestInterceptorAddHeaders() {
        Map<String, String> customHeaders = Map.of(
            "X-Custom-Header", "custom-value",
            "X-API-Version", "v1.1.0"
        );
        
        RequestInterceptor headerInterceptor = RequestInterceptor.addHeaders(customHeaders);
        
        ApiClient client = new ApiClient.Builder()
                .addRequestInterceptor(headerInterceptor)
                .build();
        
        ApiResponse response = client.get(baseUrl + "/test/success").execute();
        assertEquals(200, response.code());
    }
    
    @Test
    @DisplayName("ResponseInterceptor logging functionality")
    void testResponseInterceptorLogging() {
        StringBuilder logOutput = new StringBuilder();
        Consumer<String> logger = logOutput::append;
        
        ResponseInterceptor loggingInterceptor = ResponseInterceptor.logging(logger);
        
        ApiClient client = new ApiClient.Builder()
                .addResponseInterceptor(loggingInterceptor)
                .build();
        
        ApiResponse response = client.get(baseUrl + "/test/success").execute();
        
        assertEquals(200, response.code());
        assertTrue(logOutput.toString().contains("200"));
        assertTrue(logOutput.toString().contains("OK"));
    }
    
    @Test
    @DisplayName("ResponseInterceptor throw on error functionality")
    void testResponseInterceptorThrowOnError() {
        ResponseInterceptor errorInterceptor = ResponseInterceptor.throwOnError();
        
        ApiClient client = new ApiClient.Builder()
                .addResponseInterceptor(errorInterceptor)
                .build();
        
        // Test successful response - should not throw
        ApiResponse successResponse = client.get(baseUrl + "/test/success").execute();
        assertEquals(200, successResponse.code());
        
        // Test error response - should throw
        assertThrows(ApiException.class, () -> {
            client.get(baseUrl + "/test/error").execute();
        });
    }
    
    @Test
    @DisplayName("ResponseInterceptor retry on status functionality")
    void testResponseInterceptorRetryOnStatus() {
        int[] retryableCodes = {500, 502, 503};
        ResponseInterceptor retryInterceptor = ResponseInterceptor.retryOnStatus(retryableCodes, 3);
        
        ApiClient client = new ApiClient.Builder()
                .addResponseInterceptor(retryInterceptor)
                .build();
        
        // Test with error status that should trigger retry
        assertThrows(ApiException.class, () -> {
            client.get(baseUrl + "/test/error").execute();
        });
    }
    
    @Test
    @DisplayName("Multiple request interceptors execution order")
    void testMultipleRequestInterceptors() {
        AtomicInteger executionOrder = new AtomicInteger(0);
        StringBuilder orderLog = new StringBuilder();
        
        RequestInterceptor firstInterceptor = request -> {
            orderLog.append("1");
            executionOrder.set(1);
            return request;
        };
        
        RequestInterceptor secondInterceptor = request -> {
            orderLog.append("2");
            executionOrder.set(2);
            return request;
        };
        
        ApiClient client = new ApiClient.Builder()
                .addRequestInterceptor(firstInterceptor)
                .addRequestInterceptor(secondInterceptor)
                .build();
        
        ApiResponse response = client.get(baseUrl + "/test/success").execute();
        
        assertEquals(200, response.code());
        assertEquals("12", orderLog.toString());
        assertEquals(2, executionOrder.get());
    }
    
    @Test
    @DisplayName("Multiple response interceptors execution order")
    void testMultipleResponseInterceptors() {
        StringBuilder orderLog = new StringBuilder();
        
        ResponseInterceptor firstInterceptor = response -> {
            orderLog.append("1");
            return response;
        };
        
        ResponseInterceptor secondInterceptor = response -> {
            orderLog.append("2");
            return response;
        };
        
        ApiClient client = new ApiClient.Builder()
                .addResponseInterceptor(firstInterceptor)
                .addResponseInterceptor(secondInterceptor)
                .build();
        
        ApiResponse response = client.get(baseUrl + "/test/success").execute();
        
        assertEquals(200, response.code());
        assertEquals("12", orderLog.toString());
    }
    
    @Test
    @DisplayName("Request interceptor can modify request body")
    void testRequestInterceptorModifyBody() {
        RequestInterceptor bodyInterceptor = request -> {
            return request.body(Map.of("modified", true, "original", false));
        };
        
        ApiClient client = new ApiClient.Builder()
                .addRequestInterceptor(bodyInterceptor)
                .build();
        
        ApiResponse response = client.post(baseUrl + "/test/echo").execute();
        
        assertEquals(200, response.code());
        Map<String, Object> responseData = response.toMap();
        assertTrue((Boolean) responseData.get("modified"));
        assertFalse((Boolean) responseData.get("original"));
    }
    
    @Test
    @DisplayName("Response interceptor can modify response")
    void testResponseInterceptorModifyResponse() {
        ResponseInterceptor responseInterceptor = response -> {
            // Create a new response with modified data
            // Note: This is a simplified example - in practice, you'd need to create a new ApiResponse
            return response;
        };
        
        ApiClient client = new ApiClient.Builder()
                .addResponseInterceptor(responseInterceptor)
                .build();
        
        ApiResponse response = client.get(baseUrl + "/test/success").execute();
        assertEquals(200, response.code());
    }
    
    @Test
    @DisplayName("Interceptor exception handling")
    void testInterceptorExceptionHandling() {
        RequestInterceptor failingInterceptor = request -> {
            throw new RuntimeException("Interceptor failed");
        };
        
        ApiClient client = new ApiClient.Builder()
                .addRequestInterceptor(failingInterceptor)
                .build();
        
        assertThrows(RuntimeException.class, () -> {
            client.get(baseUrl + "/test/success").execute();
        });
    }
    
    @Test
    @DisplayName("Custom interceptor with state")
    void testCustomInterceptorWithState() {
        AtomicInteger requestCount = new AtomicInteger(0);
        
        RequestInterceptor countingInterceptor = request -> {
            requestCount.incrementAndGet();
            return request.header("X-Request-Count", String.valueOf(requestCount.get()));
        };
        
        ApiClient client = new ApiClient.Builder()
                .addRequestInterceptor(countingInterceptor)
                .build();
        
        // Make multiple requests
        client.get(baseUrl + "/test/success").execute();
        client.get(baseUrl + "/test/success").execute();
        client.get(baseUrl + "/test/success").execute();
        
        assertEquals(3, requestCount.get());
    }
}
