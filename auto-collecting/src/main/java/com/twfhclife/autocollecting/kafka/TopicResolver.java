package com.twfhclife.autocollecting.kafka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
public class TopicResolver {

    private final String suffix;

    public TopicResolver(@Value("${app.kafka.topics.suffix:_collecting_logs}") String suffix) {
        this.suffix = suffix;
    }

    public String resolve(String bizName) {
        String safe = bizName == null || bizName.isBlank()
                ? "unknown" : bizName.replaceAll("[^A-Za-z0-9._-]", "_");
        return safe + suffix;
    }
}
