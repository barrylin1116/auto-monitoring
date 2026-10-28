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
public class ExceptionEvent {

    
    @JsonProperty("eventNo")
    private String eventNo;

    
    @JsonProperty("occurredAt")
    private long occurredAt;

    
    @JsonProperty("eventType")
    private String eventType;

    
    @JsonProperty("riskLevel")
    private String riskLevel;

    
    @JsonProperty("systemType")
    private String systemType;

    
    @JsonProperty("bizName")
    private String bizName;

    
    @JsonProperty("account")
    private String account;

    
    @JsonProperty("name")
    private String name;

    
    @JsonProperty("channelBranch")
    private String channelBranch;

    
    @JsonProperty("deviceInfo")
    private String deviceInfo;

    
    @JsonProperty("sourceIp")
    private String sourceIp;

    
    @JsonProperty("description")
    private String description;

    
    @JsonProperty("detailDescription")
    private String detailDescription;

    
    @JsonProperty("status")
    private String status;

    
    @JsonProperty("createdAt")
    private long createdAt;

    
    @JsonProperty("relatedLogIds")
    private List<String> relatedLogIds;
}
