package com.example.back_end.dto.response;

import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassResponse {
    private Long id;
    private String name;
    private String schedule;
    private String room;
    private String code;
    private String teacherName;
    private Integer studentsCount;
    private String status;
    private LocalDate startedAt;
    private String notes;
}
