package com.twfhclife.autoreport.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExceptionProcessRecord {

    
    private String id;

    
    private String eventId;

    
    private String action;

    
    private String status;

    
    private String processedBy;

    
    private String processMethod;

    
    private String riskAssessment;

    
    private String processRemark;

    
    private Long processedAt;
}