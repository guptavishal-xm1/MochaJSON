package io.mochaapi.client.example;

import io.mochaapi.client.*;

import java.time.Duration;
import java.util.Map;
import java.util.List;

/**
 * Example usage of MochaJSON client library.
 * Demonstrates the simplified API with core features in v1.3.0.
 * 
 * @since 1.3.0
 */
public class ExampleUsage {
    
    public static void main(String[] args) {
        System.out.println("=== MochaJSON Simplified API Example ===\n");
        
        try {
            // Example 1: Basic GET request with Map response
            System.out.println("1. Basic GET request with Map response:");
            Map<String, Object> user = Api.get("https://jsonplaceholder.typicode.com/users/1")
                    .execute()
                    .toMap();
            
            System.out.println("User: " + user.get("name"));
            System.out.println("Email: " + user.get("email"));
            System.out.println();
            
            // Example 2: Advanced client with simplified features
            System.out.println("2. Advanced client with simplified features:");
            ApiClient client = new ApiClient.Builder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .readTimeout(Duration.ofSeconds(30))
                    .enableRetry()  // Simple retry with 3 attempts, 1-second delay
                    .allowLocalhost(true)  // Allow localhost for development
                    .enableLogging()
                    .build();
            
            // POST request with JSON body
            Map<String, Object> newPost = client.post("https://jsonplaceholder.typicode.com/posts")
                    .header("Content-Type", "application/json")
                    .body(Map.of(
                            "title", "My New Post",
                            "body", "This is the content of my new post.",
                            "userId", 1
                    ))
                    .execute()
                    .toMap();
            
            System.out.println("Created post ID: " + newPost.get("id"));
            System.out.println();
            
            // Example 3: POJO parsing
            System.out.println("3. POJO parsing:");
            Post post = Api.get("https://jsonplaceholder.typicode.com/posts/1")
                    .execute()
                    .to(Post.class);
            System.out.println("Post Object - ID: " + post.id);
            System.out.println("Post Object - Title: " + post.title);
            System.out.println("Post Object - Body: " + post.body);
            System.out.println();
            
            // Example 4: List parsing with query parameters
            System.out.println("4. List parsing with query parameters:");
            List<Object> posts = Api.get("https://jsonplaceholder.typicode.com/posts")
                    .query("userId", 1)
                    .query("_limit", 3)
                    .execute()
                    .toList();
            
            System.out.println("Number of posts: " + posts.size());
            for (int i = 0; i < posts.size(); i++) {
                Map<String, Object> postMap = (Map<String, Object>) posts.get(i);
                System.out.println("Post " + (i + 1) + ": " + postMap.get("title"));
            }
            System.out.println();
            
            // Example 5: Async usage
            System.out.println("5. Async usage:");
            Api.executeAsync(Api.get("https://jsonplaceholder.typicode.com/posts/1"),
                    response -> {
                        Map<String, Object> postData = response.toMap();
                        System.out.println("Async post title: " + postData.get("title"));
                    },
                    error -> {
                        System.err.println("Async error: " + error.getMessage());
                    });
            
            // Wait a bit for async operation to complete
            Thread.sleep(2000);
            System.out.println();
            
            // Example 6: Error handling
            System.out.println("6. Error handling:");
            ApiResponse response = Api.get("https://jsonplaceholder.typicode.com/nonexistent")
                    .execute();
            
            if (response.isError()) {
                System.out.println("Error Status: " + response.code());
                System.out.println("Error Response: " + response.body());
            }
            System.out.println();
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("=== Simplified Features ===");
        System.out.println("✅ Basic HTTP requests (GET, POST, PUT, DELETE, PATCH)");
        System.out.println("✅ Automatic JSON parsing");
        System.out.println("✅ Simple retry configuration");
        System.out.println("✅ Localhost access control");
        System.out.println("✅ Async operations");
        System.out.println("✅ Request/Response interceptors");
        System.out.println("✅ Timeout configuration");
        System.out.println("❌ Removed: Circuit breaker, HTTP caching, complex retry policies");
        System.out.println("❌ Removed: Connection pooling config, streaming multipart");
        System.out.println("\n=== Example completed successfully! ===");
    }
    
    // POJO class for Post JSON parsing
    public static class Post {
        public int id;
        public int userId;
        public String title;
        public String body;
        
        @Override
        public String toString() {
            return "Post{id=" + id + ", userId=" + userId + ", title='" + title + "', body='" + body + "'}";
        }
    }
}