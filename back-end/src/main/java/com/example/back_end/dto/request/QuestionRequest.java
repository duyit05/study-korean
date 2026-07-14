package com.example.back_end.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionRequest {
    @NotBlank(message = "QUESTION_TEXT_REQUIRED")
    private String questionText;

    @NotBlank(message = "QUESTION_TYPE_REQUIRED")
    private String questionType; // MULTIPLE_CHOICE, WRITING

    @NotNull(message = "QUESTION_POINTS_REQUIRED")
    @Min(value = 0, message = "QUESTION_POINTS_MIN")
    private BigDecimal points;

    @NotBlank(message = "SECTION_REQUIRED")
    private String section; // LISTENING, READING, WRITING

    private String audioUrl;

    private String correctAnswer;

    private List<String> wrongAnswers;
}
