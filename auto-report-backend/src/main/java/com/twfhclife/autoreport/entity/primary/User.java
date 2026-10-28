package com.twfhclife.autoreport.entity.primary;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;


@Data
@NoArgsConstructor
@Entity
@Immutable
@Table(name = "vw_USERS_01")
public class User {

    @Id
    @Column(name = "USER_ID", nullable = false, length = 50)
    private String userId;

    @Column(name = "USER_TYPE", length = 6)
    private String userType;

    @Column(name = "ROC_ID", nullable = false, length = 50)
    private String rocId;

    @Column(name = "MOBILE", length = 50)
    private String mobile;

    @Column(name = "EMAIL", length = 100)
    private String email;

    @Column(name = "LOGIN_FAIL_COUNT", precision = 38, scale = 4)
    private Integer loginFailCount;

    @Column(name = "LAST_CHANG_PASSWORD_DATE")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSS", timezone="GMT+8")
    private Date lastChangPasswordDate;

    @Column(name = "SMS_FLAG", length = 1)
    private Boolean smsFlag;

    @Column(name = "MAIL_FLAG", length = 1)
    private Boolean mailFlag;

    @Column(name = "FB_ID", length = 100)
    private String fbId;

    @Column(name = "MOICA_ID", length = 100)
    private String moicaId;

    @Column(name = "CREATE_DATE")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSS", timezone="GMT+8")
    private Date createDate;

    @Column(name = "CREATE_USER", length = 50)
    private String createUser;

    @Column(name = "ONLINE_FLAG", length = 1)
    private String onlineFlag;

    @Column(name = "STATUS", length = 10)
    private String status;

    @Column(name = "USER_NAME", length = 50)
    private String userName;

    @Column(name = "LOGIN_TIME")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSS", timezone="GMT+8")
    private Date loginTime;

    @Column(name = "CLAUSE_FLAG", length = 10)
    private Boolean clauseFlag;

    @Column(name = "SERIAL_NUM", nullable = false, length = 50)
    private String serialNum;

    @Column(name = "IC_ID", length = 50)
    private String icId;

    @Column(name = "LOGIN_SIZE", length = 50)
    private String loginSize;

    @Column(name = "EFFECTIVE_DATE", length = 50)
    private String effectiveDate;

    @Column(name = "EXPIRATION_DATE", length = 50)
    private String expirationDate;

    @Column(name = "INITPASSWORD", length = 100)
    private String initpassword;

    @Column(name = "JD_LOGGED")
    private Boolean jdLogged;

    @Column(name = "SIGN_LOGED")
    private Boolean signLoged;

    @Column(name = "ACTION_LOGGED")
    private Boolean actionLogged;

    @Column(name = "AES_PASSWORD", length = 200)
    private String aesPassword;
}
