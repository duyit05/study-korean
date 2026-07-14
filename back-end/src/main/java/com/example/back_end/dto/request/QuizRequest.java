package com.example.back_end.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizRequest {
    @NotBlank(message = "QUIZ_TITLE_REQUIRED")
    private String title;

    @NotNull(message = "TOPIK_LEVEL_REQUIRED")
    private Long topikLevelId;

    @NotNull(message = "TIME_LIMIT_REQUIRED")
    @Min(value = 1, message = "TIME_LIMIT_MIN")
    private Integer timeLimitMins;

    @NotNull(message = "TOTAL_SCORE_REQUIRED")
    @Min(value = 1, message = "TOTAL_SCORE_MIN")
    private Integer totalScore;

    @NotNull(message = "DUE_DATE_REQUIRED")
    private LocalDateTime dueDate;

    private Long classId;
}
