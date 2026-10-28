package com.twfhclife.autocollecting.processor;

import com.twfhclife.autocollecting.config.TelemetryProperties;
import com.twfhclife.autocollecting.model.LogEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Component
public class TelemetryProcessor {

    private static final Logger log = LoggerFactory.getLogger(TelemetryProcessor.class);

    private static final Map<String, Integer> LEVEL_ORDER = Map.of(
            "TRACE", 0,
            "DEBUG", 1,
            "INFO", 2,
            "WARN", 3,
            "ERROR", 4,
            "FATAL", 5);

    private final TelemetryProperties properties;
    private final int minLevelOrder;
    private final List<Pattern> redactPatterns;

    public TelemetryProcessor(TelemetryProperties properties) {
        this.properties = properties;
        this.minLevelOrder = LEVEL_ORDER.getOrDefault(
                properties.getFilter().getMinLevel().toUpperCase(), 2);
        this.redactPatterns = new ArrayList<>();
        for (String regex : properties.getProcessor().getRedactRegexes()) {
            redactPatterns.add(Pattern.compile(regex));
        }
    }

    public boolean process(LogEntry entry) {
        return passesFilter(entry) && normalize(entry);
    }

    private boolean passesFilter(LogEntry entry) {
        TelemetryProperties.Filter filter = properties.getFilter();

        List<String> allowlist = filter.getSourcesAllowlist();
        if (!allowlist.isEmpty() && !allowlist.contains(entry.getSource())) {
            return false;
        }
        List<String> denylist = filter.getSourcesDenylist();
        if (!denylist.isEmpty() && denylist.contains(entry.getSource())) {
            return false;
        }

        String level = entry.getLevel() == null ? "" : entry.getLevel().toUpperCase();
        int order = LEVEL_ORDER.getOrDefault(level, -1);
        return order >= minLevelOrder;
    }

    private boolean normalize(LogEntry entry) {
        TelemetryProperties.Processor processor = properties.getProcessor();

        if (entry.getLevel() != null) {
            entry.setLevel(entry.getLevel().trim().toUpperCase());
        }

        String message = entry.getMessage() == null ? "" : entry.getMessage().trim();
        if (processor.isDropEmptyMessage() && message.isEmpty()) {
            return false;
        }
        if (processor.getMaxMessageLength() > 0 && message.length() > processor.getMaxMessageLength()) {
            message = message.substring(0, processor.getMaxMessageLength());
            log.debug("Truncated message for source={}", entry.getSource());
        }
        for (Pattern pattern : redactPatterns) {
            message = pattern.matcher(message).replaceAll("$1=***");
        }
        entry.setMessage(message);

        if (processor.isAddProcessedAt()) {
            entry.setProcessedAt(Instant.now().toString());
        }
        return true;
    }
}