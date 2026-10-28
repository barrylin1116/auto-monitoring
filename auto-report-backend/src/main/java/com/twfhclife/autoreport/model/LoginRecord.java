package com.twfhclife.autoreport.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginRecord {

    @JsonProperty("account")
    @JsonAlias("帳號")
    private String account;

    @JsonProperty("name")
    @JsonAlias("姓名")
    private String name;

    @JsonProperty("channelBranch")
    @JsonAlias("通路/分支")
    private String channelBranch;

    @JsonProperty("role")
    @JsonAlias("角色")
    private String role;

    @JsonProperty("loginTime")
    @JsonAlias("登入時間")
    private String loginTime;

    @JsonProperty("logoutTime")
    @JsonAlias("登出時間")
    private String logoutTime;

    @JsonProperty("loginResult")
    @JsonAlias("登入結果")
    private String loginResult;

    @JsonProperty("failCount")
    @JsonAlias("失敗次數")
    private Integer failCount;

    @JsonProperty("sourceIp")
    @JsonAlias("來源IP")
    private String sourceIp;

    @JsonProperty("sourceArea")
    @JsonAlias("來源地區")
    private String sourceArea;

    @JsonProperty("deviceInfo")
    @JsonAlias("狀置資訊")
    private String deviceInfo;

    @JsonProperty("loginMethod")
    @JsonAlias("登入方式")
    private String loginMethod;
}
