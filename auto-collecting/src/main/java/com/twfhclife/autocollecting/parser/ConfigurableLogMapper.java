package com.twfhclife.autocollecting.parser;

import com.twfhclife.autocollecting.config.CollectProperties.LogSource;
import com.twfhclife.autocollecting.config.CollectProperties.PatternConfig;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public final class ConfigurableLogMapper {

    private static final Map<String, Pattern> PATTERN_CACHE = new ConcurrentHashMap<>();

    private static final DateTimeFormatter TARGET_TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final List<DateTimeFormatter> SOURCE_TIME_FORMATS = List.of(
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss.SSS"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

    private ConfigurableLogMapper() {
    }

    public static Map<String, String> map(LogSource source, String message, String bizName) {
        Map<String, String> out = new LinkedHashMap<>();
        if (message == null || message.isBlank()) {
            return out;
        }

        List<PatternConfig> patterns = source.getPatterns();
        boolean patternMatched = false;
        if (patterns != null && !patterns.isEmpty()) {
            
            for (PatternConfig pc : patterns) {
                if (!pc.isValid()) {
                    continue;
                }
                
                if (pc.getBizName() == null || pc.getBizName().isBlank()
                        || !pc.getBizName().equals(bizName)) {
                    continue;
                }
                Matcher matcher = compile(pc.getPattern()).matcher(message);
                if (matcher.find()) {
                    Map<String, String> fields = pc.getFields();
                    if (fields != null) {
                        for (Map.Entry<String, String> e : fields.entrySet()) {
                            String value = group(matcher, e.getKey());
                            if (value != null) {
                                out.put(e.getValue(), value);
                            }
                        }
                    }
                    patternMatched = true;
                    break;
                }
            }
        }

        
        
        if (patterns != null && !patterns.isEmpty() && !patternMatched) {
            return null;
        }

        Map<String, String> labels = source.getLabels();
        if (labels != null && !labels.isEmpty()) {
            Map<String, String> labelValues = extractLabels(message, labels.keySet());
            for (Map.Entry<String, String> e : labels.entrySet()) {
                String value = labelValues.get(e.getKey());
                if (value != null) {
                    out.put(e.getValue(), value);
                }
            }
        }

        List<String> dateTimeFields = source.getDateTimeFields();
        if (dateTimeFields != null && !dateTimeFields.isEmpty()) {
            for (String key : dateTimeFields) {
                String value = out.get(key);
                if (value != null) {
                    out.put(key, normalizeDateTime(value));
                }
            }
        }
        return out;
    }

    private static Pattern compile(String regex) {
        return PATTERN_CACHE.computeIfAbsent(regex, Pattern::compile);
    }

    
    static String normalizeDateTime(String value) {
        for (DateTimeFormatter formatter : SOURCE_TIME_FORMATS) {
            try {
                return LocalDateTime.parse(value, formatter).format(TARGET_TIME_FORMAT);
            } catch (DateTimeParseException ignored) {
                
            }
        }
        return value;
    }

    private static String group(Matcher matcher, String group) {
        try {
            String value = matcher.group(group);
            if (value == null) {
                return null;
            }
            String trimmed = value.trim();
            return trimmed.isEmpty() ? null : trimmed;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static Map<String, String> extractLabels(String message, Set<String> labels) {
        Map<String, String> result = new LinkedHashMap<>();
        if (labels.isEmpty()) {
            return result;
        }
        String alternation = labels.stream()
                .map(l -> Pattern.quote(l) + "[:：]")
                .collect(Collectors.joining("|"));
        Matcher matcher = Pattern.compile(alternation).matcher(message);
        String currentLabel = null;
        int valueStart = -1;
        while (matcher.find()) {
            if (currentLabel != null) {
                result.put(currentLabel, clean(message.substring(valueStart, matcher.start())));
            }
            String raw = matcher.group();
            currentLabel = raw.substring(0, raw.length() - 1);
            valueStart = matcher.end();
        }
        if (currentLabel != null) {
            result.put(currentLabel, clean(message.substring(valueStart)));
        }
        return result;
    }

    private static String clean(String value) {
        if (value == null) {
            return null;
        }
        String result = value.strip();
        if (result.isEmpty()) {
            return null;
        }
        if (result.endsWith(",")) {
            result = result.substring(0, result.length() - 1).strip();
        }
        return result.isEmpty() ? null : result;
    }
}
