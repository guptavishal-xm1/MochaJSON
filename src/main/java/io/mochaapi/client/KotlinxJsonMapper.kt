package io.mochaapi.client

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import kotlin.reflect.KClass

/**
 * Kotlinx Serialization-based implementation of JsonMapper.
 * Uses Kotlinx Serialization for JSON serialization and deserialization.
 * Provides seamless integration with Kotlin data classes.
 * 
 * Note: This is an optional mapper as of v1.3.0. Jackson is the default mapper.
 * Requires kotlinx-serialization-json dependency.
 * 
 * @since 1.1.0 Enhanced with improved generic type handling and security
 * @since 1.3.0 Made optional dependency (Jackson is default)
 */
class KotlinxJsonMapper : JsonMapper {
    
    private val json: Json
    
    /**
     * Creates a new KotlinxJsonMapper with default configuration.
     */
    constructor() : super() {
        this.json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
            // Security hardening
            allowStructuredMapKeys = false
            prettyPrint = false
        }
    }
    
    /**
     * Creates a new KotlinxJsonMapper with a custom Json instance.
     * 
     * @param customJson the custom Json instance
     */
    constructor(customJson: Json) : super() {
        this.json = customJson
    }
    
    override fun stringify(obj: Any?): String {
        return try {
            when (obj) {
                is String -> obj
                is Number, is Boolean, null -> json.encodeToString(obj)
                else -> {
                    // For non-serializable objects, convert to string
                    obj.toString()
                }
            }
        } catch (e: Exception) {
            throw io.mochaapi.client.exception.JsonException("Failed to serialize object to JSON: ${e.message}", e)
        }
    }
    
    @Suppress("UNCHECKED_CAST")
    override fun <T> parse(jsonString: String, type: Class<T>): T {
        return try {
            when (type) {
                String::class.java -> jsonString as T
                Int::class.java, Integer::class.java -> jsonString.toInt() as T
                Long::class.java, java.lang.Long::class.java -> jsonString.toLong() as T
                Double::class.java, java.lang.Double::class.java -> jsonString.toDouble() as T
                Boolean::class.java, java.lang.Boolean::class.java -> jsonString.toBoolean() as T
                Map::class.java -> toMap(jsonString) as T
                List::class.java -> toList(jsonString) as T
                else -> {
                    // For complex objects, try to parse as JSON and convert
                    val jsonElement = json.parseToJsonElement(jsonString)
                    convertJsonElement(jsonElement, type)
                }
            }
        } catch (e: Exception) {
            throw io.mochaapi.client.exception.JsonException("Failed to parse JSON to ${type.simpleName}: ${e.message}", e)
        }
    }
    
    override fun toMap(jsonString: String): Map<String, Any> {
        return try {
            val jsonElement = json.parseToJsonElement(jsonString)
            jsonElement.jsonObject.mapValues { it.value.toString() }
        } catch (e: Exception) {
            throw io.mochaapi.client.exception.JsonException("Failed to parse JSON to Map: ${e.message}", e)
        }
    }
    
    override fun toList(jsonString: String): List<Any> {
        return try {
            val jsonElement = json.parseToJsonElement(jsonString)
            jsonElement.jsonArray.map { it.toString() }
        } catch (e: Exception) {
            throw io.mochaapi.client.exception.JsonException("Failed to parse JSON to List: ${e.message}", e)
        }
    }
    
    @Suppress("UNCHECKED_CAST")
    private fun <T> convertJsonElement(jsonElement: JsonElement, type: Class<T>): T {
        return when {
            type == Map::class.java -> {
                jsonElement.jsonObject.mapValues { it.value.toString() } as T
            }
            type == List::class.java -> {
                jsonElement.jsonArray.map { it.toString() } as T
            }
            else -> {
                // For other types, return as string representation
                jsonElement.toString() as T
            }
        }
    }
}
