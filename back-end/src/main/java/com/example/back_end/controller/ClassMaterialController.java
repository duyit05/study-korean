package com.example.back_end.controller;

import com.example.back_end.dto.response.ApiResponse;
import com.example.back_end.dto.response.ClassMaterialResponse;
import com.example.back_end.service.ClassMaterialService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("${spring.servlet.context-path}/classes/{classId}/materials")
@RequiredArgsConstructor
@Slf4j
public class ClassMaterialController {

    private final ClassMaterialService classMaterialService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<ClassMaterialResponse>> getMaterials(@PathVariable Long classId) {
        List<ClassMaterialResponse> list = classMaterialService.getMaterialsByClass(classId);
        return ApiResponse.<List<ClassMaterialResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("Lấy danh sách tài liệu lớp học thành công.")
                .data(list)
                .build();
    }

    @PostMapping("/upload")
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<ClassMaterialResponse> uploadMaterial(
            @PathVariable Long classId,
            @RequestParam("file") MultipartFile file) throws IOException {
        ClassMaterialResponse data = classMaterialService.uploadMaterial(classId, file);
        return ApiResponse.<ClassMaterialResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Tải tài liệu lên lớp học thành công.")
                .data(data)
                .build();
    }

    @DeleteMapping("/{materialId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<Void> deleteMaterial(
            @PathVariable Long classId,
            @PathVariable Long materialId) {
        classMaterialService.deleteMaterial(classId, materialId);
        return ApiResponse.<Void>builder()
                .code(HttpStatus.OK.value())
                .message("Xóa tài liệu thành công.")
                .build();
    }
}
