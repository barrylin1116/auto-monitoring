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
public class ExceptionAttachment {

    
    private String id;

    
    private String eventId;

    
    private String fileName;

    
    private String contentType;

    
    private Long size;

    
    private String content;

    
    private String uploadedBy;

    
    private Long uploadedAt;
}
