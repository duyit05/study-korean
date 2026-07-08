package com.example.back_end.dto.response;

import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassResponse {
    private Long id;
    private Long courseId;
    private String courseTitle;
    private String name;
    private String schedule;
    private String room;
    private String code;
    private String teacherName;
    private Integer studentsCount;
    private String status;
    private LocalDate startedAt;
    private String notes;
    private List<StudentDto> students;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentDto {
        private Long id;
        private String name;
        private String email;
        private Integer vocabProgress;
        private Integer avgScore;
    }
}
