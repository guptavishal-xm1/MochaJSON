package io.mochaapi.client.test;

import io.mochaapi.client.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for the MochaAPI client library.
 * Tests HTTP requests using a mock server for reliability and speed.
 * 
 * @since 1.1.0 Updated to use mock server and test new async features
 */
public class ApiTests {
    
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
    
    /**
     * Test POJO class for JSON deserialization.
     */
    public static class Post {
        public int id;
        public int userId;
        public String title;
        public String body;
        
        public Post() {}
        
        public Post(int id, int userId, String title, String body) {
            this.id = id;
            this.userId = userId;
            this.title = title;
            this.body = body;
        }
        
        @Override
        public String toString() {
            return "Post{id=" + id + ", userId=" + userId + ", title='" + title + "', body='" + body + "'}";
        }
    }
    
    @Test
    @DisplayName("GET request with Map response")
    public void testGetRequestWithMapResponse() {
        System.out.println("\n=== Testing GET request with Map response ===");
        
        // Execute GET request
        ApiResponse response = Api.get(baseUrl + "/test/success")
                .execute();
        
        // Verify response
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals(200, response.code());
        
        // Print response body
        System.out.println("Response Body: " + response.body());
        
        // Convert to Map
        Map<String, Object> userMap = response.toMap();
        assertNotNull(userMap);
        assertTrue(userMap.containsKey("id"));
        assertTrue(userMap.containsKey("name"));
        
        // Print parsed map
        System.out.println("Parsed Map: " + userMap);
        System.out.println("User ID: " + userMap.get("id"));
        System.out.println("User Name: " + userMap.get("name"));
        
        // Verify map contents
        assertEquals(1, userMap.get("id"));
        assertEquals("Test User", userMap.get("name"));
        assertNotNull(userMap.get("email"));
    }
    
    @Test
    @DisplayName("GET request with POJO response")
    public void testGetRequestWithPojoResponse() {
        System.out.println("\n=== Testing GET request with POJO response ===");
        
        // Execute GET request
        ApiResponse response = Api.get(baseUrl + "/test/success")
                .execute();
        
        // Verify response
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals(200, response.code());
        
        // Print response body
        System.out.println("Response Body: " + response.body());
        
        // Convert to POJO
        Post post = response.to(Post.class);
        assertNotNull(post);
        
        // Print parsed POJO
        System.out.println("Parsed POJO: " + post);
        System.out.println("Post ID: " + post.id);
        System.out.println("Post Title: " + post.title);
        System.out.println("Post Body: " + post.body);
        
        // Verify POJO contents (adjusted for mock server response)
        assertEquals(1, post.id);
        assertNotNull(post.title);
        assertNotNull(post.body);
    }
    
    @Test
    @DisplayName("POST request with JSON body")
    public void testPostRequestWithJsonBody() {
        System.out.println("\n=== Testing POST request with JSON body ===");
        
        // Create test data
        Map<String, Object> testData = Map.of(
            "name", "Test User",
            "email", "test@example.com"
        );
        
        // Execute POST request
        ApiResponse response = Api.post(baseUrl + "/test/echo")
                .header("Content-Type", "application/json")
                .body(testData)
                .execute();
        
        // Verify response
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals(200, response.code());
        
        // Print response details
        System.out.println("Response Status Code: " + response.code());
        System.out.println("Response Body: " + response.body());
        
        // Parse response to verify (echo server returns the request body)
        Map<String, Object> responseMap = response.toMap();
        assertNotNull(responseMap);
        
        System.out.println("Echoed Data: " + responseMap);
        System.out.println("Response Headers: " + response.headers());
        
        // Verify the response contains our data
        assertEquals("Test User", responseMap.get("name"));
        assertEquals("test@example.com", responseMap.get("email"));
    }
    
    @Test
    @DisplayName("GET request with query parameters")
    public void testGetRequestWithQueryParams() {
        System.out.println("\n=== Testing GET request with query parameters ===");
        
        // Execute GET request with query parameters
        ApiResponse response = Api.get(baseUrl + "/test/query")
                .query("page", 1)
                .query("limit", 10)
                .query("search", "test")
                .execute();
        
        // Verify response
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals(200, response.code());
        
        // Print response details
        System.out.println("Response Status Code: " + response.code());
        System.out.println("Response Body: " + response.body());
        
        // Verify query parameters were properly encoded
        Map<String, Object> data = response.toMap();
        String query = (String) data.get("query");
        assertNotNull(query);
        assertTrue(query.contains("page=1"));
        assertTrue(query.contains("limit=10"));
        assertTrue(query.contains("search=test"));
        
        System.out.println("Query string: " + query);
    }
    
    @Test
    @DisplayName("PUT request with JSON body")
    public void testPutRequestWithJsonBody() {
        System.out.println("\n=== Testing PUT request with JSON body ===");
        
        // Create test data
        Post updatedPost = new Post(1, 1, "Updated Post Title", "This is the updated post body content.");
        
        // Execute PUT request
        ApiResponse response = Api.put(baseUrl + "/test/echo")
                .header("Content-Type", "application/json")
                .body(updatedPost)
                .execute();
        
        // Verify response
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals(200, response.code());
        
        // Print response details
        System.out.println("Response Status Code: " + response.code());
        System.out.println("Response Body: " + response.body());
        
        // Parse response to verify
        Post responsePost = response.to(Post.class);
        assertNotNull(responsePost);
        
        System.out.println("Updated Post: " + responsePost);
        
        // Verify the response contains our updated data
        assertEquals(1, responsePost.id);
        assertEquals("Updated Post Title", responsePost.title);
        assertEquals("This is the updated post body content.", responsePost.body);
    }
    
    @Test
    @DisplayName("DELETE request")
    public void testDeleteRequest() {
        System.out.println("\n=== Testing DELETE request ===");
        
        // Execute DELETE request
        ApiResponse response = Api.delete(baseUrl + "/test/success")
                .execute();
        
        // Verify response
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals(200, response.code());
        
        // Print response details
        System.out.println("Response Status Code: " + response.code());
        System.out.println("Response Body: " + response.body());
        
        // Verify empty response body (typical for DELETE)
        assertTrue(response.body().isEmpty() || response.body().equals("{}"));
    }
    
    @Test
    @DisplayName("Error handling test")
    public void testErrorHandling() {
        System.out.println("\n=== Testing error handling ===");
        
        // Execute request to non-existent endpoint
        ApiResponse response = Api.get(baseUrl + "/test/not-found")
                .execute();
        
        // Verify error response
        assertNotNull(response);
        assertTrue(response.isError());
        assertEquals(404, response.code());
        
        // Print error details
        System.out.println("Error Status Code: " + response.code());
        System.out.println("Error Response Body: " + response.body());
        
        // Verify error response structure
        assertTrue(response.body().isEmpty() || response.body().equals("{}"));
    }
    
    @Test
    @DisplayName("Async request execution")
    public void testAsyncRequest() throws ExecutionException, InterruptedException {
        System.out.println("\n=== Testing async request execution ===");
        
        // Execute async request
        CompletableFuture<ApiResponse> future = Api.get(baseUrl + "/test/success").executeAsync();
        
        ApiResponse response = future.get();
        
        // Verify response
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals(200, response.code());
        
        System.out.println("Async Response: " + response.body());
        
        // Convert to Map
        Map<String, Object> userMap = response.toMap();
        assertNotNull(userMap);
        assertEquals(1, userMap.get("id"));
        assertEquals("Test User", userMap.get("name"));
    }
    
    @Test
    @DisplayName("ApiClient with interceptors")
    public void testApiClientWithInterceptors() {
        System.out.println("\n=== Testing ApiClient with interceptors ===");
        
        ApiClient client = new ApiClient.Builder()
                .enableLogging()
                .addRequestInterceptor(request -> {
                    System.out.println("Request interceptor: " + request.getMethod() + " " + request.getUrl());
                    return request;
                })
                .addResponseInterceptor(response -> {
                    System.out.println("Response interceptor: " + response.code() + " " + response.getStatusDescription());
                    return response;
                })
                .build();
        
        ApiResponse response = client.get(baseUrl + "/test/success").execute();
        
        assertNotNull(response);
        assertEquals(200, response.code());
        assertTrue(response.isSuccess());
        
        System.out.println("Client Response: " + response.body());
    }
}
