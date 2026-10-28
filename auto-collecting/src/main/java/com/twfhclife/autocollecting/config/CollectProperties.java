package com.twfhclife.autocollecting.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
@ConfigurationProperties(prefix = "app.collect")
public class CollectProperties {

    private long tailDelayMs = 1000;
    private List<LogSource> sources = new ArrayList<>();

    @Data
    public static class LogSource {

        private String name;
        private String path;
        private boolean fromBeginning = false;

        
        private String bizName;

        
        private String linePattern;

        
        private Map<String, String> lineFields = new LinkedHashMap<>();

        
        private List<PatternConfig> patterns = new ArrayList<>();

        
        private Map<String, String> labels = new LinkedHashMap<>();

        
        private List<String> dateTimeFields = new ArrayList<>();

        public boolean hasMapping() {
            return (patterns != null && !patterns.isEmpty())
                    || (bizName != null && !bizName.isBlank())
                    || (labels != null && !labels.isEmpty());
        }

        public boolean hasLinePattern() {
            return linePattern != null && !linePattern.isBlank();
        }
    }

    @Data
    public static class PatternConfig {

        
        private String pattern;

        
        private String bizName;

        
        private Map<String, String> fields = new LinkedHashMap<>();

        public boolean isValid() {
            return pattern != null && !pattern.isBlank() && fields != null && !fields.isEmpty();
        }
    }
}
