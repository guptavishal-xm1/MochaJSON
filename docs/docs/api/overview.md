# API Overview

MochaAPI Client provides a simple, fluent interface for making HTTP requests and parsing JSON responses in Java and Kotlin.

## Core Concepts

### 1. Entry Point: `Api` Class

The `Api` class is the main entry point for all HTTP requests. It provides static methods for creating requests:

```java
// Create different types of requests
ApiRequest getRequest = Api.get("https://api.example.com/data");
ApiRequest postRequest = Api.post("https://api.example.com/users");
ApiRequest putRequest = Api.put("https://api.example.com/users/1");
ApiRequest deleteRequest = Api.delete("https://api.example.com/users/1");
ApiRequest patchRequest = Api.patch("https://api.example.com/users/1");
```

### 2. Request Building: `ApiRequest` Class

The `ApiRequest` class provides a fluent interface for building HTTP requests:

```java
ApiRequest request = Api.get("https://api.example.com/data")
    .header("Authorization", "Bearer token123")  // Add headers
    .query("page", 1)                           // Add query parameters
    .query("limit", 10)                         // Add more query parameters
    .body(requestData);                         // Set request body
```

### 3. Response Handling: `ApiResponse` Class

The `ApiResponse` class contains the HTTP response and provides methods for parsing JSON:

```java
ApiResponse response = request.execute();

// Basic response information
int statusCode = response.code();                // HTTP status code
String body = response.body();                  // Raw response body
Map<String, String> headers = response.headers(); // Response headers

// JSON parsing methods
User user = response.to(User.class);            // Parse to POJO
Map<String, Object> map = response.toMap();     // Parse to Map
List<Object> list = response.toList();          // Parse to List

// Status checking
boolean isSuccess = response.isSuccess();        // 200-299 status codes
boolean isError = response.isError();            // 400+ status codes
```

## HTTP Methods

| Method | Description | Usage |
|--------|-------------|-------|
| `GET` | Retrieve data | `Api.get(url)` |
| `POST` | Create new resource | `Api.post(url)` |
| `PUT` | Update existing resource | `Api.put(url)` |
| `DELETE` | Delete resource | `Api.delete(url)` |
| `PATCH` | Partial update | `Api.patch(url)` |

## Request Building Methods

### Headers

```java
ApiRequest request = Api.get("https://api.example.com/data")
    .header("Authorization", "Bearer token123")
    .header("Content-Type", "application/json")
    .header("User-Agent", "MyApp/1.0");
```

### Query Parameters

```java
ApiRequest request = Api.get("https://api.example.com/search")
    .query("q", "java")
    .query("page", 1)
    .query("limit", 10)
    .query("sort", "date");
```

### Request Body

```java
// String body
ApiRequest request = Api.post("https://api.example.com/data")
    .body("{\"name\":\"John\",\"email\":\"john@example.com\"}");

// Map body (automatically serialized to JSON)
Map<String, Object> data = Map.of("name", "John", "email", "john@example.com");
ApiRequest request = Api.post("https://api.example.com/data")
    .body(data);

// POJO body (automatically serialized to JSON)
User user = new User("John", "john@example.com");
ApiRequest request = Api.post("https://api.example.com/data")
    .body(user);
```

## Response Methods

### Basic Response Information

```java
ApiResponse response = request.execute();

// HTTP status code
int statusCode = response.code();

// Raw response body as string
String body = response.body();

// Response headers
Map<String, String> headers = response.headers();
```

### JSON Parsing

```java
// Parse to Map<String, Object>
Map<String, Object> data = response.toMap();
String name = (String) data.get("name");

// Parse to List<Object>
List<Object> items = response.toList();
for (Object item : items) {
    Map<String, Object> itemMap = (Map<String, Object>) item;
    System.out.println(itemMap.get("title"));
}

// Parse to POJO
User user = response.to(User.class);
System.out.println(user.getName());
```

### Status Checking

```java
ApiResponse response = request.execute();

// General status checking
if (response.isSuccess()) {
    // Status code 200-299
    System.out.println("Request successful");
} else if (response.isError()) {
    // Status code 400+
    System.out.println("Request failed: " + response.code());
}

// Specific status code checking
if (response.isOk()) {
    System.out.println("OK (200)");
} else if (response.isCreated()) {
    System.out.println("Created (201)");
} else if (response.isNotFound()) {
    System.out.println("Not Found (404)");
} else if (response.isUnauthorized()) {
    System.out.println("Unauthorized (401)");
} else if (response.isInternalServerError()) {
    System.out.println("Server Error (500)");
}

// Category checking
if (response.isClientError()) {
    System.out.println("Client error (400-499)");
} else if (response.isServerError()) {
    System.out.println("Server error (500-599)");
}

// Get human-readable status description
System.out.println("Status: " + response.code() + " - " + response.getStatusDescription());
```

## Async Execution

```java
// Execute asynchronously with callback
Api.get("https://api.example.com/data")
    .async(response -> {
        System.out.println("Async response: " + response.body());
        System.out.println("Status: " + response.code());
    });

// Main thread continues...
System.out.println("Request sent asynchronously");
```

## Error Handling

### Exception Types

| Exception | When Thrown | Example |
|-----------|-------------|---------|
| `ApiException` | HTTP/network errors | Connection timeout, 500 error |
| `JsonException` | JSON parsing errors | Malformed JSON, type mismatch |

### Error Handling Example

```java
try {
    ApiResponse response = Api.get("https://api.example.com/data")
        .execute();
    
    if (response.isError()) {
        System.err.println("HTTP Error: " + response.code());
        return;
    }
    
    User user = response.to(User.class);
    System.out.println("Success: " + user.getName());
    
} catch (ApiException e) {
    System.err.println("Network/HTTP Error: " + e.getMessage());
} catch (JsonException e) {
    System.err.println("JSON Parsing Error: " + e.getMessage());
} catch (Exception e) {
    System.err.println("Unexpected Error: " + e.getMessage());
}
```

## JSON Mapping

### Supported Types

| Type | Description | Example |
|------|-------------|---------|
| `Map<String, Object>` | Generic JSON object | `response.toMap()` |
| `List<Object>` | JSON array | `response.toList()` |
| `POJO` | Custom class | `response.to(User.class)` |
| `String` | Raw JSON string | `response.body()` |

### JSON Mappers

MochaAPI Client includes two JSON mappers:

1. **JacksonJsonMapper** - Uses Jackson Databind (default for Java)
2. **KotlinxJsonMapper** - Uses Kotlinx Serialization (for Kotlin)

Both implement the `JsonMapper` interface:

```java
public interface JsonMapper {
    String stringify(Object obj);
    <T> T parse(String json, Class<T> type);
    Map<String, Object> toMap(String json);
    List<Object> toList(String json);
}
```

## Configuration

### Default Settings

- **Connection Timeout**: 30 seconds
- **Read Timeout**: 30 seconds
- **User Agent**: `MochaAPI-Client/1.0.1`
- **Default Headers**: `Accept: application/json`
- **Content-Type**: `application/json` (for POST/PUT/PATCH)

### Custom Configuration

```java
// Custom HttpClient (requires modifying the library)
HttpClient customClient = HttpClient.newBuilder()
    .connectTimeout(Duration.ofSeconds(60))
    .build();

// Custom ObjectMapper (requires modifying the library)
ObjectMapper customMapper = new ObjectMapper();
customMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
```

## Best Practices

### 1. Use POJOs for Type Safety

```java
// ✅ Good: Type-safe
User user = response.to(User.class);
System.out.println(user.getName());

// ❌ Avoid: Unsafe casting
Map<String, Object> data = response.toMap();
System.out.println((String) data.get("name"));
```

### 2. Handle Errors Gracefully

```java
// ✅ Good: Comprehensive error handling
try {
    ApiResponse response = Api.get(url).execute();
    if (response.isError()) {
        handleHttpError(response.code());
        return;
    }
    processResponse(response);
} catch (ApiException e) {
    handleNetworkError(e);
} catch (JsonException e) {
    handleJsonError(e);
}
```

### 3. Use Appropriate HTTP Methods

```java
// ✅ Good: Correct HTTP methods
Api.get(url)      // For retrieving data
Api.post(url)     // For creating resources
Api.put(url)      // For updating resources
Api.delete(url)   // For deleting resources
```

## Next Steps

- **[API Reference](/MochaJSON/api/api-reference)** - Complete method documentation
- **[Exceptions](/MochaJSON/api/exceptions)** - Detailed error handling guide
- **[Java Examples](/MochaJSON/usage/java-examples)** - Complete Java usage examples
- **[Kotlin Examples](/MochaJSON/usage/kotlin-examples)** - Complete Kotlin usage examples
