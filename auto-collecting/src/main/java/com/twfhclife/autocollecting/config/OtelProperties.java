package com.twfhclife.autocollecting.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.otel")
public class OtelProperties {

    private boolean enabled = false;
    private String endpoint = "http://localhost:4318/v1/logs";
    private String serviceName = "auto-collecting";
}