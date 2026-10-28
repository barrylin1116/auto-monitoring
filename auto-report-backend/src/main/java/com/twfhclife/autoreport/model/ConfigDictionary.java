package com.twfhclife.autoreport.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfigDictionary {

    
    private String type;

    
    private String code;

    
    private String label;

    
    private Integer sort;
}