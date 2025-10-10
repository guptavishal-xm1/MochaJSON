---
title: Interceptors
description: Learn how to use MochaJSON interceptors for authentication, logging, request/response transformation, and cross-cutting concerns. Complete guide with examples.
keywords:
  - MochaJSON interceptors
  - request interceptors
  - response interceptors
  - authentication interceptors
  - logging interceptors
---

# Interceptors

Interceptors are a powerful feature in MochaJSON that allow you to modify requests and responses, add logging, implement authentication, and handle cross-cutting concerns. They provide a clean way to implement functionality that applies to multiple API calls.

## What are Interceptors?

Interceptors are functions that are called before sending requests (request interceptors) or after receiving responses (response interceptors). They allow you to:

- **Modify requests** before they're sent
- **Transform responses** after they're received
- **Add authentication** headers automatically
- **Implement logging** for debugging and monitoring
- **Handle errors** consistently across your application
- **Add retry logic** for failed requests

## Request Interceptors

Request interceptors are called before each request is sent, allowing you to modify the request.

### Basic Request Interceptor

```java
// ✅ Create a request interceptor
RequestInterceptor interceptor = request -> {
    System.out.println("Sending request: " + request.getMethod() + " " + request.getUrl());
    return request; // Return the modified request
};

ApiClient client = new ApiClient.Builder()
    .addRequestInterceptor(interceptor)
    .build();
```

### Authentication Interceptor

```java
// ✅ Add authentication to all requests
RequestInterceptor authInterceptor = request -> {
    String token = getAuthToken(); // Get token from secure storage
    return request.header("Authorization", "Bearer " + token);
};

ApiClient client = new ApiClient.Builder()
    .addRequestInterceptor(authInterceptor)
    .build();

// All requests will now include the Authorization header
User user = client.get("/api/users/123").execute().to(User.class);
```

### Built-in Authentication Interceptors

```java
// ✅ Use built-in authentication interceptors
ApiClient client = new ApiClient.Builder()
    .addRequestInterceptor(RequestInterceptor.bearerAuth(() -> getToken()))
    .addRequestInterceptor(RequestInterceptor.basicAuth("username", "password"))
    .addRequestInterceptor(RequestInterceptor.apiKey("X-API-Key", () -> getApiKey()))
    .build();
```

### Request ID Interceptor

```java
// ✅ Add unique request IDs for tracing
RequestInterceptor requestIdInterceptor = request -> {
    String requestId = UUID.randomUUID().toString();
    return request.header("X-Request-ID", requestId);
};

ApiClient client = new ApiClient.Builder()
    .addRequestInterceptor(requestIdInterceptor)
    .build();
```

### Logging Interceptor

```java
// ✅ Log all outgoing requests
RequestInterceptor loggingInterceptor = request -> {
    logger.info("Outgoing request: {} {} with headers: {}", 
        request.getMethod(), 
        request.getUrl(), 
        request.getHeaders());
    return request;
};

ApiClient client = new ApiClient.Builder()
    .addRequestInterceptor(loggingInterceptor)
    .build();
```

### Multiple Request Interceptors

```java
// ✅ Chain multiple request interceptors
ApiClient client = new ApiClient.Builder()
    .addRequestInterceptor(RequestInterceptor.bearerAuth(() -> getToken()))
    .addRequestInterceptor(request -> request.header("X-Client-Version", "1.2.0"))
    .addRequestInterceptor(request -> {
        logger.debug("Request: {} {}", request.getMethod(), request.getUrl());
        return request;
    })
    .build();
```

## Response Interceptors

Response interceptors are called after each response is received, allowing you to modify the response or perform actions based on the response.

### Basic Response Interceptor

```java
// ✅ Create a response interceptor
ResponseInterceptor interceptor = response -> {
    System.out.println("Received response: " + response.code());
    return response; // Return the modified response
};

ApiClient client = new ApiClient.Builder()
    .addResponseInterceptor(interceptor)
    .build();
```

### Error Handling Interceptor

```java
// ✅ Handle errors consistently
ResponseInterceptor errorInterceptor = response -> {
    if (response.isError()) {
        logger.error("API error: {} - {}", response.code(), response.body());
        
        // Transform error responses into exceptions
        if (response.code() == 401) {
            throw new UnauthorizedException("Authentication required");
        } else if (response.code() == 403) {
            throw new ForbiddenException("Access denied");
        } else if (response.code() >= 500) {
            throw new ServerException("Server error: " + response.code());
        }
    }
    return response;
};

ApiClient client = new ApiClient.Builder()
    .addResponseInterceptor(errorInterceptor)
    .build();
```

### Built-in Error Interceptor

```java
// ✅ Use built-in error handling
ApiClient client = new ApiClient.Builder()
    .addResponseInterceptor(ResponseInterceptor.throwOnError())
    .build();
```

### Response Logging Interceptor

```java
// ✅ Log all incoming responses
ResponseInterceptor loggingInterceptor = response -> {
    logger.info("Incoming response: {} - {}ms", 
        response.code(), 
        response.getDuration().toMillis());
    return response;
};

ApiClient client = new ApiClient.Builder()
    .addResponseInterceptor(loggingInterceptor)
    .build();
```

### Metrics Collection Interceptor

```java
// ✅ Collect metrics for monitoring
public class MetricsInterceptor implements ResponseInterceptor {
    
    private final MeterRegistry meterRegistry;
    
    public MetricsInterceptor(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }
    
    @Override
    public ApiResponse intercept(ApiResponse response) {
        Timer.Sample sample = Timer.start(meterRegistry);
        sample.stop(Timer.builder("api.requests")
            .tag("status", String.valueOf(response.code()))
            .tag("endpoint", extractEndpoint(response.getUrl()))
            .register(meterRegistry));
            
        return response;
    }
}

// Usage
ApiClient client = new ApiClient.Builder()
    .addResponseInterceptor(new MetricsInterceptor(meterRegistry))
    .build();
```

### Response Transformation Interceptor

```java
// ✅ Transform response data
ResponseInterceptor transformInterceptor = response -> {
    if (response.isSuccess()) {
        // Add metadata to successful responses
        String body = response.body();
        if (body != null) {
            // Parse and add timestamp
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode json = mapper.readTree(body);
                ((ObjectNode) json).put("timestamp", System.currentTimeMillis());
                return response.withBody(mapper.writeValueAsString(json));
            } catch (Exception e) {
                logger.warn("Failed to transform response", e);
            }
        }
    }
    return response;
};

ApiClient client = new ApiClient.Builder()
    .addResponseInterceptor(transformInterceptor)
    .build();
```

## Advanced Interceptor Patterns

### Conditional Interceptors

```java
// ✅ Apply interceptors conditionally
RequestInterceptor conditionalInterceptor = request -> {
    if (request.getUrl().contains("/api/v2/")) {
        return request.header("X-API-Version", "2.0");
    }
    return request;
};

ApiClient client = new ApiClient.Builder()
    .addRequestInterceptor(conditionalInterceptor)
    .build();
```

### Token Refresh Interceptor

```java
// ✅ Automatically refresh expired tokens
public class TokenRefreshInterceptor implements ResponseInterceptor {
    
    private final Supplier<String> tokenSupplier;
    private final Supplier<String> refreshTokenSupplier;
    
    @Override
    public ApiResponse intercept(ApiResponse response) {
        if (response.code() == 401) {
            // Token expired, try to refresh
            try {
                String newToken = refreshTokenSupplier.get();
                if (newToken != null) {
                    // Retry the original request with new token
                    // This would require access to the original request
                    logger.info("Token refreshed successfully");
                }
            } catch (Exception e) {
                logger.error("Failed to refresh token", e);
            }
        }
        return response;
    }
}
```

### Circuit Breaker Interceptor

```java
// ✅ Implement circuit breaker pattern
public class CircuitBreakerInterceptor implements ResponseInterceptor {
    
    private final CircuitBreaker circuitBreaker;
    
    public CircuitBreakerInterceptor(CircuitBreaker circuitBreaker) {
        this.circuitBreaker = circuitBreaker;
    }
    
    @Override
    public ApiResponse intercept(ApiResponse response) {
        if (response.isError()) {
            circuitBreaker.recordFailure();
        } else {
            circuitBreaker.recordSuccess();
        }
        
        if (circuitBreaker.getState() == CircuitBreaker.State.OPEN) {
            throw new CircuitBreakerOpenException("Circuit breaker is open");
        }
        
        return response;
    }
}
```

### Retry Interceptor

```java
// ✅ Custom retry logic
public class RetryInterceptor implements ResponseInterceptor {
    
    private final int maxRetries;
    private final Duration retryDelay;
    
    @Override
    public ApiResponse intercept(ApiResponse response) {
        if (shouldRetry(response) && retryCount < maxRetries) {
            try {
                Thread.sleep(retryDelay.toMillis());
                // This would require access to the original request
                // In practice, you'd use the built-in retry mechanism
                return retryRequest();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Retry interrupted", e);
            }
        }
        return response;
    }
    
    private boolean shouldRetry(ApiResponse response) {
        return response.code() >= 500 || response.code() == 429;
    }
}
```

## Real-World Examples

### Complete API Client with Interceptors

```java
public class UserApiClient {
    
    private final ApiClient client;
    private final Logger logger = LoggerFactory.getLogger(UserApiClient.class);
    
    public UserApiClient(String baseUrl, Supplier<String> tokenSupplier) {
        this.client = new ApiClient.Builder()
            .baseUrl(baseUrl)
            .addRequestInterceptor(RequestInterceptor.bearerAuth(tokenSupplier))
            .addRequestInterceptor(request -> {
                logger.debug("Request: {} {}", request.getMethod(), request.getUrl());
                return request.header("X-Client-Version", "1.2.0");
            })
            .addResponseInterceptor(response -> {
                logger.debug("Response: {} - {}ms", response.code(), response.getDuration().toMillis());
                return response;
            })
            .addResponseInterceptor(ResponseInterceptor.throwOnError())
            .enableLogging()
            .build();
    }
    
    public User getUser(String id) {
        return client.get("/api/users/" + id)
            .execute()
            .to(User.class);
    }
    
    public List<User> getUsers() {
        return client.get("/api/users")
            .execute()
            .toList();
    }
    
    public User createUser(User user) {
        return client.post("/api/users")
            .body(user)
            .execute()
            .to(User.class);
    }
}
```

### Spring Integration

```java
@Component
public class ApiClientConfig {
    
    @Value("${api.base-url}")
    private String baseUrl;
    
    @Value("${api.token}")
    private String apiToken;
    
    @Bean
    public ApiClient apiClient() {
        return new ApiClient.Builder()
            .baseUrl(baseUrl)
            .addRequestInterceptor(RequestInterceptor.bearerAuth(() -> apiToken))
            .addRequestInterceptor(request -> request.header("X-Request-ID", UUID.randomUUID().toString()))
            .addResponseInterceptor(new MetricsInterceptor(meterRegistry))
            .addResponseInterceptor(ResponseInterceptor.throwOnError())
            .enableConnectionPooling()
            .enableRetryPolicy()
            .enableCircuitBreaker()
            .build();
    }
}
```

## Best Practices

### 1. Order Matters
```java
// ✅ Interceptors are executed in the order they're added
ApiClient client = new ApiClient.Builder()
    .addRequestInterceptor(authInterceptor)      // 1. Add auth
    .addRequestInterceptor(loggingInterceptor)   // 2. Log request
    .addResponseInterceptor(loggingInterceptor)  // 3. Log response
    .addResponseInterceptor(errorInterceptor)    // 4. Handle errors
    .build();
```

### 2. Keep Interceptors Simple
```java
// ✅ GOOD: Simple, focused interceptor
RequestInterceptor authInterceptor = request -> 
    request.header("Authorization", "Bearer " + getToken());

// ❌ BAD: Complex interceptor doing too much
RequestInterceptor complexInterceptor = request -> {
    // Authentication
    String token = getToken();
    request = request.header("Authorization", "Bearer " + token);
    
    // Logging
    logger.info("Request: {}", request.getUrl());
    
    // Metrics
    recordMetric(request);
    
    // Transformation
    // ... more complex logic
    
    return request;
};
```

### 3. Handle Exceptions in Interceptors
```java
// ✅ Handle exceptions gracefully in interceptors
RequestInterceptor safeInterceptor = request -> {
    try {
        String token = getToken();
        return request.header("Authorization", "Bearer " + token);
    } catch (Exception e) {
        logger.error("Failed to add auth header", e);
        return request; // Continue without auth header
    }
};
```

### 4. Use Built-in Interceptors When Possible
```java
// ✅ Use built-in interceptors for common patterns
ApiClient client = new ApiClient.Builder()
    .addRequestInterceptor(RequestInterceptor.bearerAuth(() -> getToken()))
    .addResponseInterceptor(ResponseInterceptor.throwOnError())
    .build();

// ❌ Don't reinvent the wheel
RequestInterceptor customAuthInterceptor = request -> {
    // Custom auth logic that duplicates built-in functionality
};
```

## Testing Interceptors

### Unit Testing Interceptors
```java
@Test
public void testAuthInterceptor() {
    // Arrange
    RequestInterceptor interceptor = RequestInterceptor.bearerAuth(() -> "test-token");
    ApiRequest request = Api.get("/test");
    
    // Act
    ApiRequest modifiedRequest = interceptor.intercept(request);
    
    // Assert
    assertThat(modifiedRequest.getHeaders())
        .containsEntry("Authorization", "Bearer test-token");
}

@Test
public void testErrorInterceptor() {
    // Arrange
    ResponseInterceptor interceptor = ResponseInterceptor.throwOnError();
    ApiResponse errorResponse = mockApiResponse(404);
    
    // Act & Assert
    assertThatThrownBy(() -> interceptor.intercept(errorResponse))
        .isInstanceOf(ApiException.class);
}
```

### Integration Testing
```java
@Test
public void testClientWithInterceptors() {
    // Arrange
    ApiClient client = new ApiClient.Builder()
        .addRequestInterceptor(request -> request.header("X-Test", "true"))
        .addResponseInterceptor(response -> {
            assertThat(response.getHeaders()).containsKey("X-Test");
            return response;
        })
        .build();
    
    // Act
    ApiResponse response = client.get("https://httpbin.org/get").execute();
    
    // Assert
    assertThat(response.isSuccess()).isTrue();
}
```

## Next Steps

- **[Advanced Features](/MochaJSON/advanced/interceptors)** - Learn about other advanced features
- **[Best Practices](/MochaJSON/best-practices/production-checklist)** - Production deployment guide
