package com.twfhclife.autodatastream.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;


@Service
public class AccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountService.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private final String lockUrl;

    public AccountService(@Value("${app.misf-login.lock-url:}") String lockUrl) {
        this.lockUrl = lockUrl;
    }

    
    public LockResult apply(String account, String status) {
        if (account == null || account.isBlank()) {
            log.warn("Cannot update account status: blank account");
            return new LockResult(false, "blank account");
        }
        if (lockUrl == null || lockUrl.isBlank()) {
            log.debug("Account status API url not configured, skip [{}] for account [{}]", status, account);
            return new LockResult(false, "account status API url not configured");
        }
        try {
            Map<String, String> payload = new LinkedHashMap<>();
            payload.put("userId", account);
            payload.put("status", status);
            String json = objectMapper.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(lockUrl))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.info("Updated account [{}] to status [{}] via [{}], HTTP {}",
                        account, status, lockUrl, response.statusCode());
                return new LockResult(true, "HTTP " + response.statusCode());
            }
            log.warn("Update account [{}] to status [{}] failed via [{}], HTTP {}: {}",
                    account, status, lockUrl, response.statusCode(), response.body());
            return new LockResult(false, "HTTP " + response.statusCode());
        } catch (Exception e) {
            log.error("Failed to call account status API [{}] for account [{}] ({}): {}",
                    lockUrl, account, status, e.getMessage());
            return new LockResult(false, e.getMessage());
        }
    }

    
    public record LockResult(boolean success, String message) {
    }
}
