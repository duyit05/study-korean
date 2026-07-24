package com.example.back_end.controller;

import com.example.back_end.dto.request.GradeAttemptRequest;
import com.example.back_end.dto.request.QuizSubmitRequest;
import com.example.back_end.dto.response.ApiResponse;
import com.example.back_end.dto.response.QuizAttemptResponse;
import com.example.back_end.dto.response.PageResponse;
import com.example.back_end.service.QuizAttemptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("${spring.servlet.context-path}/quiz-attempts")
@RequiredArgsConstructor
@Slf4j
public class QuizAttemptController {

    private final QuizAttemptService quizAttemptService;

    @PostMapping("/{quizId}/submit")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<QuizAttemptResponse> submitAttempt(
            @PathVariable Long quizId,
            @Valid @RequestBody QuizSubmitRequest request) {
        QuizAttemptResponse response = quizAttemptService.submitAttempt(quizId, request);
        return ApiResponse.<QuizAttemptResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Nộp bài thi thành công.")
                .data(response)
                .build();
    }

    @PostMapping("/grade/{attemptId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<QuizAttemptResponse> gradeAttempt(
            @PathVariable Long attemptId,
            @Valid @RequestBody GradeAttemptRequest request) {
        QuizAttemptResponse response = quizAttemptService.gradeAttempt(attemptId, request);
        return ApiResponse.<QuizAttemptResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Chấm điểm bài tự luận thành công.")
                .data(response)
                .build();
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<List<QuizAttemptResponse>> getPendingAttempts() {
        List<QuizAttemptResponse> response = quizAttemptService.getPendingAttempts();
        return ApiResponse.<List<QuizAttemptResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("Lấy danh sách bài thi chờ chấm thành công.")
                .data(response)
                .build();
    }

    @GetMapping("/my-attempts")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<List<QuizAttemptResponse>> getMyAttempts() {
        List<QuizAttemptResponse> response = quizAttemptService.getMyAttempts();
        return ApiResponse.<List<QuizAttemptResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("Lấy lịch sử làm bài thành công.")
                .data(response)
                .build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<QuizAttemptResponse> getAttemptDetails(@PathVariable Long id) {
        QuizAttemptResponse response = quizAttemptService.getAttemptDetails(id);
        return ApiResponse.<QuizAttemptResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Lấy chi tiết bài làm thành công.")
                .data(response)
                .build();
    }

    @GetMapping("/teacher")
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<PageResponse<QuizAttemptResponse>> getAttemptsForTeacher(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String date,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<QuizAttemptResponse> response = quizAttemptService.getAttemptsForTeacher(
                search, status, date, page, size);
        return ApiResponse.<PageResponse<QuizAttemptResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("Lấy danh sách bài thi thành công.")
                .data(response)
                .build();
    }
}
