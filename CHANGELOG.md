# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.2.0] - 2025-10-25

### Added
- **Connection Pooling**: HTTP connection reuse with configurable pool size and keep-alive duration
- **Retry Mechanism**: Exponential backoff retry policy with selective retry conditions
- **Circuit Breaker**: Fault tolerance pattern with automatic recovery (CLOSED/OPEN/HALF_OPEN states)
- **HTTP Caching**: LRU cache with TTL support and disk storage for better performance
- **File Operations**: Multipart file upload and download with streaming support
- **Enhanced Security**: URL validation and hardened JSON parsing to prevent security vulnerabilities
- **Advanced Configuration**: New builder methods for connection pooling, retry policies, circuit breakers, and caching

### Changed
- Enhanced `ApiClient.Builder` with new configuration options for production-grade features
- Improved error handling for network failures and timeouts
- Optimized memory usage with connection pooling and caching

### Security
- Fixed potential security issues with URL validation
- Disabled polymorphic typing in JSON parsing to prevent deserialization attacks

## [1.1.0] - 2024-12-19

### Added
- **ApiClient with Builder Pattern**: New `ApiClient` class with fluent builder API for advanced configuration
- **Configurable Timeouts**: Support for connection, read, and write timeouts via builder pattern
- **Interceptor System**: 
  - `RequestInterceptor` interface for modifying requests before sending
  - `ResponseInterceptor` interface for processing responses after receiving
  - Built-in logging, authentication, and error handling interceptors
- **Virtual Threads Support**: Automatic use of Java 21 virtual threads for async operations with fallback to cached thread pool
- **Enhanced Async API**: 
  - `CompletableFuture<ApiResponse> executeAsync()` method
  - Improved async callback handling with proper exception propagation
- **Security Enhancements**:
  - URL validation to prevent open redirect attacks
  - Hardened JSON parsing with disabled polymorphic typing in Jackson
  - Input validation and sanitization
- **Improved Error Handling**: Better exception handling in async callbacks with no silent failures
- **Query Parameter Fixes**: Fixed URL concatenation issues when handling existing query parameters
- **KotlinxJsonMapper Improvements**: Enhanced generic type handling and security configuration

### Changed
- **Backward Compatibility**: All v1.0.x APIs remain fully compatible
- **Default User-Agent**: Updated to "MochaAPI-Client/1.1.0"
- **Jackson Configuration**: Enhanced security settings by default
- **Async Execution**: Improved thread safety and resource management

### Fixed
- **Query Parameter Concatenation**: Fixed issues with URL building when query parameters already exist
- **KotlinxJsonMapper Generic Types**: Resolved type loss issues in Kotlin serialization
- **Async Callback Exceptions**: Fixed silent failure issues in async operations
- **Thread Safety**: Improved thread safety across all async operations

### Security
- **URL Validation**: Added comprehensive URL validation to prevent malicious URLs
- **JSON Security**: Disabled dangerous Jackson features like polymorphic typing
- **Input Sanitization**: Enhanced input validation and sanitization
- **Timeout Protection**: Configurable timeouts to prevent resource exhaustion

### Testing
- **Comprehensive Test Suite**: Added extensive JUnit + Mockito tests with >80% coverage
- **Mock Server**: Built-in mock HTTP server for reliable testing
- **Security Tests**: Dedicated security test suite
- **Async Tests**: Comprehensive async functionality testing
- **Interceptor Tests**: Full interceptor system testing

### Documentation
- **Enhanced JavaDoc**: Improved documentation with examples and security notes
- **Usage Examples**: Added comprehensive usage examples for new features
- **Migration Guide**: Clear migration path from v1.0.x to v1.1.0

## [1.0.1] - Previous Release

### Features
- Basic HTTP client functionality
- JSON serialization/deserialization
- Support for GET, POST, PUT, DELETE, PATCH methods
- Query parameters and headers support
- Basic async operations with callbacks
- Jackson and Kotlinx Serialization support

---

## Migration Guide

### From v1.0.x to v1.1.0

The upgrade to v1.1.0 is fully backward compatible. Existing code will continue to work without changes.

#### Optional: Migrating to ApiClient

While not required, you can optionally migrate to the new `ApiClient` for advanced features:

**Before (v1.0.x):**
```java
ApiResponse response = Api.get("https://api.example.com/data")
    .query("page", 1)
    .execute();
```

**After (v1.1.0) - Optional:**
```java
ApiClient client = new ApiClient.Builder()
    .connectTimeout(Duration.ofSeconds(10))
    .readTimeout(Duration.ofSeconds(30))
    .enableLogging()
    .build();

ApiResponse response = client.get("https://api.example.com/data")
    .query("page", 1)
    .execute();
```

#### New Features Available

1. **Configurable Timeouts**:
```java
ApiClient client = new ApiClient.Builder()
    .connectTimeout(Duration.ofSeconds(5))
    .readTimeout(Duration.ofSeconds(10))
    .writeTimeout(Duration.ofSeconds(15))
    .build();
```

2. **Request/Response Interceptors**:
```java
ApiClient client = new ApiClient.Builder()
    .addRequestInterceptor(request -> {
        System.out.println("Sending: " + request.getMethod() + " " + request.getUrl());
        return request.header("X-Request-ID", UUID.randomUUID().toString());
    })
    .addResponseInterceptor(response -> {
        System.out.println("Received: " + response.code());
        return response;
    })
    .build();
```

3. **Enhanced Async Operations**:
```java
// New CompletableFuture API
CompletableFuture<ApiResponse> future = client.get("https://api.example.com/data")
    .executeAsync();

ApiResponse response = future.get();

// Chain operations
client.get("https://api.example.com/data")
    .executeAsync()
    .thenApply(r -> r.toMap())
    .thenAccept(data -> System.out.println("Data: " + data));
```

4. **Built-in Interceptors**:
```java
ApiClient client = new ApiClient.Builder()
    .addRequestInterceptor(RequestInterceptor.bearerAuth(() -> getToken()))
    .addResponseInterceptor(ResponseInterceptor.throwOnError())
    .build();
```

### Dependencies

No new required dependencies. Optional SLF4J dependency added for logging support:

```gradle
dependencies {
    implementation("org.slf4j:slf4j-api:2.0.9") // Optional for logging
}
```

### Breaking Changes

**None.** This release maintains full backward compatibility with v1.0.x.