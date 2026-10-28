package com.twfhclife.autoreport.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.twfhclife.autoreport.config.AuthProperties;
import com.twfhclife.autoreport.entity.primary.User;
import com.twfhclife.autoreport.entity.secondary.EserviceAdmParamPo;
import com.twfhclife.autoreport.exception.LoginBizException;
import com.twfhclife.autoreport.repository.jpa.primary.UserRepository;
import com.twfhclife.autoreport.repository.jpa.secondary.EserviceAdmParamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Calendar;


@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    
    private static final DateTimeFormatter API_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy/M/d");

    private final AuthProperties authProperties;
    private final CaptchaService captchaService;
    private final KeycloakService keycloakService;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final EserviceAdmParamRepository eserviceAdmParamRepository;
    private final LoginFailNotifyService loginFailNotifyService;

    
    public LoginResult login(String username, String password, String captchaId, String captchaCode) {

        if (!captchaService.validate(captchaId, captchaCode)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "驗證碼錯誤或已失效");
        }

        User user = userRepository.getUserByAccount(username);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "帳號或密碼錯誤");
        }

        try {

            String status = user.getStatus();
            if ("locked".equalsIgnoreCase(status)) {
                throw new LoginBizException(HttpStatus.FORBIDDEN, "LOGIN_ACCOUNT_LOCKED", "帳號已鎖定");
            }
            if ("disabled".equalsIgnoreCase(status)) {
                throw new LoginBizException(HttpStatus.FORBIDDEN, "LOGIN_ACCOUNT_DISABLED", "帳號已停用");
            }
            if (!isEffectiveToday(user)) {
                throw new LoginBizException(HttpStatus.FORBIDDEN, "LOGIN_ACCOUNT_EXPIRED", "此帳號已失效");
            }

            EserviceAdmParamPo paramDto = eserviceAdmParamRepository.getParameterByCode("eservice_jd", "LOGIN_EXPIRE_DAY");
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(user.getLoginTime());
            calendar.add(Calendar.DAY_OF_YEAR, Integer.parseInt(paramDto.getParameterValue()));
            if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                throw new LoginBizException(HttpStatus.FORBIDDEN, "LOGIN_EXPIRE_DAY", paramDto.getParameterName());
            }

            EserviceAdmParamPo lastChangePasswordDay = eserviceAdmParamRepository.getParameterByCode("eservice_jd",
                    "LAST_CHANGE_PASSWORD_DAY");
            if (lastChangePasswordDay != null && user.getLastChangPasswordDate() != null) {
                calendar.setTime(user.getLastChangPasswordDate());
                calendar.add(Calendar.DAY_OF_YEAR, Integer.parseInt(lastChangePasswordDay.getParameterValue()));
                if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                    throw new LoginBizException(HttpStatus.FORBIDDEN, "LOGIN_PASSWORD_EXPIRED", paramDto.getParameterName());
                }
            }

            EserviceAdmParamPo failCount = eserviceAdmParamRepository.getParameterByCode("eservice_jd", "LOGIN_FAIL_COUNT");
            if (StringUtils.isNotBlank(failCount.getParameterValue()) && user.getLoginFailCount() != null) {
                if (Integer.parseInt(failCount.getParameterValue()) <= user.getLoginFailCount()) {
                    throw new LoginBizException(HttpStatus.FORBIDDEN, "LOGIN_FAIL_COUNT_EXCEEDED", "登錄失敗次數過多，請重設密碼！");
                }
            }

            if (authProperties.getKeycloak().isEnabled()) {
                try {
                    keycloakService.authenticate(username, password);
                } catch (ResponseStatusException ex) {
                    if (ex.getStatus().is4xxClientError()) {
                        throw new LoginBizException(ex.getStatus(), "LOGIN_BAD_CREDENTIALS",
                                ex.getReason() != null ? ex.getReason() : "帳號或密碼錯誤");
                    }
                    throw ex;
                }
            } else if (!authProperties.getUsername().equals(username)
                    || !authProperties.getPassword().equals(password)) {
                throw new LoginBizException(HttpStatus.UNAUTHORIZED, "LOGIN_BAD_CREDENTIALS", "帳號或密碼錯誤");
            }

            user.setLoginFailCount(0);
            return new LoginResult(
                    jwtService.generateToken(username),
                    username,
                    jwtService.expireSeconds());
        } catch (LoginBizException e) {
            int updatedFailCount = (user.getLoginFailCount() != null ? user.getLoginFailCount() : 0) + 1;
            user.setLoginFailCount(updatedFailCount);
            if (user.getLoginFailCount() == 3) {
                user.setStatus("locked");
                log.warn("[Login-Exception] 錯誤登入達3次，帳號執行鎖定. userId: {}", user.getUserId());
            }
            throw e;
        } finally {
            loginFailNotifyService.notify(user);
        }
    }

    
    public boolean isEffectiveToday(User user) {
        LocalDate today = LocalDate.now();
        LocalDate effective = parseApiDate(user.getEffectiveDate());
        LocalDate expiration = parseApiDate(user.getExpirationDate());
        if (effective != null && today.isBefore(effective)) {
            return false;
        }
        return expiration == null || !today.isAfter(expiration);
    }

    private LocalDate parseApiDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim(), API_DATE_FORMAT);
        } catch (DateTimeParseException e) {
            log.warn("無法解析帳號有效日期：{}", value);
            return null;
        }
    }

    public record LoginResult(String token, String username, long expiresIn) {
    }
}
