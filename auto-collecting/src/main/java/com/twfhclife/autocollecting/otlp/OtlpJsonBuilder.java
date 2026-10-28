package com.twfhclife.autocollecting.otlp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twfhclife.autocollecting.model.LogEntry;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;


public final class OtlpJsonBuilder {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private OtlpJsonBuilder() {
    }

    public static String build(LogEntry entry, Map<String, String> attributes) {
        try {
            long timeMillis = entry.getTimeMillis() > 0
                    ? entry.getTimeMillis() : System.currentTimeMillis();
            Map<String, Object> root = new LinkedHashMap<>();
            root.put("id", UUID.randomUUID().toString());
            root.put("timestamp", String.valueOf(timeMillis));
            root.put("timeMillis", String.valueOf(timeMillis));
            root.put("severityText", entry.getLevel());
            root.put("severityNumber", toSeverityNumber(entry.getLevel()));

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("host", entry.getHost());
            body.put("source", entry.getSource());
            body.put("file", entry.getFile());
            body.put("biz.name", entry.getBizName());
            body.put("level", entry.getLevel());
            body.put("thread", entry.getThread());
            body.put("logger", entry.getLogger());
            body.put("time_millis", String.valueOf(timeMillis));
            body.put("message", entry.getMessage());
            if (attributes != null) {
                for (Map.Entry<String, String> e : attributes.entrySet()) {
                    body.put(e.getKey(), e.getValue());
                }
            }
            root.put("body", body);
            return MAPPER.writeValueAsString(root);
        } catch (Exception e) {
            return null;
        }
    }

    private static String toSeverityNumber(String level) {
        if (level == null) {
            return "9";
        }
        switch (level.toUpperCase()) {
            case "TRACE":
                return "1";
            case "DEBUG":
                return "5";
            case "WARN":
            case "WARNING":
                return "13";
            case "ERROR":
                return "17";
            case "FATAL":
                return "21";
            default:
                return "9";
        }
    }
}
