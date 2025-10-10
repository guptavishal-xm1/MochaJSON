---
title: Advanced Features
description: Learn about MochaJSON v1.1.0 advanced features including ApiClient, interceptors, virtual threads, and security enhancements.
---

# Advanced Features

MochaJSON v1.1.0 introduces powerful new features for enterprise-grade applications. This guide covers the advanced capabilities that make MochaJSON suitable for production environments.

## ApiClient with Builder Pattern

The new `ApiClient` class provides advanced configuration options through a fluent builder pattern.

### Basic Configuration

```java
import io.mochaapi.client.*;
import java.time.Duration;

// Create a configured client
ApiClient client = new ApiClient.Builder()
    .connectTimeout(Duration.ofSeconds(10))
    .readTimeout(Duration.ofSeconds(30))
    .writeTimeout(Duration.ofSeconds(15))
    .build();

// Use the client for requests
ApiResponse response = client.get("https://api.example.com/data")
    .query("page", 1)
    .execute();
```

### Custom Executor

```java
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

// Use a custom executor for async operations
Executor customExecutor = Executors.newFixedThreadPool(10);
ApiClient client = new ApiClient.Builder()
    .executor(customExecutor)
    .build();
```

## Request and Response Interceptors

Interceptors allow you to modify requests and responses, add logging, authentication, and more.

### Request Interceptors

```java
// Authentication interceptor
RequestInterceptor authInterceptor = RequestInterceptor.bearerAuth(() -> getToken());

// Logging interceptor
RequestInterceptor loggingInterceptor = RequestInterceptor.logging(System.out::println);

// Custom headers interceptor
Map<String, String> headers = Map.of(
    "X-API-Version", "v1.1.0",
    "X-Client-Type", "mobile"
);
RequestInterceptor headerInterceptor = RequestInterceptor.addHeaders(headers);

// Custom interceptor
RequestInterceptor customInterceptor = request -> {
    System.out.println("Sending request to: " + request.getUrl());
    return request.header("X-Request-ID", UUID.randomUUID().toString());
};

ApiClient client = new ApiClient.Builder()
    .addRequestInterceptor(authInterceptor)
    .addRequestInterceptor(loggingInterceptor)
    .addRequestInterceptor(headerInterceptor)
    .addRequestInterceptor(customInterceptor)
    .build();
```

### Response Interceptors

```java
// Logging interceptor
ResponseInterceptor loggingInterceptor = ResponseInterceptor.logging(System.out::println);

// Error handling interceptor
ResponseInterceptor errorInterceptor = ResponseInterceptor.throwOnError();

// Retry interceptor
int[] retryableCodes = {500, 502, 503, 504};
ResponseInterceptor retryInterceptor = ResponseInterceptor.retryOnStatus(retryableCodes, 3);

// Custom interceptor
ResponseInterceptor customInterceptor = response -> {
    if (response.code() >= 400) {
        System.err.println("Error response: " + response.code() + " - " + response.body());
    }
    return response;
};

ApiClient client = new ApiClient.Builder()
    .addResponseInterceptor(loggingInterceptor)
    .addResponseInterceptor(errorInterceptor)
    .addResponseInterceptor(retryInterceptor)
    .addResponseInterceptor(customInterceptor)
    .build();
```

## Enhanced Async Operations

MochaJSON v1.1.0 provides modern async APIs with virtual threads support.

### CompletableFuture API

```java
import java.util.concurrent.CompletableFuture;

ApiClient client = new ApiClient.Builder().build();

// Basic async request
CompletableFuture<ApiResponse> future = client.get("https://api.example.com/data")
    .executeAsync();

// Handle the response
future.thenAccept(response -> {
    System.out.println("Response: " + response.code());
    Map<String, Object> data = response.toMap();
    System.out.println("Data: " + data);
});

// Chain operations
CompletableFuture<String> chainedFuture = client.get("https://api.example.com/user")
    .executeAsync()
    .thenApply(response -> {
        Map<String, Object> user = response.toMap();
        return user.get("name").toString();
    })
    .thenApply(name -> {
        System.out.println("Processing user: " + name);
        return name.toUpperCase();
    });

String result = chainedFuture.get();
```

### Multiple Concurrent Requests

```java
// Start multiple requests concurrently
CompletableFuture<ApiResponse> userFuture = client.get("https://api.example.com/user").executeAsync();
CompletableFuture<ApiResponse> postsFuture = client.get("https://api.example.com/posts").executeAsync();
CompletableFuture<ApiResponse> commentsFuture = client.get("https://api.example.com/comments").executeAsync();

// Wait for all to complete
CompletableFuture<Void> allFutures = CompletableFuture.allOf(userFuture, postsFuture, commentsFuture);
allFutures.get();

// Process results
ApiResponse userResponse = userFuture.get();
ApiResponse postsResponse = postsFuture.get();
ApiResponse commentsResponse = commentsFuture.get();
```

### Async with Callbacks

```java
// Using callbacks (backward compatible)
client.get("https://api.example.com/data")
    .async(response -> {
        System.out.println("Async response: " + response.code());
        // Process response...
    });
```

## Virtual Threads Support

MochaJSON automatically uses virtual threads when running on Java 21+, providing better performance for concurrent operations.

### Automatic Virtual Threads

```java
// Virtual threads are used automatically on Java 21+
ApiClient client = new ApiClient.Builder().build();

// These requests will use virtual threads for better performance
for (int i = 0; i < 1000; i++) {
    client.get("https://api.example.com/data/" + i)
        .executeAsync()
        .thenAccept(response -> {
            // Process response...
        });
}
```

### Fallback for Older Java Versions

```java
// On Java 17 or earlier, MochaJSON automatically falls back to cached thread pool
// No code changes required - it's handled transparently
```

## Security Features

MochaJSON v1.1.0 includes comprehensive security enhancements.

### URL Validation

```java
// Valid URLs are accepted
client.get("https://api.example.com/data").execute(); // ✅ OK
client.get("http://api.example.com/data").execute();  // ✅ OK

// Dangerous URLs are rejected
try {
    client.get("javascript:alert('xss')").execute(); // ❌ IllegalArgumentException
} catch (IllegalArgumentException e) {
    System.out.println("Dangerous URL rejected: " + e.getMessage());
}

try {
    client.get("file:///etc/passwd").execute(); // ❌ IllegalArgumentException
} catch (IllegalArgumentException e) {
    System.out.println("File URL rejected: " + e.getMessage());
}
```

### Hardened JSON Parsing

```java
// JSON parsing is automatically hardened against common attacks
// - Polymorphic typing is disabled
// - Dangerous features are turned off
// - Input validation is enhanced

ApiResponse response = client.get("https://api.example.com/data").execute();
Map<String, Object> data = response.toMap(); // Safe parsing
```

### Timeout Protection

```java
// Configure timeouts to prevent resource exhaustion
ApiClient client = new ApiClient.Builder()
    .connectTimeout(Duration.ofMillis(500))  // 500ms connection timeout
    .readTimeout(Duration.ofSeconds(5))      // 5s read timeout
    .build();

// This will timeout quickly if the server is slow
try {
    client.get("https://slow-api.example.com/data").execute();
} catch (ApiException e) {
    System.out.println("Request timed out: " + e.getMessage());
}
```

## Logging Support

Enable comprehensive logging for debugging and monitoring.

### Basic Logging

```java
ApiClient client = new ApiClient.Builder()
    .enableLogging()  // Enables console logging
    .build();

// All requests and responses will be logged to console
client.get("https://api.example.com/data").execute();
```

### Custom Logging

```java
import java.util.function.Consumer;

// Custom logger function
Consumer<String> customLogger = message -> {
    // Log to your preferred logging framework
    logger.info(message);
};

ApiClient client = new ApiClient.Builder()
    .addRequestInterceptor(RequestInterceptor.logging(customLogger))
    .addResponseInterceptor(ResponseInterceptor.logging(customLogger))
    .build();
```

### SLF4J Integration

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

Logger logger = LoggerFactory.getLogger(MyClass.class);

ApiClient client = new ApiClient.Builder()
    .addRequestInterceptor(RequestInterceptor.logging(logger::info))
    .addResponseInterceptor(ResponseInterceptor.logging(logger::info))
    .build();
```

## Migration from v1.0.x

MochaJSON v1.1.0 is 100% backward compatible with v1.0.x.

### Existing Code Continues to Work

```java
// This code from v1.0.x works exactly the same in v1.1.0
ApiResponse response = Api.get("https://api.example.com/data")
    .query("page", 1)
    .execute();

Map<String, Object> data = response.toMap();
```

### Optional Migration to ApiClient

```java
// Old way (still works)
ApiResponse response = Api.get("https://api.example.com/data")
    .query("page", 1)
    .execute();

// New way (optional, for advanced features)
ApiClient client = new ApiClient.Builder()
    .connectTimeout(Duration.ofSeconds(10))
    .enableLogging()
    .build();

ApiResponse response = client.get("https://api.example.com/data")
    .query("page", 1)
    .execute();
```

## Best Practices

### Use ApiClient for Production

```java
// Recommended for production applications
ApiClient client = new ApiClient.Builder()
    .connectTimeout(Duration.ofSeconds(10))
    .readTimeout(Duration.ofSeconds(30))
    .enableLogging()
    .addRequestInterceptor(RequestInterceptor.bearerAuth(() -> getToken()))
    .addResponseInterceptor(ResponseInterceptor.throwOnError())
    .build();
```

### Handle Async Operations Properly

```java
// Good: Proper exception handling
CompletableFuture<ApiResponse> future = client.get("https://api.example.com/data")
    .executeAsync();

future.thenAccept(response -> {
    // Handle success
}).exceptionally(throwable -> {
    // Handle error
    logger.error("Request failed", throwable);
    return null;
});
```

### Use Interceptors for Cross-Cutting Concerns

```java
// Authentication for all requests
ApiClient authenticatedClient = new ApiClient.Builder()
    .addRequestInterceptor(RequestInterceptor.bearerAuth(() -> getToken()))
    .build();

// Logging for debugging
ApiClient debugClient = new ApiClient.Builder()
    .enableLogging()
    .build();
```

## Performance Considerations

### Virtual Threads

- Virtual threads provide better performance for I/O-bound operations
- Use `executeAsync()` for concurrent requests
- Virtual threads are automatically used on Java 21+

### Connection Pooling

- MochaJSON uses Java's built-in HTTP client connection pooling
- Configure timeouts appropriately for your use case
- Consider connection limits for high-traffic applications

### Memory Usage

- MochaJSON has a minimal memory footprint
- Response bodies are streamed when possible
- Use appropriate timeout values to prevent memory leaks
