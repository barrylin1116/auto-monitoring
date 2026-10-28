package com.twfhclife.autoreport.entity.secondary;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import java.io.Serializable;


@Data
@NoArgsConstructor
@Entity
@Immutable
@Table(name = "vw_USER_ROLE_01", schema = "dbo")
public class UserRole implements Serializable {

    @Id
    @Column(name = "USER_ID", nullable = false, length = 50)
    private String userId;

    @Id
    @Column(name = "ROLE_ID", nullable = false, length = 50)
    private String roleId;
}
