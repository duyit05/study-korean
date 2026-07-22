package com.example.back_end.controller;

import com.example.back_end.dto.request.TopikLevelRequest;
import com.example.back_end.dto.response.ApiResponse;
import com.example.back_end.dto.response.PageResponse;
import com.example.back_end.entity.TopikLevel;
import com.example.back_end.service.TopikLevelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${spring.servlet.context-path}/topik-levels")
@RequiredArgsConstructor
public class TopikLevelController {

    private final TopikLevelService topikLevelService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<?> getLevels(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String[] sort,
            @RequestParam(defaultValue = "true") boolean unpaged) {
        if (unpaged) {
            List<TopikLevel> list = topikLevelService.getAllLevels();
            return ApiResponse.<List<TopikLevel>>builder()
                    .code(HttpStatus.OK.value())
                    .message("Lấy danh sách cấp độ thành công.")
                    .data(list)
                    .build();
        }
        PageResponse<TopikLevel> data = topikLevelService.getPaginatedLevels(page, size, search, sort);
        return ApiResponse.<PageResponse<TopikLevel>>builder()
                .code(HttpStatus.OK.value())
                .message("Lấy danh sách cấp độ thành công.")
                .data(data)
                .build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<TopikLevel> getLevelById(@PathVariable Long id) {
        TopikLevel level = topikLevelService.getLevelById(id);
        return ApiResponse.<TopikLevel>builder()
                .code(HttpStatus.OK.value())
                .message("Lấy thông tin cấp độ thành công.")
                .data(level)
                .build();
    }

    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<TopikLevel> createLevel(@RequestBody TopikLevelRequest request) {
        TopikLevel level = topikLevelService.createLevel(request);
        return ApiResponse.<TopikLevel>builder()
                .code(HttpStatus.CREATED.value())
                .message("Tạo cấp độ thành công.")
                .data(level)
                .build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<TopikLevel> updateLevel(@PathVariable Long id, @RequestBody TopikLevelRequest request) {
        TopikLevel level = topikLevelService.updateLevel(id, request);
        return ApiResponse.<TopikLevel>builder()
                .code(HttpStatus.OK.value())
                .message("Cập nhật cấp độ thành công.")
                .data(level)
                .build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<Void> deleteLevel(@PathVariable Long id) {
        topikLevelService.deleteLevel(id);
        return ApiResponse.<Void>builder()
                .code(HttpStatus.OK.value())
                .message("Xóa cấp độ thành công.")
                .build();
    }
}
