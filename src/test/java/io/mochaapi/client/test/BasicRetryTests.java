package io.mochaapi.client.test;

import io.mochaapi.client.BasicRetry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Simple tests for BasicRetry functionality in MochaJSON v1.3.0.
 */
public class BasicRetryTests {
    
    @Test
    @DisplayName("Test BasicRetry creation and configuration")
    public void testBasicRetryCreation() {
        System.out.println("\n=== Testing BasicRetry creation ===");
        
        // Test custom configuration
        BasicRetry retry = new BasicRetry(5, Duration.ofSeconds(2));
        
        assertEquals(5, retry.getMaxAttempts());
        assertEquals(Duration.ofSeconds(2), retry.getDelay());
        
        System.out.println("✓ Custom BasicRetry created: " + retry.getMaxAttempts() + " attempts, " + 
                          retry.getDelay().toMillis() + "ms delay");
    }
    
    @Test
    @DisplayName("Test BasicRetry factory methods")
    public void testBasicRetryFactoryMethods() {
        System.out.println("\n=== Testing BasicRetry factory methods ===");
        
        // Test standard retry
        BasicRetry standard = BasicRetry.standard();
        assertEquals(3, standard.getMaxAttempts());
        assertEquals(Duration.ofSeconds(1), standard.getDelay());
        System.out.println("✓ Standard retry: " + standard.getMaxAttempts() + " attempts");
        
        // Test fast retry
        BasicRetry fast = BasicRetry.fast();
        assertEquals(5, fast.getMaxAttempts());
        assertEquals(Duration.ofMillis(500), fast.getDelay());
        System.out.println("✓ Fast retry: " + fast.getMaxAttempts() + " attempts");
        
        // Test conservative retry
        BasicRetry conservative = BasicRetry.conservative();
        assertEquals(2, conservative.getMaxAttempts());
        assertEquals(Duration.ofSeconds(2), conservative.getDelay());
        System.out.println("✓ Conservative retry: " + conservative.getMaxAttempts() + " attempts");
    }
    
    @Test
    @DisplayName("Test retry logic")
    public void testRetryLogic() {
        System.out.println("\n=== Testing retry logic ===");
        
        BasicRetry retry = new BasicRetry(3, Duration.ofMillis(100));
        
        // Test retry decisions
        assertTrue(retry.shouldRetry(1), "Should retry on attempt 1");
        assertTrue(retry.shouldRetry(2), "Should retry on attempt 2");
        assertFalse(retry.shouldRetry(3), "Should not retry on attempt 3");
        assertFalse(retry.shouldRetry(4), "Should not retry on attempt 4");
        
        System.out.println("✓ Retry logic working correctly");
        System.out.println("  - Attempt 1: " + retry.shouldRetry(1));
        System.out.println("  - Attempt 2: " + retry.shouldRetry(2));
        System.out.println("  - Attempt 3: " + retry.shouldRetry(3));
    }
    
    @Test
    @DisplayName("Test retry with different configurations")
    public void testRetryWithDifferentConfigurations() {
        System.out.println("\n=== Testing retry with different configurations ===");
        
        // Test with 1 attempt (no retries)
        BasicRetry noRetry = new BasicRetry(1, Duration.ofSeconds(1));
        assertFalse(noRetry.shouldRetry(1), "Should not retry with max attempts = 1");
        
        // Test with 10 attempts
        BasicRetry manyRetries = new BasicRetry(10, Duration.ofMillis(50));
        assertTrue(manyRetries.shouldRetry(5), "Should retry on attempt 5 with max attempts = 10");
        assertTrue(manyRetries.shouldRetry(9), "Should retry on attempt 9 with max attempts = 10");
        assertFalse(manyRetries.shouldRetry(10), "Should not retry on attempt 10");
        
        System.out.println("✓ Different retry configurations working");
    }
    
    @Test
    @DisplayName("Test retry toString method")
    public void testRetryToString() {
        System.out.println("\n=== Testing retry toString ===");
        
        BasicRetry retry = new BasicRetry(3, Duration.ofMillis(500));
        String retryString = retry.toString();
        
        assertNotNull(retryString);
        assertTrue(retryString.contains("3"));
        assertTrue(retryString.contains("PT0.5S") || retryString.contains("500"));
        
        System.out.println("✓ Retry toString: " + retryString);
    }
    
    @Test
    @DisplayName("Test retry edge cases")
    public void testRetryEdgeCases() {
        System.out.println("\n=== Testing retry edge cases ===");
        
        // Test zero attempts (should throw exception)
        try {
            new BasicRetry(0, Duration.ofSeconds(1));
            fail("Should throw exception for zero max attempts");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("maxAttempts must be at least 1"));
        }
        
        // Test negative attempts (should throw exception)
        try {
            new BasicRetry(-1, Duration.ofSeconds(1));
            fail("Should throw exception for negative max attempts");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("maxAttempts must be at least 1"));
        }
        
        // Test zero delay (should be allowed)
        BasicRetry zeroDelay = new BasicRetry(3, Duration.ZERO);
        assertEquals(Duration.ZERO, zeroDelay.getDelay());
        assertTrue(zeroDelay.shouldRetry(1), "Should retry even with zero delay");
        
        // Test negative delay (should throw exception)
        try {
            new BasicRetry(3, Duration.ofSeconds(-1));
            fail("Should throw exception for negative delay");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("delay cannot be negative"));
        }
        
        System.out.println("✓ Edge cases handled correctly");
    }
    
    @Test
    @DisplayName("Test retry consistency")
    public void testRetryConsistency() {
        System.out.println("\n=== Testing retry consistency ===");
        
        BasicRetry retry = new BasicRetry(4, Duration.ofMillis(200));
        
        // Test that the same input always gives the same output
        for (int i = 0; i < 10; i++) {
            assertTrue(retry.shouldRetry(1));
            assertTrue(retry.shouldRetry(2));
            assertTrue(retry.shouldRetry(3));
            assertFalse(retry.shouldRetry(4));
            assertFalse(retry.shouldRetry(5));
        }
        
        // Test that configuration doesn't change
        assertEquals(4, retry.getMaxAttempts());
        assertEquals(Duration.ofMillis(200), retry.getDelay());
        
        System.out.println("✓ Retry consistency verified");
    }
}
