# Java Examples

Complete Java usage examples for MochaAPI Client, including GET requests, POST requests, JSON parsing, and error handling.

## GET Request with Map Parsing

```java
import io.mochaapi.client.*;
import java.util.Map;

public class GetExample {
    public static void main(String[] args) {
        // GET request → Map
        Map<String, Object> post = Api.get("https://jsonplaceholder.typicode.com/posts/1")
            .execute()
            .toMap();
        
        System.out.println("Post ID: " + post.get("id"));
        System.out.println("Post Title: " + post.get("title"));
        System.out.println("Post Body: " + post.get("body"));
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

## GET Request with POJO Parsing

```java
import io.mochaapi.client.*;

public class PojoExample {
    public static void main(String[] args) {
        // GET request → POJO
        Post post = Api.get("https://jsonplaceholder.typicode.com/posts/1")
            .execute()
            .to(Post.class);
        
        System.out.println("Post ID: " + post.id);
        System.out.println("Post Title: " + post.title);
        System.out.println("Post Body: " + post.body);
    }
    
    // POJO class for JSON deserialization
    public static class Post {
        public int id;
        public int userId;
        public String title;
        public String body;
    }
}
```

## POST Request with JSON Body

```java
import io.mochaapi.client.*;
import java.util.Map;
import java.util.HashMap;

public class PostExample {
    public static void main(String[] args) {
        // Create request body
        Map<String, Object> newPost = new HashMap<>();
        newPost.put("title", "My New Post");
        newPost.put("body", "This is the content of my new post.");
        newPost.put("userId", 1);
        
        // POST request
        ApiResponse response = Api.post("https://jsonplaceholder.typicode.com/posts")
            .body(newPost)
            .execute();
        
        System.out.println("Status Code: " + response.code());
        
        // Parse response to Map
        Map<String, Object> responseData = response.toMap();
        System.out.println("Created Post ID: " + responseData.get("id"));
        System.out.println("Created Post Title: " + responseData.get("title"));
        
        // Parse response to POJO
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
| `title` | `String` | `"My New Post"` |
| `body` | `String` | `"This is the content of my new post."` |
| `userId` | `int` | `1` |

## Query Parameters and Headers

```java
import io.mochaapi.client.*;

public class AdvancedExample {
    public static void main(String[] args) {
        ApiResponse response = Api.get("https://jsonplaceholder.typicode.com/posts")
            .query("userId", 1)
            .query("_limit", 5)
            .header("Authorization", "Bearer token123")
            .header("User-Agent", "MyApp/1.0")
            .execute();
        
        var posts = response.toList();
        System.out.println("Found " + posts.size() + " posts");
        
        // Process each post
        for (Object postObj : posts) {
            Map<String, Object> post = (Map<String, Object>) postObj;
            System.out.println("Post " + post.get("id") + ": " + post.get("title"));
        }
    }
}
```

## Async Requests

```java
import io.mochaapi.client.*;

public class AsyncExample {
    public static void main(String[] args) {
        Api.get("https://jsonplaceholder.typicode.com/posts/1")
            .async(response -> {
                System.out.println("Async response: " + response.body());
                System.out.println("Status: " + response.code());
            });
        
        // Main thread continues...
        System.out.println("Request sent asynchronously");
    }
}
```

## Complex Nested Objects

```java
import io.mochaapi.client.*;

public class NestedObjectExample {
    public static void main(String[] args) {
        User user = Api.get("https://jsonplaceholder.typicode.com/users/1")
            .execute()
            .to(User.class);
        
        System.out.println("User Name: " + user.name);
        System.out.println("User Email: " + user.email);
        System.out.println("User Address: " + user.address.street + ", " + user.address.city);
        System.out.println("User Company: " + user.company.name);
    }
    
    public static class User {
        public int id;
        public String name;
        public String username;
        public String email;
        public String phone;
        public String website;
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

**Sample User JSON Response:**

| Field | Type | Sample Value |
|-------|------|--------------|
| `id` | `int` | `1` |
| `name` | `String` | `"Leanne Graham"` |
| `email` | `String` | `"Sincere@april.biz"` |
| `phone` | `String` | `"1-770-736-8031 x56442"` |
| `website` | `String` | `"hildegard.org"` |
| `address.street` | `String` | `"Kulas Light"` |
| `address.city` | `String` | `"Gwenborough"` |
| `company.name` | `String` | `"Romaguera-Crona"` |

## Error Handling

```java
import io.mochaapi.client.*;
import io.mochaapi.client.exception.*;

public class ErrorHandlingExample {
    public static void main(String[] args) {
        try {
            ApiResponse response = Api.get("https://jsonplaceholder.typicode.com/posts/1")
                .execute();
            
            if (response.isError()) {
                System.err.println("HTTP Error: " + response.code());
                return;
            }
            
            Post post = response.to(Post.class);
            System.out.println("Success: " + post.title);
            
        } catch (ApiException e) {
            System.err.println("Network/HTTP Error: " + e.getMessage());
        } catch (JsonException e) {
            System.err.println("JSON Parsing Error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected Error: " + e.getMessage());
        }
    }
    
    public static class Post {
        public int id;
        public int userId;
        public String title;
        public String body;
    }
}
```

## Next Steps

- **[Kotlin Examples](/MochaJSON/usage/kotlin-examples)** - See equivalent Kotlin code
- **[JSON Handling](/MochaJSON/usage/json-handling)** - Advanced JSON parsing techniques
- **[API Reference](/MochaJSON/api/overview)** - Complete API documentation
