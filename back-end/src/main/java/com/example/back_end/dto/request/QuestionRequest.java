package com.example.back_end.dto.request;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionRequest {
    private String questionText;
    private String questionType; // MULTIPLE_CHOICE, WRITING
    private BigDecimal points;
    private String section; // LISTENING, READING, WRITING
    private String audioUrl;
    private String correctAnswer;
    private List<String> wrongAnswers;
}
