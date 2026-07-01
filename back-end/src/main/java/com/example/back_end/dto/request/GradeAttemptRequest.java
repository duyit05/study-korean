package com.example.back_end.dto.request;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GradeAttemptRequest {
    private List<AnswerGrade> grades;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AnswerGrade {
        private Long answerId;
        private BigDecimal pointsEarned;
        private String feedback;
    }
}
