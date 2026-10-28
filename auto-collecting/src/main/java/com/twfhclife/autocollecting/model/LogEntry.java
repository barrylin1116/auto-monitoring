package com.twfhclife.autocollecting.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LogEntry {

    private String host;
    private String source;
    private String file;
    private String timestamp;
    private long timeMillis;
    private String level;
    private String thread;
    private String logger;
    private String message;
    private String bizName;
    private String processedAt;
}