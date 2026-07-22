package com.example.back_end.controller;

import com.example.back_end.dto.request.UserProfileUpdateRequest;
import com.example.back_end.dto.request.ChangePasswordRequest;
import com.example.back_end.dto.response.ApiResponse;
import com.example.back_end.dto.response.UserResponse;
import jakarta.validation.Valid;
import com.example.back_end.entity.User;
import com.example.back_end.enums.UserRole;
import com.example.back_end.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("${spring.servlet.context-path}/users")
@RequiredArgsConstructor
public class UserController {

        private final UserService userService;

        @GetMapping("/students")
        @PreAuthorize("hasRole('TEACHER')")
        public ApiResponse<List<UserResponse>> getAllStudents() {
                List<UserResponse> list = userService.getUsersByRole(UserRole.STUDENT);

                return ApiResponse.<List<UserResponse>>builder()
                                .code(HttpStatus.OK.value())
                                .message("Lấy danh sách học viên thành công.")
                                .data(list)
                                .build();
        }

        @PutMapping("/profile")
        @PreAuthorize("isAuthenticated()")
        public ApiResponse<UserResponse> updateProfile(@RequestBody UserProfileUpdateRequest request) {
                UserResponse response = userService.updateProfile(request.getFullName(), request.getEmail(),
                                request.getAvatarUrl());
                return ApiResponse.<UserResponse>builder()
                                .code(HttpStatus.OK.value())
                                .message("Cập nhật thông tin cá nhân thành công.")
                                .data(response)
                                .build();
        }

        @PostMapping("/{userId}/unlock")
        @PreAuthorize("hasRole('TEACHER')")
        public ApiResponse<Void> unlockAccount(@PathVariable Long userId) {
                userService.unlockAccount(userId);
                return ApiResponse.<Void>builder()
                                .code(HttpStatus.OK.value())
                                .message("Mở khóa tài khoản thành công.")
                                .build();
        }

        @PutMapping("/change-password")
        @PreAuthorize("isAuthenticated()")
        public ApiResponse<Void> changePassword(@RequestBody @Valid ChangePasswordRequest request) {
                userService.changePassword(request.getOldPassword(), request.getNewPassword());
                return ApiResponse.<Void>builder()
                                .code(HttpStatus.OK.value())
                                .message("Thay đổi mật khẩu thành công.")
                                .build();
        }
}
