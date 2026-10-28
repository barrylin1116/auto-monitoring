package com.twfhclife.autocollecting.parser;

import com.twfhclife.autocollecting.config.CollectProperties.LogSource;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public final class LogLineParser {

    private static final Map<String, Pattern> LINE_PATTERN_CACHE = new ConcurrentHashMap<>();

    private static final List<DateTimeFormatter> TIME_FORMATTERS = List.of(
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss,SSS"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss.SSS"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
            DateTimeFormatter.ofPattern("HH:mm:ss.SSS"),
            DateTimeFormatter.ofPattern("HH:mm:ss,SSS"),
            DateTimeFormatter.ofPattern("HH:mm:ss"));

    private static final Pattern BIZ_MARKER = Pattern.compile(
            "\\[auto-monitoring:([^\\]]+)\\]", Pattern.CASE_INSENSITIVE);

    private static final List<DateTimeFormatter> DATE_FORMATTERS = List.of(
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss,SSS"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss.SSS"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));

    private LogLineParser() {
    }

    public static ParsedLog parse(String line) {
        return parse(line, null);
    }

    
    public static ParsedLog parse(String line, LogSource source) {
        String bizName = null;
        Matcher marker = BIZ_MARKER.matcher(line);
        if (marker.find()) {
            bizName = marker.group(1).trim();
            line = line.substring(0, marker.start()) + line.substring(marker.end());
        }

        if (source != null && source.hasLinePattern()) {
            ParsedLog parsed = parseWithPattern(line, source.getLinePattern(),
                    source.getLineFields(), bizName);
            if (parsed != null) {
                return parsed;
            }
        }
        return new ParsedLog(null, 0, null, null, null, line.trim(), bizName);
    }

    private static ParsedLog parseWithPattern(String line, String regex,
                                              Map<String, String> fields, String markerBizName) {
        Matcher m = compile(regex).matcher(line);
        if (!m.matches()) {
            return null;
        }
        String rawTime = group(m, fields, "timestamp");
        long epochMillis = 0;
        if (rawTime != null) {
            epochMillis = toEpochMillis(rawTime);
            if (epochMillis == 0) {
                epochMillis = toEpochMillisTimeOnly(rawTime);
            }
        }

        String level = group(m, fields, "level");
        if (level != null) {
            level = level.toUpperCase();
        }
        String thread = group(m, fields, "thread");
        String logger = group(m, fields, "logger");
        String method = group(m, fields, "method");
        String message = group(m, fields, "message");
        if (method != null) {
            
            message = message == null ? method : method + " - " + message;
        }
        String bizName = group(m, fields, "bizName");
        if (bizName == null) {
            bizName = markerBizName;
        }
        return new ParsedLog(rawTime, epochMillis, level, thread, logger, message, bizName);
    }

    private static String group(Matcher m, Map<String, String> fields, String fieldName) {
        if (fields == null || fields.isEmpty()) {
            return null;
        }
        String groupName = null;
        for (Map.Entry<String, String> e : fields.entrySet()) {
            if (fieldName.equals(e.getValue())) {
                groupName = e.getKey();
                break;
            }
        }
        if (groupName == null) {
            return null;
        }
        try {
            String value = m.group(groupName);
            if (value == null) {
                return null;
            }
            String trimmed = value.trim();
            return trimmed.isEmpty() ? null : trimmed;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static Pattern compile(String regex) {
        return LINE_PATTERN_CACHE.computeIfAbsent(regex, Pattern::compile);
    }

    private static long toEpochMillisTimeOnly(String raw) {
        for (DateTimeFormatter formatter : TIME_FORMATTERS) {
            try {
                LocalTime time = LocalTime.parse(raw, formatter);
                return LocalDateTime.of(LocalDate.now(), time)
                        .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            } catch (DateTimeParseException ignored) {
            }
        }
        return 0;
    }

    private static long toEpochMillis(String raw) {
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                LocalDateTime dt = LocalDateTime.parse(raw, formatter);
                return dt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            } catch (DateTimeParseException ignored) {
                
            }
        }
        return 0;
    }

    public static final class ParsedLog {

        private final String rawTime;
        private final long epochMillis;
        private final String level;
        private final String thread;
        private final String logger;
        private final String message;
        private final String bizName;

        ParsedLog(String rawTime, long epochMillis, String level, String thread,
                  String logger, String message, String bizName) {
            this.rawTime = rawTime;
            this.epochMillis = epochMillis;
            this.level = level;
            this.thread = thread;
            this.logger = logger;
            this.message = message;
            this.bizName = bizName;
        }

        public String getRawTime() {
            return rawTime;
        }

        public long getEpochMillis() {
            return epochMillis;
        }

        public String getLevel() {
            return level;
        }

        public String getThread() {
            return thread;
        }

        public String getLogger() {
            return logger;
        }

        public String getMessage() {
            return message;
        }

        public String getBizName() {
            return bizName;
        }
    }
}