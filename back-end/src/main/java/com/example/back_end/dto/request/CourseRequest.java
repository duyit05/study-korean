package com.example.back_end.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseRequest {
    @NotBlank(message = "COURSE_TITLE_REQUIRED")
    private String title;

    private String description;

    @NotBlank(message = "COURSE_LEVEL_REQUIRED")
    private String level;

    @NotNull(message = "TOTAL_SESSIONS_REQUIRED")
    private Integer totalSessions;

    private BigDecimal price;

    private String thumbnailUrl;

    @Builder.Default
    private Boolean isPublished = false;
}
