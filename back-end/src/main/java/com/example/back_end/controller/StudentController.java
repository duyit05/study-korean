package com.example.back_end.controller;

import com.example.back_end.dto.request.StudentCreateRequest;
import com.example.back_end.dto.request.StudentUpdateRequest;
import com.example.back_end.dto.response.ApiResponse;
import com.example.back_end.dto.response.StudentResponse;
import com.example.back_end.service.StudentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("${spring.servlet.context-path}/students")
@RequiredArgsConstructor
@Slf4j
public class StudentController {

    private final StudentService studentService;

    @GetMapping
    public ApiResponse<?> getAllStudents(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "true") boolean unpaged) {
        if (unpaged) {
            List<StudentResponse> list = studentService.getAllStudents(search, level, isActive);
            return ApiResponse.<List<StudentResponse>>builder()
                    .code(HttpStatus.OK.value())
                    .message("Lấy danh sách học viên thành công.")
                    .data(list)
                    .build();
        }
        com.example.back_end.dto.response.PageResponse<StudentResponse> data = studentService.getPaginatedStudents(page, size, search, level, isActive);
        return ApiResponse.<com.example.back_end.dto.response.PageResponse<StudentResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("Lấy danh sách học viên thành công.")
                .data(data)
                .build();
    }

    @PostMapping
    public ApiResponse<StudentResponse> createStudent(@Valid @RequestBody StudentCreateRequest request) {
        StudentResponse data = studentService.createStudent(request);
        return ApiResponse.<StudentResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Tạo tài khoản học viên thành công.")
                .data(data)
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<StudentResponse> updateStudent(@PathVariable Long id, @Valid @RequestBody StudentUpdateRequest request) {
        StudentResponse data = studentService.updateStudent(id, request);
        return ApiResponse.<StudentResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Cập nhật thông tin học viên thành công.")
                .data(data)
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return ApiResponse.<Void>builder()
                .code(HttpStatus.OK.value())
                .message("Xóa tài khoản học viên thành công.")
                .build();
    }

    @GetMapping("/{id}/progress")
    public ApiResponse<com.example.back_end.dto.response.StudentProgressResponse> getStudentProgress(@PathVariable Long id) {
        com.example.back_end.dto.response.StudentProgressResponse data = studentService.getStudentProgress(id);
        return ApiResponse.<com.example.back_end.dto.response.StudentProgressResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Lấy tiến trình học tập học viên thành công.")
                .data(data)
                .build();
    }
}
