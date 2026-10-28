package com.twfhclife.autodatastream.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LogEntry {

    private String id;
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

    
    private String system;

    
    private String dayKey;

    
    private Map<String, String> attributes = new LinkedHashMap<>();

    @JsonIgnore
    private List<String> matchedRules = new ArrayList<>();

    
    @JsonIgnore
    private Map<String, List<String>> matchedRuleLogIds = new LinkedHashMap<>();

    
    public void addMatchedRule(String rule) {
        recordMatchedRule(rule, id == null ? List.of() : List.of(id));
    }

    
    public void addMatchedRule(String rule, List<LogEntry> logs) {
        List<String> logIds = new ArrayList<>();
        if (logs != null) {
            for (LogEntry le : logs) {
                if (le != null && le.getId() != null) {
                    logIds.add(le.getId());
                }
            }
        }
        recordMatchedRule(rule, logIds);
    }

    private void recordMatchedRule(String rule, List<String> logIds) {
        if (!matchedRules.contains(rule)) {
            matchedRules.add(rule);
        }
        matchedRuleLogIds.put(rule, new ArrayList<>(logIds));
    }

    
    public List<String> getRelatedLogIds(String rule) {
        return matchedRuleLogIds.getOrDefault(rule, List.of());
    }
}