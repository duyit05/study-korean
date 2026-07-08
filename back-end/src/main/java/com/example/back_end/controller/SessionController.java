package com.example.back_end.controller;

import com.example.back_end.dto.request.SessionRequest;
import com.example.back_end.dto.response.ApiResponse;
import com.example.back_end.dto.response.SessionResponse;
import com.example.back_end.enums.SessionStatus;
import com.example.back_end.service.SessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("${spring.servlet.context-path}/sessions")
@RequiredArgsConstructor
@Slf4j
public class SessionController {

    private final SessionService sessionService;

    @GetMapping("/statuses")
    public ApiResponse<SessionStatus[]> getSessionStatuses() {
        return ApiResponse.<SessionStatus[]>builder()
                .code(HttpStatus.OK.value())
                .message("Lấy danh sách trạng thái buổi học thành công.")
                .data(SessionStatus.values())
                .build();
    }

    @PostMapping
    public ApiResponse<SessionResponse> createSession(@Valid @RequestBody SessionRequest request) {
        SessionResponse response = sessionService.createSession(request);
        return ApiResponse.<SessionResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Tạo buổi học thành công.")
                .data(response)
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<SessionResponse> getSessionById(@PathVariable Long id) {
        SessionResponse response = sessionService.getSessionById(id);
        return ApiResponse.<SessionResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Lấy chi tiết buổi học thành công.")
                .data(response)
                .build();
    }

    @GetMapping("/class/{classId}")
    public ApiResponse<List<SessionResponse>> getSessionsByClass(@PathVariable Long classId) {
        List<SessionResponse> response = sessionService.getSessionsByClass(classId);
        return ApiResponse.<List<SessionResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("Lấy danh sách buổi học của lớp thành công.")
                .data(response)
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<SessionResponse> updateSession(
            @PathVariable Long id, 
            @Valid @RequestBody SessionRequest request) {
        SessionResponse response = sessionService.updateSession(id, request);
        return ApiResponse.<SessionResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Cập nhật buổi học thành công.")
                .data(response)
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteSession(@PathVariable Long id) {
        sessionService.deleteSession(id);
        return ApiResponse.<Void>builder()
                .code(HttpStatus.OK.value())
                .message("Xóa buổi học thành công.")
                .build();
    }
}
