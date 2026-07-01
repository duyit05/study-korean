package com.example.back_end.controller;

import com.example.back_end.dto.request.CardRequest;
import com.example.back_end.dto.request.StudySetRequest;
import com.example.back_end.dto.response.ApiResponse;
import com.example.back_end.dto.response.StudySetResponse;
import com.example.back_end.entity.Card;
import com.example.back_end.entity.StudySet;
import com.example.back_end.entity.User;
import com.example.back_end.exception.AppException;
import com.example.back_end.exception.ErrorCode;
import com.example.back_end.repository.UserRepository;
import com.example.back_end.service.StudySetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("${spring.servlet.context-path}/study-sets")
@RequiredArgsConstructor
@Slf4j
public class StudySetController {

    private final StudySetService studySetService;
    private final UserRepository userRepository;

    @GetMapping
    public ApiResponse<List<StudySetResponse>> getAllStudySets() {
        List<StudySetResponse> response = studySetService.getAllStudySets().stream()
                .map(set -> {
                    List<Card> cards = studySetService.getCardsByStudySet(set.getId());
                    return mapToResponse(set, cards);
                })
                .collect(Collectors.toList());
        return ApiResponse.<List<StudySetResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("Lấy danh sách bộ từ vựng thành công.")
                .data(response)
                .build();
    }

    @PostMapping
    public ApiResponse<StudySetResponse> createStudySet(@Valid @RequestBody StudySetRequest request) {
        User creator = getCurrentUser();
        StudySet studySet = studySetService.createStudySet(request.getTitle(), request.getDescription(), request.getCategory(), creator);
        return ApiResponse.<StudySetResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Tạo bộ từ vựng thành công.")
                .data(mapToResponse(studySet, List.of()))
                .build();
    }

    @PostMapping("/{id}/cards")
    public ApiResponse<StudySetResponse.CardResponse> addCard(
            @PathVariable Long id,
            @Valid @RequestBody CardRequest request) {
        Card card = studySetService.addCardToStudySet(id, request.getFrontText(), request.getBackText(), request.getExampleSentence());
        return ApiResponse.<StudySetResponse.CardResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Thêm thẻ từ vựng thành công.")
                .data(mapToCardResponse(card))
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

    private StudySetResponse mapToResponse(StudySet s, List<Card> cards) {
        return StudySetResponse.builder()
                .id(s.getId())
                .title(s.getTitle())
                .description(s.getDescription())
                .category(s.getCategory())
                .creatorName(s.getCreator().getFullName() != null ? s.getCreator().getFullName() : s.getCreator().getUsername())
                .words(cards.stream().map(this::mapToCardResponse).collect(Collectors.toList()))
                .build();
    }

    private StudySetResponse.CardResponse mapToCardResponse(Card c) {
        return StudySetResponse.CardResponse.builder()
                .id(c.getId())
                .korean(c.getFrontText())
                .vietnamese(c.getBackText())
                .example(c.getExampleSentence())
                .build();
    }

    private User getCurrentUser() {
        String username = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }
}
