package io.mochaapi.client.test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * Simple mock HTTP server for testing purposes.
 * Provides predictable responses for various test scenarios.
 */
public class MockHttpServer {
    
    private HttpServer server;
    private int port;
    private Executor executor;
    
    public MockHttpServer() {
        this.executor = Executors.newVirtualThreadPerTaskExecutor();
    }
    
    /**
     * Starts the mock server on a random available port.
     * 
     * @return the port number the server is running on
     * @throws IOException if the server cannot be started
     */
    public int start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.setExecutor(executor);
        setupHandlers();
        server.start();
        this.port = server.getAddress().getPort();
        return this.port;
    }
    
    /**
     * Stops the mock server.
     */
    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }
    
    /**
     * Gets the base URL of the mock server.
     * 
     * @return the base URL
     */
    public String getBaseUrl() {
        return "http://localhost:" + port;
    }
    
    /**
     * Sets up HTTP handlers for various test scenarios.
     */
    private void setupHandlers() {
        // GET /test/success - Returns 200 OK with JSON data
        server.createContext("/test/success", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String response = "{\"id\": 1, \"name\": \"Test User\", \"email\": \"test@example.com\"}";
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
                exchange.getResponseBody().write(response.getBytes(StandardCharsets.UTF_8));
                exchange.close();
            }
        });
        
        // GET /test/error - Returns 500 Internal Server Error
        server.createContext("/test/error", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String response = "{\"error\": \"Internal Server Error\"}";
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(500, response.getBytes(StandardCharsets.UTF_8).length);
                exchange.getResponseBody().write(response.getBytes(StandardCharsets.UTF_8));
                exchange.close();
            }
        });
        
        // GET /test/not-found - Returns 404 Not Found
        server.createContext("/test/not-found", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String response = "{\"error\": \"Not Found\"}";
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(404, response.getBytes(StandardCharsets.UTF_8).length);
                exchange.getResponseBody().write(response.getBytes(StandardCharsets.UTF_8));
                exchange.close();
            }
        });
        
        // POST /test/echo - Returns the request body
        server.createContext("/test/echo", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String method = exchange.getRequestMethod();
                if ("POST".equals(method)) {
                    // Check if it's a multipart request
                    String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
                    if (contentType != null && contentType.startsWith("multipart/form-data")) {
                        // For multipart requests, read the raw bytes and return them as a string representation
                        byte[] requestBody = exchange.getRequestBody().readAllBytes();
                        String response = "Multipart request received with " + requestBody.length + " bytes";
                        exchange.getResponseHeaders().set("Content-Type", "text/plain");
                        exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
                        exchange.getResponseBody().write(response.getBytes(StandardCharsets.UTF_8));
                    } else {
                        // For regular requests, echo the body as before
                        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                        exchange.getResponseHeaders().set("Content-Type", "application/json");
                        exchange.sendResponseHeaders(200, requestBody.getBytes(StandardCharsets.UTF_8).length);
                        exchange.getResponseBody().write(requestBody.getBytes(StandardCharsets.UTF_8));
                    }
                } else {
                    String response = "{\"error\": \"Method not allowed\"}";
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    exchange.sendResponseHeaders(405, response.getBytes(StandardCharsets.UTF_8).length);
                    exchange.getResponseBody().write(response.getBytes(StandardCharsets.UTF_8));
                }
                exchange.close();
            }
        });
        
        // GET /test/query - Echoes query parameters
        server.createContext("/test/query", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String query = exchange.getRequestURI().getQuery();
                String response = "{\"query\": \"" + query + "\"}";
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
                exchange.getResponseBody().write(response.getBytes(StandardCharsets.UTF_8));
                exchange.close();
            }
        });
        
        // GET /test/timeout - Simulates a slow response
        server.createContext("/test/timeout", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                try {
                    Thread.sleep(5000); // Sleep for 5 seconds
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                String response = "{\"message\": \"Slow response\"}";
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
                exchange.getResponseBody().write(response.getBytes(StandardCharsets.UTF_8));
                exchange.close();
            }
        });
    }
}
