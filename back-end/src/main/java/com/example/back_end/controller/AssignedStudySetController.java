package com.example.back_end.controller;

import com.example.back_end.dto.request.AssignStudySetRequest;
import com.example.back_end.dto.response.ApiResponse;
import com.example.back_end.dto.response.ClassResponse;
import com.example.back_end.service.AssignedStudySetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("${spring.servlet.context-path}/study-sets/assignments")
@RequiredArgsConstructor
public class AssignedStudySetController {

    private final AssignedStudySetService assignedStudySetService;

    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<ClassResponse.AssignedStudySetDto> assignStudySet(
            @Valid @RequestBody AssignStudySetRequest request) {
        ClassResponse.AssignedStudySetDto data = assignedStudySetService.assignStudySet(request);
        return ApiResponse.<ClassResponse.AssignedStudySetDto>builder()
                .code(HttpStatus.OK.value())
                .message("Giao bộ từ vựng cho lớp thành công.")
                .data(data)
                .build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<Void> unassignStudySet(@PathVariable Long id) {
        assignedStudySetService.unassignStudySet(id);
        return ApiResponse.<Void>builder()
                .code(HttpStatus.OK.value())
                .message("Hủy giao bộ từ vựng thành công.")
                .build();
    }
}
