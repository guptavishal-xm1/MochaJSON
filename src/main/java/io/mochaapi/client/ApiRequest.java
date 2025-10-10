package io.mochaapi.client;

import io.mochaapi.client.exception.ApiException;
import io.mochaapi.client.multipart.MultipartRequest;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Represents a configurable HTTP request with chainable methods.
 * Allows building requests step by step with headers, query parameters, and body.
 * 
 * <p>Example usage:</p>
 * <pre>
 * ApiResponse response = client.get("https://api.example.com/data")
 *     .header("Authorization", "Bearer token123")
 *     .query("page", 1)
 *     .query("limit", 10)
 *     .body(requestData)
 *     .execute();
 * </pre>
 */
public class ApiRequest {
    
    private final String url;
    private final String method;
    private final Map<String, String> headers;
    private final Map<String, Object> queryParams;
    private Object body;
    private final ApiClient client;
    private boolean isMultipart = false;
    
    /**
     * Creates a new API request with the specified URL and HTTP method.
     * This constructor is used internally by ApiClient.
     * 
     * @param url the target URL
     * @param method the HTTP method (GET, POST, PUT, DELETE, PATCH)
     * @param client the ApiClient instance
     */
    public ApiRequest(String url, String method, ApiClient client) {
        this.url = url;
        this.method = method.toUpperCase();
        this.headers = new HashMap<>();
        this.queryParams = new HashMap<>();
        this.client = client;
        
        // Set default headers
        this.headers.put("User-Agent", "MochaAPI-Client/1.2.0");
        this.headers.put("Accept", "application/json");
    }
    
    /**
     * Creates a new API request with the specified URL and HTTP method.
     * This constructor is used by the static Api class for backward compatibility.
     * 
     * @param url the target URL
     * @param method the HTTP method (GET, POST, PUT, DELETE, PATCH)
     */
    public ApiRequest(String url, String method) {
        this.url = url;
        this.method = method.toUpperCase();
        this.headers = new HashMap<>();
        this.queryParams = new HashMap<>();
        this.client = null; // Will use static Api methods
        
        // Set default headers
        this.headers.put("User-Agent", "MochaAPI-Client/1.2.0");
        this.headers.put("Accept", "application/json");
    }
    
    /**
     * Adds a header to the request.
     * 
     * @param name the header name
     * @param value the header value
     * @return this request for chaining
     * @throws IllegalArgumentException if name or value is null
     */
    public ApiRequest header(String name, String value) {
        if (name == null) {
            throw new IllegalArgumentException("Header name cannot be null");
        }
        if (value == null) {
            throw new IllegalArgumentException("Header value cannot be null");
        }
        this.headers.put(name, value);
        return this;
    }
    
    /**
     * Adds a query parameter to the request.
     * 
     * @param name the parameter name
     * @param value the parameter value
     * @return this request for chaining
     * @throws IllegalArgumentException if name is null
     */
    public ApiRequest query(String name, Object value) {
        if (name == null) {
            throw new IllegalArgumentException("Query parameter name cannot be null");
        }
        this.queryParams.put(name, String.valueOf(value));
        return this;
    }
    
    /**
     * Sets the request body. Can be a String, Map, or any object that will be JSON serialized.
     * 
     * @param body the request body
     * @return this request for chaining
     * @throws IllegalArgumentException if body is null
     */
    public ApiRequest body(Object body) {
        if (body == null) {
            throw new IllegalArgumentException("Request body cannot be null");
        }
        this.body = body;
        this.isMultipart = false;
        return this;
    }
    
    /**
     * Converts this request to a multipart request for file uploads.
     * 
     * @return multipart request builder
     */
    public MultipartRequest multipart() {
        this.isMultipart = true;
        return MultipartRequest.of(this);
    }
    
    /**
     * Downloads the response to a file.
     * 
     * @param file the target file
     * @return the downloaded file
     * @throws ApiException if download fails
     */
    public File download(File file) {
        ApiResponse response = execute();
        
        if (!response.isSuccess()) {
            throw new ApiException("Download failed with status: " + response.code());
        }
        
        try {
            // Create parent directories if needed
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            
            // Write response body to file
            Files.write(file.toPath(), response.body().getBytes());
            return file;
            
        } catch (IOException e) {
            throw new ApiException("Failed to write file: " + e.getMessage(), e);
        }
    }
    
    /**
     * Downloads the response to a file path.
     * 
     * @param path the target file path
     * @return the downloaded file
     * @throws ApiException if download fails
     */
    public File download(Path path) {
        return download(path.toFile());
    }
    
    /**
     * Downloads the response to a file with the specified filename.
     * 
     * @param filename the target filename
     * @return the downloaded file
     * @throws ApiException if download fails
     */
    public File download(String filename) {
        return download(new File(filename));
    }
    
    /**
     * Downloads the response as a ManagedInputStream for streaming.
     * The returned stream implements AutoCloseable and should be used in try-with-resources.
     * 
     * <p>Usage example:</p>
     * <pre>
     * try (ManagedInputStream stream = request.downloadStream()) {
     *     byte[] data = stream.readAllBytes();
     * } // Stream is automatically closed
     * </pre>
     * 
     * @return managed input stream for reading the response
     * @throws ApiException if download fails
     */
    public ManagedInputStream downloadStream() {
        ApiResponse response = execute();
        
        if (!response.isSuccess()) {
            throw new ApiException("Download failed with status: " + response.code());
        }
        
        return new ManagedInputStream(response.body().getBytes());
    }
    
    /**
     * Executes the request synchronously and returns the response.
     * 
     * @return ApiResponse containing the result
     * @throws ApiException if the request fails
     */
    public ApiResponse execute() {
        if (client != null) {
            return client.execute(this);
        } else {
            // Backward compatibility with static Api methods
            return Api.execute(this);
        }
    }
    
    /**
     * Executes the request asynchronously and returns a CompletableFuture.
     * 
     * @return CompletableFuture containing the response
     */
    public CompletableFuture<ApiResponse> executeAsync() {
        if (client != null) {
            return client.executeAsync(this);
        } else {
            // Backward compatibility with static Api methods
            CompletableFuture<ApiResponse> future = new CompletableFuture<>();
            Api.executeAsync(this, future::complete);
            return future;
        }
    }
    
    /**
     * Executes the request asynchronously with a callback.
     * 
     * @param callback function to handle the response
     */
    public void async(Consumer<ApiResponse> callback) {
        if (client != null) {
            client.executeAsync(this, callback);
        } else {
            // Backward compatibility with static Api methods
            Api.executeAsync(this, callback);
        }
    }
    
    // Getters for internal use
    public String getUrl() { return url; }
    public String getMethod() { return method; }
    public Map<String, String> getHeaders() { return headers; }
    public Map<String, Object> getQueryParams() { return queryParams; }
    public Object getBody() { return body; }
    public boolean isMultipart() { return isMultipart; }
}
