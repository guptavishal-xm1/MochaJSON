package io.mochaapi.client;

import java.time.Duration;

/**
 * Simple retry configuration for failed requests.
 * Provides basic retry functionality with fixed delay.
 * 
 * <p>Example usage:</p>
 * <pre>
 * BasicRetry retry = new BasicRetry(3, Duration.ofSeconds(1));
 * </pre>
 * 
 * @since 1.3.0
 */
public class BasicRetry {
    
    private final int maxAttempts;
    private final Duration delay;
    
    /**
     * Creates a new BasicRetry configuration.
     * 
     * @param maxAttempts maximum number of attempts (including initial attempt)
     * @param delay delay between retry attempts
     * @throws IllegalArgumentException if maxAttempts is less than 1 or delay is negative
     */
    public BasicRetry(int maxAttempts, Duration delay) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }
        if (delay.isNegative()) {
            throw new IllegalArgumentException("delay cannot be negative");
        }
        this.maxAttempts = maxAttempts;
        this.delay = delay;
    }
    
    /**
     * Returns the maximum number of attempts.
     * 
     * @return maximum attempts
     */
    public int getMaxAttempts() {
        return maxAttempts;
    }
    
    /**
     * Returns the delay between retry attempts.
     * 
     * @return delay duration
     */
    public Duration getDelay() {
        return delay;
    }
    
    /**
     * Determines if a request should be retried based on the attempt number.
     * 
     * @param attemptNumber current attempt number (1-based)
     * @return true if should retry
     */
    public boolean shouldRetry(int attemptNumber) {
        return attemptNumber < maxAttempts;
    }
    
    /**
     * Creates a BasicRetry with 3 attempts and 1-second delay.
     * 
     * @return standard retry configuration
     */
    public static BasicRetry standard() {
        return new BasicRetry(3, Duration.ofSeconds(1));
    }
    
    /**
     * Creates a BasicRetry with 5 attempts and 500ms delay.
     * 
     * @return fast retry configuration
     */
    public static BasicRetry fast() {
        return new BasicRetry(5, Duration.ofMillis(500));
    }
    
    /**
     * Creates a BasicRetry with 2 attempts and 2-second delay.
     * 
     * @return conservative retry configuration
     */
    public static BasicRetry conservative() {
        return new BasicRetry(2, Duration.ofSeconds(2));
    }
    
    @Override
    public String toString() {
        return String.format("BasicRetry{maxAttempts=%d, delay=%s}", maxAttempts, delay);
    }
}
