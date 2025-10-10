# JSON Handling

Advanced JSON parsing techniques with MochaAPI Client, including nested objects, arrays, and custom serialization.

## Basic JSON Parsing

MochaAPI Client automatically handles JSON serialization and deserialization. You can parse JSON responses in three ways:

### 1. Parse to Map

```java
import io.mochaapi.client.*;
import java.util.Map;

Map<String, Object> data = Api.get("https://jsonplaceholder.typicode.com/posts/1")
    .execute()
    .toMap();

System.out.println("Title: " + data.get("title"));
System.out.println("Body: " + data.get("body"));
```

### 2. Parse to List

```java
import io.mochaapi.client.*;

List<Object> posts = Api.get("https://jsonplaceholder.typicode.com/posts")
    .query("userId", 1)
    .execute()
    .toList();

System.out.println("Found " + posts.size() + " posts");
```

### 3. Parse to POJO

```java
import io.mochaapi.client.*;

Post post = Api.get("https://jsonplaceholder.typicode.com/posts/1")
    .execute()
    .to(Post.class);

System.out.println("Title: " + post.title);
```

## Nested Object Parsing

### Java Example

```java
import io.mochaapi.client.*;

public class NestedExample {
    public static void main(String[] args) {
        User user = Api.get("https://jsonplaceholder.typicode.com/users/1")
            .execute()
            .to(User.class);
        
        System.out.println("Name: " + user.name);
        System.out.println("Email: " + user.email);
        System.out.println("Address: " + user.address.street + ", " + user.address.city);
        System.out.println("Company: " + user.company.name);
    }
    
    public static class User {
        public int id;
        public String name;
        public String email;
        public Address address;
        public Company company;
        
        public static class Address {
            public String street;
            public String suite;
            public String city;
            public String zipcode;
            public Geo geo;
            
            public static class Geo {
                public String lat;
                public String lng;
            }
        }
        
        public static class Company {
            public String name;
            public String catchPhrase;
            public String bs;
        }
    }
}
```

### Kotlin Example

```kotlin
import io.mochaapi.client.*

data class User(
    val id: Int,
    val name: String,
    val email: String,
    val address: Address,
    val company: Company
)

data class Address(
    val street: String,
    val suite: String,
    val city: String,
    val zipcode: String,
    val geo: Geo
)

data class Geo(
    val lat: String,
    val lng: String
)

data class Company(
    val name: String,
    val catchPhrase: String,
    val bs: String
)

fun main() {
    val user = Api.get("https://jsonplaceholder.typicode.com/users/1")
        .execute()
        .to(User::class.java)
    
    println("Name: ${user.name}")
    println("Email: ${user.email}")
    println("Address: ${user.address.street}, ${user.address.city}")
    println("Company: ${user.company.name}")
}
```

**Sample Nested JSON Response:**

| Field | Type | Sample Value |
|-------|------|--------------|
| `id` | `int` | `1` |
| `name` | `String` | `"Leanne Graham"` |
| `email` | `String` | `"Sincere@april.biz"` |
| `address.street` | `String` | `"Kulas Light"` |
| `address.suite` | `String` | `"Apt. 556"` |
| `address.city` | `String` | `"Gwenborough"` |
| `address.zipcode` | `String` | `"92998-3874"` |
| `address.geo.lat` | `String` | `"-37.3159"` |
| `address.geo.lng` | `String` | `"81.1496"` |
| `company.name` | `String` | `"Romaguera-Crona"` |
| `company.catchPhrase` | `String` | `"Multi-layered client-server neural-net"` |
| `company.bs` | `String` | `"harness real-time e-markets"` |

## Array Processing

### Processing JSON Arrays

```java
import io.mochaapi.client.*;
import java.util.List;
import java.util.Map;

public class ArrayExample {
    public static void main(String[] args) {
        // Get array of posts
        List<Object> posts = Api.get("https://jsonplaceholder.typicode.com/posts")
            .query("userId", 1)
            .query("_limit", 3)
            .execute()
            .toList();
        
        System.out.println("Found " + posts.size() + " posts");
        
        // Process each post
        for (int i = 0; i < posts.size(); i++) {
            Map<String, Object> post = (Map<String, Object>) posts.get(i);
            System.out.println("Post " + (i + 1) + ": " + post.get("title"));
        }
    }
}
```

### Parse Array to POJO List

```java
import io.mochaapi.client.*;
import java.util.List;

public class PojoArrayExample {
    public static void main(String[] args) {
        // Get JSON array and parse to List<Post>
        List<Object> postsObj = Api.get("https://jsonplaceholder.typicode.com/posts")
            .query("userId", 1)
            .query("_limit", 3)
            .execute()
            .toList();
        
        // Convert to List<Post> manually
        List<Post> posts = new ArrayList<>();
        for (Object postObj : postsObj) {
            Map<String, Object> postMap = (Map<String, Object>) postObj;
            Post post = new Post();
            post.id = (Integer) postMap.get("id");
            post.userId = (Integer) postMap.get("userId");
            post.title = (String) postMap.get("title");
            post.body = (String) postMap.get("body");
            posts.add(post);
        }
        
        posts.forEach(p -> System.out.println("Post: " + p.title));
    }
    
    public static class Post {
        public int id;
        public int userId;
        public String title;
        public String body;
    }
}
```

## Request Body Serialization

### Java Example

```java
import io.mochaapi.client.*;
import java.util.Map;
import java.util.HashMap;

public class RequestBodyExample {
    public static void main(String[] args) {
        // Create request body as Map
        Map<String, Object> newPost = new HashMap<>();
        newPost.put("title", "My New Post");
        newPost.put("body", "This is the content of my new post.");
        newPost.put("userId", 1);
        
        // Send POST request
        ApiResponse response = Api.post("https://jsonplaceholder.typicode.com/posts")
            .body(newPost)
            .execute();
        
        System.out.println("Status: " + response.code());
        System.out.println("Response: " + response.body());
    }
}
```

### Kotlin Example

```kotlin
import io.mochaapi.client.*

fun main() {
    // Create request body as Map
    val newPost = mapOf(
        "title" to "My Kotlin Post",
        "body" to "This post was created from Kotlin!",
        "userId" to 1
    )
    
    // Send POST request
    val response = Api.post("https://jsonplaceholder.typicode.com/posts")
        .body(newPost)
        .execute()
    
    println("Status: ${response.code()}")
    println("Response: ${response.body()}")
}
```

## Custom JSON Mappers

MochaAPI Client supports custom JSON mappers for advanced use cases:

### Using Jackson Directly

```java
import io.mochaapi.client.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;

public class CustomMapperExample {
    public static void main(String[] args) {
        // Create custom ObjectMapper
        ObjectMapper customMapper = new ObjectMapper();
        customMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        
        // Use custom mapper
        JacksonJsonMapper jsonMapper = new JacksonJsonMapper(customMapper);
        
        // This would require modifying the library to accept custom mappers
        // For now, the default mapper handles most use cases
    }
}
```

## Error Handling for JSON

### Handling JSON Parsing Errors

```java
import io.mochaapi.client.*;
import io.mochaapi.client.exception.*;

public class JsonErrorHandling {
    public static void main(String[] args) {
        try {
            // This might fail if JSON is malformed
            Map<String, Object> data = Api.get("https://jsonplaceholder.typicode.com/posts/1")
                .execute()
                .toMap();
            
            System.out.println("Success: " + data.get("title"));
            
        } catch (JsonException e) {
            System.err.println("JSON Parsing Error: " + e.getMessage());
            System.err.println("Cause: " + e.getCause());
        } catch (ApiException e) {
            System.err.println("API Error: " + e.getMessage());
        }
    }
}
```

### Handling Missing Fields

```java
import io.mochaapi.client.*;

public class MissingFieldHandling {
    public static void main(String[] args) {
        Map<String, Object> data = Api.get("https://jsonplaceholder.typicode.com/posts/1")
            .execute()
            .toMap();
        
        // Safe field access
        String title = (String) data.getOrDefault("title", "No title");
        Integer id = (Integer) data.getOrDefault("id", 0);
        
        System.out.println("Title: " + title);
        System.out.println("ID: " + id);
    }
}
```

## Best Practices

### 1. Use POJOs for Type Safety

```java
// ✅ Good: Type-safe POJO
Post post = response.to(Post.class);
System.out.println(post.title);

// ❌ Avoid: Unsafe Map access
Map<String, Object> data = response.toMap();
System.out.println((String) data.get("title")); // Cast required
```

### 2. Handle Null Values

```java
// ✅ Good: Null-safe access
String title = post.title != null ? post.title : "No title";

// ❌ Avoid: Potential NullPointerException
System.out.println(post.title.length());
```

### 3. Use Appropriate Data Types

```java
// ✅ Good: Correct field types
public static class Post {
    public int id;           // int for numbers
    public String title;     // String for text
    public boolean published; // boolean for flags
}

// ❌ Avoid: Wrong types
public static class Post {
    public String id;        // Should be int
    public int title;        // Should be String
}
```

## Next Steps

- **[Java Examples](/MochaJSON/usage/java-examples)** - Complete Java usage patterns
- **[Kotlin Examples](/MochaJSON/usage/kotlin-examples)** - Complete Kotlin usage patterns
- **[API Reference](/MochaJSON/api/overview)** - Complete API documentation
