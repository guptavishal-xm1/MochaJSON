package io.mochaapi.client;

import io.mochaapi.client.exception.JsonException;
import java.util.List;
import java.util.Map;

/**
 * Represents the response from an HTTP API request.
 * Provides convenient methods for accessing response data and converting to different formats.
 * 
 * @since 1.1.0
 */
public class ApiResponse {
    
    private final int statusCode;
    private final String body;
    private final Map<String, String> headers;
    private final JsonMapper jsonMapper;
    
    /**
     * Creates a new API response with the specified data.
     * 
     * @param statusCode the HTTP status code
     * @param body the response body as a string
     * @param headers the response headers
     * @param jsonMapper the JSON mapper for parsing
     */
    public ApiResponse(int statusCode, String body, Map<String, String> headers, JsonMapper jsonMapper) {
        this.statusCode = statusCode;
        this.body = body;
        this.headers = headers;
        this.jsonMapper = jsonMapper;
    }
    
    /**
     * Returns the HTTP status code.
     * 
     * @return the status code
     */
    public int code() {
        return statusCode;
    }
    
    /**
     * Returns the response body as a string.
     * 
     * @return the response body
     */
    public String body() {
        return body;
    }
    
    /**
     * Returns the response headers.
     * 
     * @return map of header names to values
     */
    public Map<String, String> headers() {
        return headers;
    }
    
    /**
     * Returns a JSON mapper for parsing the response body.
     * 
     * @return JsonMapper instance
     */
    public JsonMapper json() {
        return jsonMapper;
    }
    
    /**
     * Converts the response body to the specified type.
     * 
     * @param <T> the target type
     * @param type the target class
     * @return the parsed object
     * @throws JsonException if parsing fails
     */
    public <T> T to(Class<T> type) {
        return jsonMapper.parse(body, type);
    }
    
    /**
     * Converts the response body to a Map.
     * 
     * @return the parsed map
     * @throws JsonException if parsing fails
     */
    public Map<String, Object> toMap() {
        return jsonMapper.toMap(body);
    }
    
    /**
     * Converts the response body to a List.
     * 
     * @return the parsed list
     * @throws JsonException if parsing fails
     */
    public List<Object> toList() {
        return jsonMapper.toList(body);
    }
    
    /**
     * Checks if the response indicates success (status code 200-299).
     * 
     * @return true if successful
     */
    public boolean isSuccess() {
        return statusCode >= 200 && statusCode < 300;
    }
    
    /**
     * Checks if the response indicates an error (status code 400+).
     * 
     * @return true if error
     */
    public boolean isError() {
        return statusCode >= 400;
    }
    
    /**
     * Checks if the response status code is 200 (OK).
     * 
     * @return true if status code is 200
     */
    public boolean isOk() {
        return statusCode == 200;
    }
    
    /**
     * Checks if the response status code is 201 (Created).
     * 
     * @return true if status code is 201
     */
    public boolean isCreated() {
        return statusCode == 201;
    }
    
    /**
     * Checks if the response status code is 204 (No Content).
     * 
     * @return true if status code is 204
     */
    public boolean isNoContent() {
        return statusCode == 204;
    }
    
    /**
     * Checks if the response status code is 400 (Bad Request).
     * 
     * @return true if status code is 400
     */
    public boolean isBadRequest() {
        return statusCode == 400;
    }
    
    /**
     * Checks if the response status code is 401 (Unauthorized).
     * 
     * @return true if status code is 401
     */
    public boolean isUnauthorized() {
        return statusCode == 401;
    }
    
    /**
     * Checks if the response status code is 403 (Forbidden).
     * 
     * @return true if status code is 403
     */
    public boolean isForbidden() {
        return statusCode == 403;
    }
    
    /**
     * Checks if the response status code is 404 (Not Found).
     * 
     * @return true if status code is 404
     */
    public boolean isNotFound() {
        return statusCode == 404;
    }
    
    /**
     * Checks if the response status code is 422 (Unprocessable Entity).
     * 
     * @return true if status code is 422
     */
    public boolean isUnprocessableEntity() {
        return statusCode == 422;
    }
    
    /**
     * Checks if the response status code is 429 (Too Many Requests).
     * 
     * @return true if status code is 429
     */
    public boolean isTooManyRequests() {
        return statusCode == 429;
    }
    
    /**
     * Checks if the response status code is 500 (Internal Server Error).
     * 
     * @return true if status code is 500
     */
    public boolean isInternalServerError() {
        return statusCode == 500;
    }
    
    /**
     * Checks if the response status code is 502 (Bad Gateway).
     * 
     * @return true if status code is 502
     */
    public boolean isBadGateway() {
        return statusCode == 502;
    }
    
    /**
     * Checks if the response status code is 503 (Service Unavailable).
     * 
     * @return true if status code is 503
     */
    public boolean isServiceUnavailable() {
        return statusCode == 503;
    }
    
    /**
     * Checks if the response status code is 504 (Gateway Timeout).
     * 
     * @return true if status code is 504
     */
    public boolean isGatewayTimeout() {
        return statusCode == 504;
    }
    
    /**
     * Checks if the response indicates a client error (status code 400-499).
     * 
     * @return true if client error
     */
    public boolean isClientError() {
        return statusCode >= 400 && statusCode < 500;
    }
    
    /**
     * Checks if the response indicates a server error (status code 500-599).
     * 
     * @return true if server error
     */
    public boolean isServerError() {
        return statusCode >= 500 && statusCode < 600;
    }
    
    /**
     * Checks if the response indicates a redirect (status code 300-399).
     * 
     * @return true if redirect
     */
    public boolean isRedirect() {
        return statusCode >= 300 && statusCode < 400;
    }
    
    /**
     * Checks if the response indicates an informational response (status code 100-199).
     * 
     * @return true if informational
     */
    public boolean isInformational() {
        return statusCode >= 100 && statusCode < 200;
    }
    
    /**
     * Gets a human-readable description of the status code.
     * 
     * @return status description
     */
    public String getStatusDescription() {
        switch (statusCode) {
            case 200: return "OK";
            case 201: return "Created";
            case 202: return "Accepted";
            case 204: return "No Content";
            case 301: return "Moved Permanently";
            case 302: return "Found";
            case 304: return "Not Modified";
            case 400: return "Bad Request";
            case 401: return "Unauthorized";
            case 403: return "Forbidden";
            case 404: return "Not Found";
            case 405: return "Method Not Allowed";
            case 409: return "Conflict";
            case 422: return "Unprocessable Entity";
            case 429: return "Too Many Requests";
            case 500: return "Internal Server Error";
            case 502: return "Bad Gateway";
            case 503: return "Service Unavailable";
            case 504: return "Gateway Timeout";
            default:
                if (statusCode >= 200 && statusCode < 300) return "Success";
                if (statusCode >= 300 && statusCode < 400) return "Redirect";
                if (statusCode >= 400 && statusCode < 500) return "Client Error";
                if (statusCode >= 500 && statusCode < 600) return "Server Error";
                return "Unknown";
        }
    }
}
