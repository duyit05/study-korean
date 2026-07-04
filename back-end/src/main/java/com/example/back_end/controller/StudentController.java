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
    public ApiResponse<List<StudentResponse>> getAllStudents() {
        List<StudentResponse> list = studentService.getAllStudents();
        return ApiResponse.<List<StudentResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("Lấy danh sách học viên thành công.")
                .data(list)
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
}
