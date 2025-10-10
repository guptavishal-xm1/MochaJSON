# MochaAPI Client — Unified HTTP & JSON Library

[![Maven Central](https://img.shields.io/maven-central/v/io.github.guptavishal-xm1/MochaJSON.svg)](https://search.maven.org/artifact/io.github.guptavishal-xm1/MochaJSON)
[![Build Status](https://img.shields.io/badge/build-passing-brightgreen.svg)](https://github.com/guptavishal-xm1/MochaJSON)
[![MIT License](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

A **one-import, unified HTTP + JSON client** for Java and Kotlin that enables fast API calls and automatic JSON parsing with zero boilerplate. Now with **v1.3.0** featuring simplified, focused API with essential features only!

## Table of Contents

- [Features](#features)
- [Installation](#installation)
- [Getting Started](#getting-started)
- [Usage Examples](#usage-examples)
  - [Basic Usage](#basic-usage)
  - [Advanced Usage with ApiClient](#advanced-usage-with-apiclient)
  - [Async Operations](#async-operations)
  - [Interceptors](#interceptors)
- [API Reference](#api-reference)
- [Security](#security)
- [Exceptions](#exceptions)
- [Migration Guide](#migration-guide)
- [Contributing](#contributing)
- [License](#license)

## Features

| Feature | Description |
|---------|-------------|
| **HTTP Methods** | GET, POST, PUT, DELETE, PATCH |
| **JSON Parsing** | Automatically parse JSON to Map, List, or POJO |
| **Fluent API** | Chainable request builder for simple usage |
| **Async Support** | Execute requests asynchronously with virtual threads (Java 21+) |
| **Kotlin + Java** | Works seamlessly in both languages |
| **Zero Boilerplate** | One import, no setup required |
| **Exception Handling** | `ApiException`, `JsonException` |
| **🆕 Simple Retry** | Basic retry with configurable attempts and delay |
| **🆕 Security Control** | Simple localhost access control |
| **🆕 Interceptors** | Request/Response interceptors for logging, auth, and more |
| **🆕 Configurable Timeouts** | Custom connection, read, and write timeouts |
| **🆕 Builder Pattern** | Simplified configuration with `ApiClient.Builder` |

## Installation

### Gradle

```gradle
implementation("io.github.guptavishal-xm1:MochaJSON:1.3.0")
```

### Maven

```xml
<dependency>
    <groupId>io.github.guptavishal-xm1</groupId>
    <artifactId>MochaJSON</artifactId>
    <version>1.3.0</version>
</dependency>
```

### Optional Dependencies

For logging support, add SLF4J:

```gradle
implementation("org.slf4j:slf4j-api:2.0.9")
```

## Getting Started

### Java

```java
import io.mochaapi.client.*;

public class Main {
    public static void main(String[] args) {
        try {
            User user = Api.get("https://jsonplaceholder.typicode.com/users/1")
                           .execute()
                           .to(User.class);
            System.out.println("Name: " + user.name);
            System.out.println("Email: " + user.email);
        } catch (ApiException | JsonException e) {
            e.printStackTrace();
        }
    }
    
    // POJO class for JSON parsing
    public static class User {
        public int id;
        public String name;
        public String username;
        public String email;
        public Address address;
        
        public static class Address {
            public String street;
            public String suite;
            public String city;
            public String zipcode;
        }
    }
}
```

### Kotlin

```kotlin
import io.mochaapi.client.*

data class User(
    val id: Int,
    val name: String,
    val username: String,
    val email: String,
    val address: Address
)

data class Address(
    val street: String,
    val suite: String,
    val city: String,
    val zipcode: String
)

fun main() {
    val user: User = Api.get("https://jsonplaceholder.typicode.com/users/1")
        .execute()
        .to(User::class.java)
    println("Name: ${user.name}")
    println("Email: ${user.email}")
    println("City: ${user.address.city}")
}
```

## Usage Examples

### Basic Usage

The simplest way to use MochaAPI is with the static `Api` class:

```java
import io.mochaapi.client.*;

// GET request
ApiResponse response = Api.get("https://api.example.com/users/1").execute();
User user = response.to(User.class);

// POST request
ApiResponse response = Api.post("https://api.example.com/users")
    .body(Map.of("name", "John", "email", "john@example.com"))
    .execute();
```

### Advanced Usage with ApiClient

For advanced features like timeouts, interceptors, and logging:

```java
import io.mochaapi.client.*;
import java.time.Duration;

// Create configured client
ApiClient client = new ApiClient.Builder()
    .connectTimeout(Duration.ofSeconds(10))
    .readTimeout(Duration.ofSeconds(30))
    .enableLogging()
    .addRequestInterceptor(RequestInterceptor.bearerAuth(() -> getToken()))
    .addResponseInterceptor(ResponseInterceptor.throwOnError())
    .build();

// Use the client
ApiResponse response = client.get("https://api.example.com/data")
    .query("page", 1)
    .execute();
```

### Async Operations

```java
// Using CompletableFuture
CompletableFuture<ApiResponse> future = client.get("https://api.example.com/data")
    .executeAsync();

ApiResponse response = future.get();

// Chain operations
client.get("https://api.example.com/data")
    .executeAsync()
    .thenApply(r -> r.toMap())
    .thenAccept(data -> System.out.println("Data: " + data));

// Using callbacks
client.get("https://api.example.com/data")
    .async(response -> {
        System.out.println("Received: " + response.code());
    });
```

### Interceptors

```java
// Request interceptors
RequestInterceptor authInterceptor = RequestInterceptor.bearerAuth(() -> getToken());
RequestInterceptor loggingInterceptor = RequestInterceptor.logging(System.out::println);

// Response interceptors
ResponseInterceptor errorHandler = ResponseInterceptor.throwOnError();
ResponseInterceptor responseLogger = ResponseInterceptor.logging(System.out::println);

ApiClient client = new ApiClient.Builder()
    .addRequestInterceptor(authInterceptor)
    .addRequestInterceptor(loggingInterceptor)
    .addResponseInterceptor(errorHandler)
    .addResponseInterceptor(responseLogger)
    .build();
```

### Java GET Request

```java
import io.mochaapi.client.*;
import java.util.Map;

public class GetExample {
    public static void main(String[] args) {
        // Parse JSON to Map
        Map<String, Object> data = Api.get("https://jsonplaceholder.typicode.com/posts/1")
                                      .execute()
                                      .toMap();
        System.out.println("Post ID: " + data.get("id"));
        System.out.println("Title: " + data.get("title"));
        
        // Parse JSON to POJO
        Post post = Api.get("https://jsonplaceholder.typicode.com/posts/1")
                       .execute()
                       .to(Post.class);
        System.out.println("Post: " + post.title);
    }
    
    public static class Post {
        public int id;
        public int userId;
        public String title;
        public String body;
    }
}
```

**Sample JSON Response:**

| Field | Type | Sample Value |
|-------|------|--------------|
| `userId` | `int` | `1` |
| `id` | `int` | `1` |
| `title` | `String` | `"sunt aut facere repellat provident occaecati excepturi optio reprehenderit"` |
| `body` | `String` | `"quia et suscipit\nsuscipit recusandae consequuntur expedita et cum\nreprehenderit molestiae ut ut quas totam\nnostrum rerum est autem sunt rem eveniet architecto"` |

### Java POST Request

```java
import io.mochaapi.client.*;
import java.util.Map;
import java.util.HashMap;

public class PostExample {
    public static void main(String[] args) {
        // Create request body
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("title", "My New Post");
        requestBody.put("body", "This is the content of my new post.");
        requestBody.put("userId", 1);

        // Send POST request
        ApiResponse response = Api.post("https://jsonplaceholder.typicode.com/posts")
                                  .body(requestBody)
                                  .execute();

        System.out.println("Status Code: " + response.code());
        
        // Parse response JSON to Map
        Map<String, Object> responseData = response.toMap();
        System.out.println("Created Post ID: " + responseData.get("id"));
        System.out.println("Title: " + responseData.get("title"));
        
        // Parse response JSON to POJO
        Post createdPost = response.to(Post.class);
        System.out.println("Created Post: " + createdPost.title);
    }
    
    public static class Post {
        public int id;
        public int userId;
        public String title;
        public String body;
    }
}
```

**Sample Response JSON:**

| Field | Type | Sample Value |
|-------|------|--------------|
| `id` | `int` | `101` |
| `title` | `String` | `"foo"` |
| `body` | `String` | `"bar"` |
| `userId` | `int` | `1` |

### Kotlin GET Request

```kotlin
val post: Post = Api.get("https://jsonplaceholder.typicode.com/posts/1")
    .execute()
    .to(Post::class.java)
println(post.title)
```

## API Reference

| Class | Description |
|-------|-------------|
| `Api.java` | Entry point for HTTP requests (GET, POST, etc.) - v1.0.x compatible |
| `ApiClient.java` | Advanced client with builder pattern for configuration |
| `ApiRequest.java` | Builds HTTP requests with headers, queries, and body |
| `ApiResponse.java` | Holds response, provides `json()`, `toMap()`, and `to(POJO.class)` |
| `JsonMapper.java` | Interface for JSON mapping (Map/List/POJO) |
| `RequestInterceptor.java` | Interface for intercepting and modifying requests |
| `ResponseInterceptor.java` | Interface for intercepting and modifying responses |

## Security

MochaAPI v1.2.0 includes several security enhancements:

### URL Validation
```java
// Automatically validates URLs to prevent open redirect attacks
Api.get("https://api.example.com/data").execute(); // ✅ Valid
Api.get("javascript:alert('xss')").execute(); // ❌ Throws IllegalArgumentException
```

### Hardened JSON Parsing
- Disabled polymorphic typing in Jackson to prevent deserialization attacks
- Enhanced input validation and sanitization
- Secure default configurations

### Timeout Protection
```java
ApiClient client = new ApiClient.Builder()
    .connectTimeout(Duration.ofSeconds(5))
    .readTimeout(Duration.ofSeconds(10))
    .build();
```

### Input Sanitization
- Automatic URL encoding for query parameters
- Header value validation
- Request body sanitization

## Exceptions

- **`ApiException`** → thrown for HTTP or connection errors
- **`JsonException`** → thrown for JSON parsing errors

### Example Error Handling

```java
try {
    Api.get("https://jsonplaceholder.typicode.com/invalid")
       .execute()
       .json();
} catch (ApiException e) {
    System.out.println("API Error: " + e.getMessage());
} catch (JsonException e) {
    System.out.println("JSON Parsing Error: " + e.getMessage());
}
```

## Migration Guide

### From v1.1.0 to v1.2.0

**✅ Full Backward Compatibility** - All existing code continues to work without changes!

#### New Features in v1.2.0

The v1.2.0 release adds powerful production-grade features while maintaining full backward compatibility:

**Connection Pooling:**
```java
ApiClient client = new ApiClient.Builder()
    .connectionPool(ConnectionPoolConfig.builder()
        .maxIdle(10)
        .keepAlive(Duration.ofMinutes(5))
        .build())
    .build();
```

**Retry Mechanism:**
```java
ApiClient client = new ApiClient.Builder()
    .retryPolicy(RetryPolicy.exponential(3))
    .build();
```

**Circuit Breaker:**
```java
ApiClient client = new ApiClient.Builder()
    .circuitBreaker(CircuitBreaker.standard())
    .build();
```

**HTTP Caching:**
```java
ApiClient client = new ApiClient.Builder()
    .cache(CacheConfig.auto())
    .build();
```

**File Operations:**
```java
// Upload
ApiResponse response = client.post("https://api.example.com/upload")
    .multipart()
    .addFile("file", new File("document.pdf"))
    .execute();

// Download
File file = client.get("https://api.example.com/download")
    .download("output.pdf");
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

### Breaking Changes

**None.** This release maintains full backward compatibility with v1.0.x.

## Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/amazing-feature`
3. Commit your changes: `git commit -m 'Add amazing feature'`
4. Push to the branch: `git push origin feature/amazing-feature`
5. Open a Pull Request

Report issues in the [GitHub issues tab](https://github.com/guptavishal-xm1/MochaJSON/issues).

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
