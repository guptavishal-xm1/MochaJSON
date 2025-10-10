# Error Handling Examples

Comprehensive examples showing how to handle different HTTP status codes and errors with MochaAPI Client.

## Basic Status Code Checking

```java
import io.mochaapi.client.*;

public class ErrorHandlingExample {
    public static void main(String[] args) {
        ApiResponse response = Api.get("https://api.example.com/users/1").execute();
        
        // Check specific status codes
        if (response.isOk()) {
            System.out.println("✅ Request successful (200)");
        } else if (response.isCreated()) {
            System.out.println("✅ Resource created (201)");
        } else if (response.isNotFound()) {
            System.out.println("❌ Resource not found (404)");
        } else if (response.isUnauthorized()) {
            System.out.println("❌ Unauthorized access (401)");
        } else if (response.isInternalServerError()) {
            System.out.println("❌ Server error (500)");
        }
        
        // Get human-readable status description
        System.out.println("Status: " + response.code() + " - " + response.getStatusDescription());
    }
}
```

## Comprehensive Error Handling

```java
import io.mochaapi.client.*;
import io.mochaapi.client.exception.*;

public class ComprehensiveErrorHandling {
    public static void main(String[] args) {
        try {
            ApiResponse response = Api.get("https://api.example.com/users/1").execute();
            
            // Handle different response categories
            if (response.isSuccess()) {
                handleSuccess(response);
            } else if (response.isClientError()) {
                handleClientError(response);
            } else if (response.isServerError()) {
                handleServerError(response);
            } else if (response.isRedirect()) {
                handleRedirect(response);
            }
            
        } catch (ApiException e) {
            System.err.println("Network/HTTP Error: " + e.getMessage());
        } catch (JsonException e) {
            System.err.println("JSON Parsing Error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected Error: " + e.getMessage());
        }
    }
    
    private static void handleSuccess(ApiResponse response) {
        if (response.isOk()) {
            System.out.println("✅ Data retrieved successfully");
            User user = response.to(User.class);
            System.out.println("User: " + user.getName());
        } else if (response.isCreated()) {
            System.out.println("✅ Resource created successfully");
            User newUser = response.to(User.class);
            System.out.println("Created user ID: " + newUser.getId());
        } else if (response.isNoContent()) {
            System.out.println("✅ Operation completed (no content returned)");
        }
    }
    
    private static void handleClientError(ApiResponse response) {
        if (response.isBadRequest()) {
            System.err.println("❌ Bad Request (400): " + response.body());
        } else if (response.isUnauthorized()) {
            System.err.println("❌ Unauthorized (401): Check your authentication");
        } else if (response.isForbidden()) {
            System.err.println("❌ Forbidden (403): You don't have permission");
        } else if (response.isNotFound()) {
            System.err.println("❌ Not Found (404): Resource doesn't exist");
        } else if (response.isUnprocessableEntity()) {
            System.err.println("❌ Unprocessable Entity (422): " + response.body());
        } else if (response.isTooManyRequests()) {
            System.err.println("❌ Rate Limited (429): Too many requests");
        } else {
            System.err.println("❌ Client Error (" + response.code() + "): " + response.getStatusDescription());
        }
    }
    
    private static void handleServerError(ApiResponse response) {
        if (response.isInternalServerError()) {
            System.err.println("❌ Internal Server Error (500): Server is having issues");
        } else if (response.isBadGateway()) {
            System.err.println("❌ Bad Gateway (502): Upstream server error");
        } else if (response.isServiceUnavailable()) {
            System.err.println("❌ Service Unavailable (503): Server is down for maintenance");
        } else if (response.isGatewayTimeout()) {
            System.err.println("❌ Gateway Timeout (504): Request timed out");
        } else {
            System.err.println("❌ Server Error (" + response.code() + "): " + response.getStatusDescription());
        }
    }
    
    private static void handleRedirect(ApiResponse response) {
        System.out.println("🔄 Redirect (" + response.code() + "): " + response.getStatusDescription());
        // Handle redirect logic if needed
    }
}
```

## REST API Error Handling Patterns

### GET Request with Error Handling

```java
public class UserService {
    public User getUserById(int userId) {
        try {
            ApiResponse response = Api.get("https://api.example.com/users/" + userId)
                .header("Authorization", "Bearer " + getAuthToken())
                .execute();
            
            if (response.isOk()) {
                return response.to(User.class);
            } else if (response.isNotFound()) {
                throw new UserNotFoundException("User with ID " + userId + " not found");
            } else if (response.isUnauthorized()) {
                throw new AuthenticationException("Invalid authentication token");
            } else if (response.isForbidden()) {
                throw new AuthorizationException("You don't have permission to access this user");
            } else {
                throw new ApiException("Unexpected error: " + response.code() + " - " + response.getStatusDescription());
            }
            
        } catch (ApiException e) {
            throw new ServiceException("Failed to fetch user", e);
        } catch (JsonException e) {
            throw new ServiceException("Failed to parse user data", e);
        }
    }
}
```

### POST Request with Validation Error Handling

```java
public class UserService {
    public User createUser(User user) {
        try {
            ApiResponse response = Api.post("https://api.example.com/users")
                .header("Authorization", "Bearer " + getAuthToken())
                .body(user)
                .execute();
            
            if (response.isCreated()) {
                return response.to(User.class);
            } else if (response.isBadRequest()) {
                // Parse validation errors from response body
                Map<String, Object> errorData = response.toMap();
                throw new ValidationException("Validation failed: " + errorData.get("message"));
            } else if (response.isUnprocessableEntity()) {
                Map<String, Object> errorData = response.toMap();
                throw new ValidationException("Unprocessable entity: " + errorData.get("errors"));
            } else if (response.isConflict()) {
                throw new ConflictException("User already exists");
            } else {
                throw new ApiException("Unexpected error: " + response.code() + " - " + response.getStatusDescription());
            }
            
        } catch (ApiException e) {
            throw new ServiceException("Failed to create user", e);
        } catch (JsonException e) {
            throw new ServiceException("Failed to parse response", e);
        }
    }
}
```

### DELETE Request with Error Handling

```java
public class UserService {
    public boolean deleteUser(int userId) {
        try {
            ApiResponse response = Api.delete("https://api.example.com/users/" + userId)
                .header("Authorization", "Bearer " + getAuthToken())
                .execute();
            
            if (response.isOk() || response.isNoContent()) {
                return true;
            } else if (response.isNotFound()) {
                return false; // User doesn't exist, consider it deleted
            } else if (response.isUnauthorized()) {
                throw new AuthenticationException("Invalid authentication token");
            } else if (response.isForbidden()) {
                throw new AuthorizationException("You don't have permission to delete this user");
            } else {
                throw new ApiException("Unexpected error: " + response.code() + " - " + response.getStatusDescription());
            }
            
        } catch (ApiException e) {
            throw new ServiceException("Failed to delete user", e);
        }
    }
}
```

## Async Error Handling

```java
public class AsyncErrorHandling {
    public static void main(String[] args) {
        Api.get("https://api.example.com/users/1")
            .async(response -> {
                try {
                    if (response.isOk()) {
                        User user = response.to(User.class);
                        System.out.println("✅ User loaded: " + user.getName());
                    } else if (response.isNotFound()) {
                        System.out.println("❌ User not found");
                    } else if (response.isError()) {
                        System.err.println("❌ Error " + response.code() + ": " + response.getStatusDescription());
                    }
                } catch (JsonException e) {
                    System.err.println("❌ JSON parsing error: " + e.getMessage());
                }
            });
        
        // Main thread continues...
        System.out.println("Request sent asynchronously");
    }
}
```

## Error Response Parsing

```java
public class ErrorResponseParsing {
    public static void main(String[] args) {
        try {
            ApiResponse response = Api.post("https://api.example.com/users")
                .body(invalidUserData)
                .execute();
            
            if (response.isUnprocessableEntity()) {
                // Parse structured error response
                Map<String, Object> errorResponse = response.toMap();
                
                if (errorResponse.containsKey("errors")) {
                    List<Map<String, Object>> errors = (List<Map<String, Object>>) errorResponse.get("errors");
                    
                    for (Map<String, Object> error : errors) {
                        String field = (String) error.get("field");
                        String message = (String) error.get("message");
                        System.err.println("Validation error in " + field + ": " + message);
                    }
                }
            }
            
        } catch (JsonException e) {
            System.err.println("Failed to parse error response: " + e.getMessage());
        }
    }
}
```

## Custom Exception Classes

```java
// Custom exceptions for better error handling
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
}

public class AuthenticationException extends RuntimeException {
    public AuthenticationException(String message) {
        super(message);
    }
}

public class AuthorizationException extends RuntimeException {
    public AuthorizationException(String message) {
        super(message);
    }
}

public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}

public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}

public class ServiceException extends RuntimeException {
    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

## Best Practices

### 1. Always Check Status Codes

```java
// ✅ Good: Check status codes
ApiResponse response = Api.get(url).execute();
if (response.isOk()) {
    processData(response);
} else {
    handleError(response);
}

// ❌ Avoid: Ignoring status codes
ApiResponse response = Api.get(url).execute();
processData(response); // This might fail!
```

### 2. Use Specific Status Code Methods

```java
// ✅ Good: Specific status checking
if (response.isCreated()) {
    // Handle resource creation
} else if (response.isNotFound()) {
    // Handle not found
}

// ❌ Avoid: Generic error checking only
if (response.isError()) {
    // Too generic - doesn't know what type of error
}
```

### 3. Handle JSON Parsing Errors

```java
// ✅ Good: Handle JSON parsing separately
try {
    User user = response.to(User.class);
} catch (JsonException e) {
    System.err.println("Failed to parse user data: " + e.getMessage());
}

// ❌ Avoid: Letting JSON errors bubble up
User user = response.to(User.class); // Might throw JsonException
```

### 4. Provide Meaningful Error Messages

```java
// ✅ Good: Meaningful error messages
if (response.isUnauthorized()) {
    throw new AuthenticationException("Invalid API token. Please check your credentials.");
}

// ❌ Avoid: Generic error messages
if (response.isUnauthorized()) {
    throw new RuntimeException("Error");
}
```

## Next Steps

- **[API Reference](/MochaJSON/api/overview)** - Complete API documentation
- **[Java Examples](/MochaJSON/usage/java-examples)** - More usage examples
- **[Kotlin Examples](/MochaJSON/usage/kotlin-examples)** - Kotlin-specific examples
