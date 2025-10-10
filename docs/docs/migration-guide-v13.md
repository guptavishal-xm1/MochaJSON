---
title: Migration Guide - v1.2.0 to v1.3.0
description: Migrate from MochaJSON v1.2.0 to v1.3.0. Learn about removed features and how to use the simplified API with essential features only.
keywords:
  - MochaJSON migration
  - v1.2.0 to v1.3.0
  - breaking changes
  - simplified API
  - removed features
---

# Migration Guide: v1.2.0 → v1.3.0

MochaJSON v1.3.0 introduces a simplified, focused API by removing complex enterprise features that were rarely used. This guide will help you migrate from v1.2.0 to the cleaner, more maintainable v1.3.0.

## 🎯 Why v1.3.0?

MochaJSON v1.3.0 returns to its core mission: **The simplest way to make HTTP requests and parse JSON in Java & Kotlin**. We removed enterprise features that added complexity without providing value to most users.

### Benefits of v1.3.0:
- **47% less code** - Smaller library size
- **Simpler API** - Easier to learn and use
- **Better performance** - Fewer features to initialize
- **Focused design** - Essential features only

## 🔄 Breaking Changes

### 1. Circuit Breaker - REMOVED

**v1.2.0 (Removed):**
```java
ApiClient client = new ApiClient.Builder()
    .enableCircuitBreaker()  // ❌ No longer available
    .build();
```

**v1.3.0 (Alternative):**
```java
ApiClient client = new ApiClient.Builder()
    .enableRetry()  // ✅ Use simple retry instead
    .build();

// Handle circuit breaker logic at application level if needed
public class ServiceClient {
    private volatile boolean serviceAvailable = true;
    
    public ApiResponse callService() {
        if (!serviceAvailable) {
            throw new ServiceUnavailableException("Service is down");
        }
        
        try {
            return client.get("/api/data").execute();
        } catch (Exception e) {
            serviceAvailable = false;
            // Implement recovery logic
            throw e;
        }
    }
}
```

**Migration Steps:**
1. Remove `.enableCircuitBreaker()` calls
2. Implement circuit breaker logic at application level if needed
3. Use load balancers or service mesh for production circuit breaking

### 2. HTTP Caching - REMOVED

**v1.2.0 (Removed):**
```java
ApiClient client = new ApiClient.Builder()
    .enableCaching()  // ❌ No longer available
    .cache(CacheConfig.builder()
        .maxSize(1000)
        .ttl(Duration.ofMinutes(10))
        .build())
    .build();
```

**v1.3.0 (Alternatives):**

**Option 1: Application-level caching**
```java
// Use Spring Cache, Caffeine, or other caching libraries
@Service
public class UserService {
    @Cacheable("users")
    public User getUser(String id) {
        return client.get("/api/users/" + id)
            .execute()
            .to(User.class);
    }
}
```

**Option 2: HTTP caching headers**
```java
// Let HTTP caching work at the protocol level
ApiResponse response = client.get("/api/data")
    .header("Cache-Control", "max-age=300")  // 5 minutes
    .execute();
```

**Option 3: Reverse proxy caching**
```nginx
# nginx.conf
location /api/ {
    proxy_pass http://backend;
    proxy_cache my_cache;
    proxy_cache_valid 200 5m;
}
```

**Migration Steps:**
1. Remove `.enableCaching()` and cache configuration
2. Implement caching at application level using Spring Cache, Caffeine, etc.
3. Use reverse proxy caching for HTTP responses
4. Leverage HTTP cache headers for browser caching

### 3. Complex Retry Policies - SIMPLIFIED

**v1.2.0 (Removed):**
```java
ApiClient client = new ApiClient.Builder()
    .retryPolicy(RetryPolicy.builder()
        .maxAttempts(5)
        .backoffStrategy(ExponentialBackoff.builder()
            .initialDelay(Duration.ofSeconds(1))
            .maxDelay(Duration.ofSeconds(30))
            .multiplier(2.0)
            .build())
        .retryCondition(response -> response.code() >= 500)
        .build())
    .build();
```

**v1.3.0 (Simplified):**
```java
ApiClient client = new ApiClient.Builder()
    .enableRetry()  // ✅ 3 attempts, 1-second delay
    .build();

// Or customize:
ApiClient client = new ApiClient.Builder()
    .enableRetry(5, Duration.ofSeconds(2))  // ✅ 5 attempts, 2-second delay
    .build();

// For complex retry logic, implement at application level:
public class RetryService {
    public ApiResponse callWithRetry(String url) {
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                return client.get(url).execute();
            } catch (Exception e) {
                if (attempt == 3) throw e;
                Thread.sleep(1000 * attempt);  // Exponential backoff
            }
        }
        throw new RuntimeException("All retry attempts failed");
    }
}
```

**Migration Steps:**
1. Replace complex retry policies with `.enableRetry()`
2. For advanced retry logic, implement at application level
3. Use libraries like Spring Retry for complex scenarios

### 4. Connection Pool Configuration - SIMPLIFIED

**v1.2.0 (Removed):**
```java
ApiClient client = new ApiClient.Builder()
    .connectionPool(ConnectionPoolConfig.builder()
        .maxIdle(20)
        .maxTotal(50)
        .keepAlive(Duration.ofMinutes(5))
        .build())
    .build();
```

**v1.3.0 (Automatic):**
```java
ApiClient client = new ApiClient.Builder()
    .build();  // ✅ Connection pooling handled automatically by Java HttpClient

// Connection pooling is managed by the underlying Java HttpClient
// No configuration needed - it's optimized by default
```

**Migration Steps:**
1. Remove connection pool configuration
2. Java HttpClient handles connection pooling automatically
3. For advanced tuning, use JVM system properties:
   ```bash
   -Djdk.httpclient.keepalivetimeout=30
   -Djdk.httpclient.maxconnections=100
   ```

### 5. Streaming Multipart Upload - REMOVED

**v1.2.0 (Removed):**
```java
// StreamingMultipartBuilder no longer available
StreamingMultipartRequest request = new StreamingMultipartRequest()
    .addFile("file", new File("large-file.zip"))
    .build();

client.post("/upload")
    .body(request)
    .execute();
```

**v1.3.0 (Alternative):**
```java
// For most files, regular multipart works fine
MultipartRequest request = new MultipartRequest()
    .addFile("file", new File("file.zip"))  // Files up to 50MB
    .build();

client.post("/upload")
    .body(request)
    .execute();

// For very large files, use dedicated libraries:
// - Apache Commons FileUpload
// - Spring MultipartFile
// - Direct HTTP client streaming
```

**Migration Steps:**
1. Replace `StreamingMultipartBuilder` with regular `MultipartRequest`
2. For files > 50MB, use specialized file upload libraries
3. Consider chunked upload for very large files

### 6. Security Configuration - SIMPLIFIED

**v1.2.0 (Removed):**
```java
ApiClient client = new ApiClient.Builder()
    .securityConfig(SecurityConfig.forProduction())  // ❌ No longer available
    .build();
```

**v1.3.0 (Simplified):**
```java
// Development
ApiClient devClient = new ApiClient.Builder()
    .allowLocalhost(true)  // ✅ Allow localhost for development
    .build();

// Production
ApiClient prodClient = new ApiClient.Builder()
    .allowLocalhost(false)  // ✅ Block localhost for production
    .build();
```

**Migration Steps:**
1. Replace `SecurityConfig.forProduction()` with `.allowLocalhost(false)`
2. Replace `SecurityConfig.forDevelopment()` with `.allowLocalhost(true)`
3. For advanced security, implement URL validation at application level

## 📋 Migration Checklist

### Step 1: Update Dependencies
```gradle
// Update version
implementation("io.github.guptavishal-xm1:MochaJSON:1.3.0")
```

```xml
<!-- Maven -->
<dependency>
    <groupId>io.github.guptavishal-xm1</groupId>
    <artifactId>MochaJSON</artifactId>
    <version>1.3.0</version>
</dependency>
```

### Step 2: Update ApiClient Configuration
```java
// Before (v1.2.0)
ApiClient client = new ApiClient.Builder()
    .enableConnectionPooling()
    .enableRetryPolicy()
    .enableCircuitBreaker()
    .enableCaching()
    .securityConfig(SecurityConfig.forProduction())
    .build();

// After (v1.3.0)
ApiClient client = new ApiClient.Builder()
    .enableRetry()                    // Simple retry
    .allowLocalhost(false)            // Simple security
    .enableLogging()                  // Keep logging
    .build();
```

### Step 3: Handle Removed Features
- [ ] Remove circuit breaker configuration
- [ ] Remove HTTP caching configuration  
- [ ] Simplify retry policies
- [ ] Remove connection pool configuration
- [ ] Replace streaming multipart with regular multipart
- [ ] Simplify security configuration

### Step 4: Test Your Application
```bash
# Run your tests to ensure everything works
./gradlew test
# or
mvn test
```

### Step 5: Update Documentation
- [ ] Update API documentation
- [ ] Update configuration examples
- [ ] Update deployment guides

## 🚀 Benefits After Migration

After migrating to v1.3.0, you'll enjoy:

- **Faster startup** - Less code to initialize
- **Smaller memory footprint** - Fewer features loaded
- **Simpler maintenance** - Less complex code to debug
- **Better performance** - Focused on core functionality
- **Easier onboarding** - New team members learn faster

## 🔧 Common Migration Patterns

### Pattern 1: Replace Circuit Breaker with Simple Retry
```java
// Old: Complex circuit breaker
.enableCircuitBreaker()

// New: Simple retry + application-level circuit breaker
.enableRetry()
// + implement circuit breaker in your service layer
```

### Pattern 2: Replace HTTP Caching with Application Caching
```java
// Old: HTTP-level caching
.enableCaching()

// New: Application-level caching
@Cacheable("api-responses")
public ApiResponse getData() {
    return client.get("/api/data").execute();
}
```

### Pattern 3: Simplify Security Configuration
```java
// Old: Complex security config
.securityConfig(SecurityConfig.forProduction())

// New: Simple boolean flag
.allowLocalhost(false)
```

## 📚 Need Help?

If you encounter issues during migration:

1. **Check the [API Reference](/MochaJSON/api/overview)** for v1.3.0 methods
2. **Review [Best Practices](/MochaJSON/best-practices/production-checklist)** for simplified patterns
3. **Join [GitHub Discussions](https://github.com/guptavishal-xm1/MochaJSON/discussions)** for community help
4. **Report issues** on [GitHub Issues](https://github.com/guptavishal-xm1/MochaJSON/issues)

## 🎉 Welcome to v1.3.0!

MochaJSON v1.3.0 brings you back to the essentials - a simple, fast, and reliable HTTP client that does one thing exceptionally well: making HTTP requests and parsing JSON in Java & Kotlin.

Happy coding! 🚀
