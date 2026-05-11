package com.maas.gateway.service;

import com.maas.common.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimiter {
    private final ConcurrentHashMap<String, WindowCounter> counters = new ConcurrentHashMap<>();

    public void check(String keyId, int maxRequestsPerMinute) {
        WindowCounter counter = counters.computeIfAbsent(keyId, k -> new WindowCounter());
        int current = counter.increment();
        if (current > maxRequestsPerMinute) {
            throw new BusinessException(429, "Rate limit exceeded");
        }
    }

    private static class WindowCounter {
        private final AtomicInteger count = new AtomicInteger(0);
        private volatile long windowStart = System.currentTimeMillis() / 60_000;

        int increment() {
            long currentWindow = System.currentTimeMillis() / 60_000;
            if (currentWindow != windowStart) {
                synchronized (this) {
                    if (currentWindow != windowStart) {
                        count.set(0);
                        windowStart = currentWindow;
                    }
                }
            }
            return count.incrementAndGet();
        }
    }

    public void cleanStaleEntries() {
        counters.clear();
    }
}
