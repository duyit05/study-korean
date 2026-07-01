package com.example.back_end.controller;

import com.example.back_end.dto.request.ClassRequest;
import com.example.back_end.dto.response.ApiResponse;
import com.example.back_end.dto.response.ClassResponse;
import com.example.back_end.entity.Class;
import com.example.back_end.entity.User;
import com.example.back_end.exception.AppException;
import com.example.back_end.exception.ErrorCode;
import com.example.back_end.repository.UserRepository;
import com.example.back_end.service.ClassService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("${spring.servlet.context-path}/classes")
@RequiredArgsConstructor
@Slf4j
public class ClassController {

    private final ClassService classService;
    private final UserRepository userRepository;

    @PostMapping
    public ApiResponse<ClassResponse> createClass(@Valid @RequestBody ClassRequest request) {
        User teacher = getCurrentUser();
        Class clazz = classService.createClass(request.getName(), request.getSchedule(), request.getRoom(), request.getNotes(), teacher);
        return ApiResponse.<ClassResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Tạo lớp học thành công.")
                .data(mapToResponse(clazz))
                .build();
    }

    @GetMapping("/teacher")
    public ApiResponse<List<ClassResponse>> getTeacherClasses() {
        User teacher = getCurrentUser();
        List<ClassResponse> list = classService.getClassesByTeacher(teacher.getId()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ApiResponse.<List<ClassResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("Lấy danh sách lớp thành công.")
                .data(list)
                .build();
    }

    @GetMapping("/student")
    public ApiResponse<List<ClassResponse>> getStudentClasses() {
        User student = getCurrentUser();
        List<ClassResponse> list = classService.getClassesByStudent(student.getId()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ApiResponse.<List<ClassResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("Lấy danh sách lớp thành công.")
                .data(list)
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteClass(@PathVariable Long id) {
        classService.deleteClass(id);
        return ApiResponse.<Void>builder()
                .code(HttpStatus.OK.value())
                .message("Xóa lớp học thành công.")
                .build();
    }

    private ClassResponse mapToResponse(Class c) {
        String name = "Lớp học";
        String schedule = "Thứ 2, 4, 6";
        String room = "Room Zoom";
        String notes = c.getNotes();
        if (notes != null && notes.contains("|")) {
            String[] parts = notes.split("\\|", -1);
            if (parts.length >= 3) {
                name = parts[0].trim();
                schedule = parts[1].trim();
                room = parts[2].trim();
                notes = parts.length > 3 ? parts[3].trim() : "";
            }
        } else if (notes != null) {
            name = notes;
        }

        String randomCode = "KOR-" + c.getId() + "-" + (100 + c.getId() * 3);

        return ClassResponse.builder()
                .id(c.getId())
                .name(name)
                .schedule(schedule)
                .room(room)
                .code(randomCode)
                .teacherName(c.getTeacher().getFullName() != null ? c.getTeacher().getFullName() : c.getTeacher().getUsername())
                .studentsCount(12) // Mock student count for UX
                .status(c.getStatus() != null ? c.getStatus().name() : "ACTIVE")
                .startedAt(c.getStartedAt())
                .notes(notes)
                .build();
    }

    private User getCurrentUser() {
        String username = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }
}
