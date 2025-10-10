package io.mochaapi.client.config;

/**
 * Simple security configuration for MochaJSON.
 * Provides basic control over URL validation and localhost access.
 * 
 * @since 1.3.0
 */
public class SecurityConfig {
    
    private final boolean allowLocalhost;
    
    /**
     * Creates a new SecurityConfig with production-safe defaults (localhost blocked).
     */
    public SecurityConfig() {
        this(false); // Production-safe default
    }
    
    /**
     * Creates a new SecurityConfig with custom localhost setting.
     * 
     * @param allowLocalhost whether to allow localhost URLs
     */
    public SecurityConfig(boolean allowLocalhost) {
        this.allowLocalhost = allowLocalhost;
    }
    
    /**
     * Returns whether localhost URLs are allowed.
     * 
     * @return true if localhost is allowed, false otherwise
     */
    public boolean isAllowLocalhost() {
        return allowLocalhost;
    }
    
    /**
     * Creates a SecurityConfig suitable for development/testing.
     * Allows localhost for local development.
     * 
     * @return SecurityConfig for development use
     */
    public static SecurityConfig forDevelopment() {
        return new SecurityConfig(true);
    }
    
    /**
     * Creates a SecurityConfig suitable for production use.
     * Blocks localhost for security.
     * 
     * @return SecurityConfig for production use
     */
    public static SecurityConfig forProduction() {
        return new SecurityConfig(false);
    }
    
    @Override
    public String toString() {
        return String.format("SecurityConfig{allowLocalhost=%s}", allowLocalhost);
    }
}
