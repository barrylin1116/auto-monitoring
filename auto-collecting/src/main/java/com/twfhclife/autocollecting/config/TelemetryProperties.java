package com.twfhclife.autocollecting.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "app.telemetry")
public class TelemetryProperties {

    private Filter filter = new Filter();
    private Processor processor = new Processor();

    @Data
    public static class Filter {

        private String minLevel = "INFO";
        private List<String> sourcesAllowlist = new ArrayList<>();
        private List<String> sourcesDenylist = new ArrayList<>();
    }

    @Data
    public static class Processor {

        private int maxMessageLength = 2000;
        private boolean addProcessedAt = true;
        private boolean dropEmptyMessage = true;
        private List<String> redactRegexes = new ArrayList<>(List.of(
                "(?i)(password|passwd|pwd|token|secret|api[_-]?key)=([^&\\s,\"]+)"));
    }
}