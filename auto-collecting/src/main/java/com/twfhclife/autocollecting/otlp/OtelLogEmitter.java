package com.twfhclife.autocollecting.otlp;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class OtelLogEmitter {


    private final String endpoint;
    private final boolean enabled;

    public OtelLogEmitter(@Value("${otlp.log.endpoint:http://localhost:4318/v1/logs}") String endpoint,
                         @Value("${app.otel.enabled:false}") boolean enabled) {
        this.endpoint = endpoint;
        this.enabled = enabled;
    }

    public void emit(String json) {
        if (!enabled) {
            return;
        }
        if (StringUtils.isEmpty(json)) {
            return;
        }
        postToEndpoint(json);
    }


    private void postToEndpoint(String json) {
        try {
            HttpURLConnection conn = (HttpURLConnection) URI.create(endpoint).toURL().openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            byte[] data = json.getBytes(StandardCharsets.UTF_8);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(data);
            }
            conn.getResponseCode();
            conn.disconnect();
        } catch (Exception e) {
            log.warn("Failed to post OTLP JSON: {}", e.getMessage());
        }
    }

    @PreDestroy
    public void shutdown() {
        log.info("OtelLogEmitter shutdown");
    }
}
