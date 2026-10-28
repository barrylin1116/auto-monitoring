package com.twfhclife.autoreport.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExceptionSetting {

    
    private String systemType;

    
    @JsonProperty("eventType")
    private String eventType;

    
    @JsonSetter("eventTypes")
    private void setEventTypes(JsonNode node) {
        if (node != null && node.isArray() && node.size() > 0) {
            this.eventType = node.get(0).asText();
        } else if (node != null && node.isTextual()) {
            this.eventType = node.asText();
        }
    }

    
    private String autoProcess;

    
    private String riskLevel;

    
    private String autoClose;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
