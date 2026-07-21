package com.example.back_end.controller;

import com.example.back_end.dto.request.ClassRequest;
import com.example.back_end.dto.response.ApiResponse;
import com.example.back_end.dto.response.ClassResponse;
import com.example.back_end.dto.response.PageResponse;
import com.example.back_end.enums.MaterialType;
import com.example.back_end.service.ClassService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("${spring.servlet.context-path}/classes")
@RequiredArgsConstructor
@Slf4j
public class ClassController {

    private final ClassService classService;

    @GetMapping("/material-types")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<MaterialType[]> getMaterialTypes() {
        return ApiResponse.<MaterialType[]>builder()
                .code(HttpStatus.OK.value())
                .message("Lấy danh sách định dạng tài liệu thành công.")
                .data(MaterialType.values())
                .build();
    }

    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<ClassResponse> createClass(@Valid @RequestBody ClassRequest request) {
        ClassResponse data = classService.createClass(request);
        return ApiResponse.<ClassResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Tạo lớp học thành công.")
                .data(data)
                .build();
    }

    @GetMapping("/teacher")
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<?> getTeacherClasses(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "6") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String level,
            @RequestParam(defaultValue = "true") boolean unpaged) {
        if (unpaged) {
            List<ClassResponse> list = classService.getTeacherClassesWithFilters(search, level);
            return ApiResponse.<List<ClassResponse>>builder()
                    .code(HttpStatus.OK.value())
                    .message("Lấy danh sách lớp thành công.")
                    .data(list)
                    .build();
        }
        PageResponse<ClassResponse> data = classService.getTeacherClassesPaginated(page, size, search, level);
        return ApiResponse.<PageResponse<ClassResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("Lấy danh sách lớp thành công.")
                .data(data)
                .build();
    }

    @GetMapping("/student")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<List<ClassResponse>> getStudentClasses() {
        List<ClassResponse> list = classService.getStudentClasses();
        return ApiResponse.<List<ClassResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("Lấy danh sách lớp thành công.")
                .data(list)
                .build();
    }

    @PostMapping("/{classId}/students/{studentId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<ClassResponse> enrollStudent(@PathVariable Long classId, @PathVariable Long studentId) {
        ClassResponse data = classService.enrollStudent(classId, studentId);
        return ApiResponse.<ClassResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Thêm học viên vào lớp thành công.")
                .data(data)
                .build();
    }

    @DeleteMapping("/{classId}/students/{studentId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<Void> removeStudent(@PathVariable Long classId, @PathVariable Long studentId) {
        classService.removeStudent(classId, studentId);
        return ApiResponse.<Void>builder()
                .code(HttpStatus.OK.value())
                .message("Xóa học viên khỏi lớp thành công.")
                .build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<ClassResponse> updateClass(@PathVariable Long id, @Valid @RequestBody ClassRequest request) {
        ClassResponse data = classService.updateClass(id, request);
        return ApiResponse.<ClassResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Cập nhật lớp học thành công.")
                .data(data)
                .build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<Void> deleteClass(@PathVariable Long id) {
        classService.deleteClass(id);
        return ApiResponse.<Void>builder()
                .code(HttpStatus.OK.value())
                .message("Xóa lớp học thành công.")
                .build();
    }
}
