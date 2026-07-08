package com.example.back_end.controller;

import com.example.back_end.dto.request.TopikLevelRequest;
import com.example.back_end.dto.response.ApiResponse;
import com.example.back_end.entity.TopikLevel;
import com.example.back_end.enums.TopikGroup;
import com.example.back_end.service.TopikLevelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${spring.servlet.context-path}/topik-levels")
@RequiredArgsConstructor
public class TopikLevelController {

    private final TopikLevelService topikLevelService;

    @GetMapping("/groups")
    public ApiResponse<TopikGroup[]> getTopikGroups() {
        return ApiResponse.<TopikGroup[]>builder()
                .code(HttpStatus.OK.value())
                .message("Lấy danh sách nhóm cấp độ TOPIK thành công.")
                .data(TopikGroup.values())
                .build();
    }

    @GetMapping
    public ApiResponse<List<TopikLevel>> getAllLevels() {
        List<TopikLevel> list = topikLevelService.getAllLevels();
        return ApiResponse.<List<TopikLevel>>builder()
                .code(HttpStatus.OK.value())
                .message("Lấy danh sách cấp độ thành công.")
                .data(list)
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<TopikLevel> getLevelById(@PathVariable Long id) {
        TopikLevel level = topikLevelService.getLevelById(id);
        return ApiResponse.<TopikLevel>builder()
                .code(HttpStatus.OK.value())
                .message("Lấy thông tin cấp độ thành công.")
                .data(level)
                .build();
    }

    @PostMapping
    public ApiResponse<TopikLevel> createLevel(@RequestBody TopikLevelRequest request) {
        TopikLevel level = topikLevelService.createLevel(request);
        return ApiResponse.<TopikLevel>builder()
                .code(HttpStatus.CREATED.value())
                .message("Tạo cấp độ thành công.")
                .data(level)
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<TopikLevel> updateLevel(@PathVariable Long id, @RequestBody TopikLevelRequest request) {
        TopikLevel level = topikLevelService.updateLevel(id, request);
        return ApiResponse.<TopikLevel>builder()
                .code(HttpStatus.OK.value())
                .message("Cập nhật cấp độ thành công.")
                .data(level)
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteLevel(@PathVariable Long id) {
        topikLevelService.deleteLevel(id);
        return ApiResponse.<Void>builder()
                .code(HttpStatus.OK.value())
                .message("Xóa cấp độ thành công.")
                .build();
    }
}
