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
        String key = REFRESH_TOKEN_PREFIX + username;
        redisTemplate.opsForValue().set(key, token, expirySec, TimeUnit.SECONDS);
    }

    public String getRefreshToken(String username) {
        String key = REFRESH_TOKEN_PREFIX + username;
        return redisTemplate.opsForValue().get(key);
    }

    public void deleteRefreshToken(String username) {
        String key = REFRESH_TOKEN_PREFIX + username;
        redisTemplate.delete(key);
    }

    // ─── Access Token Blacklist ───────────────────────────────────

    public void blacklistAccessToken(String token, long remainingMs) {
        String key = BLACKLIST_PREFIX + token;
        if (remainingMs > 0) {
            redisTemplate.opsForValue().set(key, "revoked", remainingMs, TimeUnit.MILLISECONDS);
        }
    }

    public boolean isAccessTokenBlacklisted(String token) {
        String key = BLACKLIST_PREFIX + token;
        Boolean exists = redisTemplate.hasKey(key);
        return exists != null && exists;
    }

    // ─── Lớp 1: Active Session (Single Session) ──────────────────

    public void saveActiveSession(String username, String sessionId, long expirySec) {
        redisTemplate.opsForValue().set(
                ACTIVE_SESSION_PREFIX + username, sessionId, expirySec, TimeUnit.SECONDS);
    }

    public String getActiveSession(String username) {
        return redisTemplate.opsForValue().get(ACTIVE_SESSION_PREFIX + username);
    }

    public void deleteActiveSession(String username) {
        redisTemplate.delete(ACTIVE_SESSION_PREFIX + username);
    }

    // ─── Lớp 2: IP Logging ───────────────────────────────────────

    /**
     * Thêm IP vào Set của ngày hôm nay, trả về số distinct IP trong ngày.
     * Set tự xóa sau 24h (TTL).
     */
    public long trackLoginIp(String username, String ip) {
        String key = LOGIN_IPS_PREFIX + username + ":" + LocalDate.now();
        redisTemplate.opsForSet().add(key, ip);
        redisTemplate.expire(key, 24, TimeUnit.HOURS);
        Long count = redisTemplate.opsForSet().size(key);
        return count != null ? count : 1L;
    }
}

