package com.twfhclife.autodatastream.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MisLoginInfo {

    private String account;

    private String name;

    private String channelBranch;

    private String role;

    private String loginTime;

    private String logoutTime;

    private String loginResult;

    private Integer failCount;

    private String sourceIp;

    private String sourceArea;

    private String deviceInfo;

    private String loginMethod;
}