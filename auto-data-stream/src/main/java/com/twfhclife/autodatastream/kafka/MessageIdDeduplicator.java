package com.twfhclife.autodatastream.kafka;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Component
public class MessageIdDeduplicator {

    private static final long WINDOW_MILLIS = 30 * 60 * 1000L;

    private static final long CLEANUP_INTERVAL_MILLIS = 60 * 1000L;

    private final Map<String, Long> seen = new ConcurrentHashMap<>();

    private volatile long lastCleanup = System.currentTimeMillis();

    
    public boolean isDuplicate(String id) {
        if (id == null || id.isBlank()) {
            return false;
        }
        long now = System.currentTimeMillis();
        maybeCleanup(now);
        Long previous = seen.putIfAbsent(id, now);
        return previous != null;
    }

    private void maybeCleanup(long now) {
        long last = lastCleanup;
        if (now - last < CLEANUP_INTERVAL_MILLIS) {
            return;
        }
        if (lastCleanup == last) {
            lastCleanup = now;
            long cutoff = now - WINDOW_MILLIS;
            seen.entrySet().removeIf(e -> e.getValue() < cutoff);
        }
    }
}
