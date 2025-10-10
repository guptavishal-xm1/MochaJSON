# API Reference

Complete reference for all classes, methods, and interfaces in MochaAPI Client v1.1.0.

## Core Classes

### `ApiClient` Class (New in v1.1.0)

Advanced HTTP client with configurable timeouts, interceptors, and logging.

#### Constructor

| Constructor | Description | Parameters |
|-------------|-------------|------------|
| `ApiClient(Builder builder)` | Create from Builder | `builder` - Configured builder |

#### Methods

| Method | Description | Parameters | Returns |
|--------|-------------|------------|---------|
| `get(String url)` | Create GET request | `url` - Target URL | `ApiRequest` |
| `post(String url)` | Create POST request | `url` - Target URL | `ApiRequest` |
| `put(String url)` | Create PUT request | `url` - Target URL | `ApiRequest` |
| `delete(String url)` | Create DELETE request | `url` - Target URL | `ApiRequest` |
| `patch(String url)` | Create PATCH request | `url` - Target URL | `ApiRequest` |
| `execute(ApiRequest request)` | Execute request synchronously | `request` - Configured request | `ApiResponse` |
| `executeAsync(ApiRequest request)` | Execute request asynchronously | `request` - Configured request | `CompletableFuture<ApiResponse>` |
| `executeAsync(ApiRequest request, Consumer<ApiResponse> callback)` | Execute request asynchronously with callback | `request` - Configured request, `callback` - Response handler | `void` |

#### Builder Class

| Method | Description | Parameters | Returns |
|--------|-------------|------------|---------|
| `connectTimeout(Duration timeout)` | Set connection timeout | `timeout` - Connection timeout | `Builder` |
| `readTimeout(Duration timeout)` | Set read timeout | `timeout` - Read timeout | `Builder` |
| `writeTimeout(Duration timeout)` | Set write timeout | `timeout` - Write timeout | `Builder` |
| `engine(HttpClientEngine engine)` | Set custom HTTP engine | `engine` - Custom engine | `Builder` |
| `executor(Executor executor)` | Set custom executor | `executor` - Custom executor | `Builder` |
| `addRequestInterceptor(RequestInterceptor interceptor)` | Add request interceptor | `interceptor` - Request interceptor | `Builder` |
| `addResponseInterceptor(ResponseInterceptor interceptor)` | Add response interceptor | `interceptor` - Response interceptor | `Builder` |
| `enableLogging()` | Enable logging | None | `Builder` |
| `build()` | Build ApiClient | None | `ApiClient` |

#### Example Usage

```java
// Create configured client
ApiClient client = new ApiClient.Builder()
    .connectTimeout(Duration.ofSeconds(10))
    .readTimeout(Duration.ofSeconds(30))
    .enableLogging()
    .addRequestInterceptor(RequestInterceptor.bearerAuth(() -> getToken()))
    .build();

// Use the client
ApiResponse response = client.get("https://api.example.com/data")
    .query("page", 1)
    .execute();

// Async usage
CompletableFuture<ApiResponse> future = client.get("https://api.example.com/data")
    .executeAsync();
```

### `RequestInterceptor` Interface (New in v1.1.0)

Interface for intercepting and modifying HTTP requests before they are sent.

#### Methods

| Method | Description | Parameters | Returns |
|--------|-------------|------------|---------|
| `intercept(ApiRequest request)` | Intercept and modify request | `request` - Original request | `ApiRequest` |

#### Static Factory Methods

| Method | Description | Parameters | Returns |
|--------|-------------|------------|---------|
| `logging(Consumer<String> logger)` | Create logging interceptor | `logger` - Logger function | `RequestInterceptor` |
| `bearerAuth(Supplier<String> tokenProvider)` | Create auth interceptor | `tokenProvider` - Token provider | `RequestInterceptor` |
| `addHeaders(Map<String, String> headers)` | Create header interceptor | `headers` - Headers to add | `RequestInterceptor` |

#### Example Usage

```java
// Authentication interceptor
RequestInterceptor authInterceptor = RequestInterceptor.bearerAuth(() -> getToken());

// Logging interceptor
RequestInterceptor loggingInterceptor = RequestInterceptor.logging(System.out::println);

// Custom interceptor
RequestInterceptor customInterceptor = request -> {
    System.out.println("Sending: " + request.getMethod() + " " + request.getUrl());
    return request.header("X-Request-ID", UUID.randomUUID().toString());
};
```

### `ResponseInterceptor` Interface (New in v1.1.0)

Interface for intercepting and modifying HTTP responses after they are received.

#### Methods

| Method | Description | Parameters | Returns |
|--------|-------------|------------|---------|
| `intercept(ApiResponse response)` | Intercept and modify response | `response` - Original response | `ApiResponse` |

#### Static Factory Methods

| Method | Description | Parameters | Returns |
|--------|-------------|------------|---------|
| `logging(Consumer<String> logger)` | Create logging interceptor | `logger` - Logger function | `ResponseInterceptor` |
| `throwOnError()` | Create error handling interceptor | None | `ResponseInterceptor` |
| `retryOnStatus(int[] codes, int maxRetries)` | Create retry interceptor | `codes` - Retryable status codes, `maxRetries` - Max retries | `ResponseInterceptor` |

#### Example Usage

```java
// Error handling interceptor
ResponseInterceptor errorInterceptor = ResponseInterceptor.throwOnError();

// Logging interceptor
ResponseInterceptor loggingInterceptor = ResponseInterceptor.logging(System.out::println);

// Custom interceptor
ResponseInterceptor customInterceptor = response -> {
    if (response.code() >= 400) {
        System.err.println("Error: " + response.code() + " - " + response.body());
    }
    return response;
};
```

### `Api` Class

Main entry point for creating HTTP requests.

#### Static Methods

| Method | Description | Parameters | Returns |
|--------|-------------|------------|---------|
| `get(String url)` | Create GET request | `url` - Target URL | `ApiRequest` |
| `post(String url)` | Create POST request | `url` - Target URL | `ApiRequest` |
| `put(String url)` | Create PUT request | `url` - Target URL | `ApiRequest` |
| `delete(String url)` | Create DELETE request | `url` - Target URL | `ApiRequest` |
| `patch(String url)` | Create PATCH request | `url` - Target URL | `ApiRequest` |
| `execute(ApiRequest request)` | Execute request synchronously | `request` - Configured request | `ApiResponse` |
| `executeAsync(ApiRequest request)` | Execute request asynchronously | `request` - Configured request | `CompletableFuture<ApiResponse>` |
| `executeAsync(ApiRequest request, Consumer<ApiResponse> callback)` | Execute request asynchronously with callback | `request` - Configured request, `callback` - Response handler | `void` |

#### Example Usage

```java
// Create requests
ApiRequest getRequest = Api.get("https://api.example.com/data");
ApiRequest postRequest = Api.post("https://api.example.com/users");

// Execute requests
ApiResponse response = Api.execute(getRequest);
Api.executeAsync(postRequest, response -> {
    System.out.println("Async response: " + response.body());
});
```

### `ApiRequest` Class

Fluent interface for building HTTP requests.

#### Methods

| Method | Description | Parameters | Returns |
|--------|-------------|------------|---------|
| `header(String name, String value)` | Add HTTP header | `name` - Header name, `value` - Header value | `ApiRequest` |
| `query(String name, Object value)` | Add query parameter | `name` - Parameter name, `value` - Parameter value | `ApiRequest` |
| `body(Object body)` | Set request body | `body` - Request body (String, Map, or Object) | `ApiRequest` |
| `execute()` | Execute request synchronously | None | `ApiResponse` |
| `executeAsync()` | Execute request asynchronously | None | `CompletableFuture<ApiResponse>` |
| `async(Consumer<ApiResponse> callback)` | Execute request asynchronously with callback | `callback` - Response handler | `void` |

#### Getters

| Method | Description | Returns |
|--------|-------------|---------|
| `getUrl()` | Get request URL | `String` |
| `getMethod()` | Get HTTP method | `String` |
| `getHeaders()` | Get request headers | `Map<String, String>` |
| `getQueryParams()` | Get query parameters | `Map<String, Object>` |
| `getBody()` | Get request body | `Object` |

#### Example Usage

```java
ApiRequest request = Api.get("https://api.example.com/search")
    .header("Authorization", "Bearer token123")
    .query("q", "java")
    .query("page", 1)
    .query("limit", 10)
    .body(searchData);

ApiResponse response = request.execute();
```

### `ApiResponse` Class

Container for HTTP response data with JSON parsing capabilities.

#### Methods

| Method | Description | Parameters | Returns |
|--------|-------------|------------|---------|
| `code()` | Get HTTP status code | None | `int` |
| `body()` | Get response body as string | None | `String` |
| `headers()` | Get response headers | None | `Map<String, String>` |
| `json()` | Get JSON mapper instance | None | `JsonMapper` |
| `to(Class<T> type)` | Parse JSON to specified type | `type` - Target class | `T` |
| `toMap()` | Parse JSON to Map | None | `Map<String, Object>` |
| `toList()` | Parse JSON to List | None | `List<Object>` |
| `isSuccess()` | Check if status is 200-299 | None | `boolean` |
| `isError()` | Check if status is 400+ | None | `boolean` |

#### Example Usage

```java
ApiResponse response = Api.get("https://api.example.com/users/1").execute();

// Basic response info
int statusCode = response.code();
String body = response.body();
Map<String, String> headers = response.headers();

// JSON parsing
User user = response.to(User.class);
Map<String, Object> data = response.toMap();
List<Object> items = response.toList();

// Status checking
if (response.isSuccess()) {
    System.out.println("Request successful");
} else if (response.isError()) {
    System.out.println("Request failed: " + response.code());
}
```

## JSON Mapping

### `JsonMapper` Interface

Interface for JSON serialization and deserialization.

#### Methods

| Method | Description | Parameters | Returns |
|--------|-------------|------------|---------|
| `stringify(Object obj)` | Convert object to JSON string | `obj` - Object to serialize | `String` |
| `parse(String json, Class<T> type)` | Parse JSON string to object | `json` - JSON string, `type` - Target class | `T` |
| `toMap(String json)` | Parse JSON string to Map | `json` - JSON string | `Map<String, Object>` |
| `toList(String json)` | Parse JSON string to List | `json` - JSON string | `List<Object>` |

#### Example Usage

```java
JsonMapper mapper = response.json();

// Serialize object to JSON
String json = mapper.stringify(user);

// Parse JSON to object
User user = mapper.parse(jsonString, User.class);

// Parse JSON to Map/List
Map<String, Object> map = mapper.toMap(jsonString);
List<Object> list = mapper.toList(jsonString);
```

### `JacksonJsonMapper` Class

Jackson-based implementation of JsonMapper.

#### Constructors

| Constructor | Description | Parameters |
|-------------|-------------|------------|
| `JacksonJsonMapper()` | Create with default ObjectMapper | None |
| `JacksonJsonMapper(ObjectMapper objectMapper)` | Create with custom ObjectMapper | `objectMapper` - Custom ObjectMapper |

#### Example Usage

```java
// Default mapper
JacksonJsonMapper mapper = new JacksonJsonMapper();

// Custom mapper
ObjectMapper customMapper = new ObjectMapper();
customMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
JacksonJsonMapper customJsonMapper = new JacksonJsonMapper(customMapper);
```

### `KotlinxJsonMapper` Class

Kotlinx Serialization-based implementation of JsonMapper.

#### Constructors

| Constructor | Description | Parameters |
|-------------|-------------|------------|
| `KotlinxJsonMapper()` | Create with default Json configuration | None |
| `KotlinxJsonMapper(Json json)` | Create with custom Json configuration | `json` - Custom Json instance |

#### Example Usage

```kotlin
// Default mapper
val mapper = KotlinxJsonMapper()

// Custom mapper
val customJson = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
}
val customMapper = KotlinxJsonMapper(customJson)
```

## HTTP Client Engine

### `HttpClientEngine` Interface

Interface for executing HTTP requests.

#### Methods

| Method | Description | Parameters | Returns |
|--------|-------------|------------|---------|
| `execute(ApiRequest request)` | Execute HTTP request | `request` - Configured request | `ApiResponse` |

#### Example Usage

```java
HttpClientEngine engine = new DefaultHttpClientEngine();
ApiResponse response = engine.execute(request);
```

### `DefaultHttpClientEngine` Class

Default implementation using Java 11+ HttpClient.

#### Constructors

| Constructor | Description | Parameters |
|-------------|-------------|------------|
| `DefaultHttpClientEngine()` | Create with default HttpClient and JsonMapper | None |
| `DefaultHttpClientEngine(HttpClient httpClient, JsonMapper jsonMapper)` | Create with custom HttpClient and JsonMapper | `httpClient` - Custom HttpClient, `jsonMapper` - Custom JsonMapper |

#### Features

- **Connection Timeout**: 30 seconds
- **Read Timeout**: 30 seconds
- **Automatic JSON Serialization**: For request bodies
- **UTF-8 Encoding**: For all text content
- **Header Management**: Automatic Content-Type setting

## Exception Classes

### `ApiException` Class

Exception for HTTP and network-related errors.

#### Constructors

| Constructor | Description | Parameters |
|-------------|-------------|------------|
| `ApiException(String message)` | Create with message | `message` - Error message |
| `ApiException(String message, Throwable cause)` | Create with message and cause | `message` - Error message, `cause` - Underlying cause |

#### Example Usage

```java
try {
    ApiResponse response = Api.get("https://api.example.com/data").execute();
} catch (ApiException e) {
    System.err.println("API Error: " + e.getMessage());
    if (e.getCause() != null) {
        System.err.println("Cause: " + e.getCause().getMessage());
    }
}
```

### `JsonException` Class

Exception for JSON serialization and deserialization errors.

#### Constructors

| Constructor | Description | Parameters |
|-------------|-------------|------------|
| `JsonException(String message)` | Create with message | `message` - Error message |
| `JsonException(String message, Throwable cause)` | Create with message and cause | `message` - Error message, `cause` - Underlying cause |

#### Example Usage

```java
try {
    User user = response.to(User.class);
} catch (JsonException e) {
    System.err.println("JSON Error: " + e.getMessage());
    if (e.getCause() != null) {
        System.err.println("Cause: " + e.getCause().getMessage());
    }
}
```

## Utility Classes

### `Utils` Class

Utility methods for HTTP operations.

#### Static Methods

| Method | Description | Parameters | Returns |
|--------|-------------|------------|---------|
| `inputStreamToString(InputStream inputStream)` | Convert InputStream to String | `inputStream` - Input stream | `String` |
| `buildQueryString(Map<String, Object> params)` | Build query string from parameters | `params` - Parameter map | `String` |
| `isEmpty(String str)` | Check if string is null or empty | `str` - String to check | `boolean` |
| `isNotEmpty(String str)` | Check if string is not null and not empty | `str` - String to check | `boolean` |
| `safeToString(Object obj)` | Safely convert object to string | `obj` - Object to convert | `String` |

#### Example Usage

```java
// Convert InputStream to String
String content = Utils.inputStreamToString(inputStream);

// Build query string
Map<String, Object> params = Map.of("page", 1, "limit", 10);
String queryString = Utils.buildQueryString(params);

// String utilities
if (Utils.isNotEmpty(userInput)) {
    System.out.println("User input: " + userInput);
}
```

## Sample JSON Responses

### User Object

```json
{
  "id": 1,
  "name": "Leanne Graham",
  "username": "Bret",
  "email": "Sincere@april.biz",
  "phone": "1-770-736-8031 x56442",
  "website": "hildegard.org",
  "address": {
    "street": "Kulas Light",
    "suite": "Apt. 556",
    "city": "Gwenborough",
    "zipcode": "92998-3874",
    "geo": {
      "lat": "-37.3159",
      "lng": "81.1496"
    }
  },
  "company": {
    "name": "Romaguera-Crona",
    "catchPhrase": "Multi-layered client-server neural-net",
    "bs": "harness real-time e-markets"
  }
}
```

### Post Object

```json
{
  "userId": 1,
  "id": 1,
  "title": "sunt aut facere repellat provident occaecati excepturi optio reprehenderit",
  "body": "quia et suscipit\nsuscipit recusandae consequuntur expedita et cum\nreprehenderit molestiae ut ut quas totam\nnostrum rerum est autem sunt rem eveniet architecto"
}
```

### Posts Array

```json
[
  {
    "userId": 1,
    "id": 1,
    "title": "sunt aut facere repellat provident occaecati excepturi optio reprehenderit",
    "body": "quia et suscipit\nsuscipit recusandae consequuntur expedita et cum\nreprehenderit molestiae ut ut quas totam\nnostrum rerum est autem sunt rem eveniet architecto"
  },
  {
    "userId": 1,
    "id": 2,
    "title": "qui est esse",
    "body": "est rerum tempore vitae\nsequi sint nihil reprehenderit dolor beatae ea dolores neque\nfugiat blanditiis voluptate porro vel nihil molestiae ut reiciendis\nqui aperiam non debitis possimus qui neque nisi nulla"
  }
]
```

## Next Steps

- **[API Overview](/MochaJSON/api/overview)** - High-level API concepts
- **[Exceptions](/MochaJSON/api/exceptions)** - Detailed error handling guide
- **[Java Examples](/MochaJSON/usage/java-examples)** - Complete Java usage examples
- **[Kotlin Examples](/MochaJSON/usage/kotlin-examples)** - Complete Kotlin usage examples
