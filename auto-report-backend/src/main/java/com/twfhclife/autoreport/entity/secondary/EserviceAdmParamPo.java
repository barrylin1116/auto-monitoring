package com.twfhclife.autoreport.entity.secondary;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@Entity
@Immutable
@Table(name = "vw_PARAMETER_01" , schema = "dbo")
public class EserviceAdmParamPo {
    @Id
    @Column(name = "PARAMETER_ID", nullable = false, precision = 38, scale = 4)
    private BigDecimal id;

    @Column(name = "PARAMETER_NAME", nullable = false, length = 100)
    private String parameterName;

    @Column(name = "PARAMETER_VALUE", nullable = false, length = 4000)
    private String parameterValue;

}
