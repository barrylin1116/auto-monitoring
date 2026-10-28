package com.twfhclife.autoreport.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExceptionEventUpdateRequest {

    
    private String status;

    
    private String processedBy;

    
    private String processMethod;

    
    private String riskAssessment;

    
    private String processRemark;

    
    private Long processedAt;

    
    private String action;
}
