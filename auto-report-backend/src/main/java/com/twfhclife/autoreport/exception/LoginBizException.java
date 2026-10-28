package com.twfhclife.autoreport.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;


public class LoginBizException extends ResponseStatusException {

    
    private final String code;

    public LoginBizException(HttpStatus status, String code, String message) {
        super(status, message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
