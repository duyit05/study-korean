package com.example.back_end.configuration;

import com.example.back_end.entity.User;
import com.example.back_end.exception.ErrorCode;
import com.example.back_end.repository.UserRepository;
import com.example.back_end.service.RedisTokenService;
import com.example.back_end.utils.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final RedisTokenService redisTokenService;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt)) {
                if (redisTokenService.isAccessTokenBlacklisted(jwt)) {
                    throw new InsufficientAuthenticationException("Token is blacklisted");
                }
                if (tokenProvider.validateToken(jwt)) {
                    String username = tokenProvider.getUsernameFromJWT(jwt);

                    User user = userRepository.findByUsername(username)
                            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

                    if (user.getIsActive() != null && !user.getIsActive()) {
                        throw new DisabledException("User is blocked");
                    }

                    // Validate sessionId — Lớp 1: Single Session (chỉ chặn nếu có session khác đang active)
                    String sessionId = tokenProvider.getSessionIdFromJWT(jwt);
                    String activeSession = redisTokenService.getActiveSession(user.getUsername());

                    if (activeSession != null && (sessionId == null || !sessionId.equals(activeSession))) {
                        throw new InsufficientAuthenticationException(
                                ErrorCode.SESSION_HIJACKED.getMessage());
                    } else if (activeSession == null && sessionId != null) {
                        // Nếu Redis chưa có session (do hết hạn/mới flush), tự động sync lại session này làm active
                        redisTokenService.saveActiveSession(
                                user.getUsername(), sessionId,
                                tokenProvider.getRefreshTokenExpirationInSec());
                    }

                    SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().name());
                    
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            user, null, Collections.singletonList(authority));
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (AuthenticationException ex) {
            authenticationEntryPoint.commence(request, response, ex);
            return;
        } catch (Exception ex) {
            logger.error("Could not set user authentication in security context", ex);
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
