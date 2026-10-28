package com.twfhclife.autoreport.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.twfhclife.autoreport.config.AuthProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;


@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakService {

    private final AuthProperties authProperties;
    private final RestTemplate restTemplate;

    
    public String authenticate(String username, String password) {
        AuthProperties.Keycloak keycloak = authProperties.getKeycloak();
        String url = String.format("%s/realms/%s/protocol/openid-connect/token",
                keycloak.getBaseUrl(), keycloak.getRealm());

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "password");
        form.add("client_id", keycloak.getClientId());
        if (StringUtils.hasText(keycloak.getClientSecret())) {
            form.add("client_secret", keycloak.getClientSecret());
        }
        form.add("username", username);
        form.add("password", password);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        try {
            ResponseEntity<JsonNode> response = restTemplate.postForEntity(
                    url, new HttpEntity<>(form, headers), JsonNode.class);
            JsonNode body = response.getBody();
            if (body != null && body.hasNonNull("access_token")) {
                return body.get("access_token").asText();
            }
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "帳號或密碼錯誤");
        } catch (HttpClientErrorException.Unauthorized e) {
            log.warn("Keycloak 帳密驗證失敗: {}", e.getResponseBodyAsString());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "帳號或密碼錯誤");
        } catch (HttpClientErrorException e) {
            log.error("Keycloak 回應錯誤 ({}): {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Keycloak 驗證服務異常");
        } catch (RestClientException e) {
            log.error("無法連線至 Keycloak: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "無法連線至 Keycloak");
        }
    }
}
