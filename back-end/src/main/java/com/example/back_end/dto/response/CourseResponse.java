package com.example.back_end.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseResponse {
    private Long id;
    private Long teacherId;
    private String teacherName;
    private String title;
    private String description;
    private String level;
    private Integer totalSessions;
    private BigDecimal price;
    private String thumbnailUrl;
    private Boolean isPublished;
    private LocalDateTime createdAt;
}
