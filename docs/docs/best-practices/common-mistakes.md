---
title: Common Mistakes
description: Avoid these common mistakes when using MochaJSON. Learn about error handling, async operations, connection management, and security pitfalls.
keywords:
  - MochaJSON mistakes
  - HTTP client errors
  - Java HTTP mistakes
  - Kotlin HTTP mistakes
  - debugging MochaJSON
---

# Common Mistakes

Avoid these common pitfalls when using MochaJSON. Learn from others' mistakes and build more reliable applications.

## ❌ Error Handling Mistakes

### Mistake 1: Not Handling Exceptions

```java
// ❌ BAD: Silent failures
public User getUser(String id) {
    Map<String, Object> response = Api.get("/api/users/" + id)
        .execute()
        .toMap();
    return new User(response); // Could throw NullPointerException
}

// ✅ GOOD: Proper error handling
public User getUser(String id) {
    try {
        Map<String, Object> response = Api.get("/api/users/" + id)
            .execute()
            .toMap();
        return new User(response);
    } catch (ApiException e) {
        logger.error("Failed to get user: " + id, e);
        throw new UserServiceException("User service unavailable", e);
    } catch (JsonException e) {
        logger.error("Invalid response format for user: " + id, e);
        throw new DataProcessingException("Invalid user data", e);
    }
}
```

### Mistake 2: Not Managing Resources Properly

```java
// ❌ BAD: Memory leak - InputStream not closed
public byte[] downloadFile(String url) {
    InputStream stream = client.get(url).downloadStream();
    return stream.readAllBytes(); // Stream never closed!
}

// ✅ GOOD: Always use try-with-resources
public byte[] downloadFile(String url) {
    try (ManagedInputStream stream = client.get(url).downloadStream()) {
        return stream.readAllBytes();
    } // Stream is automatically closed
}
```

### Mistake 3: Swallowing Exceptions

```java
// ❌ BAD: Swallowing exceptions
public User getUser(String id) {
    try {
        return Api.get("/api/users/" + id)
            .execute()
            .to(User.class);
    } catch (Exception e) {
        // Silent failure - very bad!
        return null;
    }
}

// ✅ GOOD: Proper exception propagation
public User getUser(String id) {
    try {
        return Api.get("/api/users/" + id)
            .execute()
            .to(User.class);
    } catch (ApiException e) {
        if (e.getStatusCode() == 404) {
            throw new UserNotFoundException("User not found: " + id);
        }
        throw new UserServiceException("Failed to retrieve user", e);
    }
}
```

### Mistake 3: Not Checking Response Status

```java
// ❌ BAD: Not checking response status
public String getData(String url) {
    ApiResponse response = Api.get(url).execute();
    return response.body(); // Could be error response body
}

// ✅ GOOD: Check response status
public String getData(String url) {
    ApiResponse response = Api.get(url).execute();
    
    if (response.isError()) {
        throw new ApiException("API returned error: " + response.code());
    }
    
    return response.body();
}
```

## ❌ Async Operation Mistakes

### Mistake 4: Blocking Async Operations

```java
// ❌ BAD: Blocking async operations
public List<User> getAllUsers() {
    List<CompletableFuture<User>> futures = new ArrayList<>();
    
    for (String id : userIds) {
        CompletableFuture<User> future = client.get("/api/users/" + id)
            .executeAsync()
            .thenApply(response -> response.to(User.class));
        futures.add(future);
    }
    
    // ❌ This blocks the thread!
    return futures.stream()
        .map(CompletableFuture::join)
        .collect(Collectors.toList());
}

// ✅ GOOD: Proper async handling
public CompletableFuture<List<User>> getAllUsersAsync() {
    List<CompletableFuture<User>> futures = userIds.stream()
        .map(id -> client.get("/api/users/" + id)
            .executeAsync()
            .thenApply(response -> response.to(User.class)))
        .collect(Collectors.toList());
    
    return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
        .thenApply(v -> futures.stream()
            .map(CompletableFuture::join)
            .collect(Collectors.toList()));
}
```

### Mistake 5: Not Handling Async Exceptions

```java
// ❌ BAD: Unhandled async exceptions
public void processUsers(List<String> userIds) {
    userIds.forEach(id -> 
        client.get("/api/users/" + id)
            .executeAsync()
            .thenAccept(response -> {
                // ❌ Exception here will be swallowed!
                User user = response.to(User.class);
                processUser(user);
            })
    );
}

// ✅ GOOD: Handle async exceptions
public void processUsers(List<String> userIds) {
    userIds.forEach(id -> 
        client.get("/api/users/" + id)
            .executeAsync()
            .thenAccept(response -> {
                try {
                    User user = response.to(User.class);
                    processUser(user);
                } catch (Exception e) {
                    logger.error("Failed to process user: " + id, e);
                }
            })
            .exceptionally(throwable -> {
                logger.error("Failed to get user: " + id, throwable);
                return null;
            })
    );
}
```

## ❌ Connection Management Mistakes

### Mistake 6: Creating New Clients for Each Request

```java
// ❌ BAD: Creating new client for each request
public User getUser(String id) {
    ApiClient client = new ApiClient.Builder().build(); // ❌ New client every time!
    return client.get("/api/users/" + id)
        .execute()
        .to(User.class);
}

// ✅ GOOD: Reuse client instance
public class UserService {
    private final ApiClient client = new ApiClient.Builder()
        .enableConnectionPooling()
        .build();
    
    public User getUser(String id) {
        return client.get("/api/users/" + id)
            .execute()
            .to(User.class);
    }
}
```

### Mistake 7: Not Configuring Timeouts

```java
// ❌ BAD: Using default timeouts in production
ApiClient client = new ApiClient.Builder().build(); // Default timeouts might be too long

// ✅ GOOD: Configure appropriate timeouts
ApiClient client = new ApiClient.Builder()
    .connectTimeout(Duration.ofSeconds(10))    // Fast connection timeout
    .readTimeout(Duration.ofSeconds(30))       // Reasonable read timeout
    .build();
```

### Mistake 8: Not Enabling Connection Pooling

```java
// ❌ BAD: No connection pooling
ApiClient client = new ApiClient.Builder().build();

// ✅ GOOD: Enable connection pooling for better performance
ApiClient client = new ApiClient.Builder()
    .enableConnectionPooling()
    .build();
```

## ❌ Security Mistakes

### Mistake 9: Hardcoding Secrets

```java
// ❌ BAD: Hardcoded API key
ApiClient client = new ApiClient.Builder()
    .addRequestInterceptor(RequestInterceptor.bearerAuth(() -> "secret-api-key"))
    .build();

// ✅ GOOD: Use environment variables or secure storage
@Value("${api.key}")
private String apiKey;

ApiClient client = new ApiClient.Builder()
    .addRequestInterceptor(RequestInterceptor.bearerAuth(() -> apiKey))
    .build();
```

### Mistake 10: Not Validating Input

```java
// ❌ BAD: No input validation
public User getUser(String id) {
    return client.get("/api/users/" + id)  // ❌ Could be malicious input
        .execute()
        .to(User.class);
}

// ✅ GOOD: Validate and sanitize input
public User getUser(String id) {
    if (id == null || id.trim().isEmpty()) {
        throw new IllegalArgumentException("User ID cannot be null or empty");
    }
    
    // Sanitize the ID
    String sanitizedId = id.trim().replaceAll("[^a-zA-Z0-9-_]", "");
    
    return client.get("/api/users/" + sanitizedId)
        .execute()
        .to(User.class);
}
```

### Mistake 11: Using HTTP in Production

```java
// ❌ BAD: HTTP in production
ApiClient client = new ApiClient.Builder()
    .baseUrl("http://api.example.com")  // ❌ Not secure
    .build();

// ✅ GOOD: Always use HTTPS in production
ApiClient client = new ApiClient.Builder()
    .baseUrl("https://api.example.com")  // ✅ Secure
    .build();
```

## ❌ Performance Mistakes

### Mistake 12: Not Using Caching

```java
// ❌ BAD: No caching for frequently accessed data
public User getUser(String id) {
    return client.get("/api/users/" + id)  // ❌ Hits API every time
        .execute()
        .to(User.class);
}

// ✅ GOOD: Enable caching for better performance
ApiClient client = new ApiClient.Builder()
    .enableCaching()
    .build();

public User getUser(String id) {
    return client.get("/api/users/" + id)  // ✅ Uses cache when appropriate
        .execute()
        .to(User.class);
}
```

### Mistake 13: Synchronous Operations in Loops

```java
// ❌ BAD: Synchronous operations in loop
public List<User> getUsers(List<String> ids) {
    return ids.stream()
        .map(id -> client.get("/api/users/" + id).execute().to(User.class))  // ❌ Sequential
        .collect(Collectors.toList());
}

// ✅ GOOD: Parallel async operations
public CompletableFuture<List<User>> getUsersAsync(List<String> ids) {
    List<CompletableFuture<User>> futures = ids.stream()
        .map(id -> client.get("/api/users/" + id)
            .executeAsync()
            .thenApply(response -> response.to(User.class)))
        .collect(Collectors.toList());
    
    return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
        .thenApply(v -> futures.stream()
            .map(CompletableFuture::join)
            .collect(Collectors.toList()));
}
```

## ❌ Configuration Mistakes

### Mistake 14: Not Using Retry Logic

```java
// ❌ BAD: No retry for transient failures
ApiClient client = new ApiClient.Builder().build();

// ✅ GOOD: Enable retry for resilience
ApiClient client = new ApiClient.Builder()
    .enableRetryPolicy()
    .build();
```

### Mistake 15: Not Using Circuit Breaker

```java
// ❌ BAD: No circuit breaker protection
ApiClient client = new ApiClient.Builder().build();

// ✅ GOOD: Enable circuit breaker for fault tolerance
ApiClient client = new ApiClient.Builder()
    .enableCircuitBreaker()
    .build();
```

## ❌ Testing Mistakes

### Mistake 16: Not Testing Error Scenarios

```java
// ❌ BAD: Only testing happy path
@Test
public void testGetUser() {
    User user = userService.getUser("123");
    assertThat(user).isNotNull();
}

// ✅ GOOD: Test error scenarios too
@Test
public void testGetUser_Success() {
    User user = userService.getUser("123");
    assertThat(user).isNotNull();
}

@Test
public void testGetUser_NotFound() {
    assertThatThrownBy(() -> userService.getUser("nonexistent"))
        .isInstanceOf(UserNotFoundException.class);
}

@Test
public void testGetUser_ServerError() {
    assertThatThrownBy(() -> userService.getUser("server-error"))
        .isInstanceOf(UserServiceException.class);
}
```

### Mistake 17: Not Mocking External Dependencies

```java
// ❌ BAD: Tests depend on external API
@Test
public void testGetUser() {
    // ❌ This will fail if external API is down
    User user = userService.getUser("123");
    assertThat(user).isNotNull();
}

// ✅ GOOD: Mock external dependencies
@Test
public void testGetUser() {
    // Mock the API response
    when(mockClient.get("/api/users/123"))
        .thenReturn(mockResponse);
    when(mockResponse.to(User.class))
        .thenReturn(new User("123", "John Doe"));
    
    User user = userService.getUser("123");
    assertThat(user.getName()).isEqualTo("John Doe");
}
```

## ❌ Logging Mistakes

### Mistake 18: Logging Sensitive Information

```java
// ❌ BAD: Logging sensitive data
public User authenticate(String username, String password) {
    logger.info("Authenticating user: {} with password: {}", username, password);  // ❌ Logs password!
    
    return client.post("/api/auth")
        .body(Map.of("username", username, "password", password))
        .execute()
        .to(User.class);
}

// ✅ GOOD: Don't log sensitive information
public User authenticate(String username, String password) {
    logger.info("Authenticating user: {}", username);  // ✅ Safe logging
    
    return client.post("/api/auth")
        .body(Map.of("username", username, "password", password))
        .execute()
        .to(User.class);
}
```

### Mistake 19: Too Verbose Logging in Production

```java
// ❌ BAD: Debug logging in production
ApiClient client = new ApiClient.Builder()
    .enableLogging()  // ❌ Might be too verbose
    .build();

// ✅ GOOD: Appropriate logging level
ApiClient client = new ApiClient.Builder()
    .enableLogging()  // ✅ Configure appropriate level in production
    .build();
```

## ❌ Resource Management Mistakes

### Mistake 20: Not Cleaning Up Resources

```java
// ❌ BAD: Not properly managing resources
public void uploadFile(File file) {
    client.post("/api/upload")
        .multipart()
        .addFile("file", file)
        .execute();  // ❌ Large file might not be cleaned up properly
}

// ✅ GOOD: Proper resource management
public void uploadFile(File file) {
    try (InputStream inputStream = new FileInputStream(file)) {
        client.post("/api/upload")
            .multipart()
            .addFile("file", inputStream, file.getName())
            .execute();
    } catch (IOException e) {
        throw new FileProcessingException("Failed to upload file", e);
    }
}
```

## How to Avoid These Mistakes

1. **Always handle exceptions** - Don't let them be swallowed or ignored
2. **Use proper async patterns** - Don't block async operations unnecessarily
3. **Reuse client instances** - Don't create new clients for each request
4. **Configure timeouts** - Set appropriate timeouts for your use case
5. **Enable production features** - Use connection pooling, retry, circuit breaker
6. **Validate input** - Always validate and sanitize user input
7. **Use HTTPS** - Never use HTTP in production
8. **Enable caching** - Use caching for frequently accessed data
9. **Test error scenarios** - Don't just test the happy path
10. **Log appropriately** - Don't log sensitive information or be too verbose

## Next Steps

- **[Production Checklist](/MochaJSON/best-practices/production-checklist)** - Ensure your deployment is production-ready
- **[Performance Tips](/MochaJSON/best-practices/performance-tips)** - Optimize your HTTP client performance
