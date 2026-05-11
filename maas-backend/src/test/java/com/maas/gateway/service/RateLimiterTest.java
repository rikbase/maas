package com.maas.gateway.service;

import com.maas.common.exception.BusinessException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RateLimiterTest {

    @Test
    void check_shouldAllowUpToLimit() {
        RateLimiter limiter = new RateLimiter();
        // Should not throw for first 5 requests
        for (int i = 0; i < 5; i++) {
            limiter.check("test-key", 5);
        }
    }

    @Test
    void check_shouldThrowWhenExceeded() {
        RateLimiter limiter = new RateLimiter();
        for (int i = 0; i < 5; i++) {
            limiter.check("test-key", 5);
        }
        assertThrows(BusinessException.class, () -> limiter.check("test-key", 5));
    }

    @Test
    void check_shouldAllowAfterWindowReset() {
        RateLimiter limiter = new RateLimiter();
        // Fill the window
        for (int i = 0; i < 5; i++) {
            limiter.check("test-key", 5);
        }
        // Should be blocked
        assertThrows(BusinessException.class, () -> limiter.check("test-key", 5));

        // Simulate next window by clearing
        limiter.cleanStaleEntries();
        // Now should be allowed again
        limiter.check("test-key", 5);
    }

    @Test
    void check_shouldTrackKeysIndependently() {
        RateLimiter limiter = new RateLimiter();
        for (int i = 0; i < 3; i++) {
            limiter.check("key-a", 3);
            limiter.check("key-b", 5);
        }
        // key-a should be at limit now
        assertThrows(BusinessException.class, () -> limiter.check("key-a", 3));
        // key-b should still be allowed
        limiter.check("key-b", 5);
    }
}
