package com.twfhclife.autodatastream.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


@Service
@RequiredArgsConstructor
public class EventNoGenerator {

    private static final Logger log = LoggerFactory.getLogger(EventNoGenerator.class);
    private static final String PREFIX = "EV";
    private static final String KEY_PREFIX = "auto:data-stream:event-no:";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final String SEQ_FORMAT = "%012d";

    private final StringRedisTemplate redisTemplate;

    
    public String next() {
        String date = LocalDate.now().format(DATE_FORMAT);
        String key = KEY_PREFIX + date;
        try {
            Long seq = redisTemplate.opsForValue().increment(key);
            if (seq != null) {
                if (seq == 1L) {
                    redisTemplate.expire(key, Duration.ofDays(7));
                }
                return PREFIX + date + String.format(SEQ_FORMAT, seq);
            }
        } catch (Exception e) {
            log.warn("Failed to increment event-no counter in Redis, fallback to timestamp: {}", e.getMessage());
        }
        return PREFIX + date + String.format(SEQ_FORMAT, System.currentTimeMillis() % 1_000_000_000_000L);
    }
}
