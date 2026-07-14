package com.example.back_end.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GradeAttemptRequest {
    @NotEmpty(message = "GRADE_LIST_EMPTY")
    @Valid
    private List<AnswerGrade> grades;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AnswerGrade {
        @NotNull(message = "ANSWER_ID_REQUIRED")
        private Long answerId;

        @NotNull(message = "POINTS_EARNED_REQUIRED")
        @Min(value = 0, message = "POINTS_EARNED_MIN")
        private BigDecimal pointsEarned;

        private String feedback;
    }
}
