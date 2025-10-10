package io.mochaapi.client.internal;

import io.mochaapi.client.config.SecurityConfig;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.regex.Pattern;

/**
 * Utility class for URL validation and security checks.
 * 
 * @since 1.1.0
 */
public class Utils {
    
    // Pattern to match dangerous URL schemes that could be used for open redirects
    private static final Pattern DANGEROUS_SCHEMES = Pattern.compile(
        "^(javascript|data|vbscript|file|ftp):", Pattern.CASE_INSENSITIVE);
    
    // Pattern to match localhost and private IP ranges
    private static final Pattern LOCALHOST_PATTERN = Pattern.compile(
        "^(localhost|127\\.|192\\.168\\.|10\\.|172\\.(1[6-9]|2[0-9]|3[0-1])\\.)", Pattern.CASE_INSENSITIVE);
    
    // Default security configuration (production-safe)
    private static SecurityConfig DEFAULT_SECURITY_CONFIG = SecurityConfig.forProduction();
    
    /**
     * Sets the default security configuration for URL validation.
     * 
     * @param config the security configuration
     * @throws IllegalArgumentException if config is null
     */
    public static void setDefaultSecurityConfig(SecurityConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("Security configuration cannot be null");
        }
        DEFAULT_SECURITY_CONFIG = config;
    }
    
    /**
     * Validates a URL for security and format correctness using default security settings.
     * 
     * @param url the URL to validate
     * @return true if the URL is valid and safe
     * @throws IllegalArgumentException if the URL is invalid or unsafe
     */
    public static boolean validateUrl(String url) {
        return validateUrl(url, DEFAULT_SECURITY_CONFIG);
    }
    
    /**
     * Validates a URL for security and format correctness.
     * 
     * @param url the URL to validate
     * @param securityConfig the security configuration to use
     * @return true if the URL is valid and safe
     * @throws IllegalArgumentException if the URL is invalid or unsafe
     */
    public static boolean validateUrl(String url, SecurityConfig securityConfig) {
        if (url == null || url.trim().isEmpty()) {
            throw new IllegalArgumentException("URL cannot be null or empty");
        }
        if (securityConfig == null) {
            throw new IllegalArgumentException("Security configuration cannot be null");
        }
        
        try {
            URI uri = new URI(url.trim());
            
            // Check if URL has a scheme
            String scheme = uri.getScheme();
            if (scheme == null || scheme.isEmpty()) {
                throw new IllegalArgumentException("URL must have a valid scheme (http/https)");
            }
            
            // Only allow HTTP and HTTPS schemes
            if (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https")) {
                throw new IllegalArgumentException("Only HTTP and HTTPS schemes are allowed");
            }
            
            // Check for dangerous schemes (additional safety check)
            if (DANGEROUS_SCHEMES.matcher(url).find()) {
                throw new IllegalArgumentException("Dangerous URL scheme detected");
            }
            
            // Check for valid host
            String host = uri.getHost();
            if (host == null || host.isEmpty()) {
                throw new IllegalArgumentException("URL must have a valid host");
            }
            
            // Check localhost restrictions based on security configuration
            if (LOCALHOST_PATTERN.matcher(host).find()) {
                if (!securityConfig.isAllowLocalhost()) {
                    throw new IllegalArgumentException("Localhost URLs are not allowed. Use SecurityConfig.forDevelopment() to allow localhost.");
                }
            }
            
            return true;
            
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid URL format: " + e.getMessage());
        }
    }
    
    
    /**
     * Safely parses a URL string into a URI object.
     * 
     * @param url the URL string to parse
     * @return the parsed URI
     * @throws IllegalArgumentException if the URL is invalid
     */
    public static URI parseUrl(String url) {
        validateUrl(url);
        try {
            return new URI(url.trim());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid URL format: " + e.getMessage());
        }
    }
    
    /**
     * Checks if a URL is safe for HTTP requests.
     * 
     * @param url the URL to check
     * @return true if the URL is safe
     */
    public static boolean isUrlSafe(String url) {
        try {
            return validateUrl(url);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}