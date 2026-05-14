package com.maas.dify.service;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class AuthorizationCodeStore {

    private static final Logger log = LoggerFactory.getLogger(AuthorizationCodeStore.class);
    private static final long CODE_TTL_MS = 60_000;
    private static final long EVICTION_INTERVAL_MS = 30_000;

    private final ConcurrentHashMap<String, CodeEntry> store = new ConcurrentHashMap<>();
    private final SecureRandom secureRandom = new SecureRandom();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public record CodeEntry(String clientId, String userId, String state, Instant createdAt) {}

    @PostConstruct
    void startEviction() {
        scheduler.scheduleAtFixedRate(this::evictExpired, EVICTION_INTERVAL_MS, EVICTION_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }

    public String generate(String clientId, String userId, String state) {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        String code = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        store.put(code, new CodeEntry(clientId, userId, state, Instant.now()));
        return code;
    }

    public CodeEntry consume(String code) {
        CodeEntry entry = store.remove(code);
        if (entry == null) return null;
        if (Instant.now().minusMillis(CODE_TTL_MS).isAfter(entry.createdAt())) {
            return null; // expired
        }
        return entry;
    }

    private void evictExpired() {
        Instant cutoff = Instant.now().minusMillis(CODE_TTL_MS);
        store.values().removeIf(e -> cutoff.isAfter(e.createdAt()));
    }
}
