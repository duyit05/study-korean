package com.example.back_end.controller;

import com.example.back_end.dto.request.CardProgressRequest;
import com.example.back_end.dto.response.ApiResponse;
import com.example.back_end.service.CardProgressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("${spring.servlet.context-path}/study-sets/cards")
@RequiredArgsConstructor
@Slf4j
public class CardProgressController {

    private final CardProgressService cardProgressService;

    @PostMapping("/{cardId}/progress")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<Void> updateProgress(
            @PathVariable Long cardId,
            @Valid @RequestBody CardProgressRequest request) {
        cardProgressService.updateCardProgress(cardId, request.getStatus());
        return ApiResponse.<Void>builder()
                .code(HttpStatus.OK.value())
                .message("Cập nhật tiến độ học từ thành công.")
                .build();
    }
}
