package com.example.back_end.controller;

import com.example.back_end.dto.request.CardRequest;
import com.example.back_end.dto.request.StudySetRequest;
import com.example.back_end.dto.response.ApiResponse;
import com.example.back_end.dto.response.StudySetResponse;
import com.example.back_end.service.StudySetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("${spring.servlet.context-path}/study-sets")
@RequiredArgsConstructor
@Slf4j
public class StudySetController {

    private final StudySetService studySetService;

    @GetMapping
    public ApiResponse<List<StudySetResponse>> getAllStudySets() {
        List<StudySetResponse> response = studySetService.getAllStudySets();
        return ApiResponse.<List<StudySetResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("Lấy danh sách bộ từ vựng thành công.")
                .data(response)
                .build();
    }

    @GetMapping("/{id}/cards")
    public ApiResponse<List<StudySetResponse.CardResponse>> getCards(@PathVariable Long id) {
        List<StudySetResponse.CardResponse> response = studySetService.getCardsByStudySet(id);
        return ApiResponse.<List<StudySetResponse.CardResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("Lấy danh sách thẻ từ vựng thành công.")
                .data(response)
                .build();
    }

    @PostMapping
    public ApiResponse<StudySetResponse> createStudySet(@Valid @RequestBody StudySetRequest request) {
        StudySetResponse response = studySetService.createStudySet(request);
        return ApiResponse.<StudySetResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Tạo bộ từ vựng thành công.")
                .data(response)
                .build();
    }

    @PostMapping("/{id}/cards")
    public ApiResponse<StudySetResponse.CardResponse> addCard(
            @PathVariable Long id,
            @Valid @RequestBody CardRequest request) {
        StudySetResponse.CardResponse response = studySetService.addCardToStudySet(id, request);
        return ApiResponse.<StudySetResponse.CardResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Thêm thẻ từ vựng thành công.")
                .data(response)
                .build();
    }

    @PostMapping("/{id}/cards/batch")
    public ApiResponse<List<StudySetResponse.CardResponse>> addCardsBatch(
            @PathVariable Long id,
            @Valid @RequestBody List<CardRequest> requests) {
        List<StudySetResponse.CardResponse> response = studySetService.addCardsToStudySet(id, requests);
        return ApiResponse.<List<StudySetResponse.CardResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("Nhập danh sách thẻ từ vựng thành công.")
                .data(response)
                .build();
    }

    @DeleteMapping("/{setId}/cards/{cardId}")
    public ApiResponse<Void> deleteCard(@PathVariable Long setId, @PathVariable Long cardId) {
        studySetService.deleteCard(setId, cardId);
        return ApiResponse.<Void>builder()
                .code(HttpStatus.OK.value())
                .message("Xóa thẻ từ vựng thành công.")
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<StudySetResponse> updateStudySet(
            @PathVariable Long id,
            @Valid @RequestBody StudySetRequest request) {
        StudySetResponse response = studySetService.updateStudySet(id, request);
        return ApiResponse.<StudySetResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Cập nhật bộ từ vựng thành công.")
                .data(response)
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteStudySet(@PathVariable Long id) {
        studySetService.deleteStudySet(id);
        return ApiResponse.<Void>builder()
                .code(HttpStatus.OK.value())
                .message("Xóa bộ từ vựng thành công.")
                .build();
    }
}
