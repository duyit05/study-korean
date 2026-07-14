package com.example.back_end.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizSubmitRequest {
    @NotEmpty(message = "SUBMIT_ANSWERS_EMPTY")
    @Valid
    private List<AnswerSubmit> answers;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AnswerSubmit {
        @NotNull(message = "QUESTION_ID_REQUIRED")
        private Long questionId;

        private String studentAnswer;

        @NotNull(message = "TIME_TAKEN_REQUIRED")
        @Min(value = 0, message = "TIME_TAKEN_MIN")
        private Integer timeTakenMs;
    }
}
