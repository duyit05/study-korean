package com.example.back_end.controller;

import com.example.back_end.dto.request.CourseRequest;
import com.example.back_end.dto.response.ApiResponse;
import com.example.back_end.dto.response.CourseResponse;
import com.example.back_end.service.CourseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("${spring.servlet.context-path}/courses")
@RequiredArgsConstructor
@Slf4j
public class CourseController {

    private final CourseService courseService;

    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<CourseResponse> createCourse(@Valid @RequestBody CourseRequest request) {
        CourseResponse response = courseService.createCourse(request);
        return ApiResponse.<CourseResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Tạo khóa học thành công.")
                .data(response)
                .build();
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<CourseResponse>> getAllCourses() {
        List<CourseResponse> response = courseService.getAllCourses();
        return ApiResponse.<List<CourseResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("Lấy danh sách khóa học thành công.")
                .data(response)
                .build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<CourseResponse> getCourseById(@PathVariable Long id) {
        CourseResponse response = courseService.getCourseById(id);
        return ApiResponse.<CourseResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Lấy chi tiết khóa học thành công.")
                .data(response)
                .build();
    }

    @GetMapping("/teacher/{teacherId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<List<CourseResponse>> getCoursesByTeacher(@PathVariable Long teacherId) {
        List<CourseResponse> response = courseService.getCoursesByTeacher(teacherId);
        return ApiResponse.<List<CourseResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("Lấy danh sách khóa học của giáo viên thành công.")
                .data(response)
                .build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<CourseResponse> updateCourse(
            @PathVariable Long id,
            @Valid @RequestBody CourseRequest request) {
        CourseResponse response = courseService.updateCourse(id, request);
        return ApiResponse.<CourseResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Cập nhật khóa học thành công.")
                .data(response)
                .build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<Void> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return ApiResponse.<Void>builder()
                .code(HttpStatus.OK.value())
                .message("Xóa khóa học thành công.")
                .build();
    }
}
