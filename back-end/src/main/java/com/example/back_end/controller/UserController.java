package com.example.back_end.controller;

import com.example.back_end.dto.request.UserProfileUpdateRequest;
import com.example.back_end.dto.response.ApiResponse;
import com.example.back_end.dto.response.UserResponse;
import com.example.back_end.entity.User;
import com.example.back_end.enums.UserRole;
import com.example.back_end.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("${spring.servlet.context-path}/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/students")
    public ApiResponse<List<UserResponse>> getAllStudents() {
        List<UserResponse> list = userService.getUsersByRole(UserRole.STUDENT).stream()
                .map(u -> UserResponse.builder()
                        .id(u.getId())
                        .email(u.getEmail())
                        .username(u.getUsername())
                        .fullName(u.getFullName())
                        .phone(u.getPhone())
                        .avatarUrl(u.getAvatarUrl())
                        .role(u.getRole())
                        .build())
                .collect(Collectors.toList());

        return ApiResponse.<List<UserResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("Lấy danh sách học viên thành công.")
                .data(list)
                .build();
    }

    @PutMapping("/profile")
    public ApiResponse<UserResponse> updateProfile(@RequestBody UserProfileUpdateRequest request) {
        User updated = userService.updateProfile(request.getFullName(), request.getEmail(), request.getAvatarUrl());
        UserResponse response = UserResponse.builder()
                .id(updated.getId())
                .email(updated.getEmail())
                .username(updated.getUsername())
                .fullName(updated.getFullName())
                .phone(updated.getPhone())
                .avatarUrl(updated.getAvatarUrl())
                .role(updated.getRole())
                .build();
        return ApiResponse.<UserResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Cập nhật thông tin cá nhân thành công.")
                .data(response)
                .build();
    }

    @PostMapping("/{userId}/unlock")
    public ApiResponse<Void> unlockAccount(@PathVariable Long userId) {
        userService.unlockAccount(userId);
        return ApiResponse.<Void>builder()
                .code(HttpStatus.OK.value())
                .message("Mở khóa tài khoản thành công.")
                .build();
    }
}
