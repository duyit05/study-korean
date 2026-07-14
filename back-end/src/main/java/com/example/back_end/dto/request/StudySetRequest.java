package com.example.back_end.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudySetRequest {
    @NotBlank(message = "STUDY_SET_TITLE_REQUIRED")
    private String title;

    private String description;

    @NotNull(message = "TOPIK_LEVEL_REQUIRED")
    private Long topikLevelId;
}
