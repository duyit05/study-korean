package com.example.back_end.dto.request;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizSubmitRequest {
    private List<AnswerSubmit> answers;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AnswerSubmit {
        private Long questionId;
        private String studentAnswer;
        private Integer timeTakenMs;
    }
}
