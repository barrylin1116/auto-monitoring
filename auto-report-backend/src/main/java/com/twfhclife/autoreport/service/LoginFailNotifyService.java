package com.twfhclife.autoreport.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twfhclife.autoreport.entity.primary.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;


@Slf4j
@Service
public class LoginFailNotifyService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private final String url;

    public LoginFailNotifyService(@Value("${app.auth.login-fail.url:}") String url) {
        this.url = url;
    }

    
    public void notify(User user) {
        if (url == null || url.isBlank()) {
            log.debug("Login-fail API url not configured, skip notify");
            return;
        }
        try {
            String json = objectMapper.writeValueAsString(user);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.info("Notified login failure via [{}], HTTP {} (reason: {})",
                        url, response.statusCode());
            } else {
                log.warn("Login-fail API [{}] failed, HTTP {}: {}",
                        url, response.statusCode(), response.body());
            }
        } catch (Exception e) {
            log.error("Failed to call login-fail API [{}]: {}", url, e.getMessage());
        }
    }
}
