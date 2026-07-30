package com.example.back_end.filter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiting filter bảo vệ hệ thống khỏi spam và brute-force.
 * Tích hợp 2 lớp giới hạn:
 * 1. Global Rate Limit (Mặc định: 100 requests / 1 phút / mỗi IP)
 * 2. Auth / Sensitive Rate Limit (Mặc định: 5 requests / 15 phút / mỗi IP) cho login, register, change-password.
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    @Value("${rate-limit.global.capacity:100}")
    private int globalCapacity;

    @Value("${rate-limit.global.duration-minutes:1}")
    private int globalDurationMinutes;

    @Value("${rate-limit.auth.capacity:5}")
    private int authCapacity;

    @Value("${rate-limit.auth.duration-minutes:15}")
    private int authDurationMinutes;

    /** Cache bucket theo IP */
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Bỏ qua rate limit cho Swagger UI và API Docs
        return path.contains("/v3/api-docs")
                || path.contains("/swagger-ui")
                || path.contains("/swagger-ui.html");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        String clientIp = resolveClientIp(request);

        // 1. Kiểm tra Global Rate Limit
        Bucket globalBucket = buckets.computeIfAbsent(clientIp + ":global", ip -> newGlobalBucket());
        if (!globalBucket.tryConsume(1)) {
            sendErrorResponse(response, HttpStatus.TOO_MANY_REQUESTS,
                    String.format("Quá nhiều yêu cầu. Vui lòng thử lại sau %d phút.", globalDurationMinutes));
            return;
        }

        // 2. Kiểm tra Auth / Sensitive Rate Limit
        if (isAuthRoute(path)) {
            Bucket authBucket = buckets.computeIfAbsent(clientIp + ":auth", ip -> newAuthBucket());
            if (!authBucket.tryConsume(1)) {
                sendErrorResponse(response, HttpStatus.TOO_MANY_REQUESTS,
                        String.format("Bạn đã vượt quá số lần thử đăng nhập/xác thực cho phép (tối đa %d lần trong %d phút). Vui lòng thử lại sau.",
                                authCapacity, authDurationMinutes));
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private Bucket newGlobalBucket() {
        Bandwidth limit = Bandwidth.classic(globalCapacity, Refill.greedy(globalCapacity, Duration.ofMinutes(globalDurationMinutes)));
        return Bucket.builder().addLimit(limit).build();
    }

    private Bucket newAuthBucket() {
        Bandwidth limit = Bandwidth.classic(authCapacity, Refill.greedy(authCapacity, Duration.ofMinutes(authDurationMinutes)));
        return Bucket.builder().addLimit(limit).build();
    }

    private boolean isAuthRoute(String path) {
        return path.contains("/auth/login")
                || path.contains("/auth/register")
                || path.contains("/auth/google")
                || path.contains("/users/change-password");
    }

    private void sendErrorResponse(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(String.format(
                "{\"code\":%d,\"message\":\"%s\",\"data\":null}",
                status.value(), message
        ));
    }

    /**
     * Ưu tiên header X-Forwarded-For (khi đứng sau reverse proxy/Nginx).
     * Fallback về RemoteAddr.
     */
    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
