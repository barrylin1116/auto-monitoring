package com.twfhclife.autoreport.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
public class ExceptionEvent {

    
    private String id;

    
    private String eventNo;

    
    private Long occurredAt;

    
    private String eventType;

    
    private String riskLevel;

    
    private String systemType;

    
    private String bizName;

    
    private String account;

    
    private String name;

    
    private String channelBranch;

    
    private String deviceInfo;

    
    private String sourceIp;

    
    private String description;

    
    private String detailDescription;

    
    private String status;

    
    private String processedBy;

    
    private String processMethod;

    
    private String riskAssessment;

    
    private String processRemark;

    
    private Long processedAt;

    
    private List<String> relatedLogIds;

    
    private List<ExceptionAttachment> attachments;
}
