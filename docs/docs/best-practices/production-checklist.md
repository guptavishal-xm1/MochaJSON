---
title: Production Checklist
description: Essential checklist for deploying MochaJSON in production environments. Learn about timeout configuration, error handling, connection pooling, logging, and security hardening.
keywords:
  - MochaJSON production
  - production deployment
  - HTTP client best practices
  - Java production checklist
  - Kotlin production
---

# Production Checklist

Deploying MochaJSON in production? Follow this checklist to ensure your HTTP client is configured for reliability, performance, and security.

## ✅ Configuration Checklist

### Timeout Configuration
```java
ApiClient client = new ApiClient.Builder()
    // ✅ Set appropriate timeouts
    .connectTimeout(Duration.ofSeconds(10))    // Connection establishment
    .readTimeout(Duration.ofSeconds(30))       // Response reading
    .writeTimeout(Duration.ofSeconds(15))      // Request writing
    .build();
```

**Why:** Prevents hanging requests and resource exhaustion.

### Connection Pooling
```java
ApiClient client = new ApiClient.Builder()
    // ✅ Enable connection pooling for better performance
    .enableConnectionPooling()
    .build();
```

**Why:** Reuses connections, reduces latency, and improves throughput.

### Security Configuration
```java
ApiClient client = new ApiClient.Builder()
    // ✅ Use production-safe security settings (blocks localhost/private IPs)
    .securityConfig(SecurityConfig.forProduction())
    .build();
```

**Why:** Prevents SSRF attacks and ensures production security.

### Retry Configuration
```java
ApiClient client = new ApiClient.Builder()
    // ✅ Enable retry for transient failures
    .enableRetryPolicy()
    .build();
```

**Why:** Handles temporary network issues and server errors automatically.

### Resource Management
```java
// ✅ Always use try-with-resources for streams
try (ManagedInputStream stream = client.get(url).downloadStream()) {
    byte[] data = stream.readAllBytes();
    // Process data
} // Stream is automatically closed

// ✅ For development, allow localhost access
Utils.setDefaultSecurityConfig(SecurityConfig.forDevelopment());

// ✅ For production, use strict security
Utils.setDefaultSecurityConfig(SecurityConfig.forProduction());
```

**Why:** Prevents memory leaks and ensures proper resource cleanup.

### Circuit Breaker
```java
ApiClient client = new ApiClient.Builder()
    // ✅ Enable circuit breaker for fault tolerance
    .enableCircuitBreaker()
    .build();
```

**Why:** Prevents cascading failures and protects downstream services.

## ✅ Error Handling Checklist

### Comprehensive Exception Handling
```java
public class ApiService {
    private final ApiClient client;
    
    public User getUser(String id) {
        try {
            return client.get("/api/users/" + id)
                .execute()
                .to(User.class);
                
        } catch (ApiException e) {
            // ✅ Handle HTTP errors appropriately
            switch (e.getStatusCode()) {
                case 404:
                    throw new UserNotFoundException("User not found: " + id);
                case 429:
                    throw new RateLimitExceededException("Rate limit exceeded");
                case 500:
                case 502:
                case 503:
                    throw new ServiceUnavailableException("Service temporarily unavailable");
                default:
                    throw new ApiErrorException("API error: " + e.getStatusCode());
            }
            
        } catch (JsonException e) {
            // ✅ Handle JSON parsing errors
            logger.error("Failed to parse JSON response", e);
            throw new DataProcessingException("Invalid response format");
            
        } catch (Exception e) {
            // ✅ Handle unexpected errors
            logger.error("Unexpected error in getUser", e);
            throw new ServiceException("Internal error");
        }
    }
}
```

### Graceful Degradation
```java
public class ResilientApiService {
    private final ApiClient primaryClient;
    private final ApiClient fallbackClient;
    
    public User getUser(String id) {
        try {
            // ✅ Try primary service first
            return primaryClient.get("/api/users/" + id)
                .execute()
                .to(User.class);
                
        } catch (ApiException e) {
            if (e.getStatusCode() >= 500) {
                // ✅ Fallback to secondary service
                logger.warn("Primary service failed, using fallback", e);
                return fallbackClient.get("/api/users/" + id)
                    .execute()
                    .to(User.class);
            }
            throw e;
        }
    }
}
```

## ✅ Logging Configuration

### Request/Response Logging
```java
ApiClient client = new ApiClient.Builder()
    // ✅ Enable structured logging
    .enableLogging()
    .build();
```

### Custom Logging Interceptors
```java
// ✅ Custom logging for production monitoring
RequestInterceptor requestLogger = request -> {
    logger.info("API Request: {} {}", request.getMethod(), request.getUrl());
    return request.header("X-Request-ID", UUID.randomUUID().toString());
};

ResponseInterceptor responseLogger = response -> {
    logger.info("API Response: {} - {}ms", 
        response.code(), 
        response.getDuration().toMillis());
    return response;
};

ApiClient client = new ApiClient.Builder()
    .addRequestInterceptor(requestLogger)
    .addResponseInterceptor(responseLogger)
    .build();
```

### Log Levels
```yaml
# logback.xml or application.yml
logging:
  level:
    io.mochaapi.client: INFO      # ✅ Production level
    # io.mochaapi.client: DEBUG   # ❌ Only for debugging
```

## ✅ Security Checklist

### Authentication
```java
ApiClient client = new ApiClient.Builder()
    // ✅ Use secure authentication
    .addRequestInterceptor(RequestInterceptor.bearerAuth(() -> getSecureToken()))
    .build();

// ✅ Token refresh mechanism
private String getSecureToken() {
    if (token == null || token.isExpired()) {
        token = refreshToken();
    }
    return token.getValue();
}
```

### URL Validation
```java
// ✅ MochaJSON automatically validates URLs
// These will throw IllegalArgumentException:
// client.get("javascript:alert('xss')").execute();  // ❌ Rejected
// client.get("file:///etc/passwd").execute();      // ❌ Rejected

// ✅ Only these are allowed:
client.get("https://api.example.com/data").execute();  // ✅ OK
client.get("http://localhost:8080/api").execute();     // ✅ OK (local)
```

### Input Sanitization
```java
public User getUser(String id) {
    // ✅ Validate and sanitize input
    if (id == null || id.trim().isEmpty()) {
        throw new IllegalArgumentException("User ID cannot be null or empty");
    }
    
    // ✅ Sanitize the ID
    String sanitizedId = id.trim().replaceAll("[^a-zA-Z0-9-_]", "");
    
    return client.get("/api/users/" + sanitizedId)
        .execute()
        .to(User.class);
}
```

## ✅ Performance Checklist

### Caching Strategy
```java
ApiClient client = new ApiClient.Builder()
    // ✅ Enable HTTP caching for better performance
    .enableCaching()
    .build();
```

### Async Operations
```java
// ✅ Use async for non-blocking operations
public CompletableFuture<List<User>> getUsersAsync() {
    return client.get("/api/users")
        .executeAsync()
        .thenApply(response -> response.toList());
}

// ✅ Proper exception handling in async
public CompletableFuture<User> getUserAsync(String id) {
    return client.get("/api/users/" + id)
        .executeAsync()
        .thenApply(response -> response.to(User.class))
        .exceptionally(throwable -> {
            logger.error("Failed to get user: " + id, throwable);
            return null; // or handle gracefully
        });
}
```

### Resource Management
```java
public class UserService {
    // ✅ Singleton pattern for client reuse
    private static final ApiClient client = new ApiClient.Builder()
        .enableConnectionPooling()
        .enableCaching()
        .build();
    
    // ✅ Don't create new clients for each request
    public User getUser(String id) {
        return client.get("/api/users/" + id)
            .execute()
            .to(User.class);
    }
}
```

## ✅ Monitoring Checklist

### Health Checks
```java
@RestController
public class HealthController {
    
    private final ApiClient client;
    
    @GetMapping("/health/api")
    public ResponseEntity<Map<String, Object>> checkApiHealth() {
        try {
            // ✅ Check external API health
            ApiResponse response = client.get("/api/health")
                .timeout(Duration.ofSeconds(5))
                .execute();
            
            Map<String, Object> health = Map.of(
                "status", response.isSuccess() ? "UP" : "DOWN",
                "responseTime", response.getDuration().toMillis(),
                "statusCode", response.code()
            );
            
            return ResponseEntity.ok(health);
            
        } catch (Exception e) {
            Map<String, Object> health = Map.of(
                "status", "DOWN",
                "error", e.getMessage()
            );
            return ResponseEntity.status(503).body(health);
        }
    }
}
```

### Metrics Collection
```java
// ✅ Collect metrics for monitoring
public class MetricsInterceptor implements ResponseInterceptor {
    
    private final MeterRegistry meterRegistry;
    
    @Override
    public ApiResponse intercept(ApiResponse response) {
        // ✅ Record response metrics
        Timer.Sample sample = Timer.start(meterRegistry);
        sample.stop(Timer.builder("api.requests")
            .tag("status", String.valueOf(response.code()))
            .register(meterRegistry));
            
        return response;
    }
}
```

## ✅ Testing Checklist

### Unit Tests
```java
@Test
public void testGetUser_Success() {
    // ✅ Test successful scenarios
    User user = userService.getUser("123");
    assertThat(user).isNotNull();
    assertThat(user.getId()).isEqualTo("123");
}

@Test
public void testGetUser_NotFound() {
    // ✅ Test error scenarios
    assertThatThrownBy(() -> userService.getUser("nonexistent"))
        .isInstanceOf(UserNotFoundException.class)
        .hasMessageContaining("User not found");
}
```

### Integration Tests
```java
@Test
public void testApiIntegration() {
    // ✅ Test with real API endpoints
    ApiResponse response = client.get("https://httpbin.org/get")
        .execute();
        
    assertThat(response.isSuccess()).isTrue();
    assertThat(response.code()).isEqualTo(200);
}
```

## ✅ Deployment Checklist

### Environment Configuration
```java
@Configuration
public class ApiClientConfig {
    
    @Bean
    public ApiClient apiClient(@Value("${api.base-url}") String baseUrl,
                              @Value("${api.timeout:30}") int timeoutSeconds) {
        return new ApiClient.Builder()
            .baseUrl(baseUrl)
            .connectTimeout(Duration.ofSeconds(timeoutSeconds))
            .readTimeout(Duration.ofSeconds(timeoutSeconds))
            .enableConnectionPooling()
            .enableRetryPolicy()
            .enableCircuitBreaker()
            .enableLogging()
            .build();
    }
}
```

### Configuration Properties
```yaml
# application.yml
api:
  base-url: https://api.production.com
  timeout: 30
  
logging:
  level:
    io.mochaapi.client: INFO
    
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,info
```

## ✅ Security Hardening

### SSL/TLS Configuration
```java
// ✅ Use HTTPS in production
ApiClient client = new ApiClient.Builder()
    .baseUrl("https://api.example.com")  // ✅ HTTPS only
    .build();

// ❌ Never use HTTP in production
// .baseUrl("http://api.example.com")
```

### Secrets Management
```java
// ✅ Use environment variables or secret management
@Value("${api.token}")
private String apiToken;

ApiClient client = new ApiClient.Builder()
    .addRequestInterceptor(RequestInterceptor.bearerAuth(() -> apiToken))
    .build();

// ❌ Never hardcode secrets
// .addRequestInterceptor(RequestInterceptor.bearerAuth(() -> "hardcoded-token"))
```

## ✅ Performance Tuning

### Connection Pool Tuning
```java
ApiClient client = new ApiClient.Builder()
    // ✅ Tune connection pool for your workload
    .connectionPool(ConnectionPoolConfig.builder()
        .maxIdle(20)                    // Maximum idle connections
        .maxTotal(50)                   // Maximum total connections
        .keepAlive(Duration.ofMinutes(5)) // Connection keep-alive
        .build())
    .build();
```

### Cache Configuration
```java
ApiClient client = new ApiClient.Builder()
    // ✅ Configure cache for your use case
    .cache(CacheConfig.builder()
        .maxSize(1000)                  // Maximum cache entries
        .ttl(Duration.ofMinutes(10))    // Cache TTL
        .diskStorage(true)              // Enable disk storage
        .build())
    .build();
```

## ✅ Final Verification

Before deploying to production:

- [ ] **Timeouts configured** - Appropriate for your network conditions
- [ ] **Error handling** - Comprehensive exception handling implemented
- [ ] **Logging enabled** - Structured logging with appropriate levels
- [ ] **Security hardened** - HTTPS, input validation, secure authentication
- [ ] **Performance optimized** - Connection pooling, caching, async operations
- [ ] **Monitoring setup** - Health checks and metrics collection
- [ ] **Tests passing** - Unit and integration tests cover critical paths
- [ ] **Documentation updated** - API documentation and runbooks current

## Next Steps

- **[Common Mistakes](/MochaJSON/best-practices/common-mistakes)** - What to avoid in production
- **[Performance Tips](/MochaJSON/best-practices/performance-tips)** - Optimize your HTTP client performance
