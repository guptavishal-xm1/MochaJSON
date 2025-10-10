package io.mochaapi.client;

import io.mochaapi.client.exception.JsonException;
import java.util.List;
import java.util.Map;

/**
 * Interface for JSON serialization and deserialization.
 * Provides methods to convert objects to JSON strings and parse JSON back to objects.
 */
public interface JsonMapper {
    
    /**
     * Converts an object to a JSON string.
     * 
     * @param obj the object to serialize
     * @return JSON string representation
     * @throws JsonException if serialization fails
     */
    String stringify(Object obj);
    
    /**
     * Parses a JSON string to an object of the specified type.
     * 
     * @param <T> the target type
     * @param json the JSON string to parse
     * @param type the target class
     * @return the parsed object
     * @throws JsonException if parsing fails
     */
    <T> T parse(String json, Class<T> type);
    
    /**
     * Parses a JSON string to a Map.
     * 
     * @param json the JSON string to parse
     * @return the parsed map
     * @throws JsonException if parsing fails
     */
    Map<String, Object> toMap(String json);
    
    /**
     * Parses a JSON string to a List.
     * 
     * @param json the JSON string to parse
     * @return the parsed list
     * @throws JsonException if parsing fails
     */
    List<Object> toList(String json);
}
