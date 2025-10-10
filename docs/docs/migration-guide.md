---
title: Migration Guide
description: Complete migration guide from MochaJSON v1.0.x to v1.1.0 with examples and best practices.
---

# Migration Guide

This guide helps you migrate from MochaJSON v1.0.x to v1.1.0. The good news is that **MochaJSON v1.1.0 is 100% backward compatible** with v1.0.x, so your existing code will continue to work without any changes!

## What's New in v1.1.0

### 🆕 New Features

- **ApiClient with Builder Pattern**: Advanced configuration with timeouts, interceptors, and logging
- **Virtual Threads Support**: Automatic use of Java 21 virtual threads with fallback support
- **Request/Response Interceptors**: Powerful interceptor system for authentication, logging, and more
- **Enhanced Async APIs**: Modern CompletableFuture support with improved async operations
- **Security Enhancements**: URL validation, hardened JSON parsing, and input sanitization
- **Configurable Timeouts**: Fine-grained timeout control for connection, read, and write operations
- **Optional Logging**: SLF4J integration for comprehensive request/response logging

### 🔒 Security Improvements

- URL validation prevents open redirect attacks
- Hardened JSON parsing with disabled polymorphic typing
- Enhanced input validation and sanitization
- Configurable timeouts for DoS protection

### ⚡ Performance Enhancements

- Virtual threads for better concurrent performance (Java 21+)
- Improved async operation handling
- Better resource management and cleanup

## Backward Compatibility

**✅ All existing v1.0.x code works exactly the same in v1.1.0!**

### Your Existing Code Still Works

```java
// This code from v1.0.x works exactly the same in v1.1.0
ApiResponse response = Api.get("https://api.example.com/data")
    .query("page", 1)
    .execute();

Map<String, Object> data = response.toMap();
User user = response.to(User.class);

// Async with callbacks still works
Api.get("https://api.example.com/data")
    .async(response -> {
        System.out.println("Response: " + response.code());
    });
```

## Optional Migration Paths

While not required, you can optionally migrate to the new `ApiClient` for advanced features.

### 1. Basic Migration to ApiClient

**Before (v1.0.x):**
```java
ApiResponse response = Api.get("https://api.example.com/data")
    .query("page", 1)
    .execute();
```

**After (v1.1.0) - Optional:**
```java
ApiClient client = new ApiClient.Builder().build();

ApiResponse response = client.get("https://api.example.com/data")
    .query("page", 1)
    .execute();
```

### 2. Advanced Configuration Migration

**Before (v1.0.x):**
```java
// Limited configuration options
ApiResponse response = Api.get("https://api.example.com/data")
    .header("Authorization", "Bearer " + getToken())
    .execute();
```

**After (v1.1.0) - Enhanced:**
```java
ApiClient client = new ApiClient.Builder()
    .connectTimeout(Duration.ofSeconds(10))
    .readTimeout(Duration.ofSeconds(30))
    .enableLogging()
    .addRequestInterceptor(RequestInterceptor.bearerAuth(() -> getToken()))
    .addResponseInterceptor(ResponseInterceptor.throwOnError())
    .build();

ApiResponse response = client.get("https://api.example.com/data").execute();
```

### 3. Async Operations Migration

**Before (v1.0.x):**
```java
Api.get("https://api.example.com/data")
    .async(response -> {
        System.out.println("Response: " + response.code());
    });
```

**After (v1.1.0) - Enhanced:**
```java
// New CompletableFuture API
CompletableFuture<ApiResponse> future = client.get("https://api.example.com/data")
    .executeAsync();

future.thenAccept(response -> {
    System.out.println("Response: " + response.code());
});

// Or chain operations
client.get("https://api.example.com/data")
    .executeAsync()
    .thenApply(r -> r.toMap())
    .thenAccept(data -> System.out.println("Data: " + data));
```

## Step-by-Step Migration

### Step 1: Update Dependencies

Update your build files to use v1.1.0:

**Gradle:**
```gradle
dependencies {
    implementation("io.github.guptavishal-xm1:MochaJSON:1.1.0")
    
    // Optional: For logging support
    implementation("org.slf4j:slf4j-api:2.0.9")
}
```

**Maven:**
```xml
<dependency>
    <groupId>io.github.guptavishal-xm1</groupId>
    <artifactId>MochaJSON</artifactId>
    <version>1.1.0</version>
</dependency>

<!-- Optional: For logging support -->
<dependency>
    <groupId>org.slf4j</groupId>
    <artifactId>slf4j-api</artifactId>
    <version>2.0.9</version>
</dependency>
```

### Step 2: Test Existing Code

Your existing code should work without changes. Run your tests to verify:

```java
// All existing code continues to work
ApiResponse response = Api.get("https://api.example.com/data").execute();
Map<String, Object> data = response.toMap();
```

### Step 3: Optional - Migrate to ApiClient

If you want to use the new advanced features, gradually migrate to `ApiClient`:

```java
// Create a configured client for your application
ApiClient client = new ApiClient.Builder()
    .connectTimeout(Duration.ofSeconds(10))
    .readTimeout(Duration.ofSeconds(30))
    .build();

// Replace static Api calls with client calls
// OLD: Api.get("https://api.example.com/data").execute()
// NEW: client.get("https://api.example.com/data").execute()
```

### Step 4: Add Interceptors (Optional)

Add interceptors for cross-cutting concerns:

```java
ApiClient client = new ApiClient.Builder()
    .addRequestInterceptor(RequestInterceptor.bearerAuth(() -> getToken()))
    .addResponseInterceptor(ResponseInterceptor.throwOnError())
    .build();

// Now all requests automatically include authentication
ApiResponse response = client.get("https://api.example.com/data").execute();
```

### Step 5: Enable Logging (Optional)

Add logging for debugging and monitoring:

```java
ApiClient client = new ApiClient.Builder()
    .enableLogging()  // Console logging
    .build();

// Or custom logging
ApiClient client = new ApiClient.Builder()
    .addRequestInterceptor(RequestInterceptor.logging(logger::info))
    .addResponseInterceptor(ResponseInterceptor.logging(logger::info))
    .build();
```

## Migration Examples

### Example 1: Simple API Client

**Before:**
```java
public class UserService {
    public User getUser(int id) {
        ApiResponse response = Api.get("https://api.example.com/users/" + id).execute();
        return response.to(User.class);
    }
}
```

**After (Optional Enhancement):**
```java
public class UserService {
    private final ApiClient client;
    
    public UserService() {
        this.client = new ApiClient.Builder()
            .connectTimeout(Duration.ofSeconds(5))
            .readTimeout(Duration.ofSeconds(10))
            .addRequestInterceptor(RequestInterceptor.bearerAuth(() -> getToken()))
            .build();
    }
    
    public User getUser(int id) {
        ApiResponse response = client.get("https://api.example.com/users/" + id).execute();
        return response.to(User.class);
    }
}
```

### Example 2: Async Operations

**Before:**
```java
public void fetchUsersAsync(List<Integer> userIds) {
    for (int id : userIds) {
        Api.get("https://api.example.com/users/" + id)
            .async(response -> {
                User user = response.to(User.class);
                processUser(user);
            });
    }
}
```

**After (Enhanced):**
```java
public CompletableFuture<List<User>> fetchUsersAsync(List<Integer> userIds) {
    ApiClient client = new ApiClient.Builder().build();
    
    List<CompletableFuture<User>> futures = userIds.stream()
        .map(id -> client.get("https://api.example.com/users/" + id)
            .executeAsync()
            .thenApply(response -> response.to(User.class)))
        .collect(Collectors.toList());
    
    return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
        .thenApply(v -> futures.stream()
            .map(CompletableFuture::join)
            .collect(Collectors.toList()));
}
```

### Example 3: Error Handling

**Before:**
```java
try {
    ApiResponse response = Api.get("https://api.example.com/data").execute();
    if (response.isError()) {
        throw new RuntimeException("API Error: " + response.code());
    }
    return response.toMap();
} catch (ApiException e) {
    logger.error("Request failed", e);
    throw e;
}
```

**After (Enhanced):**
```java
ApiClient client = new ApiClient.Builder()
    .addResponseInterceptor(ResponseInterceptor.throwOnError())  // Automatic error handling
    .build();

try {
    ApiResponse response = client.get("https://api.example.com/data").execute();
    return response.toMap();
} catch (ApiException e) {
    logger.error("Request failed", e);
    throw e;
}
```

## Best Practices for Migration

### 1. Gradual Migration

Don't try to migrate everything at once. Start with new code using `ApiClient`, then gradually migrate existing code:

```java
// Keep existing code working
ApiResponse response = Api.get("https://api.example.com/data").execute();

// Use ApiClient for new features
ApiClient client = new ApiClient.Builder()
    .addRequestInterceptor(RequestInterceptor.bearerAuth(() -> getToken()))
    .build();
```

### 2. Centralized Configuration

Create a centralized client configuration:

```java
public class ApiClientFactory {
    private static final ApiClient INSTANCE = new ApiClient.Builder()
        .connectTimeout(Duration.ofSeconds(10))
        .readTimeout(Duration.ofSeconds(30))
        .addRequestInterceptor(RequestInterceptor.bearerAuth(() -> getToken()))
        .addResponseInterceptor(ResponseInterceptor.throwOnError())
        .build();
    
    public static ApiClient getClient() {
        return INSTANCE;
    }
}

// Use throughout your application
ApiClient client = ApiClientFactory.getClient();
```

### 3. Environment-Specific Configuration

Configure clients based on environment:

```java
public class ApiClientFactory {
    public static ApiClient createClient(Environment env) {
        ApiClient.Builder builder = new ApiClient.Builder();
        
        if (env == Environment.PRODUCTION) {
            builder.connectTimeout(Duration.ofSeconds(5))
                   .readTimeout(Duration.ofSeconds(10));
        } else {
            builder.enableLogging()  // Only in dev/test
                   .connectTimeout(Duration.ofSeconds(30))
                   .readTimeout(Duration.ofSeconds(60));
        }
        
        return builder.build();
    }
}
```

### 4. Testing

Update your tests to work with both old and new approaches:

```java
@Test
public void testUserFetch() {
    // Both approaches should work
    ApiResponse response1 = Api.get("https://api.example.com/users/1").execute();
    ApiResponse response2 = client.get("https://api.example.com/users/1").execute();
    
    assertEquals(response1.toMap(), response2.toMap());
}
```

## Troubleshooting

### Common Issues

1. **Import Changes**: No import changes needed - all existing imports continue to work.

2. **Method Signatures**: All existing method signatures are preserved.

3. **Exception Handling**: Exception handling remains the same.

4. **Async Behavior**: Existing async code continues to work, but you can now use the enhanced CompletableFuture API.

### Performance Considerations

- Virtual threads are automatically used on Java 21+ for better performance
- On Java 17 or earlier, MochaJSON falls back to cached thread pool
- No code changes required for performance improvements

### Security Notes

- URL validation is now enabled by default - malicious URLs will be rejected
- JSON parsing is hardened against common attacks
- These security improvements are automatic and don't require code changes

## Need Help?

If you encounter any issues during migration:

1. **Check the [API Reference](/MochaJSON/api/api-reference)** for detailed method documentation
2. **Review the [Advanced Features Guide](/MochaJSON/advanced/interceptors)** for new capabilities
3. **Report issues on [GitHub](https://github.com/guptavishal-xm1/MochaJSON/issues)**
4. **Join discussions on [GitHub Discussions](https://github.com/guptavishal-xm1/MochaJSON/discussions)**

## Summary

- ✅ **100% Backward Compatible** - No code changes required
- ✅ **Enhanced Features Available** - Use `ApiClient` for advanced capabilities
- ✅ **Better Performance** - Virtual threads support (Java 21+)
- ✅ **Improved Security** - URL validation and hardened JSON parsing
- ✅ **Modern Async APIs** - CompletableFuture support
- ✅ **Production Ready** - Interceptors, logging, and configurable timeouts

MochaJSON v1.1.0 provides a smooth upgrade path while introducing powerful new features for modern Java applications.
