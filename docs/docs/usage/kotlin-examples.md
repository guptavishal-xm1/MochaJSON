# Kotlin Examples

Complete Kotlin usage examples for MochaAPI Client, including GET requests, POST requests, JSON parsing, and error handling.

## GET Request with Map Parsing

```kotlin
import io.mochaapi.client.*

fun main() {
    // GET request → Map
    val post = Api.get("https://jsonplaceholder.typicode.com/posts/1")
        .execute()
        .toMap()
    
    println("Post ID: ${post["id"]}")
    println("Post Title: ${post["title"]}")
    println("Post Body: ${post["body"]}")
}
```

**Sample JSON Response:**

| Field | Type | Sample Value |
|-------|------|--------------|
| `userId` | `Int` | `1` |
| `id` | `Int` | `1` |
| `title` | `String` | `"sunt aut facere repellat provident occaecati excepturi optio reprehenderit"` |
| `body` | `String` | `"quia et suscipit\nsuscipit recusandae consequuntur expedita et cum\nreprehenderit molestiae ut ut quas totam\nnostrum rerum est autem sunt rem eveniet architecto"` |

## GET Request with Data Class

```kotlin
import io.mochaapi.client.*

data class Post(
    val id: Int,
    val userId: Int,
    val title: String,
    val body: String
)

fun main() {
    // GET request → Data class
    val post = Api.get("https://jsonplaceholder.typicode.com/posts/1")
        .execute()
        .to(Post::class.java)
    
    println("Post ID: ${post.id}")
    println("Post Title: ${post.title}")
    println("Post Body: ${post.body}")
}
```

## POST Request with JSON Body

```kotlin
import io.mochaapi.client.*

data class Post(
    val id: Int,
    val userId: Int,
    val title: String,
    val body: String
)

fun main() {
    // Create request body
    val newPost = mapOf(
        "title" to "My Kotlin Post",
        "body" to "This post was created from Kotlin!",
        "userId" to 1
    )
    
    // POST request
    val response = Api.post("https://jsonplaceholder.typicode.com/posts")
        .body(newPost)
        .execute()
    
    println("Status Code: ${response.code()}")
    
    // Parse response to Map
    val responseMap = response.toMap()
    println("Created Post ID: ${responseMap["id"]}")
    println("Created Post Title: ${responseMap["title"]}")
    
    // Parse response to Data class
    val createdPost = response.to(Post::class.java)
    println("Created Post: ${createdPost.title}")
}
```

**Sample Response JSON:**

| Field | Type | Sample Value |
|-------|------|--------------|
| `id` | `Int` | `101` |
| `title` | `String` | `"My Kotlin Post"` |
| `body` | `String` | `"This post was created from Kotlin!"` |
| `userId` | `Int` | `1` |

## Query Parameters and Headers

```kotlin
import io.mochaapi.client.*

fun main() {
    val response = Api.get("https://jsonplaceholder.typicode.com/posts")
        .query("userId", 1)
        .query("_limit", 5)
        .header("Authorization", "Bearer token123")
        .header("User-Agent", "MyApp/1.0")
        .execute()
    
    val posts = response.toList()
    println("Found ${posts.size} posts")
    
    // Process each post
    posts.forEachIndexed { index, postObj ->
        val post = postObj as Map<String, Any>
        println("Post ${post["id"]}: ${post["title"]}")
    }
}
```

## Async Requests

```kotlin
import io.mochaapi.client.*

fun main() {
    Api.get("https://jsonplaceholder.typicode.com/posts/1")
        .async { response ->
            println("Async response: ${response.body()}")
            println("Status: ${response.code()}")
        }
    
    // Main thread continues...
    println("Request sent asynchronously")
}
```

## Complex Nested Objects

```kotlin
import io.mochaapi.client.*

data class User(
    val id: Int,
    val name: String,
    val username: String,
    val email: String,
    val phone: String,
    val website: String,
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
    
    println("User Name: ${user.name}")
    println("User Email: ${user.email}")
    println("User Address: ${user.address.street}, ${user.address.city}")
    println("User Company: ${user.company.name}")
}
```

**Sample User JSON Response:**

| Field | Type | Sample Value |
|-------|------|--------------|
| `id` | `Int` | `1` |
| `name` | `String` | `"Leanne Graham"` |
| `email` | `String` | `"Sincere@april.biz"` |
| `phone` | `String` | `"1-770-736-8031 x56442"` |
| `website` | `String` | `"hildegard.org"` |
| `address.street` | `String` | `"Kulas Light"` |
| `address.city` | `String` | `"Gwenborough"` |
| `company.name` | `String` | `"Romaguera-Crona"` |

## Error Handling

```kotlin
import io.mochaapi.client.*
import io.mochaapi.client.exception.*

data class Post(
    val id: Int,
    val userId: Int,
    val title: String,
    val body: String
)

fun main() {
    try {
        val response = Api.get("https://jsonplaceholder.typicode.com/posts/1")
            .execute()
        
        if (response.isError()) {
            println("HTTP Error: ${response.code()}")
            return
        }
        
        val post = response.to(Post::class.java)
        println("Success: ${post.title}")
        
    } catch (e: ApiException) {
        println("Network/HTTP Error: ${e.message}")
    } catch (e: JsonException) {
        println("JSON Parsing Error: ${e.message}")
    } catch (e: Exception) {
        println("Unexpected Error: ${e.message}")
    }
}
```

## List Processing

```kotlin
import io.mochaapi.client.*

data class Post(
    val id: Int,
    val userId: Int,
    val title: String,
    val body: String
)

fun main() {
    val response = Api.get("https://jsonplaceholder.typicode.com/posts")
        .query("userId", 1)
        .query("_limit", 3)
        .execute()
    
    val posts = response.toList()
    println("Found ${posts.size} posts for user 1")
    
    posts.forEach { postObj ->
        val post = postObj as Map<String, Any>
        println("Post ${post["id"]}: ${post["title"]}")
    }
}
```

## Functional Style with Extension Functions

```kotlin
import io.mochaapi.client.*

// Extension function for cleaner syntax
fun ApiRequest.executeToMap(): Map<String, Any> = execute().toMap()

fun main() {
    val posts = Api.get("https://jsonplaceholder.typicode.com/posts")
        .query("userId", 1)
        .query("_limit", 2)
        .executeToMap()
    
    println("Posts: $posts")
}
```

## Next Steps

- **[Java Examples](/MochaJSON/usage/java-examples)** - See equivalent Java code
- **[JSON Handling](/MochaJSON/usage/json-handling)** - Advanced JSON parsing techniques
- **[API Reference](/MochaJSON/api/overview)** - Complete API documentation
