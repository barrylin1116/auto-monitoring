package com.twfhclife.autodatastream.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExceptionSetting {

    private String systemType;

    
    private String eventType;

    
    private String autoProcess;

    
    private String riskLevel;

    
    private String autoClose;

    
    private String updatedAt;
}