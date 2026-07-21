package com.example.back_end.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisTokenService {

    private final StringRedisTemplate redisTemplate;

    private static final String BLACKLIST_PREFIX = "blacklist:";
    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";
    private static final String ACTIVE_SESSION_PREFIX = "active_session:";
    private static final String LOGIN_IPS_PREFIX = "login_ips:";

    // ─── Refresh Token ────────────────────────────────────────────

    public void saveRefreshToken(String username, String token, long expirySec) {
        try {
            String key = REFRESH_TOKEN_PREFIX + username;
            redisTemplate.opsForValue().set(key, token, expirySec, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("Redis connection failed on saveRefreshToken: {}", e.getMessage());
        }
    }

    public String getRefreshToken(String username) {
        try {
            String key = REFRESH_TOKEN_PREFIX + username;
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.warn("Redis connection failed on getRefreshToken: {}", e.getMessage());
            return null;
        }
    }

    public void deleteRefreshToken(String username) {
        try {
            String key = REFRESH_TOKEN_PREFIX + username;
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.warn("Redis connection failed on deleteRefreshToken: {}", e.getMessage());
        }
    }

    // ─── Access Token Blacklist ───────────────────────────────────

    public void blacklistAccessToken(String token, long remainingMs) {
        try {
            String key = BLACKLIST_PREFIX + token;
            if (remainingMs > 0) {
                redisTemplate.opsForValue().set(key, "revoked", remainingMs, TimeUnit.MILLISECONDS);
            }
        } catch (Exception e) {
            log.warn("Redis connection failed on blacklistAccessToken: {}", e.getMessage());
        }
    }

    public boolean isAccessTokenBlacklisted(String token) {
        try {
            String key = BLACKLIST_PREFIX + token;
            Boolean exists = redisTemplate.hasKey(key);
            return exists != null && exists;
        } catch (Exception e) {
            log.warn("Redis connection failed on isAccessTokenBlacklisted: {}", e.getMessage());
            return false;
        }
    }

    // ─── Lớp 1: Active Session (Single Session) ──────────────────

    public void saveActiveSession(String username, String sessionId, long expirySec) {
        try {
            redisTemplate.opsForValue().set(
                    ACTIVE_SESSION_PREFIX + username, sessionId, expirySec, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("Redis connection failed on saveActiveSession: {}", e.getMessage());
        }
    }

    public String getActiveSession(String username) {
        try {
            return redisTemplate.opsForValue().get(ACTIVE_SESSION_PREFIX + username);
        } catch (Exception e) {
            log.warn("Redis connection failed on getActiveSession: {}", e.getMessage());
            return null;
        }
    }

    public void deleteActiveSession(String username) {
        try {
            redisTemplate.delete(ACTIVE_SESSION_PREFIX + username);
        } catch (Exception e) {
            log.warn("Redis connection failed on deleteActiveSession: {}", e.getMessage());
        }
    }

    // ─── Lớp 2: IP Logging ───────────────────────────────────────

    public void recordLoginIp(String username, String ip) {
        try {
            String today = LocalDate.now().toString();
            String key = LOGIN_IPS_PREFIX + username + ":" + today;
            redisTemplate.opsForSet().add(key, ip);
            redisTemplate.expire(key, 2, TimeUnit.DAYS);
        } catch (Exception e) {
            log.warn("Redis connection failed on recordLoginIp: {}", e.getMessage());
        }
    }

    public int getDistinctIpCountToday(String username) {
        try {
            String today = LocalDate.now().toString();
            String key = LOGIN_IPS_PREFIX + username + ":" + today;
            Long size = redisTemplate.opsForSet().size(key);
            return size != null ? size.intValue() : 0;
        } catch (Exception e) {
            log.warn("Redis connection failed on getDistinctIpCountToday: {}", e.getMessage());
            return 0;
        }
    }

    public long trackLoginIp(String username, String ip) {
        recordLoginIp(username, ip);
        return getDistinctIpCountToday(username);
    }
}
