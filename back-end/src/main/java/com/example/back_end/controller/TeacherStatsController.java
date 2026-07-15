package com.example.back_end.controller;

import com.example.back_end.dto.response.ApiResponse;
import com.example.back_end.dto.response.TeacherStatsResponse;
import com.example.back_end.service.TeacherStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${spring.servlet.context-path}/teacher/stats")
@RequiredArgsConstructor
public class TeacherStatsController {

    private final TeacherStatsService teacherStatsService;

    @GetMapping
    public ApiResponse<TeacherStatsResponse> getTeacherStats() {
        TeacherStatsResponse data = teacherStatsService.getTeacherStats();
        return ApiResponse.<TeacherStatsResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Lấy thống kê học tập thành công.")
                .data(data)
                .build();
    }
}
