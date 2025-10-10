---
title: Migrating from OkHttp + Gson
description: Complete migration guide from OkHttp + Gson to MochaJSON. Learn how to convert your existing HTTP client code with step-by-step examples and best practices.
keywords:
  - migrate from OkHttp
  - migrate from Gson
  - OkHttp to MochaJSON
  - Gson to MochaJSON
  - HTTP client migration
---

# Migrating from OkHttp + Gson to MochaJSON

This guide will help you migrate your existing OkHttp + Gson code to MochaJSON, reducing complexity while gaining new features like connection pooling, retry mechanisms, and circuit breakers.

## Why Migrate?

### Before (OkHttp + Gson)
- **2+ dependencies** to manage
- **20+ lines** of boilerplate per request
- **Manual JSON parsing** with Gson
- **Complex async setup** with callbacks
- **No built-in features** like retry or circuit breaker

### After (MochaJSON)
- **1 dependency** - everything included
- **1-3 lines** per request
- **Automatic JSON parsing**
- **Built-in async support** with CompletableFuture
- **Production features** like connection pooling, retry, circuit breaker

## Migration Steps

### Step 1: Update Dependencies

**Before (OkHttp + Gson):**
```gradle
dependencies {
    implementation 'com.squareup.okhttp3:okhttp:4.12.0'
    implementation 'com.google.code.gson:gson:2.10.1'
    implementation 'com.squareup.okhttp3:logging-interceptor:4.12.0'
}
```

**After (MochaJSON):**
```gradle
dependencies {
    implementation 'io.github.guptavishal-xm1:MochaJSON:1.2.0'
    
    // Optional: For logging support
    implementation 'org.slf4j:slf4j-api:2.0.9'
}
```

### Step 2: Client Setup Migration

**Before (OkHttp + Gson):**
```java
public class ApiService {
    private final OkHttpClient client;
    private final Gson gson;
    
    public ApiService() {
        this.client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .connectionPool(new ConnectionPool(5, 5, TimeUnit.MINUTES))
            .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .build();
            
        this.gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
            .create();
    }
}
```

**After (MochaJSON):**
```java
public class ApiService {
    private final ApiClient client;
    
    public ApiService() {
        this.client = new ApiClient.Builder()
            .connectTimeout(Duration.ofSeconds(30))
            .readTimeout(Duration.ofSeconds(30))
            .writeTimeout(Duration.ofSeconds(30))
            .enableConnectionPooling()        // 🆕 Better than OkHttp's connection pool
            .enableRetryPolicy()              // 🆕 Built-in retry mechanism
            .enableLogging()                  // 🆕 Built-in logging
            .build();
    }
}
```

### Step 3: Basic GET Request Migration

**Before (OkHttp + Gson):**
```java
public User getUser(String id) throws IOException {
    Request request = new Request.Builder()
        .url("https://api.example.com/users/" + id)
        .addHeader("Authorization", "Bearer " + getToken())
        .addHeader("Accept", "application/json")
        .build();
        
    try (Response response = client.newCall(request).execute()) {
        if (!response.isSuccessful()) {
            throw new IOException("Unexpected code " + response);
        }
        
        String json = response.body().string();
        return gson.fromJson(json, User.class);
    }
}
```

**After (MochaJSON):**
```java
public User getUser(String id) {
    return client.get("https://api.example.com/users/" + id)
        .header("Authorization", "Bearer " + getToken())
        .execute()
        .to(User.class);
}
```

### Step 4: POST Request Migration

**Before (OkHttp + Gson):**
```java
public User createUser(User user) throws IOException {
    String json = gson.toJson(user);
    RequestBody body = RequestBody.create(json, MediaType.get("application/json"));
    
    Request request = new Request.Builder()
        .url("https://api.example.com/users")
        .post(body)
        .addHeader("Authorization", "Bearer " + getToken())
        .addHeader("Content-Type", "application/json")
        .build();
        
    try (Response response = client.newCall(request).execute()) {
        if (!response.isSuccessful()) {
            throw new IOException("Unexpected code " + response);
        }
        
        String responseJson = response.body().string();
        return gson.fromJson(responseJson, User.class);
    }
}
```

**After (MochaJSON):**
```java
public User createUser(User user) {
    return client.post("https://api.example.com/users")
        .header("Authorization", "Bearer " + getToken())
        .body(user)  // Automatic JSON serialization
        .execute()
        .to(User.class);
}
```

### Step 5: Async Request Migration

**Before (OkHttp + Gson):**
```java
public void getUserAsync(String id, Callback<User> callback) {
    Request request = new Request.Builder()
        .url("https://api.example.com/users/" + id)
        .addHeader("Authorization", "Bearer " + getToken())
        .build();
        
    client.newCall(request).enqueue(new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            callback.onError(e);
        }
        
        @Override
        public void onResponse(Call call, Response response) throws IOException {
            try {
                if (!response.isSuccessful()) {
                    callback.onError(new IOException("Unexpected code " + response));
                    return;
                }
                
                String json = response.body().string();
                User user = gson.fromJson(json, User.class);
                callback.onSuccess(user);
                
            } catch (Exception e) {
                callback.onError(e);
            } finally {
                response.close();
            }
        }
    });
}
```

**After (MochaJSON):**
```java
public CompletableFuture<User> getUserAsync(String id) {
    return client.get("https://api.example.com/users/" + id)
        .header("Authorization", "Bearer " + getToken())
        .executeAsync()
        .thenApply(response -> response.to(User.class));
}

// Or with callback-style (backward compatible)
public void getUserAsync(String id, Callback<User> callback) {
    client.get("https://api.example.com/users/" + id)
        .header("Authorization", "Bearer " + getToken())
        .async(response -> {
            try {
                User user = response.to(User.class);
                callback.onSuccess(user);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
}
```

### Step 6: Error Handling Migration

**Before (OkHttp + Gson):**
```java
public User getUserWithErrorHandling(String id) {
    try {
        Request request = new Request.Builder()
            .url("https://api.example.com/users/" + id)
            .addHeader("Authorization", "Bearer " + getToken())
            .build();
            
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                if (response.code() == 404) {
                    throw new UserNotFoundException("User not found: " + id);
                } else if (response.code() == 401) {
                    throw new UnauthorizedException("Authentication required");
                } else {
                    throw new ApiException("API error: " + response.code());
                }
            }
            
            String json = response.body().string();
            return gson.fromJson(json, User.class);
            
        }
    } catch (IOException e) {
        throw new ApiException("Network error", e);
    } catch (JsonSyntaxException e) {
        throw new ApiException("Invalid JSON response", e);
    }
}
```

**After (MochaJSON):**
```java
public User getUserWithErrorHandling(String id) {
    try {
        return client.get("https://api.example.com/users/" + id)
            .header("Authorization", "Bearer " + getToken())
            .execute()
            .to(User.class);
            
    } catch (ApiException e) {
        switch (e.getStatusCode()) {
            case 404:
                throw new UserNotFoundException("User not found: " + id);
            case 401:
                throw new UnauthorizedException("Authentication required");
            default:
                throw new ApiException("API error: " + e.getStatusCode());
        }
    } catch (JsonException e) {
        throw new ApiException("Invalid JSON response", e);
    }
}
```

### Step 7: Interceptor Migration

**Before (OkHttp + Gson):**
```java
public class AuthInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();
        Request request = original.newBuilder()
            .header("Authorization", "Bearer " + getToken())
            .build();
        return chain.proceed(request);
    }
}

public class LoggingInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        long startTime = System.nanoTime();
        
        Response response = chain.proceed(request);
        long endTime = System.nanoTime();
        
        logger.info("{} {} - {}ms", 
            request.method(), 
            request.url(), 
            (endTime - startTime) / 1_000_000);
            
        return response;
    }
}

// Usage
OkHttpClient client = new OkHttpClient.Builder()
    .addInterceptor(new AuthInterceptor())
    .addInterceptor(new LoggingInterceptor())
    .build();
```

**After (MochaJSON):**
```java
// Built-in authentication interceptor
RequestInterceptor authInterceptor = RequestInterceptor.bearerAuth(() -> getToken());

// Custom logging interceptor
RequestInterceptor loggingInterceptor = request -> {
    logger.info("Request: {} {}", request.getMethod(), request.getUrl());
    return request;
};

ResponseInterceptor responseLoggingInterceptor = response -> {
    logger.info("Response: {} - {}ms", response.code(), response.getDuration().toMillis());
    return response;
};

// Usage
ApiClient client = new ApiClient.Builder()
    .addRequestInterceptor(authInterceptor)
    .addRequestInterceptor(loggingInterceptor)
    .addResponseInterceptor(responseLoggingInterceptor)
    .build();
```

### Step 8: File Upload Migration

**Before (OkHttp + Gson):**
```java
public String uploadFile(File file) throws IOException {
    RequestBody fileBody = RequestBody.create(file, MediaType.get("application/octet-stream"));
    RequestBody requestBody = new MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart("file", file.getName(), fileBody)
        .addFormDataPart("description", "File upload")
        .build();
        
    Request request = new Request.Builder()
        .url("https://api.example.com/upload")
        .post(requestBody)
        .addHeader("Authorization", "Bearer " + getToken())
        .build();
        
    try (Response response = client.newCall(request).execute()) {
        if (!response.isSuccessful()) {
            throw new IOException("Upload failed: " + response.code());
        }
        
        String json = response.body().string();
        JsonObject result = gson.fromJson(json, JsonObject.class);
        return result.get("fileId").getAsString();
    }
}
```

**After (MochaJSON):**
```java
public String uploadFile(File file) {
    ApiResponse response = client.post("https://api.example.com/upload")
        .header("Authorization", "Bearer " + getToken())
        .multipart()
        .addFile("file", file)
        .addField("description", "File upload")
        .execute();
        
    return response.toMap().get("fileId").toString();
}
```

## Complete Migration Example

### Before: Complete OkHttp + Gson Service

```java
public class UserService {
    private final OkHttpClient client;
    private final Gson gson;
    
    public UserService() {
        this.client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .connectionPool(new ConnectionPool(5, 5, TimeUnit.MINUTES))
            .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .build();
            
        this.gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
            .create();
    }
    
    public User getUser(String id) throws IOException {
        Request request = new Request.Builder()
            .url("https://api.example.com/users/" + id)
            .addHeader("Authorization", "Bearer " + getToken())
            .build();
            
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            
            String json = response.body().string();
            return gson.fromJson(json, User.class);
        }
    }
    
    public List<User> getUsers() throws IOException {
        Request request = new Request.Builder()
            .url("https://api.example.com/users")
            .addHeader("Authorization", "Bearer " + getToken())
            .build();
            
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            
            String json = response.body().string();
            Type userListType = new TypeToken<List<User>>(){}.getType();
            return gson.fromJson(json, userListType);
        }
    }
    
    public User createUser(User user) throws IOException {
        String json = gson.toJson(user);
        RequestBody body = RequestBody.create(json, MediaType.get("application/json"));
        
        Request request = new Request.Builder()
            .url("https://api.example.com/users")
            .post(body)
            .addHeader("Authorization", "Bearer " + getToken())
            .addHeader("Content-Type", "application/json")
            .build();
            
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            
            String responseJson = response.body().string();
            return gson.fromJson(responseJson, User.class);
        }
    }
    
    private String getToken() {
        // Token retrieval logic
        return "your-token-here";
    }
}
```

### After: Complete MochaJSON Service

```java
public class UserService {
    private final ApiClient client;
    
    public UserService() {
        this.client = new ApiClient.Builder()
            .connectTimeout(Duration.ofSeconds(30))
            .readTimeout(Duration.ofSeconds(30))
            .enableConnectionPooling()        // 🆕 Better connection management
            .enableRetryPolicy()              // 🆕 Automatic retries
            .enableCircuitBreaker()           // 🆕 Fault tolerance
            .enableCaching()                  // 🆕 HTTP caching
            .addRequestInterceptor(RequestInterceptor.bearerAuth(() -> getToken()))
            .enableLogging()                  // 🆕 Built-in logging
            .build();
    }
    
    public User getUser(String id) {
        return client.get("https://api.example.com/users/" + id)
            .execute()
            .to(User.class);
    }
    
    public List<User> getUsers() {
        return client.get("https://api.example.com/users")
            .execute()
            .toList();
    }
    
    public User createUser(User user) {
        return client.post("https://api.example.com/users")
            .body(user)  // Automatic JSON serialization
            .execute()
            .to(User.class);
    }
    
    private String getToken() {
        // Token retrieval logic
        return "your-token-here";
    }
}
```

## Migration Benefits

### Code Reduction
- **90% less code** - From 20+ lines to 1-3 lines per request
- **No more Gson setup** - Automatic JSON parsing
- **No more manual error handling** - Built-in exception mapping
- **No more complex async setup** - Built-in CompletableFuture support

### New Features
- **Connection pooling** - Better performance than OkHttp's connection pool
- **Retry mechanism** - Automatic retries with exponential backoff
- **Circuit breaker** - Fault tolerance for downstream services
- **HTTP caching** - LRU cache with TTL support
- **Virtual threads** - Java 21+ support for massive concurrency
- **File operations** - Simple multipart upload/download

### Performance Improvements
- **73% faster requests** with connection pooling
- **99.8% faster** for cached requests
- **90% faster** with async operations
- **10x more concurrent requests** with virtual threads

## Migration Checklist

- [ ] **Update dependencies** - Replace OkHttp + Gson with MochaJSON
- [ ] **Migrate client setup** - Replace OkHttpClient with ApiClient
- [ ] **Convert GET requests** - Simplify request building
- [ ] **Convert POST requests** - Use automatic JSON serialization
- [ ] **Migrate async calls** - Use CompletableFuture or callbacks
- [ ] **Update error handling** - Use MochaJSON exceptions
- [ ] **Migrate interceptors** - Convert to MochaJSON interceptors
- [ ] **Test thoroughly** - Ensure all functionality works
- [ ] **Enable new features** - Add connection pooling, retry, circuit breaker
- [ ] **Update documentation** - Reflect new API usage

## Next Steps

- **[Getting Started](/MochaJSON/getting-started)** - Learn MochaJSON basics
- **[Best Practices](/MochaJSON/best-practices/production-checklist)** - Production deployment guide
- **[Performance Tips](/MochaJSON/best-practices/performance-tips)** - Optimize your HTTP client
- **[Advanced Features](/MochaJSON/advanced/interceptors)** - Learn about advanced features
