# Exceptions

Comprehensive guide to error handling and exception management in MochaAPI Client.

## Exception Hierarchy

MochaAPI Client provides a clear exception hierarchy for different types of errors:

```
RuntimeException
├── ApiException          # HTTP/Network errors
└── JsonException         # JSON parsing errors
```

## Exception Types

### `ApiException`

Thrown for HTTP-related errors, network issues, and request failures.

#### When Thrown

- **Network Errors**: Connection timeouts, DNS failures, connection refused
- **HTTP Errors**: Server errors (500, 503), client errors (400, 401, 403, 404)
- **Request Failures**: Malformed URLs, invalid request parameters
- **Interruption**: Thread interruption during request execution

#### Constructor Signatures

```java
public ApiException(String message)
public ApiException(String message, Throwable cause)
```

#### Example Usage

```java
try {
    ApiResponse response = Api.get("https://api.example.com/data").execute();
} catch (ApiException e) {
    System.err.println("API Error: " + e.getMessage());
    if (e.getCause() != null) {
        System.err.println("Root Cause: " + e.getCause().getMessage());
    }
}
```

### `JsonException`

Thrown for JSON serialization and deserialization errors.

#### When Thrown

- **Serialization Errors**: Object → JSON conversion failures
- **Deserialization Errors**: JSON → Object conversion failures
- **Type Mismatches**: JSON structure doesn't match expected POJO
- **Malformed JSON**: Invalid JSON syntax
- **Missing Fields**: Required fields not present in JSON

#### Constructor Signatures

```java
public JsonException(String message)
public JsonException(String message, Throwable cause)
```

#### Example Usage

```java
try {
    User user = response.to(User.class);
} catch (JsonException e) {
    System.err.println("JSON Error: " + e.getMessage());
    if (e.getCause() != null) {
        System.err.println("Root Cause: " + e.getCause().getMessage());
    }
}
```

## Error Scenarios

### Network Errors

```java
try {
    // This will fail if the server is unreachable
    ApiResponse response = Api.get("https://unreachable-server.com/data").execute();
} catch (ApiException e) {
    // Handle network errors
    System.err.println("Network Error: " + e.getMessage());
    // Output: "Network Error: Connection refused"
}
```

### HTTP Status Errors

```java
try {
    // This will succeed but return an error status
    ApiResponse response = Api.get("https://api.example.com/nonexistent").execute();
    
    if (response.isError()) {
        System.err.println("HTTP Error: " + response.code());
        System.err.println("Error Body: " + response.body());
    }
} catch (ApiException e) {
    // This catches network/connection errors
    System.err.println("Request Failed: " + e.getMessage());
}
```

### JSON Parsing Errors

```java
try {
    // This will fail if JSON doesn't match User structure
    User user = Api.get("https://api.example.com/invalid-json").execute()
        .to(User.class);
} catch (JsonException e) {
    // Handle JSON parsing errors
    System.err.println("JSON Parsing Error: " + e.getMessage());
    // Output: "JSON Parsing Error: Failed to parse JSON to User: Unrecognized field 'unknownField'"
}
```

### Type Conversion Errors

```java
try {
    // This will fail if JSON contains wrong data types
    Map<String, Object> data = Api.get("https://api.example.com/data").execute()
        .toMap();
    
    // Safe type conversion
    String name = (String) data.getOrDefault("name", "Unknown");
    Integer age = (Integer) data.getOrDefault("age", 0);
    
} catch (JsonException e) {
    System.err.println("Type Conversion Error: " + e.getMessage());
}
```

## Comprehensive Error Handling

### Complete Error Handling Pattern

```java
import io.mochaapi.client.*;
import io.mochaapi.client.exception.*;

public class ErrorHandlingExample {
    public static void main(String[] args) {
        try {
            ApiResponse response = Api.get("https://jsonplaceholder.typicode.com/users/1")
                .execute();
            
            // Check HTTP status
            if (response.isError()) {
                handleHttpError(response);
                return;
            }
            
            // Parse JSON safely
            User user = response.to(User.class);
            System.out.println("Success: " + user.name);
            
        } catch (ApiException e) {
            handleApiError(e);
        } catch (JsonException e) {
            handleJsonError(e);
        } catch (Exception e) {
            handleUnexpectedError(e);
        }
    }
    
    private static void handleHttpError(ApiResponse response) {
        System.err.println("HTTP Error: " + response.code());
        System.err.println("Error Response: " + response.body());
        
        switch (response.code()) {
            case 400:
                System.err.println("Bad Request - Check your request parameters");
                break;
            case 401:
                System.err.println("Unauthorized - Check your authentication");
                break;
            case 403:
                System.err.println("Forbidden - You don't have permission");
                break;
            case 404:
                System.err.println("Not Found - Resource doesn't exist");
                break;
            case 500:
                System.err.println("Server Error - Try again later");
                break;
            default:
                System.err.println("Unknown HTTP error");
        }
    }
    
    private static void handleApiError(ApiException e) {
        System.err.println("API Error: " + e.getMessage());
        
        if (e.getCause() != null) {
            System.err.println("Root Cause: " + e.getCause().getMessage());
            
            // Handle specific network errors
            if (e.getCause() instanceof java.net.ConnectException) {
                System.err.println("Connection failed - Check your internet connection");
            } else if (e.getCause() instanceof java.net.SocketTimeoutException) {
                System.err.println("Request timeout - Server is slow or unreachable");
            }
        }
    }
    
    private static void handleJsonError(JsonException e) {
        System.err.println("JSON Error: " + e.getMessage());
        
        if (e.getCause() != null) {
            System.err.println("Root Cause: " + e.getCause().getMessage());
            
            // Handle specific JSON errors
            if (e.getCause() instanceof com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException) {
                System.err.println("Unknown field in JSON - Check your POJO structure");
            } else if (e.getCause() instanceof com.fasterxml.jackson.core.JsonParseException) {
                System.err.println("Invalid JSON format - Check server response");
            }
        }
    }
    
    private static void handleUnexpectedError(Exception e) {
        System.err.println("Unexpected Error: " + e.getMessage());
        e.printStackTrace();
    }
    
    public static class User {
        public int id;
        public String name;
        public String email;
    }
}
```

### Kotlin Error Handling

```kotlin
import io.mochaapi.client.*
import io.mochaapi.client.exception.*

fun main() {
    try {
        val response = Api.get("https://jsonplaceholder.typicode.com/users/1")
            .execute()
        
        if (response.isError()) {
            handleHttpError(response)
            return
        }
        
        val user = response.to(User::class.java)
        println("Success: ${user.name}")
        
    } catch (e: ApiException) {
        handleApiError(e)
    } catch (e: JsonException) {
        handleJsonError(e)
    } catch (e: Exception) {
        handleUnexpectedError(e)
    }
}

private fun handleHttpError(response: ApiResponse) {
    println("HTTP Error: ${response.code()}")
    println("Error Response: ${response.body()}")
}

private fun handleApiError(e: ApiException) {
    println("API Error: ${e.message}")
    e.cause?.let { cause ->
        println("Root Cause: ${cause.message}")
    }
}

private fun handleJsonError(e: JsonException) {
    println("JSON Error: ${e.message}")
    e.cause?.let { cause ->
        println("Root Cause: ${cause.message}")
    }
}

private fun handleUnexpectedError(e: Exception) {
    println("Unexpected Error: ${e.message}")
    e.printStackTrace()
}

data class User(
    val id: Int,
    val name: String,
    val email: String
)
```

## Error Recovery Strategies

### Retry Logic

```java
public class RetryExample {
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 1000;
    
    public static ApiResponse executeWithRetry(String url) {
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                ApiResponse response = Api.get(url).execute();
                
                if (response.isSuccess()) {
                    return response;
                } else if (response.code() >= 500) {
                    // Server error - retry
                    System.out.println("Server error, retrying... (attempt " + attempt + ")");
                    if (attempt < MAX_RETRIES) {
                        Thread.sleep(RETRY_DELAY_MS);
                        continue;
                    }
                }
                
                return response; // Client error - don't retry
                
            } catch (ApiException e) {
                System.out.println("Network error, retrying... (attempt " + attempt + ")");
                if (attempt < MAX_RETRIES) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new ApiException("Retry interrupted", ie);
                    }
                } else {
                    throw e;
                }
            }
        }
        
        throw new ApiException("Max retries exceeded");
    }
}
```

### Fallback Data

```java
public class FallbackExample {
    public static User getUserWithFallback(int userId) {
        try {
            // Try to get user from API
            return Api.get("https://api.example.com/users/" + userId)
                .execute()
                .to(User.class);
                
        } catch (ApiException e) {
            System.err.println("API failed, using fallback data: " + e.getMessage());
            
            // Return fallback user
            User fallbackUser = new User();
            fallbackUser.id = userId;
            fallbackUser.name = "Unknown User";
            fallbackUser.email = "unknown@example.com";
            return fallbackUser;
            
        } catch (JsonException e) {
            System.err.println("JSON parsing failed: " + e.getMessage());
            throw e; // Re-throw JSON errors as they're usually programming errors
        }
    }
}
```

### Graceful Degradation

```java
public class GracefulDegradationExample {
    public static void processUserData(int userId) {
        try {
            ApiResponse response = Api.get("https://api.example.com/users/" + userId)
                .execute();
            
            if (response.isError()) {
                handleHttpError(response);
                return;
            }
            
            // Try to parse as User POJO first
            try {
                User user = response.to(User.class);
                processUser(user);
                
            } catch (JsonException e) {
                System.err.println("POJO parsing failed, falling back to Map: " + e.getMessage());
                
                // Fallback to Map parsing
                Map<String, Object> userData = response.toMap();
                processUserMap(userData);
            }
            
        } catch (ApiException e) {
            System.err.println("Request failed: " + e.getMessage());
            // Continue with default behavior or show error message
        }
    }
    
    private static void processUser(User user) {
        System.out.println("Processing user: " + user.name);
    }
    
    private static void processUserMap(Map<String, Object> userData) {
        String name = (String) userData.getOrDefault("name", "Unknown");
        System.out.println("Processing user map: " + name);
    }
}
```

## Error Logging

### Structured Logging

```java
import java.util.logging.Logger;
import java.util.logging.Level;

public class LoggingExample {
    private static final Logger logger = Logger.getLogger(LoggingExample.class.getName());
    
    public static void makeApiCall(String url) {
        try {
            ApiResponse response = Api.get(url).execute();
            
            if (response.isError()) {
                logger.log(Level.WARNING, "HTTP error: {0} - {1}", 
                    new Object[]{response.code(), response.body()});
            } else {
                logger.log(Level.INFO, "API call successful: {0}", url);
            }
            
        } catch (ApiException e) {
            logger.log(Level.SEVERE, "API error for URL {0}: {1}", 
                new Object[]{url, e.getMessage()});
            
        } catch (JsonException e) {
            logger.log(Level.SEVERE, "JSON parsing error for URL {0}: {1}", 
                new Object[]{url, e.getMessage()});
        }
    }
}
```

## Best Practices

### 1. Always Handle Exceptions

```java
// ✅ Good: Comprehensive error handling
try {
    User user = Api.get(url).execute().to(User.class);
    processUser(user);
} catch (ApiException e) {
    handleNetworkError(e);
} catch (JsonException e) {
    handleJsonError(e);
}

// ❌ Avoid: Ignoring exceptions
User user = Api.get(url).execute().to(User.class); // Can throw exceptions
```

### 2. Check HTTP Status

```java
// ✅ Good: Check status before parsing
ApiResponse response = Api.get(url).execute();
if (response.isSuccess()) {
    User user = response.to(User.class);
    processUser(user);
} else {
    handleHttpError(response);
}

// ❌ Avoid: Parsing without status check
User user = Api.get(url).execute().to(User.class); // Might parse error response
```

### 3. Provide Meaningful Error Messages

```java
// ✅ Good: Descriptive error messages
catch (ApiException e) {
    System.err.println("Failed to connect to API server: " + e.getMessage());
    // Suggest solutions
    System.err.println("Please check your internet connection and try again.");
}

// ❌ Avoid: Generic error messages
catch (ApiException e) {
    System.err.println("Error: " + e.getMessage());
}
```

### 4. Log Errors Appropriately

```java
// ✅ Good: Appropriate log levels
logger.log(Level.SEVERE, "Critical API failure: " + e.getMessage());
logger.log(Level.WARNING, "API returned error status: " + response.code());
logger.log(Level.INFO, "API call completed successfully");

// ❌ Avoid: Wrong log levels
logger.log(Level.SEVERE, "User not found"); // Should be WARNING or INFO
```

## Next Steps

- **[API Reference](/MochaJSON/api/api-reference)** - Complete method documentation
- **[Java Examples](/MochaJSON/usage/java-examples)** - Complete Java usage examples
- **[Kotlin Examples](/MochaJSON/usage/kotlin-examples)** - Complete Kotlin usage examples
- **[JSON Handling](/MochaJSON/usage/json-handling)** - Advanced JSON parsing techniques
