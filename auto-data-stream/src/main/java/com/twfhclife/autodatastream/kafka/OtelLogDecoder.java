package com.twfhclife.autodatastream.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twfhclife.autodatastream.model.LogEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public final class OtelLogDecoder {

    private static final Logger log = LoggerFactory.getLogger(OtelLogDecoder.class);

    private OtelLogDecoder() {
    }

    public static LogEntry decode(String json, ObjectMapper mapper) throws Exception {
        if (json == null || json.isBlank()) {
            return null;
        }
        JsonNode root = mapper.readTree(json);
        if (root == null || root.isNull()) {
            return null;
        }
        if (!root.isObject() || !root.path("body").isObject()) {
            throw new IllegalArgumentException("Log message must contain an object body");
        }
        JsonNode body = root.path("body");
        LogEntry entry = new LogEntry();
        entry.setId(root.path("id").asText(null));
        entry.setMessage(body.path("stringValue").asText(null));
        entry.setTimestamp(root.path("timestamp").asText(null));
        entry.setTimeMillis(root.path("timeMillis").asLong(0));
        entry.setLevel(root.path("severityText").asText(null));

        Map<String, JsonNode> fields = new LinkedHashMap<>();
        JsonNode attrs = root.path("attributes");
        if (attrs.isArray()) {
            for (JsonNode attr : attrs) {
                String key = attr.path("key").asText(null);
                JsonNode value = attr.path("value").path("stringValue");
                if (key != null && !value.isMissingNode() && !value.isNull()) {
                    fields.put(key, value);
                }
            }
        }
        Iterator<Map.Entry<String, JsonNode>> bodyFields = body.fields();
        while (bodyFields.hasNext()) {
            Map.Entry<String, JsonNode> field = bodyFields.next();
            if (field.getValue().isValueNode() && !field.getValue().isNull()) {
                fields.put(field.getKey(), field.getValue());
            }
        }
        Map<String, String> attributes = new LinkedHashMap<>();
        for (Map.Entry<String, JsonNode> field : fields.entrySet()) {
            String key = field.getKey();
            String v = field.getValue().asText();
            if ("host".equals(key)) {
                entry.setHost(v);
            } else if ("source".equals(key)) {
                entry.setSource(v);
            } else if ("file".equals(key)) {
                entry.setFile(v);
            } else if ("biz.name".equals(key)) {
                entry.setBizName(v);
            } else if ("time_millis".equals(key)) {
                try {
                    entry.setTimeMillis(Long.parseLong(v));
                } catch (NumberFormatException ignored) {
                }
            } else if ("level".equals(key)) {
                entry.setLevel(v);
            } else if ("thread".equals(key)) {
                entry.setThread(v);
            } else if ("logger".equals(key)) {
                entry.setLogger(v);
            } else if ("message".equals(key)) {
                entry.setMessage(v);
            } else {
                attributes.put(key, v);
            }
        }
        if (!attributes.isEmpty()) {
            entry.setAttributes(attributes);
        }
        String bizName = entry.getBizName();
        int separator = bizName == null ? -1 : bizName.lastIndexOf('_');
        if (separator <= 0) {
            log.warn("Skipped log message with invalid bizName [{}]: missing '_' prefix", bizName);
            return null;
        }
        entry.setSystem(bizName.substring(0, separator));
        if (entry.getTimeMillis() > 0) {
            entry.setDayKey(LocalDate.ofInstant(
                    Instant.ofEpochMilli(entry.getTimeMillis()), ZoneId.systemDefault()).toString());
        }
        return entry;
    }
}
