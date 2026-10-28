package com.twfhclife.autodatastream.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExceptionOperationRecord {

    
    @JsonProperty("bizName")
    private String bizName;

    
    @JsonProperty("account")
    private String account;

    
    @JsonProperty("matchedRules")
    private List<String> matchedRules;

    
    @JsonProperty("autoProcess")
    private boolean autoProcess;

    
    @JsonProperty("processStatus")
    private String processStatus;

    
    @JsonProperty("message")
    private String message;

    
    @JsonProperty("processedAt")
    private Long processedAt;

    
    @JsonProperty("processedBy")
    private String processedBy;

    
    @JsonProperty("createdAt")
    private long createdAt;
}