package com.example.back_end.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizAttemptResponse {
    private Long id;
    private Long quizId;
    private String quizTitle;
    private String studentName;
    private BigDecimal score;
    private Integer totalQuestions;
    private Integer correctCount;
    private Integer timeTakenSecs;
    private LocalDateTime startedAt;
    private LocalDateTime submittedAt;
    private BigDecimal listeningScore;
    private BigDecimal readingScore;
    private BigDecimal writingScore;
    private String status;
    private List<AnswerResponse> answers;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AnswerResponse {
        private Long id;
        private Long questionId;
        private String questionText;
        private String questionType;
        private String studentAnswer;
        private Boolean isCorrect;
        private Integer timeTakenMs;
        private BigDecimal pointsEarned;
        private String feedback;
    }
}
