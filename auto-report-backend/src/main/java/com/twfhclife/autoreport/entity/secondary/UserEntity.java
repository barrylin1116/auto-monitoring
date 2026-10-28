package com.twfhclife.autoreport.entity.secondary;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Data
@NoArgsConstructor
@Entity
@Immutable
@Table(name = "vw_USER_ENTITY_01", schema = "dbo")
public class UserEntity {

    @Id
    @Column(name = "ID", nullable = false, length = 36)
    private String id;

    @Column(name = "EMAIL", length = 255)
    private String email;

    @Column(name = "EMAIL_CONSTRAINT", length = 255)
    private String emailConstraint;

    @Column(name = "EMAIL_VERIFIED", nullable = false)
    private Boolean emailVerified;

    @Column(name = "ENABLED", nullable = false)
    private Boolean enabled;

    @Column(name = "FEDERATION_LINK", length = 255)
    private String federationLink;

    @Column(name = "FIRST_NAME", length = 255)
    private String firstName;

    @Column(name = "LAST_NAME", length = 255)
    private String lastName;

    @Column(name = "REALM_ID", length = 255)
    private String realmId;

    @Column(name = "USERNAME", length = 255)
    private String username;

    @Column(name = "CREATED_TIMESTAMP")
    private Long createdTimestamp;

    @Column(name = "SERVICE_ACCOUNT_CLIENT_LINK", length = 36)
    private String serviceAccountClientLink;

    @Column(name = "NOT_BEFORE", nullable = false)
    private Long notBefore;
}
