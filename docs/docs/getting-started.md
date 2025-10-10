---
title: Getting Started with MochaJSON
description: Get up and running with MochaJSON v1.2.0 in minutes. Learn installation, basic usage, and production-grade features like connection pooling, retry mechanisms, and circuit breakers.
keywords:
  - MochaJSON installation
  - MochaJSON setup
  - Java HTTP client tutorial
  - Kotlin HTTP client
  - getting started
---

# Getting Started with MochaJSON

Welcome to MochaJSON v1.2.0! This guide will help you get up and running with the library in just a few minutes, including the new production-grade features like connection pooling, retry mechanisms, and circuit breakers.

## Installation

### Gradle

Add the dependency to your `build.gradle.kts` file:

```kotlin
dependencies {
    implementation("io.github.guptavishal-xm1:MochaJSON:1.2.0")
    
    // Optional: For logging support
    implementation("org.slf4j:slf4j-api:2.0.9")
}
```

Or if you're using Groovy syntax:

```gradle
dependencies {
    implementation 'io.github.guptavishal-xm1:MochaJSON:1.2.0'
    
    // Optional: For logging support
    implementation 'org.slf4j:slf4j-api:2.0.9'
}
```

### Maven

Add the dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>io.github.guptavishal-xm1</groupId>
    <artifactId>MochaJSON</artifactId>
    <version>1.2.0</version>
</dependency>

<!-- Optional: For logging support -->
<dependency>
    <groupId>org.slf4j</groupId>
    <artifactId>slf4j-api</artifactId>
    <version>2.0.9</version>
</dependency>
```

## Your First API Call

Let's make a simple GET request and parse the JSON response:

### Java

```java
import io.mochaapi.client.*;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class Main {
    public static void main(String[] args) {
        try {
            // Basic usage - Make a GET request and parse JSON to Map
            Map<String, Object> user = Api.get("https://jsonplaceholder.typicode.com/users/1")
                .execute()
                .toMap();
            
            System.out.println("User Name: " + user.get("name"));
            System.out.println("User Email: " + user.get("email"));
            
            // Advanced usage with v1.2.0 production features
            ApiClient client = new ApiClient.Builder()
                .connectTimeout(Duration.ofSeconds(10))
                .readTimeout(Duration.ofSeconds(30))
                .enableConnectionPooling()        // 🆕 Connection reuse
                .enableRetryPolicy()              // 🆕 Automatic retries
                .enableCircuitBreaker()           // 🆕 Fault tolerance
                .enableCaching()                  // 🆕 HTTP caching
                .enableLogging()
                .build();
            
            // Async request with CompletableFuture
            CompletableFuture<ApiResponse> future = client.get("https://jsonplaceholder.typicode.com/posts/1")
                .executeAsync();
            
            ApiResponse response = future.get();
            Map<String, Object> post = response.toMap();
            System.out.println("Post Title: " + post.get("title"));
            
        } catch (ApiException e) {
            System.err.println("API Error: " + e.getMessage());
        } catch (JsonException e) {
            System.err.println("JSON Error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
```

### Kotlin

```kotlin
import io.mochaapi.client.*
import java.time.Duration
import java.util.concurrent.CompletableFuture

fun main() {
    try {
        // Basic usage - Make a GET request and parse JSON to Map
        val user = Api.get("https://jsonplaceholder.typicode.com/users/1")
            .execute()
            .toMap()
        
        println("User Name: ${user["name"]}")
        println("User Email: ${user["email"]}")
        
        // Advanced usage with v1.2.0 production features
        val client = ApiClient.Builder()
            .connectTimeout(Duration.ofSeconds(10))
            .readTimeout(Duration.ofSeconds(30))
            .enableConnectionPooling()        // 🆕 Connection reuse
            .enableRetryPolicy()              // 🆕 Automatic retries
            .enableCircuitBreaker()           // 🆕 Fault tolerance
            .enableCaching()                  // 🆕 HTTP caching
            .enableLogging()
            .build()
        
        // Async request
        val future = client.get("https://jsonplaceholder.typicode.com/posts/1")
            .executeAsync()
        
        val response = future.get()
        val post = response.toMap()
        println("Post Title: ${post["title"]}")
        
    } catch (e: ApiException) {
        println("API Error: ${e.message}")
    } catch (e: JsonException) {
        println("JSON Error: ${e.message}")
    } catch (e: Exception) {
        println("Error: ${e.message}")
    }
}
```

## Sample Response

The API call above returns JSON data that gets automatically parsed:

| Field | Type | Sample Value |
|-------|------|--------------|
| `id` | `int` | `1` |
| `name` | `String` | `"Leanne Graham"` |
| `email` | `String` | `"Sincere@april.biz"` |
| `username` | `String` | `"Bret"` |
| `phone` | `String` | `"1-770-736-8031 x56442"` |
| `website` | `String` | `"hildegard.org"` |

## What's Next?

Now that you have MochaJSON installed and working, explore these guides:

### 📖 Usage Examples
- **[Java Examples](/MochaJSON/usage/java-examples)** - Complete Java usage patterns and best practices
- **[Kotlin Examples](/MochaJSON/usage/kotlin-examples)** - Complete Kotlin usage patterns and coroutine integration  
- **[JSON Handling](/MochaJSON/usage/json-handling)** - Advanced JSON parsing techniques and edge cases

### 🔧 Advanced Features
- **[Advanced Features](/MochaJSON/advanced/interceptors)** - Production-grade features overview
- **[Interceptors](/MochaJSON/advanced/interceptors)** - Request/response interceptors for authentication and logging

### ✅ Best Practices
- **[Production Checklist](/MochaJSON/best-practices/production-checklist)** - Deploy with confidence
- **[Common Mistakes](/MochaJSON/best-practices/common-mistakes)** - What to avoid
- **[Performance Tips](/MochaJSON/best-practices/performance-tips)** - Optimize your applications

### 📚 Reference
- **[API Reference](/MochaJSON/api/overview)** - Complete API documentation
- **[Migration Guides](/MochaJSON/migration/from-okhttp)** - Migrate from other HTTP clients

## Need Help?

- 📖 Check out the [examples](/MochaJSON/usage/java-examples) for more complex usage patterns
- 📚 Review the [API reference](/MochaJSON/api/overview) for detailed method documentation
- 🐛 Report issues on [GitHub](https://github.com/guptavishal-xm1/MochaJSON/issues)
- 💬 Join discussions on [GitHub Discussions](https://github.com/guptavishal-xm1/MochaJSON/discussions)
