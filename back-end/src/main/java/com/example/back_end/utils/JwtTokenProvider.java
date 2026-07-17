package com.example.back_end.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class JwtTokenProvider {

    @Value("${jwt.secret-key}")
    private String secretKeyString;

    @Value("${jwt.secret-refresh-key}")
    private String secretRefreshKeyString;

    @Value("${jwt.expiry-time}")
    private long expiryTimeHours;

    @Value("${jwt.expiry-day}")
    private long expiryTimeDays;

    private Key jwtSecret;
    private long jwtExpirationInMs;

    private Key jwtRefreshSecret;
    private long jwtRefreshExpirationInMs;

    @PostConstruct
    public void init() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKeyString);
        this.jwtSecret = Keys.hmacShaKeyFor(keyBytes);
        this.jwtExpirationInMs = expiryTimeHours * 60 * 60 * 1000;

        byte[] refreshKeyBytes = Decoders.BASE64.decode(secretRefreshKeyString);
        this.jwtRefreshSecret = Keys.hmacShaKeyFor(refreshKeyBytes);
        this.jwtRefreshExpirationInMs = expiryTimeDays * 24 * 60 * 60 * 1000;
    }

    public String generateToken(UserDetails userDetails) {
        return generateToken(userDetails, null);
    }

    public String generateToken(UserDetails userDetails, String sessionId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        Map<String, Object> claims = new HashMap<>();
        claims.put("role", userDetails.getAuthorities());
        if (sessionId != null) {
            claims.put("sid", sessionId);
        }

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(jwtSecret, SignatureAlgorithm.HS512)
                .compact();
    }

    public String getUsernameFromJWT(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(jwtSecret)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    public String getSessionIdFromJWT(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(jwtSecret)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return (String) claims.get("sid");
        } catch (Exception ex) {
            return null;
        }
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(jwtSecret).build().parseClaimsJws(authToken);
            return true;
        } catch (Exception ex) {
            log.error("Invalid JWT token: {}", ex.getMessage());
        }
        return false;
    }

    public String generateRefreshToken(UserDetails userDetails) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtRefreshExpirationInMs);

        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(jwtRefreshSecret, SignatureAlgorithm.HS512)
                .compact();
    }

    public String getUsernameFromRefreshToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(jwtRefreshSecret)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    public boolean validateRefreshToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(jwtRefreshSecret).build().parseClaimsJws(authToken);
            return true;
        } catch (Exception ex) {
            log.error("Invalid JWT Refresh token: {}", ex.getMessage());
        }
        return false;
    }

    public long getRefreshTokenExpirationInSec() {
        return expiryTimeDays * 24 * 60 * 60;
    }

    public long getRemainingExpiryTimeMs(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(jwtSecret)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            Date expiration = claims.getExpiration();
            return Math.max(0, expiration.getTime() - System.currentTimeMillis());
        } catch (Exception ex) {
            return 0;
        }
    }
}
