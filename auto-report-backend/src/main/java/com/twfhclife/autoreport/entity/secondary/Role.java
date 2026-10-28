package com.twfhclife.autoreport.entity.secondary;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDate;


@Data
@NoArgsConstructor
@Entity
@Immutable
@Table(name = "vw_ROLE_01", schema = "dbo")
public class Role {

    @Id
    @Column(name = "ROLE_ID", nullable = false, length = 50)
    private String roleId;

    @Column(name = "ROLE_NAME", nullable = false, length = 100)
    private String roleName;

    @Column(name = "DESCRIPTION", length = 200)
    private String description;

    @Column(name = "PARENT_ROLE_ID", length = 50)
    private String parentRoleId;

    @Column(name = "DEPARTMENT_ID", length = 50)
    private String departmentId;

    @Column(name = "CREATE_USER", length = 50)
    private String createUser;

    @Column(name = "CREATE_DATE")
    private LocalDate createDate;

    @Column(name = "MODIFY_USER", length = 50)
    private String modifyUser;

    @Column(name = "MODIFY_DATE")
    private LocalDate modifyDate;

    @Column(name = "DIV_ROLE_ID", length = 50)
    private String divRoleId;
}
