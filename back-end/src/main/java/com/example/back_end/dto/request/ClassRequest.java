package com.example.back_end.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassRequest {
    @NotNull(message = "TOPIK_LEVEL_REQUIRED")
    private Long topikLevelId;

    private Long courseId;

    private String schedule;

    private String room;

    private String notes;
}
