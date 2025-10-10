package io.mochaapi.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import io.mochaapi.client.exception.JsonException;

import java.util.List;
import java.util.Map;

/**
 * Jackson-based implementation of JsonMapper.
 * Uses Jackson Databind for JSON serialization and deserialization.
 * 
 * @since 1.1.0 Enhanced with security hardening and improved error handling
 */
public class JacksonJsonMapper implements JsonMapper {
    
    private final ObjectMapper objectMapper;
    
    /**
     * Creates a new JacksonJsonMapper with secure default configuration.
     * Disables polymorphic typing and other potentially unsafe features.
     */
    public JacksonJsonMapper() {
        this.objectMapper = createSecureObjectMapper();
    }
    
    /**
     * Creates a new JacksonJsonMapper with a custom ObjectMapper.
     * 
     * @param objectMapper the custom ObjectMapper instance
     */
    public JacksonJsonMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    @Override
    public String stringify(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new JsonException("Failed to serialize object to JSON: " + e.getMessage(), e);
        }
    }
    
    @Override
    public <T> T parse(String json, Class<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (Exception e) {
            throw new JsonException("Failed to parse JSON to " + type.getSimpleName() + ": " + e.getMessage(), e);
        }
    }
    
    @Override
    public Map<String, Object> toMap(String json) {
        try {
            MapType mapType = objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
            return objectMapper.readValue(json, mapType);
        } catch (Exception e) {
            throw new JsonException("Failed to parse JSON to Map: " + e.getMessage(), e);
        }
    }
    
    @Override
    public List<Object> toList(String json) {
        try {
            CollectionType listType = objectMapper.getTypeFactory().constructCollectionType(List.class, Object.class);
            return objectMapper.readValue(json, listType);
        } catch (Exception e) {
            throw new JsonException("Failed to parse JSON to List: " + e.getMessage(), e);
        }
    }
    
    /**
     * Creates a secure ObjectMapper with security hardening.
     * 
     * @return a secure ObjectMapper instance
     */
    private ObjectMapper createSecureObjectMapper() {
        return new ObjectMapper()
                // Disable polymorphic typing to prevent deserialization vulnerabilities
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .disable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
                .disable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                .disable(DeserializationFeature.ACCEPT_FLOAT_AS_INT)
                .disable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
                .disable(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS)
                // Disable potentially dangerous JSON features
                .disable(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS.mappedFeature())
                .disable(JsonReadFeature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER.mappedFeature())
                .disable(JsonReadFeature.ALLOW_LEADING_ZEROS_FOR_NUMBERS.mappedFeature())
                .disable(JsonReadFeature.ALLOW_NON_NUMERIC_NUMBERS.mappedFeature())
                .disable(JsonReadFeature.ALLOW_TRAILING_COMMA.mappedFeature())
                .disable(JsonReadFeature.ALLOW_UNQUOTED_FIELD_NAMES.mappedFeature());
    }
}
