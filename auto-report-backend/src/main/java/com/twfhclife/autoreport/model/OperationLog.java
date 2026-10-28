package com.twfhclife.autoreport.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OperationLog {

    
    @JsonProperty("id")
    private String id;

    
    @JsonProperty("systemType")
    private String systemType;

    
    @JsonProperty("account")
    private String account;

    
    @JsonProperty("operationTime")
    private String operationTime;

    
    @JsonProperty("functionScreen")
    private String functionScreen;

    
    @JsonProperty("action")
    private String action;

    
    @JsonProperty("actionResult")
    private String actionResult;

    
    @JsonProperty("actionContent")
    private String actionContent;
}