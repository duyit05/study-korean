package com.example.back_end.controller;

import com.example.back_end.dto.response.ApiResponse;
import com.example.back_end.dto.response.UserResponse;
import com.example.back_end.enums.UserRole;
import com.example.back_end.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
                        .role(u.getRole())
                        .build())
                .collect(Collectors.toList());

        return ApiResponse.<List<UserResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("Lấy danh sách học viên thành công.")
                .data(list)
                .build();
    }
}
