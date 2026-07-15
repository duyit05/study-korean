package com.example.back_end.dto.response;

import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentProgressResponse {
    private List<ClassInfo> classes;
    private List<QuizAttemptInfo> quizAttempts;
    private VocabProgressInfo vocabProgress;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClassInfo {
        private Long id;
        private String name;
        private String courseTitle;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuizAttemptInfo {
        private Long id;
        private String quizTitle;
        private Double score;
        private String status;
        private String submittedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VocabProgressInfo {
        private Integer totalLearnedCards;
        private Integer totalReviewCards;
        private Integer totalStudySets;
    }
}
