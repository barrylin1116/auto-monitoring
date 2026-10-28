package com.twfhclife.autoreport.service;

import com.twfhclife.autoreport.config.AuthProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;


@Service
@RequiredArgsConstructor
public class JwtService {

    private final AuthProperties authProperties;

    
    public String generateToken(String username) {
        Date now = new Date();
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expireMillis()))
                .signWith(signingKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    
    public long expireSeconds() {
        return authProperties.getJwt().getExpireMinutes() * 60L;
    }

    
    public String parseUsername(String token) {
        Jws<Claims> jws = Jwts.parserBuilder()
                .setSigningKey(signingKey())
                .build()
                .parseClaimsJws(token);
        return jws.getBody().getSubject();
    }

    private long expireMillis() {
        return authProperties.getJwt().getExpireMinutes() * 60_000L;
    }

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(
                authProperties.getJwt().getSecret().getBytes(StandardCharsets.UTF_8));
    }
}
