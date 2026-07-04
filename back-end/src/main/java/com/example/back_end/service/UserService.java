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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final TeacherProfileRepository teacherProfileRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

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
            log.info("Teacher profile created successfully for {}", savedUser.getEmail());
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

    public AuthResponse login(LoginRequest request) {
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

        UserDetails userDetails = org.springframework.security.core.userdetails.User.withUsername(user.getUsername())
                .password(user.getPasswordHash())
                .authorities("ROLE_" + user.getRole().name())
                .build();

        String token = jwtTokenProvider.generateToken(userDetails);

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .build();
    }

    public User getCurrentUser() {
        String username = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    public List<User> getUsersByRole(UserRole role) {
        return userRepository.findByRole(role);
    }
}
