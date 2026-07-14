package com.example.back_end.controller;

import com.example.back_end.dto.request.QuizRequest;
import com.example.back_end.dto.request.QuestionRequest;
import com.example.back_end.dto.response.ApiResponse;
import com.example.back_end.dto.response.QuizResponse;
import com.example.back_end.enums.QuestionSection;
import com.example.back_end.enums.QuestionType;
import com.example.back_end.service.QuizService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("${spring.servlet.context-path}/quizzes")
@RequiredArgsConstructor
@Slf4j
public class QuizController {

    private final QuizService quizService;

    @GetMapping("/question-types")
    public ApiResponse<QuestionType[]> getQuestionTypes() {
        return ApiResponse.<QuestionType[]>builder()
                .code(HttpStatus.OK.value())
                .message("Lấy danh sách dạng câu hỏi thành công.")
                .data(QuestionType.values())
                .build();
    }

    @GetMapping("/sections")
    public ApiResponse<QuestionSection[]> getQuestionSections() {
        return ApiResponse.<QuestionSection[]>builder()
                .code(HttpStatus.OK.value())
                .message("Lấy danh sách phần thi thành công.")
                .data(QuestionSection.values())
                .build();
    }

    @PostMapping
    public ApiResponse<QuizResponse> createQuiz(@Valid @RequestBody QuizRequest request) {
        QuizResponse response = quizService.createQuiz(request);
        return ApiResponse.<QuizResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Tạo đề thi thành công.")
                .data(response)
                .build();
    }

    @PostMapping("/{id}/questions")
    public ApiResponse<QuizResponse.QuestionResponse> addQuestion(
            @PathVariable Long id, 
            @Valid @RequestBody QuestionRequest request) {
        QuizResponse.QuestionResponse response = quizService.addQuestionToQuiz(id, request);
        return ApiResponse.<QuizResponse.QuestionResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Thêm câu hỏi thành công.")
                .data(response)
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<QuizResponse> getQuizDetails(@PathVariable Long id) {
        QuizResponse response = quizService.getQuizDetails(id);
        return ApiResponse.<QuizResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Lấy chi tiết đề thi thành công.")
                .data(response)
                .build();
    }

    @GetMapping("/class/{classId}")
    public ApiResponse<List<QuizResponse>> getQuizzesByClass(@PathVariable Long classId) {
        List<QuizResponse> response = quizService.getQuizzesByClass(classId);
        return ApiResponse.<List<QuizResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("Lấy danh sách đề thi theo lớp thành công.")
                .data(response)
                .build();
    }

    @GetMapping("/creator")
    public ApiResponse<List<QuizResponse>> getMyCreatedQuizzes() {
        List<QuizResponse> response = quizService.getMyCreatedQuizzes();
        return ApiResponse.<List<QuizResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("Lấy danh sách đề thi tự tạo thành công.")
                .data(response)
                .build();
    }

    @PutMapping("/{quizId}/questions/{questionId}")
    public ApiResponse<QuizResponse.QuestionResponse> updateQuestion(
            @PathVariable Long quizId,
            @PathVariable Long questionId,
            @Valid @RequestBody QuestionRequest request) {
        QuizResponse.QuestionResponse response = quizService.updateQuestion(quizId, questionId, request);
        return ApiResponse.<QuizResponse.QuestionResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Cập nhật câu hỏi thành công.")
                .data(response)
                .build();
    }

    @DeleteMapping("/{quizId}/questions/{questionId}")
    public ApiResponse<Void> deleteQuestion(@PathVariable Long quizId, @PathVariable Long questionId) {
        quizService.deleteQuestion(quizId, questionId);
        return ApiResponse.<Void>builder()
                .code(HttpStatus.OK.value())
                .message("Xóa câu hỏi thành công.")
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<QuizResponse> updateQuiz(@PathVariable Long id, @Valid @RequestBody QuizRequest request) {
        QuizResponse response = quizService.updateQuiz(id, request);
        return ApiResponse.<QuizResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Cập nhật đề thi thành công.")
                .data(response)
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteQuiz(@PathVariable Long id) {
        quizService.deleteQuiz(id);
        return ApiResponse.<Void>builder()
                .code(HttpStatus.OK.value())
                .message("Xóa đề thi thành công.")
                .build();
    }
}
