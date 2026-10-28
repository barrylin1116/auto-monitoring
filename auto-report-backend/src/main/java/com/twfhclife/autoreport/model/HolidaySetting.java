package com.twfhclife.autoreport.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HolidaySetting {

    
    private String holidayDate;

    private String remark;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
