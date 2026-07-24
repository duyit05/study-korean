package com.example.back_end.dto.request;

import com.example.back_end.enums.ExamType;
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

    @NotNull(message = "EXAM_TYPE_REQUIRED")
    private ExamType examType;

    @NotNull(message = "TOPIK_LEVEL_REQUIRED")
    private Long topikLevelId;

    private Integer timeLimitMins;

    private Integer totalScore;

    @NotNull(message = "DUE_DATE_REQUIRED")
    private LocalDateTime dueDate;

    private Long classId;
}
