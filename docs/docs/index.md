---
title: MochaJSON — Unified HTTP & JSON Client for Java & Kotlin
slug: /
description: MochaJSON v1.2.0 is a production-ready HTTP client with automatic JSON parsing for Java & Kotlin. Features connection pooling, retry mechanisms, circuit breakers, HTTP caching, and file operations. Replace OkHttp + Gson with one dependency.
keywords: 
  - MochaJSON
  - Java HTTP client
  - Kotlin JSON parser
  - OkHttp alternative
  - Gson alternative
  - fluent API
  - REST client Java
  - async HTTP
  - virtual threads Java 21
  - connection pooling
  - retry mechanism
  - circuit breaker
  - HTTP caching
  - file operations
image: /img/social-card.jpg
---

# MochaJSON — Unified HTTP & JSON Client for Java & Kotlin

**The simplest way to make HTTP requests and parse JSON in Java & Kotlin - Now with v1.2.0 featuring production-grade features like connection pooling, retry mechanisms, and circuit breakers!**

Stop juggling OkHttp, Gson, and custom parsers. MochaJSON v1.2.0 unifies HTTP requests and JSON parsing into a single, fluent API that works seamlessly across both Java and Kotlin projects. This lightweight library eliminates boilerplate while providing enterprise-grade performance and resilience features.

```java
// Basic usage - One line to rule them all
Map<String, Object> user = Api.get("https://api.github.com/users/octocat").execute().toMap();

// Advanced usage with v1.2.0 production features
ApiClient client = new ApiClient.Builder()
    .connectTimeout(Duration.ofSeconds(10))
    .readTimeout(Duration.ofSeconds(30))
    .enableConnectionPooling()        // 🆕 Connection reuse
    .enableRetryPolicy()              // 🆕 Automatic retries
    .enableCircuitBreaker()           // 🆕 Fault tolerance
    .enableCaching()                  // 🆕 HTTP caching
    .addRequestInterceptor(RequestInterceptor.bearerAuth(() -> getToken()))
    .build();

CompletableFuture<ApiResponse> future = client.get("https://api.github.com/user")
    .executeAsync();
```

<div className="flex flex-col sm:flex-row gap-4 justify-center my-8">
  <a href="/MochaJSON/getting-started" className="btn-primary text-white no-underline">Get Started with MochaJSON →</a>
  <a href="/MochaJSON/api/overview" className="btn-secondary text-white no-underline">API Reference →</a>
</div>

## Why Choose MochaJSON Over OkHttp + Gson?

Traditional Java HTTP clients require multiple dependencies and verbose boilerplate. You need OkHttp for requests, Gson for JSON parsing, custom error handling, retry logic, and connection pooling. MochaJSON eliminates this complexity by providing a unified API that handles everything automatically.

Instead of writing 20+ lines of setup code, you get clean, readable requests that work identically in Java and Kotlin. This makes MochaJSON the perfect OkHttp alternative and Gson alternative for modern Java development.

```java
// Traditional approach: OkHttp + Gson (20+ lines of boilerplate)
OkHttpClient client = new OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .retryOnConnectionFailure(true)
    .connectionPool(new ConnectionPool(5, 5, TimeUnit.MINUTES))
    .build();

Gson gson = new Gson();
Request request = new Request.Builder()
    .url("https://api.example.com/user/123")
    .build();

Response response = client.newCall(request).execute();
String json = response.body().string();
User user = gson.fromJson(json, User.class);

// MochaJSON approach: 1 line with automatic JSON parsing + production features
User user = Api.get("/api/user/123").execute().toObject(User.class);
```

## MochaJSON vs Traditional Java HTTP Client Stack

| Traditional Stack | MochaJSON HTTP Client |
|------------------|----------------------|
| OkHttp + Gson + Custom Parsers | Single dependency |
| 20+ lines per request | 1-3 lines per request |
| Manual error handling | Automatic exception mapping |
| Separate Java/Kotlin code | Unified API for both languages |
| Complex async setup | Built-in async support with virtual threads |
| Multiple imports | One import statement |
| Heavy dependencies | Lightweight footprint |
| Manual timeout configuration | Configurable timeouts with builder pattern |
| No built-in interceptors | Request/Response interceptors |
| Limited security features | Enhanced security with URL validation |
| Manual connection pooling | 🆕 Automatic connection pooling |
| No retry mechanism | 🆕 Built-in retry with exponential backoff |
| No circuit breaker | 🆕 Circuit breaker for fault tolerance |
| No HTTP caching | 🆕 LRU cache with TTL support |
| No file operations | 🆕 Multipart upload/download |

## Core Features of MochaJSON

### 🚀 **Fluent API Design**
Chainable request builder with intuitive syntax that reads like natural language. Perfect for building REST clients in Java and Kotlin.

### 📦 **Automatic JSON Parsing**
Parse JSON to `Map`, `List`, or POJO objects with zero configuration required. No more manual JSON parsing headaches.

### ⚡ **Modern Async Support**
Execute requests asynchronously with virtual threads (Java 21+), CompletableFuture, or callbacks. Built for modern Java and Kotlin applications.

### 🔗 **Cross-Language Compatibility**
Works identically in Java and Kotlin with full interoperability and type safety. One library, two languages.

### 🛡️ **Enhanced Security**
URL validation, hardened JSON parsing, and input sanitization. Protection against common security vulnerabilities.

### 🔧 **Advanced Configuration**
Configurable timeouts, interceptors, and logging with the new `ApiClient` builder pattern. Enterprise-ready features.

### 📱 **Zero Boilerplate Setup**
One import, no setup required. Start making API calls immediately with this lightweight Java JSON parser.

### 🎯 **Type-Safe Operations**
Compile-time type checking with generic support for POJOs and collections. Type safety without the complexity.

### ⚖️ **Lightweight Performance**
Minimal dependencies and small footprint compared to traditional HTTP client stacks. Faster than OkHttp + Gson.

### 🆕 **New in v1.2.0**
- **Connection Pooling**: HTTP connection reuse with configurable pool size and keep-alive duration
- **Retry Mechanism**: Exponential backoff retry policy with selective retry conditions
- **Circuit Breaker**: Fault tolerance pattern with automatic recovery (CLOSED/OPEN/HALF_OPEN states)
- **HTTP Caching**: LRU cache with TTL support and disk storage for better performance
- **File Operations**: Multipart file upload and download with streaming support
- **Enhanced Security**: URL validation and hardened JSON parsing to prevent security vulnerabilities

### ✨ **Previous Features (v1.1.0)**
- Virtual threads support for Java 21+
- Request/Response interceptors
- Configurable timeouts with builder pattern
- Enhanced security features
- Improved async APIs with CompletableFuture

## Installation & Setup

### Gradle (Recommended)

Add MochaJSON to your Java or Kotlin project with Gradle:

```gradle
implementation("io.github.guptavishal-xm1:MochaJSON:1.2.0")
```

### Maven

Include MochaJSON in your Maven project:

```xml
<dependency>
    <groupId>io.github.guptavishal-xm1</groupId>
    <artifactId>MochaJSON</artifactId>
    <version>1.2.0</version>
</dependency>
```

### Optional Dependencies

For logging support, add SLF4J:

```gradle
implementation("org.slf4j:slf4j-api:2.0.9")
```

**📦 Available on [Maven Central](https://search.maven.org/artifact/io.github.guptavishal-xm1/MochaJSON/1.2.0/jar)**

## Quick Start Guide

Make your first API call in seconds with MochaJSON's fluent HTTP client:

```java
import io.mochaapi.client.*;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

// Basic usage - GET request with automatic JSON parsing
Map<String, Object> response = Api.get("https://jsonplaceholder.typicode.com/posts/1")
    .execute()
    .toMap();

System.out.println("Title: " + response.get("title"));

// Advanced usage with v1.2.0 production features
ApiClient client = new ApiClient.Builder()
    .connectTimeout(Duration.ofSeconds(10))
    .readTimeout(Duration.ofSeconds(30))
    .enableConnectionPooling()        // 🆕 Connection reuse
    .enableRetryPolicy()              // 🆕 Automatic retries
    .enableCircuitBreaker()           // 🆕 Fault tolerance
    .enableCaching()                  // 🆕 HTTP caching
    .enableLogging()
    .addRequestInterceptor(RequestInterceptor.bearerAuth(() -> getToken()))
    .build();

// Async request with CompletableFuture
CompletableFuture<ApiResponse> future = client.get("https://jsonplaceholder.typicode.com/posts")
    .executeAsync();

ApiResponse result = future.get();
Map<String, Object> data = result.toMap();
```

## Real-World Use Cases

MochaJSON excels in these common Java and Kotlin development scenarios:

- **REST API Integration**: Perfect for consuming REST APIs with automatic JSON parsing
- **Microservices Communication**: Lightweight HTTP client for service-to-service calls
- **Mobile App Backend**: Kotlin HTTP client for Android applications
- **Spring Boot Applications**: Drop-in replacement for RestTemplate with better performance
- **Data Pipeline Processing**: Efficient JSON parsing for ETL operations

## Frequently Asked Questions

### Can I use MochaJSON with Spring Boot?
Yes! MochaJSON works seamlessly with Spring Boot applications. It's a lightweight alternative to RestTemplate and WebClient.

### Does MochaJSON support async operations?
Absolutely! MochaJSON v1.1.0 provides modern async support with virtual threads (Java 21+), CompletableFuture, callbacks, and reactive streams for non-blocking operations.

### Is MochaJSON compatible with Kotlin?
Yes! MochaJSON is designed for both Java and Kotlin with full interoperability and type safety.

## Documentation & Resources

- **[🚀 Getting Started](/MochaJSON/getting-started)** — Complete setup guide and basic usage patterns
- **[⚖️ vs Alternatives](/MochaJSON/comparison)** — Compare MochaJSON with OkHttp, Gson, RestTemplate
- **[📖 Java Examples](/MochaJSON/usage/java-examples)** — Comprehensive Java usage examples and best practices
- **[📖 Kotlin Examples](/MochaJSON/usage/kotlin-examples)** — Kotlin-specific examples and coroutine integration
- **[🔧 Advanced Features](/MochaJSON/advanced/interceptors)** — Connection pooling, retry, circuit breaker, caching
- **[✅ Best Practices](/MochaJSON/best-practices/production-checklist)** — Production deployment guide
- **[📚 API Reference](/MochaJSON/api/overview)** — Complete API documentation and method reference
- **[🔄 Migration Guides](/MochaJSON/migration/from-okhttp)** — Migrate from other HTTP clients
- **[🤝 Contributing](/MochaJSON/contributing)** — How to contribute to the MochaJSON project

## External Resources

- **📦 [Maven Central Repository](https://search.maven.org/artifact/io.github.guptavishal-xm1/MochaJSON/1.2.0/jar)** — Download MochaJSON
- **🐙 [GitHub Repository](https://github.com/guptavishal-xm1/MochaJSON)** — Source code and issues
- **📚 [JavaDocs](https://javadoc.io/doc/io.github.guptavishal-xm1/MochaJSON/latest/index.html)** — API documentation

---

<div className="text-center py-8">
  <p className="text-lg mb-4">
    <strong>MIT Licensed</strong> • Made with ❤️ for the Java and Kotlin community
  </p>
  <div className="flex justify-center gap-4">
    <a href="https://github.com/guptavishal-xm1/MochaJSON" className="text-purple-600 hover:text-purple-800">
      ⭐ Star MochaJSON on GitHub
    </a>
    <a href="https://github.com/guptavishal-xm1/MochaJSON/issues" className="text-purple-600 hover:text-purple-800">
      🐛 Report Issues
    </a>
    <a href="https://github.com/guptavishal-xm1/MochaJSON/discussions" className="text-purple-600 hover:text-purple-800">
      💬 Join Discussions
    </a>
  </div>
</div>


