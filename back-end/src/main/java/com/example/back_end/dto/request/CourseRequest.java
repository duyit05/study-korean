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
    @NotBlank(message = "Tiêu đề khóa học không được để trống.")
    private String title;

    private String description;

    @NotBlank(message = "Cấp độ khóa học không được để trống.")
    private String level;

    @NotNull(message = "Số buổi học không được để trống.")
    private Integer totalSessions;

    private BigDecimal price;

    private String thumbnailUrl;

    @Builder.Default
    private Boolean isPublished = false;
}
