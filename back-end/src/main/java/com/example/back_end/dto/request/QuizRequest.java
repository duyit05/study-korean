package com.example.back_end.dto.request;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizRequest {
    private String title;
    private String topikLevel;
    private Integer timeLimitMins;
    private Integer totalScore;
    private LocalDateTime dueDate;
    private Long classId;
}
