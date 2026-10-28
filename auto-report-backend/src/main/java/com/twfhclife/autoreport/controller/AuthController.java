package com.twfhclife.autoreport.controller;

import com.twfhclife.autoreport.service.AuthService;
import com.twfhclife.autoreport.service.CaptchaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.NoSuchAlgorithmException;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final CaptchaService captchaService;
    private final AuthService authService;

    
    @GetMapping("/captcha")
    public CaptchaResponse captcha() throws NoSuchAlgorithmException {
        CaptchaService.Captcha captcha = captchaService.generate();
        return new CaptchaResponse(captcha.captchaId(), captcha.imageBase64());
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        AuthService.LoginResult result = authService.login(
                request.username(), request.password(), request.captchaId(), request.captchaCode());
        return new LoginResponse(result.token(), result.username(), result.expiresIn());
    }

    public record CaptchaResponse(String captchaId, String captchaBase64) {
    }

    public record LoginRequest(String username, String password, String captchaId, String captchaCode) {
    }

    public record LoginResponse(String token, String username, long expiresIn) {
    }
}
