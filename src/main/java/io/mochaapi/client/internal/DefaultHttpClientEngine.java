package io.mochaapi.client.internal;

import io.mochaapi.client.*;
import io.mochaapi.client.exception.ApiException;
import io.mochaapi.client.multipart.MultipartBodyBuilder;
import io.mochaapi.client.multipart.MultipartRequest;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Default implementation of HttpClientEngine using Java 11+ HttpClient.
 * Handles HTTP requests with automatic JSON serialization, timeouts, and error handling.
 * 
 * @since 1.1.0 Enhanced with configurable timeouts and improved error handling
 */
public class DefaultHttpClientEngine implements HttpClientEngine {
    
    private final HttpClient httpClient;
    private final JsonMapper jsonMapper;
    private final Duration connectTimeout;
    private final Duration readTimeout;
    private final Duration writeTimeout;
    
    /**
     * Creates a new DefaultHttpClientEngine with default configuration.
     * Uses 30-second timeouts for all operations.
     */
    public DefaultHttpClientEngine() {
        this(Duration.ofSeconds(30), Duration.ofSeconds(30), Duration.ofSeconds(30));
    }
    
    /**
     * Creates a new DefaultHttpClientEngine with custom timeout configuration.
     * 
     * @param connectTimeout the connection timeout
     * @param readTimeout the read timeout
     * @param writeTimeout the write timeout
     */
    public DefaultHttpClientEngine(Duration connectTimeout, Duration readTimeout, Duration writeTimeout) {
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
        this.writeTimeout = writeTimeout;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(connectTimeout)
                .build();
        this.jsonMapper = createSecureJsonMapper();
    }
    
    
    /**
     * Creates a new DefaultHttpClientEngine with custom HttpClient and JsonMapper.
     * 
     * @param httpClient the custom HttpClient instance
     * @param jsonMapper the custom JsonMapper instance
     */
    public DefaultHttpClientEngine(HttpClient httpClient, JsonMapper jsonMapper) {
        this.httpClient = httpClient;
        this.jsonMapper = jsonMapper;
        this.connectTimeout = Duration.ofSeconds(30);
        this.readTimeout = Duration.ofSeconds(30);
        this.writeTimeout = Duration.ofSeconds(30);
    }
    
    @Override
    public ApiResponse execute(ApiRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        
        try {
            // Validate and build the URL with query parameters
            String url = buildUrl(request);
            Utils.validateUrl(url);
            
            // Build the HTTP request
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(readTimeout);
            
            // Set HTTP method and body
            String method = request.getMethod();
            Object body = request.getBody();
            
            if (body != null && !method.equals("GET") && !method.equals("DELETE")) {
                if (request.isMultipart() && body instanceof MultipartRequest) {
                    // Handle multipart request
                    MultipartRequest multipartRequest = (MultipartRequest) body;
                    MultipartBodyBuilder.MultipartBody multipartBody = MultipartBodyBuilder.build(multipartRequest);
                    requestBuilder.method(method, HttpRequest.BodyPublishers.ofByteArray(multipartBody.getContent()));
                    requestBuilder.header("Content-Type", multipartBody.getContentType());
                } else {
                    // Handle regular JSON/string body
                    String bodyString = serializeBody(body);
                    requestBuilder.method(method, HttpRequest.BodyPublishers.ofString(bodyString, StandardCharsets.UTF_8));
                    
                    // Set Content-Type header if not already set
                    if (!request.getHeaders().containsKey("Content-Type")) {
                        requestBuilder.header("Content-Type", "application/json");
                    }
                }
            } else {
                requestBuilder.method(method, HttpRequest.BodyPublishers.noBody());
            }
            
            // Add headers
            for (Map.Entry<String, String> header : request.getHeaders().entrySet()) {
                requestBuilder.header(header.getKey(), header.getValue());
            }
            
            // Execute the request
            HttpRequest httpRequest = requestBuilder.build();
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            
            // Convert headers to Map
            Map<String, String> responseHeaders = new HashMap<>();
            response.headers().map().forEach((key, values) -> {
                if (!values.isEmpty()) {
                    responseHeaders.put(key, values.get(0));
                }
            });
            
            // Create response and validate status code
            ApiResponse apiResponse = new ApiResponse(
                    response.statusCode(),
                    response.body(),
                    responseHeaders,
                    jsonMapper
            );
            
            // Log status code for debugging (optional)
            if (response.statusCode() >= 400) {
                System.err.println("HTTP Error " + response.statusCode() + ": " + apiResponse.getStatusDescription());
            }
            
            return apiResponse;
            
        } catch (IOException e) {
            throw new ApiException("Network error: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException("Request interrupted: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new ApiException("Request failed: " + e.getMessage(), e);
        }
    }
    
    
    /**
     * Creates the default JsonMapper (Jackson).
     * 
     * @return Jackson JsonMapper instance
     */
    private JsonMapper createSecureJsonMapper() {
        return new JacksonJsonMapper();
    }
    
    /**
     * Builds the complete URL with query parameters.
     * Fixed to properly handle existing query parameters and URL encoding.
     */
    private String buildUrl(ApiRequest request) {
        String url = request.getUrl();
        Map<String, Object> queryParams = request.getQueryParams();
        
        if (queryParams.isEmpty()) {
            return url;
        }
        
        StringBuilder urlBuilder = new StringBuilder(url);
        
        // Determine the separator based on whether URL already has query parameters
        String separator = url.contains("?") ? "&" : "?";
        
        for (Map.Entry<String, Object> param : queryParams.entrySet()) {
            urlBuilder.append(separator);
            
            String key = URLEncoder.encode(param.getKey(), StandardCharsets.UTF_8);
            String value = URLEncoder.encode(String.valueOf(param.getValue()), StandardCharsets.UTF_8);
            urlBuilder.append(key).append("=").append(value);
            
            // Use "&" for subsequent parameters
            separator = "&";
        }
        
        return urlBuilder.toString();
    }
    
    /**
     * Serializes the request body to a string.
     */
    private String serializeBody(Object body) {
        if (body instanceof String) {
            return (String) body;
        } else {
            return jsonMapper.stringify(body);
        }
    }
}
