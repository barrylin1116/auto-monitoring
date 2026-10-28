package com.twfhclife.autoreport.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Component
@ConfigurationProperties(prefix = "app.auth")
@Data
public class AuthProperties {

    
    private String username;

    
    private String password;

    private Jwt jwt = new Jwt();

    private Captcha captcha = new Captcha();

    private Keycloak keycloak = new Keycloak();

    private LoginFail loginFail = new LoginFail();

    @Data
    public static class Jwt {

        
        private String secret;

        
        private long expireMinutes = 120;
    }

    @Data
    public static class Captcha {

        
        private int length = 4;

        
        private long ttlSeconds = 300;
    }

    @Data
    public static class Keycloak {

        
        private boolean enabled = true;

        
        private String baseUrl = "http://localhost:8080";

        
        private String realm = "auto-monitoring";

        
        private String clientId = "auto-monitoring-backend";

        
        private String clientSecret = "";
    }

    @Data
    public static class LoginFail {

        
        private String url = "";
    }
}
