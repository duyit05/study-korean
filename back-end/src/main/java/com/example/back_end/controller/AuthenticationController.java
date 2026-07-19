package com.example.back_end.controller;

import com.example.back_end.dto.request.RegisterRequest;
import com.example.back_end.dto.request.RefreshRequest;
import com.example.back_end.dto.response.ApiResponse;
import com.example.back_end.dto.response.AuthResponse;
import com.example.back_end.dto.request.LoginRequest;
import com.example.back_end.entity.User;
import com.example.back_end.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${spring.servlet.context-path}/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthenticationController {

    private final UserService userService;

    @PostMapping("/register")
    public ApiResponse<User> register(@Valid @RequestBody RegisterRequest request) {
        User registeredUser = userService.register(request);
        return ApiResponse.<User>builder()
                .code(HttpStatus.OK.value())
                .message("Đăng ký tài khoản thành công.")
                .data(registeredUser)
                .build();
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        String clientIp = getClientIp(httpRequest);
        AuthResponse authResponse = userService.login(request, clientIp);

        return ApiResponse.<AuthResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Đăng nhập thành công.")
                .data(authResponse)
                .build();
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        String ip;
        if (forwarded != null && !forwarded.isBlank()) {
            ip = forwarded.split(",")[0].trim();
        } else {
            ip = request.getRemoteAddr();
        }
        if ("0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) {
            return "127.0.0.1";
        }
        return ip;
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        AuthResponse authResponse = userService.refreshToken(request);
        return ApiResponse.<AuthResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Refresh token thành công.")
                .data(authResponse)
                .build();
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody(required = false) RefreshRequest request) {
        String refreshToken = request != null ? request.getRefreshToken() : null;
        userService.logout(authHeader, refreshToken);
        return ApiResponse.<Void>builder()
                .code(HttpStatus.OK.value())
                .message("Đăng xuất thành công.")
                .build();
    }
}
