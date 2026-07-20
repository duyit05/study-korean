package com.example.back_end.dto.response;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizResponse {
    private Long id;
    private String title;
    private String quizType;
    private String examType;
    private String topikLevel;
    private Long topikLevelId;
    private Integer timeLimitMins;
    private Integer totalScore;
    private LocalDateTime dueDate;
    private LocalDateTime createdAt;
    private List<QuestionResponse> questions;
    private Integer questionCount;
    private Long classId;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QuestionResponse {
        private Long id;
        private String questionText;
        private String questionType;
        private Integer order;
        private String koreanText;
        private String audioUrl;
        private String audioSource;
        private String section;
        private java.math.BigDecimal points;
        private String explanation;
        private String correctAnswer;
        private List<String> wrongAnswers;
        private String wordType;
        private String pronunciation;
    }
}
