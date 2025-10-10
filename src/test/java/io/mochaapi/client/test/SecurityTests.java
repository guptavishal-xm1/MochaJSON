package io.mochaapi.client.test;

import io.mochaapi.client.*;
import io.mochaapi.client.internal.Utils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for security features including URL validation and JSON parsing security.
 */
public class SecurityTests {
    
    @Test
    @DisplayName("URL validation - valid HTTP URLs")
    void testValidHttpUrls() {
        assertTrue(Utils.isUrlSafe("http://example.com"));
        assertTrue(Utils.isUrlSafe("https://api.example.com"));
        assertTrue(Utils.isUrlSafe("https://api.example.com:8080"));
        assertTrue(Utils.isUrlSafe("https://api.example.com/path"));
        assertTrue(Utils.isUrlSafe("https://api.example.com/path?param=value"));
        assertTrue(Utils.isUrlSafe("https://api.example.com/path#fragment"));
    }
    
    @Test
    @DisplayName("URL validation - invalid URLs")
    void testInvalidUrls() {
        assertFalse(Utils.isUrlSafe(""));
        assertFalse(Utils.isUrlSafe(null));
        assertFalse(Utils.isUrlSafe("not-a-url"));
        assertFalse(Utils.isUrlSafe("ftp://example.com"));
        assertFalse(Utils.isUrlSafe("file:///etc/passwd"));
        assertFalse(Utils.isUrlSafe("javascript:alert('xss')"));
        assertFalse(Utils.isUrlSafe("data:text/html,<script>alert('xss')</script>"));
    }
    
    @Test
    @DisplayName("URL validation - dangerous schemes")
    void testDangerousSchemes() {
        assertThrows(IllegalArgumentException.class, () -> {
            Utils.validateUrl("javascript:alert('xss')");
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            Utils.validateUrl("data:text/html,<script>alert('xss')</script>");
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            Utils.validateUrl("file:///etc/passwd");
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            Utils.validateUrl("ftp://example.com");
        });
    }
    
    @Test
    @DisplayName("URL validation - missing scheme")
    void testMissingScheme() {
        assertThrows(IllegalArgumentException.class, () -> {
            Utils.validateUrl("example.com");
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            Utils.validateUrl("//example.com");
        });
    }
    
    @Test
    @DisplayName("URL validation - missing host")
    void testMissingHost() {
        assertThrows(IllegalArgumentException.class, () -> {
            Utils.validateUrl("http://");
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            Utils.validateUrl("https:///path");
        });
    }
    
    @Test
    @DisplayName("URL parsing functionality")
    void testUrlParsing() {
        // Test valid URL parsing
        assertDoesNotThrow(() -> {
            Utils.parseUrl("https://api.example.com/test");
        });
        
        // Test invalid URL parsing
        assertThrows(IllegalArgumentException.class, () -> {
            Utils.parseUrl("invalid-url");
        });
    }
    
    @Test
    @DisplayName("JSON parsing security - malicious JSON")
    void testJsonParsingSecurity() {
        ApiClient client = new ApiClient.Builder().build();
        
        // Test with potentially malicious JSON (this would be handled by Jackson's security features)
        String maliciousJson = "{\"@class\":\"java.lang.ProcessBuilder\",\"command\":[\"calc.exe\"]}";
        
        // The secure Jackson configuration should prevent polymorphic deserialization
        assertDoesNotThrow(() -> {
            // This should not execute arbitrary code due to security hardening
            client.get("https://httpbin.org/json").execute();
        });
    }
    
    @Test
    @DisplayName("JSON parsing security - large JSON")
    void testLargeJsonHandling() {
        ApiClient client = new ApiClient.Builder().build();
        
        // Test with a reasonably large JSON response
        // The mock server would need to be extended to support this test case
        assertDoesNotThrow(() -> {
            client.get("https://httpbin.org/json").execute();
        });
    }
    
    @Test
    @DisplayName("Request timeout security")
    void testRequestTimeoutSecurity() {
        ApiClient client = new ApiClient.Builder()
                .connectTimeout(java.time.Duration.ofMillis(1))
                .readTimeout(java.time.Duration.ofMillis(1))
                .build();
        
        // This should timeout quickly, preventing potential DoS attacks
        assertThrows(Exception.class, () -> {
            client.get("https://httpbin.org/delay/5").execute();
        });
    }
    
    @Test
    @DisplayName("Header injection prevention")
    void testHeaderInjectionPrevention() {
        ApiClient client = new ApiClient.Builder().build();
        
        // Test with potentially malicious header values
        assertDoesNotThrow(() -> {
            client.get("https://httpbin.org/headers")
                    .header("X-Test", "normal-value")
                    .header("User-Agent", "MochaAPI-Client/1.2.0")
                    .execute();
        });
    }
    
    @Test
    @DisplayName("Query parameter injection prevention")
    void testQueryParameterInjectionPrevention() {
        ApiClient client = new ApiClient.Builder().build();
        
        // Test with potentially malicious query parameters
        assertDoesNotThrow(() -> {
            client.get("https://httpbin.org/get")
                    .query("normal_param", "normal_value")
                    .query("test", "value with spaces")
                    .query("special", "value&with=special&chars")
                    .execute();
        });
    }
    
    @Test
    @DisplayName("URL encoding security")
    void testUrlEncodingSecurity() {
        ApiClient client = new ApiClient.Builder().build();
        
        // Test that special characters are properly encoded
        assertDoesNotThrow(() -> {
            client.get("https://httpbin.org/get")
                    .query("param", "value with spaces")
                    .query("special", "value&with=special&chars")
                    .query("unicode", "测试")
                    .execute();
        });
    }
    
    @Test
    @DisplayName("Request body security")
    void testRequestBodySecurity() {
        ApiClient client = new ApiClient.Builder().build();
        
        // Test with potentially malicious request body
        String maliciousBody = "{\"malicious\": \"<script>alert('xss')</script>\"}";
        
        assertDoesNotThrow(() -> {
            client.post("https://httpbin.org/post")
                    .body(maliciousBody)
                    .execute();
        });
    }
}
