package com.example.back_end.service;

import com.example.back_end.dto.request.RegisterRequest;
import com.example.back_end.dto.response.AuthResponse;
import com.example.back_end.dto.response.UserResponse;
import com.example.back_end.dto.request.LoginRequest;
import com.example.back_end.entity.StudentProfile;
import com.example.back_end.entity.TeacherProfile;
import com.example.back_end.entity.User;
import com.example.back_end.enums.UserRole;
import com.example.back_end.enums.AuthProvider;
import com.example.back_end.exception.AppException;
import com.example.back_end.exception.ErrorCode;
import com.example.back_end.mapper.UserMapper;
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
import java.util.Map;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;

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
    private final UserMapper userMapper;

    @Value("${google.client-id}")
    private String googleClientId;

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
                .authProvider(AuthProvider.SYSTEM)
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

        if (user.getAuthProvider() == AuthProvider.GOOGLE) {
            throw new AppException(ErrorCode.GOOGLE_ACCOUNT_REQUIRED);
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

        Integer xp = null;
        Integer level = null;
        Integer streak = null;
        if (user.getRole() == UserRole.STUDENT) {
            StudentProfile profile = studentProfileRepository.findByUserId(user.getId()).orElse(null);
            if (profile != null) {
                xp = profile.getXp();
                level = profile.getLevel();
                streak = profile.getStreak();
            }
        }

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
                .xp(xp)
                .level(level)
                .streak(streak)
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

        Integer xp = null;
        Integer level = null;
        Integer streak = null;
        if (user.getRole() == UserRole.STUDENT) {
            StudentProfile profile = studentProfileRepository.findByUserId(user.getId()).orElse(null);
            if (profile != null) {
                xp = profile.getXp();
                level = profile.getLevel();
                streak = profile.getStreak();
            }
        }

        return AuthResponse.builder()
                .token(newAccessToken)
                .refreshToken(newRefreshToken)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole())
                .xp(xp)
                .level(level)
                .streak(streak)
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

    public List<UserResponse> getUsersByRole(UserRole role) {
        return userMapper.toUserResponseList(userRepository.findByRole(role));
    }

    @Transactional
    public UserResponse updateProfile(String fullName, String email, String avatarUrl) {
        User user = getCurrentUser();
        if (fullName != null)
            user.setFullName(fullName);
        if (email != null)
            user.setEmail(email);
        if (avatarUrl != null)
            user.setAvatarUrl(avatarUrl);
        User updated = userRepository.save(user);
        return userMapper.toUserResponse(updated);
    }

    @Transactional
    public void unlockAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        user.setIsActive(true);
        userRepository.save(user);
        log.info("Account unlocked by admin: userId={}, username={}", userId, user.getUsername());
    }

    @Transactional
    public void changePassword(String oldPassword, String newPassword) {
        User user = getCurrentUser();
        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new AppException(ErrorCode.INVALID_PASSWORD);
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public AuthResponse loginWithGoogle(String idToken, String clientIp) {
        String googleUrl = "https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken;
        Map<String, Object> payload;
        try {
            RestTemplate restTemplate = new RestTemplate();
            payload = restTemplate.getForObject(googleUrl, Map.class);
        } catch (Exception e) {
            log.error("Google token verification failed", e);
            throw new AppException(ErrorCode.INVALID_KEY);
        }

        if (payload == null || !payload.containsKey("email")) {
            throw new AppException(ErrorCode.INVALID_KEY);
        }

        String aud = (String) payload.get("aud");
        if (aud == null || !aud.equals(googleClientId)) {
            log.error("Google token audience mismatch: expected={}, actual={}", googleClientId, aud);
            throw new AppException(ErrorCode.INVALID_KEY);
        }

        String email = (String) payload.get("email");
        String fullName = (String) payload.get("name");
        String picture = (String) payload.get("picture");

        User user = userRepository.findByEmail(email).orElseGet(() -> {
            String baseUsername = email.split("@")[0];
            String username = baseUsername;
            int count = 1;
            while (userRepository.existsByUsername(username)) {
                username = baseUsername + count++;
            }

            User newUser = User.builder()
                    .email(email)
                    .username(username)
                    .passwordHash(passwordEncoder.encode(UUID.randomUUID().toString()))
                    .fullName(fullName != null ? fullName : baseUsername)
                    .avatarUrl(picture)
                    .role(UserRole.STUDENT)
                    .isActive(true)
                    .authProvider(AuthProvider.GOOGLE)
                    .build();

            User saved = userRepository.save(newUser);

            StudentProfile studentProfile = StudentProfile.builder()
                    .user(saved)
                    .xp(0)
                    .level(1)
                    .streak(0)
                    .build();
            studentProfileRepository.save(studentProfile);

            log.info("Auto-registered new student via Google: email={}, username={}", email, username);
            return saved;
        });

        if (user.getIsActive() != null && !user.getIsActive()) {
            throw new AppException(ErrorCode.USER_BLOCKED);
        }

        long distinctIpCount = redisTokenService.trackLoginIp(user.getUsername(), clientIp);
        if (distinctIpCount > 2) {
            user.setIsActive(false);
            userRepository.save(user);
            redisTokenService.deleteActiveSession(user.getUsername());
            redisTokenService.deleteRefreshToken(user.getUsername());
            throw new AppException(ErrorCode.ACCOUNT_LOCKED_SHARING);
        }

        boolean ipWarning = (distinctIpCount == 2);

        UserDetails userDetails = org.springframework.security.core.userdetails.User.withUsername(user.getUsername())
                .password(user.getPasswordHash())
                .authorities("ROLE_" + user.getRole().name())
                .build();

        String sessionId = UUID.randomUUID().toString();
        redisTokenService.saveActiveSession(
                user.getUsername(), sessionId,
                jwtTokenProvider.getRefreshTokenExpirationInSec());

        String token = jwtTokenProvider.generateToken(userDetails, sessionId);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

        redisTokenService.saveRefreshToken(user.getUsername(), refreshToken,
                jwtTokenProvider.getRefreshTokenExpirationInSec());

        Integer xp = null;
        Integer level = null;
        Integer streak = null;
        if (user.getRole() == UserRole.STUDENT) {
            StudentProfile profile = studentProfileRepository.findByUserId(user.getId()).orElse(null);
            if (profile != null) {
                xp = profile.getXp();
                level = profile.getLevel();
                streak = profile.getStreak();
            }
        }

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
                .xp(xp)
                .level(level)
                .streak(streak)
                .build();
    }
}
