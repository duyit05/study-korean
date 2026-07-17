package com.example.back_end.service;

import com.example.back_end.dto.request.RegisterRequest;
import com.example.back_end.dto.response.AuthResponse;
import com.example.back_end.dto.request.LoginRequest;
import com.example.back_end.entity.StudentProfile;
import com.example.back_end.entity.TeacherProfile;
import com.example.back_end.entity.User;
import com.example.back_end.enums.UserRole;
import com.example.back_end.exception.AppException;
import com.example.back_end.exception.ErrorCode;
import com.example.back_end.repository.StudentProfileRepository;
import com.example.back_end.repository.TeacherProfileRepository;
import com.example.back_end.repository.UserRepository;
import com.example.back_end.utils.JwtTokenProvider;
import com.example.back_end.dto.request.RefreshRequest;
import com.example.back_end.service.RedisTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final TeacherProfileRepository teacherProfileRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTokenService redisTokenService;

    @Transactional
    public User register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        User user = User.builder()
                .email(request.getEmail())
                .username(request.getUsername())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .role(UserRole.STUDENT)
                .isActive(true)
                .build();

        User savedUser = userRepository.save(user);

        // Auto-seed profiles based on roles
        if (request.getRole() == UserRole.TEACHER) {
            TeacherProfile teacherProfile = TeacherProfile.builder()
                    .user(savedUser)
                    .build();
            teacherProfileRepository.save(teacherProfile);
        } else if (request.getRole() == UserRole.STUDENT) {
            StudentProfile studentProfile = StudentProfile.builder()
                    .user(savedUser)
                    .xp(0)
                    .level(1)
                    .streak(0)
                    .build();
            studentProfileRepository.save(studentProfile);
        }

        return savedUser;
    }

    public AuthResponse login(LoginRequest request, String clientIp) {
        User user = userRepository.findByUsername(request.getUsername())
                .or(() -> userRepository.findByEmail(request.getUsername()))
                .orElseThrow(() -> {
                    return new AppException(ErrorCode.USER_NOT_FOUND);
                });

        if (user.getIsActive() != null && !user.getIsActive()) {
            throw new AppException(ErrorCode.USER_BLOCKED);
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new AppException(ErrorCode.INVALID_PASSWORD);
        }

        // ─── Lớp 2: IP Tracking ──────────────────────────────────────
        long distinctIpCount = redisTokenService.trackLoginIp(user.getUsername(), clientIp);

        if (distinctIpCount > 2) {
            // Auto-lock account
            user.setIsActive(false);
            userRepository.save(user);
            redisTokenService.deleteActiveSession(user.getUsername());
            redisTokenService.deleteRefreshToken(user.getUsername());
            log.warn("Account auto-locked due to IP sharing: username={}, ip={}, ipCount={}",
                    user.getUsername(), clientIp, distinctIpCount);
            throw new AppException(ErrorCode.ACCOUNT_LOCKED_SHARING);
        }

        boolean ipWarning = (distinctIpCount == 2);

        UserDetails userDetails = org.springframework.security.core.userdetails.User.withUsername(user.getUsername())
                .password(user.getPasswordHash())
                .authorities("ROLE_" + user.getRole().name())
                .build();

        // ─── Lớp 1: Active Session ───────────────────────────────────
        String sessionId = UUID.randomUUID().toString();
        redisTokenService.saveActiveSession(
                user.getUsername(), sessionId,
                jwtTokenProvider.getRefreshTokenExpirationInSec());

        String token = jwtTokenProvider.generateToken(userDetails, sessionId);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

        // Save refresh token in Redis
        redisTokenService.saveRefreshToken(user.getUsername(), refreshToken,
                jwtTokenProvider.getRefreshTokenExpirationInSec());

        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole())
                .ipWarning(ipWarning ? true : null)
                .warningMessage(ipWarning
                    ? "⚠️ Tài khoản của bạn đã đăng nhập từ nhiều nơi hôm nay. Nếu không phải bạn, vui lòng liên hệ giáo viên ngay."
                    : null)
                .build();
    }

    public AuthResponse refreshToken(RefreshRequest request) {
        String refreshToken = request.getRefreshToken();

        if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
            throw new AppException(ErrorCode.INVALID_KEY);
        }

        String username = jwtTokenProvider.getUsernameFromRefreshToken(refreshToken);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (user.getIsActive() != null && !user.getIsActive()) {
            throw new AppException(ErrorCode.USER_BLOCKED);
        }

        // Verify with the active token stored in Redis
        String activeRefreshToken = redisTokenService.getRefreshToken(username);
        if (activeRefreshToken == null || !activeRefreshToken.equals(refreshToken)) {
            redisTokenService.deleteRefreshToken(username);
            throw new AppException(ErrorCode.INVALID_KEY);
        }

        UserDetails userDetails = org.springframework.security.core.userdetails.User.withUsername(user.getUsername())
                .password(user.getPasswordHash())
                .authorities("ROLE_" + user.getRole().name())
                .build();

        // Rotate sessionId khi refresh
        String newSessionId = UUID.randomUUID().toString();
        redisTokenService.saveActiveSession(
                user.getUsername(), newSessionId,
                jwtTokenProvider.getRefreshTokenExpirationInSec());

        String newAccessToken = jwtTokenProvider.generateToken(userDetails, newSessionId);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

        // Save new refresh token in Redis
        redisTokenService.saveRefreshToken(user.getUsername(), newRefreshToken,
                jwtTokenProvider.getRefreshTokenExpirationInSec());

        return AuthResponse.builder()
                .token(newAccessToken)
                .refreshToken(newRefreshToken)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole())
                .build();
    }

    public void logout(String authHeader, String refreshToken) {
        String username = null;

        // 1. Blacklist the Access Token
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String accessToken = authHeader.substring(7);
            long remainingMs = jwtTokenProvider.getRemainingExpiryTimeMs(accessToken);
            if (remainingMs > 0) {
                redisTokenService.blacklistAccessToken(accessToken, remainingMs);
            } else {
                log.warn("Access Token remaining validity is 0 or negative. Not blacklisting.");
            }
        } else {
            log.warn("No valid Authorization Bearer header found in logout request.");
        }

        // 2. Delete the Refresh Token from Redis
        if (refreshToken != null && !refreshToken.isEmpty() && jwtTokenProvider.validateRefreshToken(refreshToken)) {
            username = jwtTokenProvider.getUsernameFromRefreshToken(refreshToken);
            redisTokenService.deleteRefreshToken(username);
        } else {
            log.warn("No valid refresh token provided for eviction.");
        }

        // 3. Delete Active Session
        if (username != null) {
            redisTokenService.deleteActiveSession(username);
        }
    }

    public User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof User) {
            return (User) principal;
        }
        String username = (String) principal;
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    public List<User> getUsersByRole(UserRole role) {
        return userRepository.findByRole(role);
    }

    @Transactional
    public User updateProfile(String fullName, String email, String avatarUrl) {
        User user = getCurrentUser();
        if (fullName != null)
            user.setFullName(fullName);
        if (email != null)
            user.setEmail(email);
        if (avatarUrl != null)
            user.setAvatarUrl(avatarUrl);
        return userRepository.save(user);
    }

    @Transactional
    public void unlockAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        user.setIsActive(true);
        userRepository.save(user);
        log.info("Account unlocked by admin: userId={}, username={}", userId, user.getUsername());
    }
}
